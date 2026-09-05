package gateway.middlewarewebservice.handler;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import pk.vaulsys.apigateway.base.config.MWSessionConfig;
import pk.vaulsys.apigateway.beneficiary.entity.MWBeneficiary;
import pk.vaulsys.apigateway.beneficiary.entity.MWRemitBeneficiary;
import pk.vaulsys.apigateway.customer.*;
import pk.vaulsys.apigateway.customer.Currency;
import pk.vaulsys.apigateway.email.base.EmailGatewayHandler;
import pk.vaulsys.apigateway.entity.SystemConfig;
import pk.vaulsys.apigateway.notification.handler.NotificationHandler;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.base.APIVersion;
import pk.vaulsys.apigateway.protocols.webservice.base.CustomerType;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.CMSKYCStatus;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWWSOperation;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.*;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.*;
import pk.vaulsys.apigateway.smsgateway.base.DeliveryModes;
import pk.vaulsys.apigateway.smsgateway.base.SMSCategory;
import pk.vaulsys.apigateway.smsgateway.entity.SMSOTPLog;
import pk.vaulsys.apigateway.smsgateway.handler.SMSGatewayHandler;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;
import pk.vaulsys.apigateway.wfe.GlobalContext;

import java.util.*;

import static pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWWSOperation.*;

/**
 * Created by RAZA MURTAZA BAIG on 1/28/2018.
 */
public class MiddlewareWebServiceHandler {
    private static final Logger logger = LogManager.getLogger(MiddlewareWebServiceHandler.class);

    public static AppWsEntity processSignUpRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteSignUpRequest(wsmodel);
    }

    public static AppWsEntity processUpdateMerchantDashboardRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteUpdateMerchantDashboardRequest(wsmodel);
    }

//    public static AppWsEntity processGetCustomerByMobileNumberRequest(AppWsEntity wsmodel)
//    {
//        return MWWSOperation.ExecuteGetCustomerByMobileNumberRequest(wsmodel);
//    }

    public static String GetTranAPIByTxnRefNum(String txnrefnum, String lang)
    {
        try
        {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.tranrefnumber= :TXNREF ";
            params = new HashMap<String, Object>();
            params.put("TXNREF", txnrefnum);

            AppWsEntity dbtxn = (AppWsEntity)GeneralDao.Instance.findObject(dbQuery, params);

            if(dbtxn != null)
            {
                if(Util.hasText(lang) && lang.equals("FRA"))
                {
                    return GlobalContext.getInstance().getTransactionCodeDescbyAPI(dbtxn.getServicename()).getFrapiname();
                }
                else
                {
                    return GlobalContext.getInstance().getTransactionCodeDescbyAPI(dbtxn.getServicename()).getApiname();
                }
            }
            else
            {
                logger.error("No Transaction found against tranrefnumber [" + txnrefnum + "], returning null...");
                return null;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while getting Txn from DB, returning null...");
            e.printStackTrace();
//logger.error(WebServiceUtil.getStrException(e));
            return null;
        }
    }

    public static String GetCustomerlang(String mobile)
    {
        try
        {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", mobile);

            MWCustomer cust = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(cust != null)
            {
                if(Util.hasText(cust.getLanguage()))
                {
                    return cust.getLanguage();
                }
                else
                {
                    logger.error("Language [" + cust.getLanguage() + "] not found against Customer [" + mobile + "], returning FRA");
                    return Util.getDefaultMobileAppLanguage();
                }
            }
            else
            {
                logger.error("Customer not found against MobileNumber [" + mobile + "], returning FRA...");
                return Util.getDefaultMobileAppLanguage();
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while getting Customer from DB, returning FRA...");
            logger.error(WebServiceUtil.getStrException(e));
            return Util.getDefaultMobileAppLanguage();
        }
    }

    public static AppWsEntity processLogInRequest(AppWsEntity wsmodel)
    {
        /*if(wsmodel.getSecurityparams() != null && !wsmodel.getSecurityparams().getDeviceid().equals("10cbb8712e76027e")) //Raza for testing only
        {
            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
            return wsmodel;
        }*/

        //TODO: Raza reomove after testing start
        if(Util.hasText(wsmodel.getMobilenumber()) && wsmodel.getMobilenumber().equals("00243111111111"))
        {
            //logger.info("" + wsmodel.getPassword());
            if(Util.hasText(wsmodel.getPassword()) && !wsmodel.getPassword().equals("EzEGI1R+x2t8bT8GjPJYDw==")){
                //logger.info("--------");
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            }
            else{
                //logger.info("!!!!!");
                wsmodel.setPassword("FA0Wl/NPpFY=");
            }
        }
        //TODO: Raza reomove after testing end

        return MWWSOperation.ExecuteLogInRequest(wsmodel, false, false);
    }

    public static AppWsEntity processTitleFetchRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteTitleFetchRequest(wsmodel);
    }

    public static AppWsEntity processPrivacyPolicy(AppWsEntity wsmodel)
    {
        return MWWSOperation.executePrivacyPolicy(wsmodel);
    }

    public static AppWsEntity processGenerateTruIDToken(AppWsEntity wsModel)
    {
        return MWWSOperation.executeGenerateTruIDToken(wsModel);
    }

    public static AppWsEntity processLogOutRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteLogOutRequest(wsmodel);
    }

    public static AppWsEntity processChangePasswordRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteChangePasswordRequest(wsmodel);
    }

    public static AppWsEntity processChangeUserNameRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteChangeUserNameRequest(wsmodel);
    }

    public static AppWsEntity processChangeResetPasswordRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteChangeResetPasswordRequest(wsmodel);
    }

    public static AppWsEntity processChangeResetUserNameRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteChangeResetUserNameRequest(wsmodel);
    }

    public static AppWsEntity processChangeUserNamePasswordRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteChangeUserNamePasswordRequest(wsmodel);
    }

    public static AppWsEntity processResetPasswordRequest(AppWsEntity wsmodel)
    {
        if(Util.hasText(wsmodel.getMobilenumber()))
        {
            /*String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);
            if(customer != null && customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
            {
                logger.info("Reset Password for Migrated Customer recevied, replying to verify Secret Questions...");
                if(customer.getHaswallet() != null && customer.getHaswallet())
                {
                    wsmodel.setRespcode(ISOResponseCodes.MIGRATED_REKYC_REQ);
                    return wsmodel;
                }
                else
                {
                    wsmodel.setRespcode(ISOResponseCodes.MIGRATED_SIGNUP_REQUIRED);
                    return wsmodel;
                }
            }
            else
            {*/
                return MWWSOperation.ExecuteResetPasswordRequest(wsmodel);
            //}
        }
        else
        {
            logger.error("MobileNumber [" + wsmodel.getMobilenumber() + "] not found for ResetPassword, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
            return wsmodel;
        }
    }

    public static AppWsEntity processResetUserNameRequest(AppWsEntity wsmodel)
    {
        if(Util.hasText(wsmodel.getMobilenumber()))
        {
            //String dbQuery;
            //Map<String, Object> params;

//            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
//            params = new HashMap<String, Object>();
//            params.put("MOB", wsmodel.getMobilenumber());

            //MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);
            /*if(customer != null && customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
            {
                logger.info("Reset UserName for Migrated Customer recevied, replying OK...");

                logger.info("Reset UserName for Migrated Customer recevied, replying to verify Secret Questions...");
                if(customer.getHaswallet() != null && customer.getHaswallet())
                {
                    wsmodel.setRespcode(ISOResponseCodes.MIGRATED_REKYC_REQ);
                    return wsmodel;
                }
                else
                {
                    wsmodel.setRespcode(ISOResponseCodes.MIGRATED_SIGNUP_REQUIRED);
                    return wsmodel;
                }
            }
            else
            {*/
                return MWWSOperation.ExecuteResetUserNameRequest(wsmodel);
            //}
        }
        else
        {
            logger.error("MobileNumber [" + wsmodel.getMobilenumber() + "] not found for ResetPassword, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
            return wsmodel;
        }
    }

    public static AppWsEntity processCheckNationalityIdRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWCustomer.class.getName() + " c where c.cnic= :CNIC ";
        params = new HashMap<String, Object>();
        params.put("CNIC", wsmodel.getIdentificationno().trim());

        MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);
        if(customer == null ||
                (customer != null && customer.getIsmigratedactive() != null && customer.getIsmigratedactive()))
        {
            logger.info("IdentificationNumber [" + wsmodel.getIdentificationno() + "] verified, replying OK...");
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        else
        {
            logger.error("Customer found against IdentificationNumber [" + wsmodel.getIdentificationno() + "] rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_CUSTOMER);
            return wsmodel;
        }
    }

    public static AppWsEntity processCheckEmailRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWCustomer.class.getName() + " c where c.emailaddress= :EMAIL ";
        params = new HashMap<String, Object>();
        params.put("EMAIL", wsmodel.getEmailaddress());

        MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);
        if(customer == null || (customer != null && customer.getHaswallet() != null && !customer.getHaswallet()))
        {
            logger.info("EmailAddress [" + wsmodel.getEmailaddress() + "] verified, replying OK...");
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        else
        {
            logger.error("Customer found against EmailAddress [" + wsmodel.getEmailaddress() + "] ignoring...");
            //logger.error("Customer found against EmailAddress [" + wsmodel.getEmailaddress() + "] rejecting...");
            //wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_CUSTOMER);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
    }

    public static AppWsEntity processCheckUserNameRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :USR ";
        params = new HashMap<String, Object>();
        params.put("USR", wsmodel.getUsername().trim());

        MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

//        if(customer == null || (customer != null && customer.getHaswallet() != null && !customer.getHaswallet()))
//        {
//            if(Util.hasText(wsmodel.getMobilenumber()) && !wsmodel.getMobilenumber().equals(customer.getMobilenumber()))
//            {
//                logger.error("UserName [" + wsmodel.getUsername() + "] already taken by Customer without Wallets, reejcting...");
//                wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_USERNAME);
//                return wsmodel;
//            }
//            else
//            {
//                logger.info("UserName [" + wsmodel.getEmailaddress() + "] verified, replying OK...");
//                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                return wsmodel;
//            }
//        }
//        else
//        {
//            logger.error("Customer found against UserName [" + wsmodel.getUsername() + "] rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_USERNAME);
//            return wsmodel;
//        }


        if (customer == null) {
            logger.info("UserName [" + wsmodel.getUsername() + "] verified, replying OK...");
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }

        logger.error("Customer found against UserName [" + wsmodel.getUsername() + "] rejecting...");
        wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_USERNAME);
        return wsmodel;
    }

    public static AppWsEntity processCreateAliasRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processDeleteAliasRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetAliasRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetRegisteredContactsRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetRegisteredContacts(wsmodel);
    }

    public static AppWsEntity processGetInviteContactsRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetInviteContacts(wsmodel);
    }

    public static AppWsEntity processGetDRCContactsRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetDRCContacts(wsmodel);
    }

    public static AppWsEntity processCreateWalletRequest(AppWsEntity wsModel) {
        if (!Util.hasText(wsModel.getKycstatus())) {
            wsModel.setKycstatus(CMSKYCStatus.STANDARD);
        }

        return MWWSOperation.ExecuteCreateWalletRequest(wsModel);
    }

    public static void processBulkCreateWalletRequest(AppWsEntity wsModel) {
        MWWSOperation.executeBulkCreateWalletRequest(wsModel);
    }

    public static AppWsEntity processFetchUnbindingReasons(AppWsEntity wsmodel)
    {
        return MWWSOperation.executeFetchUnbindingReasons(wsmodel);
    }

    public static AppWsEntity processSoftDeleteUser(AppWsEntity wsmodel)
    {
        return MWWSOperation.executeSoftDeleteUser(wsmodel);
    }

    public static AppWsEntity processGetAllUsers(AppWsEntity wsmodel)
    {
        return MWWSOperation.executeGetAllUsers(wsmodel);
    }

    public static AppWsEntity processGetEnvVars(AppWsEntity wsmodel) {
        return MWWSOperation.executeEnvVars(wsmodel);
    }

    public static AppWsEntity processBanksList(AppWsEntity wsmodel) {
        return MWWSOperation.executeBanksListRequest(wsmodel);
    }

    public static void processBulkDisbursementRequest(AppWsEntity wsModel) {
        MWWSOperation.executeBulkDisbursementRequest(wsModel);
    }

    public static AppWsEntity processGetSignUpFormMetaRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetFormTemplateRequest(wsmodel);

    }

    public static AppWsEntity processGetTrackingIdRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetTrackingIdRequest(wsmodel);

    }

    public static AppWsEntity processUpdateProfileRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteUpdateProfileRequest(wsmodel);
    }

    public static AppWsEntity processUpdateProfileRequestForTruID(AppWsEntity wsmodel, MWCustomer customer)
    {
        return MWWSOperation.ExecuteUpdateProfileRequestForTruID(wsmodel, customer);
    }

    public static AppWsEntity processCreateWalletPINRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processDeleteProvisionalWalletRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processActivateProvisionalWalletRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCustomerEnableWalletAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCreateWalletLevelOneRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processRegisterCustomerRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processChangeWalletPinRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processVerifyWalletPinRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
        params = new HashMap<String, Object>();
        params.put("MOB", wsmodel.getMobilenumber());

        MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

        if(customer != null && customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
        {
            return wsmodel;
                //if(customer.getMigratedpin().equals(WSEncrptionUtil.EncryptAppPassword(wsmodel.getPindata())))
//                if((PinBlockUtil.generatePinBlockUnderTPKForMigratedCustomers(wsmodel.getMobilenumber(), customer.getMigratedpin())).equals(wsmodel.getPindata().toLowerCase()))
//                {
//                    logger.info("PIN Validated for Migrated Customer, replying...");
//
//                wsmodel.setUsername(customer.getUsername());
//                wsmodel.setFirstname(customer.getFirstname());
//                wsmodel.setLastname(customer.getLastname());
//                    wsmodel.setLastname(customer.getLastname());
//                    wsmodel.setLanguage(customer.getLanguage());
//                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
//                if(Util.hasText(wsmodel.getReason())  && wsmodel.getReason().equals(SMSCategory.FORGOT_USERNAME)) {
//                if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.FORGOT_USERNAME, customer.getMobilenumber(), wsmodel))
//                {
//                    logger.error("Failed to create & Send OTP for Mobile [" + customer.getMobilenumber() + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
//                    return wsmodel;
//                }
//                }
//
//                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                    return wsmodel;
//                }
//                else
//                {
//                    logger.error("Failed to Validate PIN for Migrated Customer with Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.BAD_PIN);
//                    return wsmodel;
//                }
        }
        else if(customer != null)
        {
            wsmodel.setUsername(customer.getUsername());
            wsmodel.setFirstname(customer.getFirstname());
            MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);

            if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
            {
                if(Util.hasText(wsmodel.getReason())  && wsmodel.getReason().equals(SMSCategory.FORGOT_USERNAME)) {
                if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.FORGOT_USERNAME, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send OTP for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    }
                }
            }
            else
            {
                logger.error("Failed to verify Wallet PIN, rejecting...");
            }
            return wsmodel;
        }
        else
        {
            logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
            return wsmodel;
        }
    }

    public static AppWsEntity processLinkBankAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetBankDetailsRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processConfirmLinkAccountRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUnLinkBankAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processCreateWalletLevelTwoRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processLoadMoneyRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processWalletTransactionRequest(AppWsEntity wsmodel)
    {
            if(Util.hasText(wsmodel.getDestmobilenumber()) && wsmodel.getDestmobilenumber().equals(wsmodel.getMobilenumber()))
            {
                logger.error("Send Money Transaction not allowed to own mobile number [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return wsmodel;
            }

            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processWalletBulkPayRequest(AppWsEntity wsmodel) {
        if (Util.hasText(wsmodel.getDestmobilenumber()) && wsmodel.getDestmobilenumber().equals(wsmodel.getMobilenumber())) {
            logger.error("Send Money Transaction not allowed to own mobile number [" + wsmodel.getDestmobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return wsmodel;
        }
        return MWWSOperation.ExecuteBulkPayTransaction(wsmodel);
    }

    public static AppWsEntity processBankToWalletRequest(AppWsEntity wsmodel)
    {
        if(Util.hasText(wsmodel.getDestmobilenumber()) && wsmodel.getDestmobilenumber().equals(wsmodel.getMobilenumber()))
        {
            logger.error("Send Money Transaction not allowed to own mobile number [" + wsmodel.getDestmobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return wsmodel;
        }

        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processBankToAliasRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processBankToBankRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processRequestMoneyRequest(AppWsEntity wsmodel)
    {
        if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getDestmobilenumber()))
        {
            if(wsmodel.getMobilenumber().equals(wsmodel.getDestmobilenumber()))
            {
                logger.error("Cannot AskMoney from same Src [" + wsmodel.getMobilenumber() + "] Dest [" + wsmodel.getDestmobilenumber() + "], rejecting.... ");
                wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                return wsmodel;
            }
        }
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processRejectRequestMoneyRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCancelRequestMoneyRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processConfirmRequestMoneyRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetRequestMoneyListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetEnvoiCashListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCashOutReqRequest(AppWsEntity wsmodel)
    {
        //Raza adding to stop EnvoiCash to own mobile number
        if(wsmodel.getServicename().equals("EnvoiCashRequest") && wsmodel.getMobilenumber().equals(wsmodel.getDestmobilenumber()))
        {
            logger.error("EnvoiCash not allowed on own mobile number [" + wsmodel.getDestmobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return wsmodel;
        }

        /*if(Util.hasText(wsmodel.getDestmobilenumber()))
        {
            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getDestmobilenumber());
            MWCustomer destcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if(destcustomer != null)
            {
                logger.error("EnvoiCash not allowed to illico Cash User [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_TO_ACCOUNT);
                return wsmodel;
            }
        }*/


        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCashOutAuthRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCashOutNonIllicoReqRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCashOutNonIllicoAuthRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processCancelCashOutRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetCashOutListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUnreadApprovalCountRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetOutgoingPendingRemitLogListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetIncomingPendingRemitLogListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }
    public static AppWsEntity processMerchantBillerTransactionRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processMerchantBillerCoreTransactionRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false); //ExecuteMerchantCoreTransaction(wsmodel);
    }

    public static AppWsEntity processMerchantRetailTransactionRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processMerchantRetailCoreTransactionRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false); //ExecuteMerchantCoreTransaction(wsmodel);
    }

    public static AppWsEntity processGetWalletBalanceRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processSupportPortalActivateDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processActivateDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processEnableDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processUpdateUserProfileRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processMiniStatementRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processConfirmOtpRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processLoadWalletRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUnloadWalletRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processPaymentRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processMiniStatementOtpRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetDebitCardStatusRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetWalletStatusRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processConfirmFraudOtpRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processChangeDebitCardPinRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processLinkBankAccountOTPRequest(AppWsEntity wsmodel)
    {
        MWWSOperation.ExecuteConfirmOTPRequest(wsmodel);
        
        if(!wsmodel.getRespcode().equals(ISOResponseCodes.MW_APPROVED))
        {
            logger.error("Failed to validate OTP for LinkAccountOTP, rejecting...");
            return wsmodel;
        }
        else
        {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
        }
    }

    public static AppWsEntity processLinkDebitCardAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetDebitCardAccountListRequest(AppWsEntity wsmodel)
    {
            //Raza Call Bank Service...
            List<LinkedAccountObj> listlnkaccts = new ArrayList<LinkedAccountObj>();
            LinkedAccountObj lnkaccount = new LinkedAccountObj();
            lnkaccount.setBankcode(wsmodel.getBankcode());
            lnkaccount.setAccountnumber("123456789012");
            lnkaccount.setCurrency("586");
            listlnkaccts.add(lnkaccount);
            wsmodel.setLinkedaccounts(listlnkaccts);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
            //return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processUnLinkDebitCardAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processUpdateLinkedAccountAliasRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processSetPrimaryLinkedAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserTokenRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserWalletListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserWalletRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserLinkedAccountListRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserTransactionRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetUserTransactionListRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processSupportPortalGetUserTransactionListRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAddMerchantProfileRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetMerchantProfileRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUpdateMerchantProfileRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processEnableMerchantRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processCloseMerchantRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetMerchantTransactionListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetMerchantTransactionRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processBlockMerchantRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processAddMerchantRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUpdateMerchantRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetMerchantRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processAddMerchantCategoryRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUpdateMerchantCategoryRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processDeleteMerchantCategoryRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetMerchantCategoryRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processAddMerchantCategoryRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUpdateMerchantCategoryRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processDeleteMerchantCategoryRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetMerchantCategoryRatesRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processMerchantSendMoneyRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processSendMoneyBankAccountRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

//    public static AppWsEntity processS2MCall(AppWsEntity wsmodel)
//    {
//        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//    }

    public static AppWsEntity processInitSendMoneyInternationalRequest(AppWsEntity wsmodel)
    {
//        if(!Util.hasText(wsmodel.getDestcountry()) || !Util.hasText(wsmodel.getAmounttransaction()) || !Util.hasText(wsmodel.getPayerid())){
//            logger.error("DestCountry [" + wsmodel.getDestcountry() + "] or PayerID [" + wsmodel.getPayerid() + "] or AmountTransaction [" + wsmodel.getAmounttransaction() + "] not found in request, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//            return wsmodel;
//        }
        if(!Util.hasText(wsmodel.getDestcountry()) || !Util.hasText(wsmodel.getPayerid())){
            logger.error("DestCountry [" + wsmodel.getDestcountry() + "] or PayerID [" + wsmodel.getPayerid() + "] not found in request, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processSendMoneyInternationalRequest(AppWsEntity wsmodel)
    {
        if(Util.hasText(wsmodel.getApiversion()) && (wsmodel.getApiversion().equals(APIVersion.VERSION2) || wsmodel.getApiversion().equals(APIVersion.VERSION3))){
            try {
                String dbQuery;
                Map<String, Object> params;

                JSONObject obj = new JSONObject(wsmodel.getBeneficiaryobj());

                dbQuery = "from " + MWRemitBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.id= :BEN ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("BEN", Long.parseLong(obj.getString("id")));

                MWRemitBeneficiary dbbenef = (MWRemitBeneficiary)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbbenef != null)
                {
                    obj = new JSONObject(dbbenef.getBeneficiaryinfo());
                    wsmodel.setBeneficiaryobj(dbbenef.getBeneficiaryinfo());

                    if(obj.has("details")){
                        if(obj.getJSONArray("details") != null && !obj.getJSONArray("details").isEmpty()){

                            for(int i =0 ; i<obj.getJSONArray("details").length() ; i++){

                                if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("consumerno")){
                                    wsmodel.setConsumerno(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("accountnumber")){
                                    wsmodel.setDestaccount(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                    if(!Util.hasText(wsmodel.getCreditaccountnumber())){ //Raza adding 16-08-2024 -- Canada Issue Production
                                        wsmodel.setCreditaccountnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                    }

                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("account_number")){
                                    wsmodel.setDestaccount(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("bank_account_number")){
                                    wsmodel.setDestaccount(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("swiftbiccode")){
                                    wsmodel.setSwiftbiccode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("swift_bic_code")){
                                    wsmodel.setSwiftbiccode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("iban")){
                                    wsmodel.setIban(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("ifscode")){
                                    wsmodel.setIfscode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("ifs_code")){
                                    wsmodel.setIfscode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("bankcode")){
                                    wsmodel.setBankcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("cardnumber")){
                                    wsmodel.setCardnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("card_number")){
                                    wsmodel.setCardnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("clabe")){
                                    wsmodel.setClabe(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("cbu")){
                                    wsmodel.setCbu(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("cbualias")){
                                    wsmodel.setCbualias(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("cbu_alias")){
                                    wsmodel.setCbualias(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("bikcode")){
                                    wsmodel.setBikcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("bik_code")){
                                    wsmodel.setBikcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("abaroutingnumber")){
                                    wsmodel.setAbaroutingnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("aba_routing_number")){
                                    wsmodel.setAbaroutingnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("bsbnumber")){
                                    wsmodel.setBsbnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("bsb_number")){
                                    wsmodel.setBsbnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("branchcode")){
                                    wsmodel.setBranchcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("branch_number")){
                                    wsmodel.setBranchcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("routingcode")){
                                    wsmodel.setRoutingcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("routing_code")){
                                    wsmodel.setRoutingcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("entityttid")){
                                    wsmodel.setEntityttid(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("entity_tt_id")){
                                    wsmodel.setEntityttid(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("accounttype")){
                                    wsmodel.setAccounttype(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("account_type")){
                                    wsmodel.setAccounttype(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("creditaccountnumber")){
                                    wsmodel.setCreditaccountnumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("firstname")){
                                    wsmodel.setDestfirstname(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("lastname")){
                                    wsmodel.setDestlastname(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("country")){
                                    wsmodel.setDestcountry(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("identificationno")){
                                    wsmodel.setIdentificationno(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("id_number")){
                                    wsmodel.setIdentificationno(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("identificationtype")){
                                    wsmodel.setTypefilter(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("destmobilenumber")){
                                    wsmodel.setDestmobilenumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                    if(!Util.hasText(wsmodel.getConsumerno())){ //Raza adding 30-08-2024
                                        wsmodel.setConsumerno(wsmodel.getDestmobilenumber());
                                    }
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("msisdn")){
                                    wsmodel.setDestmobilenumber(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                    if(!Util.hasText(wsmodel.getConsumerno())){ //Raza adding 30-08-2024
                                        wsmodel.setConsumerno(wsmodel.getDestmobilenumber());
                                    }
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("destaddress")){
                                    wsmodel.setDestaddress(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("payerid")){
                                    wsmodel.setPayerid(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("email")){
                                    wsmodel.setDestemailaddress(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("sort_code")){
                                    wsmodel.setBankcode(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("id_type")){
                                    wsmodel.setTypefilter(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("id_code")){
                                    wsmodel.setIdentificationno(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }
                                else if(obj.getJSONArray("details").getJSONObject(i).getString("key").equals("state")){
                                    wsmodel.setState(obj.getJSONArray("details").getJSONObject(i).getString("value"));
                                }


                            }

                        }
                        else{
                            logger.error("Empty details object found against beneficiary [" + dbbenef.getId() + "]");
                            wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                            return wsmodel;
                        }
                    }
                    else{
                        if(obj.has("consumerno")){
                            wsmodel.setConsumerno(obj.getString("consumerno"));
                        }
                        if(obj.has("accountnumber")){
                            wsmodel.setDestaccount(obj.getString("accountnumber"));
                        }
                        if(obj.has("account_number")){
                            wsmodel.setDestaccount(obj.getString("account_number"));
                        }
                        if(obj.has("bank_account_number")){
                            wsmodel.setDestaccount(obj.getString("bank_account_number"));
                        }
                        if(obj.has("swiftbiccode")){
                            wsmodel.setSwiftbiccode(obj.getString("swiftbiccode"));
                        }
                        if(obj.has("swift_bic_code")){
                            wsmodel.setSwiftbiccode(obj.getString("swift_bic_code"));
                        }
                        if(obj.has("iban")){
                            wsmodel.setIban(obj.getString("iban"));
                        }
                        if(obj.has("ifscode")){
                            wsmodel.setIfscode(obj.getString("ifscode"));
                        }
                        if(obj.has("ifs_code")){
                            wsmodel.setIfscode(obj.getString("ifs_code"));
                        }
                        if(obj.has("bankcode")){
                            wsmodel.setBankcode(obj.getString("bankcode"));
                        }
                        if(obj.has("cardnumber")){
                            wsmodel.setCardnumber(obj.getString("cardnumber"));
                        }
                        if(obj.has("card_number")){
                            wsmodel.setCardnumber(obj.getString("card_number"));
                        }
                        if(obj.has("clabe")){
                            wsmodel.setClabe(obj.getString("clabe"));
                        }
                        if(obj.has("cbu")){
                            wsmodel.setCbu(obj.getString("cbu"));
                        }
                        if(obj.has("cbualias")){
                            wsmodel.setCbualias(obj.getString("cbualias"));
                        }
                        if(obj.has("cbu_alias")){
                            wsmodel.setCbualias(obj.getString("cbu_alias"));
                        }
                        if(obj.has("bikcode")){
                            wsmodel.setBikcode(obj.getString("bikcode"));
                        }
                        if(obj.has("bik_code")){
                            wsmodel.setBikcode(obj.getString("bik_code"));
                        }
                        if(obj.has("abaroutingnumber")){
                            wsmodel.setAbaroutingnumber(obj.getString("abaroutingnumber"));
                        }
                        if(obj.has("aba_routing_number")){
                            wsmodel.setAbaroutingnumber(obj.getString("aba_routing_number"));
                        }
                        if(obj.has("bsbnumber")){
                            wsmodel.setBsbnumber(obj.getString("bsbnumber"));
                        }
                        if(obj.has("bsb_number")){
                            wsmodel.setBsbnumber(obj.getString("bsb_number"));
                        }
                        if(obj.has("branchcode")){
                            wsmodel.setBranchcode(obj.getString("branchcode"));
                        }
                        if(obj.has("branch_number")){
                            wsmodel.setBranchcode(obj.getString("branch_number"));
                        }
                        if(obj.has("routingcode")){
                            wsmodel.setRoutingcode(obj.getString("routingcode"));
                        }
                        if(obj.has("routing_code")){
                            wsmodel.setRoutingcode(obj.getString("routing_code"));
                        }
                        if(obj.has("entityttid")){
                            wsmodel.setEntityttid(obj.getString("entityttid"));
                        }
                        if(obj.has("entity_tt_id")){
                            wsmodel.setEntityttid(obj.getString("entity_tt_id"));
                        }
                        if(obj.has("accounttype")){
                            wsmodel.setAccounttype(obj.getString("accounttype"));
                        }
                        if(obj.has("account_type")){
                            wsmodel.setAccounttype(obj.getString("account_type"));
                        }
                        if(obj.has("creditaccountnumber")){
                            wsmodel.setCreditaccountnumber(obj.getString("creditaccountnumber"));
                        }
                        if(obj.has("firstname")){
                            wsmodel.setDestfirstname(obj.getString("firstname"));
                        }
                        if(obj.has("lastname")){
                            wsmodel.setDestlastname(obj.getString("lastname"));
                        }
                        if(obj.has("country")){
                            wsmodel.setDestcountry(obj.getString("country"));
                        }
                        if(obj.has("identificationno")){
                            wsmodel.setIdentificationno(obj.getString("identificationno"));
                        }
                        if(obj.has("identificationtype")){
                            wsmodel.setTypefilter(obj.getString("identificationtype"));
                        }
                        if(obj.has("destmobilenumber")){
                            wsmodel.setDestmobilenumber(obj.getString("destmobilenumber"));
                            if(!Util.hasText(wsmodel.getConsumerno())){ //Raza adding 30-08-2024
                                wsmodel.setConsumerno(wsmodel.getDestmobilenumber());
                            }
                        }
                        if(obj.has("msisdn")){
                            wsmodel.setDestmobilenumber(obj.getString("msisdn"));
                            if(!Util.hasText(wsmodel.getConsumerno())){ //Raza adding 30-08-2024
                                wsmodel.setConsumerno(wsmodel.getDestmobilenumber());
                            }
                        }
                        if(obj.has("destaddress")){
                            wsmodel.setDestaddress(obj.getString("destaddress"));
                        }
                        if(obj.has("payerid")){
                            wsmodel.setPayerid(obj.getString("payerid"));
                        }
                        if(obj.has("email")){
                            wsmodel.setDestemailaddress(obj.getString("email"));
                        }
                        if(obj.has("sort_code")){
                            wsmodel.setBankcode(obj.getString("sort_code"));
                        }
                        if(obj.has("id_type")){
                            wsmodel.setTypefilter(obj.getString("id_type"));
                        }
                        if(obj.has("id_code")){
                            wsmodel.setIdentificationno(obj.getString("id_code"));
                        }
                    }

                    try{ //Raza adding 01-09-2024 for Invalid DestMobileNumber/MSISDN/ConsumerNo position in JSON Object issue..
                        if(!Util.hasText(wsmodel.getConsumerno())){
                            logger.info("Retrying ConsumerNo...");
                            if(obj.has("destmobilenumber")){
                                wsmodel.setConsumerno( obj.getString("destmobilenumber"));
                                logger.info("ConsumerNo [" + wsmodel.getConsumerno() + "] added finally..");
                                if(!Util.hasText(wsmodel.getDestmobilenumber())){
                                    wsmodel.setDestmobilenumber(wsmodel.getConsumerno());
                                }
                            }
                        }
                    }
                    catch (Exception e){
                        logger.error("Exception caught while explicitly getting ConsumerNo, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }





                    return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);

                }
                else
                {
                    logger.error("Beneficiary not found against ID [" + obj.getString("id") + "] and Customer Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
                    //wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                    //return wsmodel;
                    try{
                        obj = new JSONObject(dbbenef.getBeneficiaryinfo());
                        wsmodel.setBeneficiaryobj(dbbenef.getBeneficiaryinfo());

                        if(obj.has("consumerno")){
                            wsmodel.setConsumerno(obj.getString("consumerno"));
                        }
                        if(obj.has("accountnumber")){
                            wsmodel.setDestaccount(obj.getString("accountnumber"));
                        }
                        if(obj.has("swiftbiccode")){
                            wsmodel.setSwiftbiccode(obj.getString("swiftbiccode"));
                        }
                        if(obj.has("iban")){
                            wsmodel.setIban(obj.getString("iban"));
                        }
                        if(obj.has("ifscode")){
                            wsmodel.setIfscode(obj.getString("ifscode"));
                        }
                        if(obj.has("bankcode")){
                            wsmodel.setBankcode(obj.getString("bankcode"));
                        }
                        if(obj.has("cardnumber")){
                            wsmodel.setCardnumber(obj.getString("cardnumber"));
                        }
                        if(obj.has("clabe")){
                            wsmodel.setClabe(obj.getString("clabe"));
                        }
                        if(obj.has("cbu")){
                            wsmodel.setCbu(obj.getString("cbu"));
                        }
                        if(obj.has("cbualias")){
                            wsmodel.setCbualias(obj.getString("cbualias"));
                        }
                        if(obj.has("bikcode")){
                            wsmodel.setBikcode(obj.getString("bikcode"));
                        }
                        if(obj.has("abaroutingnumber")){
                            wsmodel.setAbaroutingnumber(obj.getString("abaroutingnumber"));
                        }
                        if(obj.has("bsbnumber")){
                            wsmodel.setBsbnumber(obj.getString("bsbnumber"));
                        }
                        if(obj.has("branchcode")){
                            wsmodel.setBranchcode(obj.getString("branchcode"));
                        }
                        if(obj.has("routingcode")){
                            wsmodel.setRoutingcode(obj.getString("routingcode"));
                        }
                        if(obj.has("entityttid")){
                            wsmodel.setEntityttid(obj.getString("entityttid"));
                        }
                        if(obj.has("accounttype")){
                            wsmodel.setAccounttype(obj.getString("accounttype"));
                        }
                        if(obj.has("creditaccountnumber")){
                            wsmodel.setCreditaccountnumber(obj.getString("creditaccountnumber"));
                        }
                        if(obj.has("firstname")){
                            wsmodel.setDestfirstname(obj.getString("firstname"));
                        }
                        if(obj.has("lastname")){
                            wsmodel.setDestlastname(obj.getString("lastname"));
                        }
                        if(obj.has("country")){
                            wsmodel.setDestcountry(obj.getString("country"));
                        }
                        if(obj.has("identificationno")){
                            wsmodel.setIdentificationno(obj.getString("identificationno"));
                        }
                        if(obj.has("identificationtype")){
                            wsmodel.setTypefilter(obj.getString("identificationtype"));
                        }
                        if(obj.has("destmobilenumber")){
                            wsmodel.setDestmobilenumber(obj.getString("destmobilenumber"));
                        }
                        if(obj.has("destaddress")){
                            wsmodel.setDestaddress(obj.getString("destaddress"));
                        }
                        if(obj.has("payerid")){
                            wsmodel.setPayerid(obj.getString("payerid"));
                        }
                    }
                    catch (Exception e){
                        logger.error("Exception caught while translatting Beneficiary object, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                        // Zaid Add this line
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                        return wsmodel;
                    }
                }
            }
            catch(Exception e)
            {
                logger.error("Exception caught while getting Beneficiary from DB, ignoring...");
                logger.error(WebServiceUtil.getStrException(e));
                //Zaid add this line
                wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                return wsmodel;
            }
        }
        else{
            //Raza commenting below fetching of Beneficiary on 20-08-2021 -- Reopenning 10-05-2022 for address only
            try {
                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.utilcompany= :UTILCOMP " + " and c.consumerno= :BENEFICIARY ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("BENEFICIARY", wsmodel.getConsumerno());
                params.put("UTILCOMP", wsmodel.getUtilcompanyid());


                MWBeneficiary dbbenef = (MWBeneficiary)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbbenef != null)
                {
//                wsmodel.setDestfirstname(dbbenef.getFirstname());
//                wsmodel.setDestlastname(dbbenef.getLastname());
                    wsmodel.setDestaddress(dbbenef.getDestaddress());

                    //Raza adding 24-05-2022 start
                    wsmodel.setSwiftbiccode(dbbenef.getSwiftcode());
                    wsmodel.setIban(dbbenef.getIban());
                    wsmodel.setIfscode(dbbenef.getIfscode());
                    wsmodel.setBankcode(dbbenef.getSortcode());
                    wsmodel.setCardnumber(dbbenef.getCardnumber());
                    wsmodel.setClabe(dbbenef.getClabe());
                    wsmodel.setCbu(dbbenef.getCbu());
                    wsmodel.setCbualias(dbbenef.getCbualias());
                    wsmodel.setBikcode(dbbenef.getBikcode());
                    wsmodel.setAbaroutingnumber(dbbenef.getAbaroutingnumber());
                    wsmodel.setBsbnumber(dbbenef.getBsbnumber());
                    wsmodel.setBranchcode(dbbenef.getBranchnumber());
                    wsmodel.setRoutingcode(dbbenef.getRoutingcode());
                    wsmodel.setEntityttid(dbbenef.getEntityttid());
                    wsmodel.setAccounttype(dbbenef.getAccounttype());
                    wsmodel.setCreditaccountnumber(dbbenef.getAccount());
                    wsmodel.setEmailaddress(dbbenef.getEmail());
                    wsmodel.setTypefilter(dbbenef.getIdtype());
                    wsmodel.setIdentificationno(dbbenef.getIdcode());

                    //wsmodel.setCreditaccountnumber(dbbenef.getCreditaccount());
                    //wsmodel.setAccountnumber(dbbenef.getAccount());
                    //Raza adding 24-05-2022 end
                }
                else
                {
                    logger.error("Beneficiary not found against ConsumerNo [" + wsmodel.getConsumerno() + "], UtilityCompany [" + wsmodel.getUtilcompanyid() + "], ignoring...");
                }
            }
            catch(Exception e)
            {
                logger.error("Failed to Get & Set Beneficiary Details, ignoring...");
            }
        }
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processMerchantQRSendMoneyRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processSendMOneyInquiryRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processFetchProvisionalWalletRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processFetchProvisionalWalletListRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processUpdateProvisionalWalletAddressRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processVerifyConsumerRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetConsumerTransactionsRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetTransactionDetailsRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processUpdateWalletAddressRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processUpdateWalletSecondaryPhoneNumberRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processResetWalletPinRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
        params = new HashMap<String, Object>();
        params.put("MOBNO", wsmodel.getMobilenumber());

        MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

        if(customer != null && customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
        {
//            if(GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()) != null &&
//                    GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()).getSmsenabled() != null &&
//                    GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()).getSmsenabled())
//            {
            wsmodel.setPindata(customer.getMigratedpin());
                if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.RESET_WALLET_PIN, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This //Raza adding 03032021
                          return wsmodel;
                }
                else{
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED); //Raza adding 03032021
                    return wsmodel;
                }
//            }
//            else{
//                return wsmodel;
//            }
        }
        else
        {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
        }
    }

    public static AppWsEntity processAdminBlockWalletAccountRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processBlockDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processBlockChannelRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processOnelinkBillerRequest(AppWsEntity wsmodel) //Raza adding for OneLink
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false); //Raza Verify THIS
    }

    public static AppWsEntity processEnvelopLoadRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processEnvelopUnloadRequest(AppWsEntity wsmodel)
    {
            //1st Get LoadEnvelop & verify if it is 00
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.tranrefnumber= :TXNREFNUM "
                    + "and c.servicename = :SERVNAME ";
            params = new HashMap<String, Object>();
            params.put("TXNREFNUM", wsmodel.getOrigdataelement());
            params.put("SERVNAME", "EnvelopLoad");

            AppWsEntity dbloadtxn = (AppWsEntity)GeneralDao.Instance.findObject(dbQuery, params);

            if(dbloadtxn == null || !dbloadtxn.getRespcode().equals("00"))
            {
                logger.error("Load Transaction with TxnRefNum [" + wsmodel.getOrigdataelement() +  "] not found or declined, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND); //30 Original not found, rejecting...
                return wsmodel;
            }

            //2nd Find if Unload is received, if yes then check 00 & reject
            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.origdataelement = :TXNREFNUM "
                    + "and c.servicename = :SERVNAME ";
            params = new HashMap<String, Object>();
            params.put("TXNREFNUM", wsmodel.getOrigdataelement());
            params.put("SERVNAME", "EnvelopUnload");

            List<AppWsEntity> dbunloadtxnlist = GeneralDao.Instance.find(dbQuery, params);

            if(dbunloadtxnlist != null && dbunloadtxnlist.size() > 0)
            {
                for(AppWsEntity unloadtxn : dbunloadtxnlist)
                {
                    if(!unloadtxn.getTranrefnumber().equals(wsmodel.getTranrefnumber()) && unloadtxn.getRespcode().equals("00"))
                    {
                        logger.error("Unload Envelop already recevied for TxnRefNum [" + wsmodel.getOrigdataelement() +  "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_ALREADY_REVERSED); //35 Original Already Reversed - Refer to Doc
                        return wsmodel;
                    }
                }
            }

            //3rd Find if its Reversal is received, if yes then check 00 & reject
            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.origdataelement = :TXNREFNUM "
                    + "and c.servicename = :SERVNAME ";
            params = new HashMap<String, Object>();
            params.put("TXNREFNUM", wsmodel.getOrigdataelement());
            params.put("SERVNAME", "ReverseEnvelop");

            List<AppWsEntity> dbrevloadtxnlist = GeneralDao.Instance.find(dbQuery, params);

            if(dbrevloadtxnlist != null && dbrevloadtxnlist.size() > 0)
            {
                for(AppWsEntity rextxn : dbrevloadtxnlist) {
                    if (rextxn.getRespcode().equals("00")) {
                        logger.error("Reverse Envelop already recevied for TxnRefNum [" + wsmodel.getOrigdataelement() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_ALREADY_REVERSED); //35 Original Already Reversed - Refer to Doc
                        return wsmodel;
                    }
                }
            }



            //For wallet adding fields from original Transaction start
            wsmodel.setUserid(dbloadtxn.getUserid());
            wsmodel.setDestuserid(dbloadtxn.getDestuserid());
            wsmodel.setAmounttransaction(dbloadtxn.getAmounttransaction());
            //For wallet adding fields from original Transaction end

            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processReverseEnvelopRequest(AppWsEntity wsmodel)
    {
            //1st Get LoadEnvelop & verify if it is 00
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.tranrefnumber= :TXNREFNUM "
                    + "and c.servicename = :SERVNAME ";
            params = new HashMap<String, Object>();
            params.put("TXNREFNUM", wsmodel.getOrigdataelement());
            params.put("SERVNAME", "EnvelopLoad");

            AppWsEntity dbloadtxn = (AppWsEntity)GeneralDao.Instance.findObject(dbQuery, params);

            if(dbloadtxn == null || !dbloadtxn.getRespcode().equals("00"))
            {
                logger.error("Load Transaction with TxnRefNum [" + wsmodel.getOrigdataelement() +  "] not found or declined, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND); //30 Original not found, rejecting...
                return wsmodel;
            }

            //2nd Find if its Unload is received, if yes then check 00 & reject
            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.origdataelement = :TXNREFNUM "
                    + "and c.servicename = :SERVNAME ";
            params = new HashMap<String, Object>();
            params.put("TXNREFNUM", wsmodel.getOrigdataelement());
            params.put("SERVNAME", "EnvelopUnload");

            List<AppWsEntity> dbunloadtxnlist = GeneralDao.Instance.find(dbQuery, params);

            if(dbunloadtxnlist != null && dbunloadtxnlist.size() > 0)
            {
                for(AppWsEntity unloadtxn : dbunloadtxnlist)
                {
                    if(!unloadtxn.getTranrefnumber().equals(wsmodel.getTranrefnumber()) && unloadtxn.getRespcode().equals("00"))
                    {
                        logger.error("Unload Envelop already recevied for TxnRefNum [" + wsmodel.getOrigdataelement() +  "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_ALREADY_REVERSED); //35 Original Already Reversed - Refer to Doc
                        return wsmodel;
                    }
                }
            }

            //3rd Find if its Reversal is received, if yes then check 00 & reject
            dbQuery = "from " + AppWsEntity.class.getName() + " c where c.origdataelement = :TXNREFNUM "
                    + "and c.servicename = :SERVNAME ";
            params = new HashMap<String, Object>();
            params.put("TXNREFNUM", wsmodel.getOrigdataelement());
            params.put("SERVNAME", "ReverseEnvelop");

            List<AppWsEntity> dbrevloadtxnlist = GeneralDao.Instance.find(dbQuery, params);

            if(dbrevloadtxnlist != null && dbrevloadtxnlist.size() > 0)
            {
                for(AppWsEntity rextxn : dbrevloadtxnlist) {
                    if (!rextxn.getTranrefnumber().equals(wsmodel.getTranrefnumber()) && rextxn.getRespcode().equals("00")) {
                        logger.error("Reverse Envelop already recevied for TxnRefNum [" + wsmodel.getOrigdataelement() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_ALREADY_REVERSED); //35 Original Already Reversed - Refer to Doc
                        return wsmodel;
                    }
                }
            }


            //For wallet adding fields from original Transaction start
            wsmodel.setUserid(dbloadtxn.getUserid());
            wsmodel.setDestuserid(dbloadtxn.getDestuserid());
            wsmodel.setAmounttransaction(dbloadtxn.getAmounttransaction());
            //For wallet adding fields from original Transaction end

            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetKycQuestionsRequestV2(AppWsEntity wsmodel, Boolean verifysession)
    {
        try {
            String dbQuery;
            logger.info("Validating Session...");
            if(verifysession)
            {
                if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
                {
                    logger.error("Failed to Validate Session, rejecting...");
                    return wsmodel;
                }
            }

            if(Util.isMultiLangEnabled()){
                if(Util.isMultiLangSecretQuestionsEnabled()){

                    dbQuery = "select distinct r from MWSecurQuestions r " +
                            "left join fetch r.translations t ";

                    // Execute query with JOIN FETCH
                    List<MWSecurQuestions> dbquestions = GeneralDao.Instance.find(dbQuery);

                    if (dbquestions != null && !dbquestions.isEmpty()) {
                        List<SecretQuestionObj> secretquestlist = new ArrayList<>();
                        int count = 0;
                        for (MWSecurQuestions r : dbquestions) {
                            count++;
                            // Default values from parent
                            String question = null;
                            if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
                                question = r.getQuestion();
                            }
                            else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
                                question = r.getFrquestion();
                            }
                            else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
                                question = r.getQuestion();
                            }
                            else{
                                question = r.getFrquestion();
                            }

                            // Override if translation exists for requested language
                            if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
                                Optional<MWSecurQuestionsTranslation> match = r.getTranslations()
                                        .stream()
                                        .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
                                        .findFirst();
                                if (match.isPresent() && Util.hasText(match.get().getQuestion())) {
                                    question = match.get().getQuestion();
                                    if(count == 1){
                                        wsmodel.setSecretquestion1(question);
                                    }
                                    else if(count == 2){
                                        wsmodel.setSecretquestion2(question);
                                    }
                                }
                            }

                            SecretQuestionObj obj = new SecretQuestionObj();
                            obj.setSecretquestion(question);

                            secretquestlist.add(obj);
                        }

                        wsmodel.setSecretquestionslist(secretquestlist);
                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    } else {
                        logger.error("Secret Questions not found in DB, falling back to old implementation...");
                        processGetKycQuestionsRequestV1(wsmodel, false);
                    }
                }
                else{
                    logger.info("MultiLanguage SecretQuestions feature disabled, processing through old implementation...");
                    processGetKycQuestionsRequestV1(wsmodel, false);
                }
            }
            else{
                logger.info("MultiLanguage feature disabled, processing through old implementation...");
                processGetKycQuestionsRequestV1(wsmodel, false);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while getting KYCQuestions, falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            processGetKycQuestionsRequestV1(wsmodel, false);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetKycQuestionsRequestV1(AppWsEntity wsmodel, Boolean verifysession)
    {
        try {
            logger.info("Validating Session...");
            if(verifysession)
            {
                if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
                {
                    logger.error("Failed to Validate Session, rejecting...");
                    return wsmodel;
                }
            }

            String dbQuery;
            dbQuery = "from " + MWSecurQuestions.class.getName();

            List<MWSecurQuestions> secretquestions = GeneralDao.Instance.find(dbQuery);

            if(secretquestions != null && secretquestions.size() > 0)
            {
                logger.info("SecretQuestions found, replying...");
                List<SecretQuestionObj> secretquestlist = new ArrayList<>();
                for(int i=0 ; i<secretquestions.size() ; i++) //Raza there should be atleast 2 Questions in DB
                {
                    SecretQuestionObj sq = new SecretQuestionObj();
                    if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
                        if(i==1){
                            wsmodel.setSecretquestion1(secretquestions.get(i).getQuestion());
                        }
                        else if(i==2){
                            wsmodel.setSecretquestion2(secretquestions.get(i).getQuestion());
                        }
                        sq.setSecretquestion(secretquestions.get(i).getQuestion());
                    }
                    else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
                        if(i==1){
                            wsmodel.setSecretquestion1(secretquestions.get(i).getFrquestion());
                        }
                        else if(i==2){
                            wsmodel.setSecretquestion2(secretquestions.get(i).getFrquestion());
                        }
                        sq.setSecretquestion(secretquestions.get(i).getFrquestion());
                    }
                    else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
                        if(i==1){
                            wsmodel.setSecretquestion1(secretquestions.get(i).getQuestion());
                        }
                        else if(i==2){
                            wsmodel.setSecretquestion2(secretquestions.get(i).getQuestion());
                        }
                        sq.setSecretquestion(secretquestions.get(i).getQuestion());
                    }
                    else{
                        if(i==1){
                            wsmodel.setSecretquestion1(secretquestions.get(i).getFrquestion());
                        }
                        else if(i==2){
                            wsmodel.setSecretquestion2(secretquestions.get(i).getFrquestion());
                        }
                        sq.setSecretquestion(secretquestions.get(i).getFrquestion());
                    }
                    secretquestlist.add(sq);
                }
                wsmodel.setSecretquestionslist(secretquestlist);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
            else
            {
                logger.error("Secret Questions not found in DB, sending as per code...");
                wsmodel.setSecretquestion1("What was your first Job?"); //TODO: Raza remove This after updating on App
                wsmodel.setSecretquestion2("What was your first Car?"); //TODO: Raza remove This after updating on App

                List<SecretQuestionObj> secretquestlist = new ArrayList<>();
                SecretQuestionObj sq = new SecretQuestionObj();
                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG"))
                {
                    sq.setSecretquestion("In what town was your first job?");
                }
                else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
                    sq.setSecretquestion("Dans quelle ville était ton premier emploi?");
                }
                else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
                    sq.setSecretquestion("In what town was your first job?");
                }
                else
                {
                    sq.setSecretquestion("Dans quelle ville était ton premier emploi?");
                }
                secretquestlist.add(sq);

                SecretQuestionObj sq2 = new SecretQuestionObj();
                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG"))
                {
                    sq2.setSecretquestion("What was your first car?");
                }
                else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
                    sq2.setSecretquestion("Quel était votre première voiture ?");
                }
                else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
                    sq2.setSecretquestion("What was your first car?");
                }
                else
                {
                    sq2.setSecretquestion("Quel était votre première voiture ?");
                }
                secretquestlist.add(sq2);

                wsmodel.setSecretquestionslist(secretquestlist);

                //wsmodel.setSecretquestion1("Who the Fook is that guy?");
                //wsmodel.setSecretquestion2("Did you kiss your mother with that mouth?");
                //wsmodel.setSecretquestion1("When did you take your first Shit?");
                //wsmodel.setSecretquestion2("Why haven't someone sensible shot you yet?");
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while getting KYCQuestions, rejecting...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processGetUserKycQuestionsRequestV2(AppWsEntity wsmodel)
    {
        try {

        logger.info("Validating Session...");
        if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
        {
            logger.error("Failed to Validate Session, rejecting...");
            return wsmodel;
        }

            String dbQuery;
            Map<String, Object> params;
            wsmodel.setSecretquestion1(null);
            wsmodel.setSecretquestion2(null);
            //new start
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(customer != null) //Raza not validating customer...
            {
                    dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUSTOMER ";
                    params = new HashMap<String, Object>();
                    params.put("CUSTOMER", customer);
                    List<MWCustSecurQuestions> cmsCustSecurQuestionsList = GeneralDao.Instance.find(dbQuery, params);

                    if(cmsCustSecurQuestionsList == null || cmsCustSecurQuestionsList.size() <=0)
                    {
                        logger.error("Security Questions not found for User ID  [" + wsmodel.getUserid() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE); //NP_6002 - Secondary Data Element not found
                    }
                    else
                    {
                        logger.info("Getting Customer's Security Questions...");
                        for (MWCustSecurQuestions cmsCustSecurQuestions : cmsCustSecurQuestionsList) {

                            if (Util.hasText(wsmodel.getLanguage()) && cmsCustSecurQuestions.getQuestion().getTranslations() != null) {
                                Optional<MWSecurQuestionsTranslation> match = cmsCustSecurQuestions.getQuestion().getTranslations()
                                        .stream()
                                        .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
                                        .findFirst();
                                if (match.isPresent() && Util.hasText(match.get().getQuestion())) {
                                    if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                        wsmodel.setSecretquestion1(match.get().getQuestion());
                                    }
                                    else if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                        wsmodel.setSecretquestion2(match.get().getQuestion());
                                    }
                                } else {
                                    if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                        wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getQuestion());
                                    }
                                    else if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                        wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getQuestion());
                                    }
                                }
                            }
                            else{
                                if(Util.hasText(customer.getLanguage()) && customer.getLanguage().equals("ENG"))
                                {
                                    if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                        wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getQuestion());
                                    }
                                    else if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                        wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getQuestion());
                                    }
                                }
                                else
                                {
                                    if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                        wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getQuestion());
                                    }
                                    else if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                        wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getQuestion());
                                    }
                                }
                            }
                        }

                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    }
            }
            else
            {
                logger.error("Customer not found for Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            logger.error("Exception caught while Executing GetUserKYCQuestionList, falling back to old implementation...");
            processGetUserKycQuestionsRequestV1(wsmodel);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetUserKycQuestionsRequestV1(AppWsEntity wsmodel)
    {
        try {

            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;
            wsmodel.setSecretquestion1(null);
            wsmodel.setSecretquestion2(null);
            //new start
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(customer != null) //Raza not validating customer...
            {
                dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUSTOMER ";
                params = new HashMap<String, Object>();
                params.put("CUSTOMER", customer);
                List<MWCustSecurQuestions> cmsCustSecurQuestionsList = GeneralDao.Instance.find(dbQuery, params);

                if(cmsCustSecurQuestionsList == null || cmsCustSecurQuestionsList.size() <=0)
                {
                    logger.error("Security Questions not found for User ID  [" + wsmodel.getUserid() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE); //NP_6002 - Secondary Data Element not found
                    return wsmodel;
                }
                else
                {
                    logger.info("Getting Customer's Security Questions...");
                    for (MWCustSecurQuestions cmsCustSecurQuestions : cmsCustSecurQuestionsList) {
                        if(Util.hasText(customer.getLanguage()) && customer.getLanguage().equals("ENG"))
                        {
                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getQuestion());
                            }

                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getQuestion());
                            }
                        }
                        else if(Util.hasText(customer.getLanguage()) && customer.getLanguage().equals("FRA")){
                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getFrquestion());
                            }

                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getFrquestion());
                            }
                        }
                        else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getQuestion());
                            }

                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getQuestion());
                            }
                        }
                        else
                        {
                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                wsmodel.setSecretquestion1(cmsCustSecurQuestions.getQuestion().getFrquestion());
                            }

                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                wsmodel.setSecretquestion2(cmsCustSecurQuestions.getQuestion().getFrquestion());
                            }
                        }
                    }

                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    return wsmodel;
                }
            }
            else
            {
                logger.error("Customer not found for Mobile Number [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            logger.error("Exception caught while Executing GetUserKYCQuestionList..!");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

    }

    public static AppWsEntity processUpdateSecretQuestionsRequest(AppWsEntity wsmodel)
    {
        try {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(customer != null)
            {

                if(Util.hasText(wsmodel.getPindata()))
                {
                    String servicename = wsmodel.getServicename();
                    wsmodel.setServicename("VerifyPin");
                    MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                    wsmodel.setServicename(servicename);
                    if(!wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
                    {
                        logger.error("Failed to verify PIN cannot update secret questions, rejecting...");
                        return wsmodel;
                    }
                    else
                    {
                        if(UpdateSecurityQuestions(wsmodel, customer)) //TODO: Raza update This Logic
                        {
                            logger.info("Secret Questions updated successfully!");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                            return wsmodel;
                        }
                        else
                        {
                            logger.info("Unable to Update Secret Questions for UserId [ "+ wsmodel.getUserid() +", rejecting...");
                            return wsmodel;
                        }
                    }
                }
                else
                {
                    if(UpdateSecurityQuestions(wsmodel, customer)) //TODO: Raza update This Logic
                    {
                        logger.info("Secret Questions updated successfully!");
                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        return wsmodel;
                    }
                    else
                    {
                        logger.info("Unable to Update Secret Questions for UserId [ "+ wsmodel.getUserid() +", rejecting...");
                        return wsmodel;
                    }
                }

            }
            else
            {
                logger.info("Customer not found against MobileNumber [ "+ wsmodel.getMobilenumber() +"] to Update Secret Questions, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND); //90 Customer not found
                return wsmodel;
            }


        }
        catch (Exception e)
        {
            logger.error("Exception caught while Updating Secret Questions, rejecting...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processVerifySecretQuestionsRequest(AppWsEntity wsmodel)
    {
        try {

            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }


            if(!Util.hasText(wsmodel.getSecretquestion1()) || !Util.hasText(wsmodel.getSecretquestion2())
              || !Util.hasText(wsmodel.getSecretquestionanswer1()) || !Util.hasText(wsmodel.getSecretquestionanswer2())) //Raza 15-02-2021
            {
                logger.error("Incomplete Questions and Answers Data in request, assuming migrated customer...");
                //wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED); //Raza commenting 04032021
                //return wsmodel; ////Raza commenting 04032021
            }



            String dbQuery;
            Map<String, Object> params;

            //new start
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);
            Boolean firstverified = false; //TODO: Raza update this Logic adding 04032021
            Boolean secondverified = false; //TODO: Raza update this Logic adding 04032021
            if(customer != null)
            {
                    dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUSTOMER ";
                    params = new HashMap<String, Object>();
                    params.put("CUSTOMER", customer);
                    List<MWCustSecurQuestions> cmsCustSecurQuestionsList = GeneralDao.Instance.find(dbQuery, params);

                    if(cmsCustSecurQuestionsList == null || cmsCustSecurQuestionsList.size() <=0)
                    {
                        logger.error("Security Questions not found for User ID  [" + wsmodel.getUserid() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED); //Invalid Card Record ; Raza update THIS
                        return wsmodel;
                    }
                    else
                    {
                        logger.info("Verifying Customer's Security Questions/Answers...");

                        for (MWCustSecurQuestions cmsCustSecurQuestions : cmsCustSecurQuestionsList) {

                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(1)) {
                                boolean answerMismatch = !Util.normalizeStringExcludingWhiteSpaces(
                                                cmsCustSecurQuestions.getAnswer().toUpperCase().trim())
                                        .equals(Util.normalizeStringExcludingWhiteSpaces(
                                                wsmodel.getSecretquestionanswer1().toUpperCase().trim()));

                                boolean questionMismatch = !cmsCustSecurQuestions.getQuestion().getQuestion().toUpperCase()
                                        .equals(wsmodel.getSecretquestion1().toUpperCase())
                                        && cmsCustSecurQuestions.getQuestion().getTranslations().stream()
                                        .noneMatch(q -> q.getQuestion().toUpperCase().equals(wsmodel.getSecretquestion1().toUpperCase()));

                                if (answerMismatch || questionMismatch) {
                                    logger.error("Secret Question/Answer 1 verification failed, rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SECUR_QUES_VER_FAILED);
                                    return wsmodel;
                                }
                            firstverified = true;
                            }

                            if (cmsCustSecurQuestions.getQuestionnumber() != null && cmsCustSecurQuestions.getQuestionnumber().equals(2)) {
                                boolean answerMismatch = !Util.normalizeStringExcludingWhiteSpaces(
                                                cmsCustSecurQuestions.getAnswer().toUpperCase().trim())
                                        .equals(Util.normalizeStringExcludingWhiteSpaces(
                                                wsmodel.getSecretquestionanswer2().toUpperCase().trim()));

                                boolean questionMismatch = !cmsCustSecurQuestions.getQuestion().getQuestion().toUpperCase()
                                        .equals(wsmodel.getSecretquestion2().toUpperCase())
                                        && cmsCustSecurQuestions.getQuestion().getTranslations().stream()
                                        .noneMatch(q -> q.getQuestion().toUpperCase().equals(wsmodel.getSecretquestion2().toUpperCase()));

                                if (answerMismatch || questionMismatch) {
                                    logger.error("Secret Question/Answer 2 verification failed, rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SECUR_QUES_VER_FAILED);
                                    return wsmodel;
                                }
                                secondverified = true;
                            }
                        }
                        logger.info("Security Questions verified for customer [" + wsmodel.getMobilenumber() + "]");


                        if(firstverified && secondverified)
                        {
                            logger.info("Both Secret Questions verified OK, returning...");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        }
                        else if(firstverified && !secondverified && customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
                        {
                            logger.info("First Secret Question verified OK, Customer is migrated returning...");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        }
                        else if(!firstverified && secondverified && customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
                        {
                            logger.info("Second Secret Question verified OK, Customer is migrated returning...");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        }
                        else if(firstverified && !secondverified && customer.getIsmigrated() != null && customer.getIsmigrated() && wsmodel.getReason().equals(SMSCategory.FORGOT_PASSWORD) && cmsCustSecurQuestionsList.size() == 1) //forgetpasswordflow added by waleed
                        {
                            logger.info("First Secret Question verified OK, Customer is migrated returning...");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        }
                        else if(!firstverified && secondverified && customer.getIsmigrated() != null && customer.getIsmigrated() && wsmodel.getReason().equals(SMSCategory.FORGOT_PASSWORD) && cmsCustSecurQuestionsList.size() == 1) //forgetpasswordflow added by waleed
                        {
                            logger.info("Second Secret Question verified OK, Customer is migrated returning...");
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        }
                        else
                        {
                            logger.info("Failed to verify both Secret Questions for Non-Migrated Customer, rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SECUR_QUES_VER_FAILED);
                        }


                        if (Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals(SMSCategory.LOGOUT_ALL_SESSIONS)) {
                            MarkSessionsExpire(wsmodel);

                            String deviceID = wsmodel.getSecurityparams().getDeviceid();

                            dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid = :DEVICE_ID";
                            params = new HashMap<String, Object>();

                            params.put("DEVICE_ID", deviceID);

                            MWDeviceLog deviceLog = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

                            if (deviceLog == null) {
                                logger.info("device [{}] not found, creating new one...", deviceID);
                                deviceLog = new MWDeviceLog();
                                deviceLog.setDeviceid(deviceID);
                            } else {
                                logger.info("device [{}] found, using existing one...", deviceID);
                            }

                            deviceLog.setCustomer(customer);

                            GeneralDao.Instance.saveOrUpdate(deviceLog);

                            MWSessionConfig sessionConfig = MWSessionConfig.getSessionConfig(CustomerType.CUSTOMER);

                            if(sessionConfig.getGlobalswitch()){
                                MWCustDeviceBindingLog bindLog = MWCustDeviceBindingLog
                                        .getCustomerDeviceBinding(customer, deviceLog);

                                if(bindLog == null){
                                    logger.info("Binding device [{}] with customer [{}]", deviceLog.getDeviceid(), customer.getMobilenumber());

                                    bindLog = new MWCustDeviceBindingLog();
                                    bindLog.setDevice(deviceLog);
                                    bindLog.setCustomer(customer);
                                    bindLog.setReason("SIGNUP");
                                    bindLog.setCreatedate(new Date());
                                    bindLog.setLastupdatedate(new Date());
                                    bindLog.setStatus("00");

                                    GeneralDao.Instance.save(bindLog);
                                } else {
                                    logger.info("Device [{}] already binded with customer [{}]", deviceLog.getDeviceid(), customer.getMobilenumber());
                                }

                            }

                        }

                        return wsmodel;
                    }

            }
            else
            {
                logger.info("Customer not Found for MobileNumber [ "+ wsmodel.getMobilenumber() +", cannot verify secret questions. rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND); //90 Customer not found
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Verifying User Secret Questions, rejecting...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processAdminPortalTempBlockDebitCardRequest(AppWsEntity wsmodel)
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }
	
    //m.rehman: for Nayapaya, adding new call for document 2.0 <start>
    public static AppWsEntity processMerchantReversalTransactionRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processBillInquiryRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processBillPaymentRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetBillPackageListRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processOnelinkBillerTransactionRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processOnelinkBillerCoreTransactionRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processBlockMerchantParentRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processAdminPortalGetUserWalletRequest(AppWsEntity wsmodel) {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAdminPortalGetUserDebitCardRequest(AppWsEntity wsmodel) {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAdminPortalGetUserLinkedAccountListRequest(AppWsEntity wsmodel) {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAdminPortalGetUserTransactionListRequest(AppWsEntity wsmodel) {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAdminPortalGetTransactionDetailRequest(AppWsEntity wsmodel) {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAdminPortalBlockWalletAccountRequest(AppWsEntity wsmodel) {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }
    //m.rehman: for Nayapaya, adding new call for document 2.0 <end>

    public static AppWsEntity processGetTransactionChargeRequest(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
            return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processFundManagementRequest(AppWsEntity wsmodel)
    {
        return wsmodel;
        //return MWWSOperation.ExecuteFundManagementRequest(wsmodel);
    }

    public static AppWsEntity processGeneratePINBlock(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }


    public static AppWsEntity processCreateOTP(AppWsEntity wsmodel) throws Exception
    {
        if (Util.hasText(wsmodel.getMobilenumber())) {
            logger.info("Checking source mobile number...");
            Boolean isValidMobileNumber = SystemConfig.isValidMobileNumber(wsmodel.getMobilenumber());

            if (!isValidMobileNumber) {
                logger.error(
                        "Invalid source mobile number [{}], rejecting...",
                        wsmodel.getMobilenumber()
                );
                wsmodel.setRespcode(ISOResponseCodes.ERROR_INVALIDMOBILENUMBER);
                return wsmodel;
            }
        }

        if (Util.hasText(wsmodel.getDestmobilenumber())) {
            logger.info("Checking destination mobile number...");

            Boolean isValidMobileNumber = SystemConfig.isValidMobileNumber(wsmodel.getDestmobilenumber());

            if (!isValidMobileNumber) {
                logger.error(
                        "Invalid destination mobile number [{}], rejecting...",
                        wsmodel.getDestmobilenumber()
                );
                wsmodel.setRespcode(ISOResponseCodes.ERROR_INVALIDMOBILENUMBER);
                return wsmodel;
            }
        }

        return MWWSOperation.ExecuteCreateOTPRequest(wsmodel);
    }


    public static AppWsEntity processConfirmOTP(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteConfirmOTPRequest(wsmodel);
    }

    public static AppWsEntity processForexGetRate(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processForexPurchase(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processDonation(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processCustomerInquiry(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processCustomerInquiryForEdit(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        String dbQuery;
        Map<String, Object> params;

        //new start
        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB";
        params = new HashMap<String, Object>();
        params.put("MOB", wsmodel.getMobilenumber());

        MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

        if(customer != null && (customer.getIsmigratedactive() != null && customer.getIsmigratedactive())
           && (customer.getHaswallet() == null || (customer.getHaswallet() != null  && !customer.getHaswallet())))
        {
            logger.error("BankClient Migrated Customer found, returning profile from apigateway...");
            wsmodel.setCustomerpicture(customer.getCustomerpicture());
            wsmodel.setCustomername(customer.getFirstname() + " " + customer.getLastname());
            wsmodel.setFirstname(customer.getFirstname());
            wsmodel.setMiddlename(customer.getMiddlename());
            wsmodel.setGender(customer.getGender());
            wsmodel.setLastname(customer.getLastname());
            wsmodel.setEmailaddress(customer.getEmailaddress());
            wsmodel.setCountry(customer.getCountry());
            wsmodel.setNationality(customer.getNationality());
            wsmodel.setDateofbirth(customer.getDateofbirth());
            wsmodel.setAddress(customer.getAddress());
            wsmodel.setIdentificationno(customer.getCnic());
            wsmodel.setOtpchannel(customer.getOtpchannel());

            wsmodel.setIdentificationtype(customer.getIdentificationtype());


            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        //Raza ignore else case. Walelt will determine that


        MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);

        logger.info("Updating CustomerId [" + wsmodel.getCustomerid() + "] for Mobile [" + customer.getMobilenumber() + "]");

        if(Util.hasText(wsmodel.getCustomerid()) && !Util.hasText(customer.getCustomerId()))
        {
            logger.info("Customer ID updated for Mobile [" + customer.getMobilenumber() + "]!");
            customer.setCustomerId(wsmodel.getCustomerid());
            GeneralDao.Instance.saveOrUpdate(customer);
        }
        wsmodel.setOtpchannel(customer.getOtpchannel());

        if(Util.hasText(wsmodel.getKycstatus()) && !wsmodel.getKycstatus().equals(customer.getKycstatus()))
        {
            logger.info("Different KYC status found for Mobile [" + customer.getMobilenumber() + "]!");
            customer.setKycstatus(wsmodel.getKycstatus());
            GeneralDao.Instance.saveOrUpdate(customer);
        }

        if(Util.hasText(wsmodel.getFirstname()) && !wsmodel.getFirstname().equals(customer.getFirstname()))
        {
            logger.info("Different First Name found for Mobile [" + customer.getMobilenumber() + "]!");
            customer.setFirstname(wsmodel.getFirstname());
//            GeneralDao.Instance.saveOrUpdate(customer);
        }
        if(Util.hasText(wsmodel.getLastname()) && !wsmodel.getLastname().equals(customer.getLastname()))
        {
            logger.info("Different Last Name found for Mobile [" + customer.getMobilenumber() + "]!");
            customer.setLastname(wsmodel.getLastname());
//            GeneralDao.Instance.saveOrUpdate(customer);
        }
        if(Util.hasText(wsmodel.getEmailaddress()) && !wsmodel.getEmailaddress().equals(customer.getEmailaddress()))
        {
            logger.info("Different Email Address found for Mobile [" + customer.getMobilenumber() + "]!");
            customer.setEmailaddress(wsmodel.getEmailaddress());
//            GeneralDao.Instance.saveOrUpdate(customer);
        }
        if(Util.hasText(wsmodel.getDateofbirth()) && !wsmodel.getDateofbirth().equals(customer.getDateofbirth()))
        {
            logger.info("Different Date of Birth found for Mobile [" + customer.getMobilenumber() + "]!");
            customer.setDateofbirth(wsmodel.getDateofbirth());
//            GeneralDao.Instance.saveOrUpdate(customer);
        }
        customer.setMobilenumber(wsmodel.getMobilenumber());
        GeneralDao.Instance.saveOrUpdate(customer);



        return  wsmodel;
    }

    public static AppWsEntity processGetIdPictures(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processMerchantInquiry(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetAppGraph(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetBankGraph(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processSendInvite(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteSendInvite(wsmodel);
    }

    public static AppWsEntity processGetNotifications(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteGetNotifications(wsmodel);
    }

    public static AppWsEntity processGetAcceptedInvites(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteGetAcceptedInvites(wsmodel);
    }

    public static AppWsEntity processGetCancelInvites(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetCancelInvites(wsmodel);
    }

    public static AppWsEntity processGetSessionState(AppWsEntity wsmodel)
    {
        /*try
        {
            logger.info("**********Going to Sleep after Insert and Flush**********");
            Thread.sleep(1200000);
            logger.info("**********Going to Sleep after Insert and Flush**********");
        }
        catch (Exception e)
        {
            logger.error("**********Exception caught while sleeping Thread**********");
            e.printStackTrace();
        }*/
        return MWWSOperation.ExecuteGetSessionState(wsmodel);
    }


    public static AppWsEntity processGetStateListV2(AppWsEntity wsmodel)
    {
        try {
            String dbQuery;
            Map<String, Object> params;

            if(Util.hasText(wsmodel.getCountry())){

                if(Util.isMultiLangEnabled()){
                    if(Util.isMultiLangStateEnabled()){

                        if(Util.hasText(wsmodel.getMobilenumber())) { //Raza enabling conditional session Validation 01-09-2025
                            logger.info("Validating Session...");
                            if (!ValidateUserandAppSession(wsmodel)) {
                                logger.error("Failed to Validate Session, ignoring...");
                            }
                        }

//                        dbQuery = "select distinct r from State r " +
//                                "left join fetch r.translations t where r.country = "+ GlobalContext.getInstance().getCountry(wsmodel.getCountry()).getCode();
//
//                        // Execute query with JOIN FETCH
//                        List<State> dbstates = GeneralDao.Instance.find(dbQuery);
//
//                        if (dbstates != null && !dbstates.isEmpty()) {
//                            List<StateListObj> statelist = new ArrayList<>();
//                            for (State r : dbstates) {
//                                // Default values from parent
//                                String name = null;
//                                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                                    name = r.getName();
//                                }
//                                else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
//                                    name = r.getFra_name();
//                                }
//                                else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                                    name = r.getName();
//                                }
//                                else{
//                                    name = r.getFra_name();
//                                }
//
//                                // Override if translation exists for requested language
//                                if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
//                                    Optional<StateTranslation> match = r.getTranslations()
//                                            .stream()
//                                            .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
//                                            .findFirst();
//                                    if (match.isPresent() && Util.hasText(match.get().getName())) {
//                                        name = match.get().getName();
//                                    }
//                                }
//
//                                StateListObj obj = new StateListObj();
//                                obj.setName(name);
//                                obj.setIsocode(name);
//                                statelist.add(obj);
//                            }
//
//                            wsmodel.setStatelist(statelist);
//
//                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                        } else {
//                            logger.error("No State List found in DB, falling back to old implementation...");
//                            processGetStateListV1(wsmodel);
//                        }
                    }
                    else{
                        logger.info("MultiLanguage State feature disabled, processing through old implementation...");
                        processGetStateListV1(wsmodel);
                    }
                }
                else{
                    logger.info("MultiLanguage feature disabled, processing through old implementation...");
                    processGetStateListV1(wsmodel);
                }

            }
            else{
                logger.error("No Country [" + wsmodel.getCountry() + "] found in request, returning OK with empty list...");
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        }
        catch (Exception e){
            logger.error("Exception caught while getting StateList from DB, falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            processGetStateListV1(wsmodel);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetStateListV1(AppWsEntity wsmodel)
    {
        logger.info("Getting State from Db...");
//        String query = "from "+ State.class.getName();
//        if(Util.hasText(wsmodel.getCountry()))
//        {
//            query += " where country = "+ GlobalContext.getInstance().getCountry(wsmodel.getCountry()).getCode();
//
//            List<State> list = GeneralDao.Instance.find(query);
//
//            if(list != null && list.size() > 0)
//            {
//                List<StateListObj> statelist = new ArrayList<>();
//                for(State state : list)
//                {
//                    StateListObj c = new StateListObj();
//                    if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
//                    {
//                        c.setName(state.getFra_name());
//                    }
//                    else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                        c.setName(state.getName());
//                    }
//                    else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                        c.setName(state.getName());
//                    }
//                    else
//                    {
//                        c.setName(state.getName());
//                    }
//                    c.setIsocode(state.getName());
//                    statelist.add(c);
//                }
//                wsmodel.setStatelist(statelist);
//            }
//            else
//            {
//                logger.error("No state found in DB, replying OK...");
//            }
//        }

        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetCityListV2(AppWsEntity wsmodel)
    {
        try {
            String dbQuery;
            Map<String, Object> params;

            if(Util.hasText(wsmodel.getProvince())){

                if(Util.isMultiLangEnabled()){
                    if(Util.isMultiLangCityEnabled()){

                        if(Util.hasText(wsmodel.getMobilenumber())) { //Raza enabling conditional session Validation 01-09-2025
                            logger.info("Validating Session...");
                            if (!ValidateUserandAppSession(wsmodel)) {
                                logger.error("Failed to Validate Session, ignoring...");
                            }
                        }

//                        dbQuery = "select distinct r from City r " +
//                                "left join fetch r.translations t where r.state = "+ GlobalContext.getInstance().getState(wsmodel.getProvince()).getCode();
//
//                        // Execute query with JOIN FETCH
//                        List<City> dbcities = GeneralDao.Instance.find(dbQuery);
//
//                        if (dbcities != null && !dbcities.isEmpty()) {
//                            List<CityListObj> citylist = new ArrayList<>();
//                            for (City r : dbcities) {
//                                // Default values from parent
//                                String name = null;
//                                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                                    name = r.getName();
//                                }
//                                else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
//                                    name = r.getFra_name();
//                                }
//                                else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                                    name = r.getName();
//                                }
//                                else{
//                                    name = r.getFra_name();
//                                }
//
//                                // Override if translation exists for requested language
//                                if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
//                                    Optional<CityTranslation> match = r.getTranslations()
//                                            .stream()
//                                            .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
//                                            .findFirst();
//                                    if (match.isPresent() && Util.hasText(match.get().getName())) {
//                                        name = match.get().getName();
//                                    }
//                                }
//
//                                CityListObj obj = new CityListObj();
//                                obj.setName(name);
//                                obj.setIsocode(name);
//                                citylist.add(obj);
//                            }
//
//                            wsmodel.setCitylist(citylist);
//
//                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                        } else {
//                            logger.error("No City List found in DB, falling back to old implementation...");
//                            processGetCityListV1(wsmodel);
//                        }
                    }
                    else{
                        logger.info("MultiLanguage City feature disabled, processing through old implementation...");
                        processGetCityListV1(wsmodel);
                    }
                }
                else{
                    logger.info("MultiLanguage feature disabled, processing through old implementation...");
                    processGetCityListV1(wsmodel);
                }

            }
            else{
                logger.error("No Province/State [" + wsmodel.getProvince() + "] found in request, returning OK with empty list...");
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        }
        catch (Exception e){
            logger.error("Exception caught while getting CityList from DB, falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            processGetCityListV1(wsmodel);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetCityListV1(AppWsEntity wsmodel)
    {
        logger.info("Getting City from Db...");
//        String query = "from "+ City.class.getName();
//
//        if(Util.hasText(wsmodel.getProvince()))
//        {
//            query += " where state = "+ GlobalContext.getInstance().getState(wsmodel.getProvince()).getCode();
//
//            List<City> list = GeneralDao.Instance.find(query);
//
//            if(list != null && list.size() > 0)
//            {
//                List<CityListObj> citylist = new ArrayList<>();
//                for(City city : list)
//                {
//                    //logger.info("Country [" + country.getName() + "]");
//                    CityListObj c = new CityListObj();
//                    if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
//                    {
//                        c.setName(city.getFra_name());
//                    }
//                    else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                        c.setName(city.getName());
//                    }
//                    else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                        c.setName(city.getName());
//                    }
//                    else
//                    {
//                        c.setName(city.getFra_name());
//                    }
//                    c.setIsocode(city.getName());
//                    citylist.add(c);
//                }
//                wsmodel.setCitylist(citylist);
//            }
//            else
//            {
//                logger.error("No city found in DB, replying OK...");
//            }
//        }


        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetMunicipalityListV2(AppWsEntity wsmodel)
    {
        try {
            String dbQuery;
            Map<String, Object> params;

            if(Util.hasText(wsmodel.getCity())){

                if(Util.isMultiLangEnabled()){
                    if(Util.isMultiLangMunicipalityEnabled()){

                        if(Util.hasText(wsmodel.getMobilenumber())) { //Raza enabling conditional session Validation 01-09-2025
                            logger.info("Validating Session...");
                            if (!ValidateUserandAppSession(wsmodel)) {
                                logger.error("Failed to Validate Session, ignoring...");
                            }
                        }

//                        dbQuery = "select distinct r from Municipality r " +
//                                "left join fetch r.translations t where r.city = "+ GlobalContext.getInstance().getCity(wsmodel.getCity()).getCode();
//
//                        // Execute query with JOIN FETCH
//                        List<Municipality> dbmunicipalicties = GeneralDao.Instance.find(dbQuery);
//
//                        if (dbmunicipalicties != null && !dbmunicipalicties.isEmpty()) {
//                            List<MunicipalityListObj> municipalitylist = new ArrayList<>();
//                            for (Municipality r : dbmunicipalicties) {
//                                // Default values from parent
//                                String name = null;
//                                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                                    name = r.getName();
//                                }
//                                else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
//                                    name = r.getFra_name();
//                                }
//                                else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                                    name = r.getName();
//                                }
//                                else{
//                                    name = r.getFra_name();
//                                }
//
//                                // Override if translation exists for requested language
//                                if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
//                                    Optional<MunicipalityTranslation> match = r.getTranslations()
//                                            .stream()
//                                            .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
//                                            .findFirst();
//                                    if (match.isPresent() && Util.hasText(match.get().getName())) {
//                                        name = match.get().getName();
//                                    }
//                                }
//
//                                MunicipalityListObj obj = new MunicipalityListObj();
//                                obj.setName(name);
//                                obj.setIsocode(name);
//                                municipalitylist.add(obj);
//                            }
//
//                            wsmodel.setMunicipalitylist(municipalitylist);
//
//                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                        } else {
//                            logger.error("No Municipality List found in DB, falling back to old implementation...");
//                            processGetMunicipalityListV1(wsmodel);
//                        }
                    }
                    else{
                        logger.info("MultiLanguage Municipality feature disabled, processing through old implementation...");
                        processGetMunicipalityListV1(wsmodel);
                    }
                }
                else{
                    logger.info("MultiLanguage feature disabled, processing through old implementation...");
                    processGetMunicipalityListV1(wsmodel);
                }

            }
            else{
                logger.error("No City [" + wsmodel.getCity() + "] found in request, returning OK with empty list...");
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            return wsmodel;
        }
        catch (Exception e){
            logger.error("Exception caught while getting MunicipalityList from DB, falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            processGetMunicipalityListV1(wsmodel);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetMunicipalityListV1(AppWsEntity wsmodel)
    {
        logger.info("Getting Municipality from Db...");
//        String query = "from "+ Municipality.class.getName();
//        if(Util.hasText(wsmodel.getCity()))
//        {
//            query += " where city = "+ GlobalContext.getInstance().getCity(wsmodel.getCity()).getCode();
//
//            List<Municipality> list = GeneralDao.Instance.find(query);
//
//            if(list != null && list.size() > 0)
//            {
//                List<MunicipalityListObj> municipalitylist = new ArrayList<>();
//                for(Municipality municipality : list)
//                {
//                    //logger.info("Country [" + country.getName() + "]");
//
//                    MunicipalityListObj c = new MunicipalityListObj();
//                    if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
//                    {
//                        c.setName(municipality.getFra_name());
//                    }
//                    else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                        c.setName(municipality.getName());
//                    }
//                    else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                        c.setName(municipality.getName());
//                    }
//                    else
//                    {
//                        c.setName(municipality.getFra_name());
//                    }
//                    c.setIsocode(municipality.getName());
//                    municipalitylist.add(c);
//                }
//                wsmodel.setMunicipalitylist(municipalitylist);
//            }
//            else
//            {
//                logger.error("No municipality found in DB, replying OK...");
//            }
//        }


        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }



    public static AppWsEntity processGetMonthlyIncomeList(AppWsEntity wsmodel)
    {
        logger.info("Getting MonthlyIncomeList from Db...");
        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }


    public static AppWsEntity processGetMonthlyExpenditureList(AppWsEntity wsmodel)
    {
        logger.info("Getting MonthlyExpenditureList from Db...");
        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetOccupationList(AppWsEntity wsmodel)
    {
        logger.info("Getting OccupationList from Db...");
        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetCountryListV2(AppWsEntity wsmodel)
    {
        try {
            String dbQuery;
            Map<String, Object> params;

            if(Util.isMultiLangEnabled()){
                if(Util.isMultiLangCountryEnabled()){

                    if(Util.hasText(wsmodel.getMobilenumber())) { //Raza enabling conditional session Validation 01-09-2025
                        logger.info("Validating Session...");
                        if (!ValidateUserandAppSession(wsmodel)) {
                            logger.error("Failed to Validate Session, ignoring...");
                        }
                    }

                    dbQuery = "select distinct r from Country r " +
                            "left join fetch r.translations t ";

//                    // Execute query with JOIN FETCH
//                    List<Country> dbcountries = GeneralDao.Instance.find(dbQuery);
//
//                    if (dbcountries != null && !dbcountries.isEmpty()) {
//                        List<CountryListObj> countrylist = new ArrayList<>();
//                        for (Country r : dbcountries) {
//                            // Default values from parent
//                            String name = null;
//                            if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                                name = r.getName();
//                            }
//                            else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
//                                name = r.getFra_name();
//                            }
//                            else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                                name = r.getName();
//                            }
//                            else{
//                                name = r.getFra_name();
//                            }
//
//
//                            // Override if translation exists for requested language
//                            if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
//                                Optional<CountryTranslation> match = r.getTranslations()
//                                        .stream()
//                                        .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : "FRA" ))
//                                        .findFirst();
//                                if (match.isPresent() && Util.hasText(match.get().getName())) {
//                                    name = match.get().getName();
//                                }
//                            }
//
//                            CountryListObj obj = new CountryListObj();
//                            obj.setName(name);
//                            obj.setIsocode(r.getAlpha3());
//                            countrylist.add(obj);
//                        }
//
//                        wsmodel.setCountrylist(countrylist);
//
//                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                    } else {
//                        logger.error("No Country List found in DB, falling back to old implementation...");
//                        processGetCountryListV1(wsmodel);
//                    }
                }
                else{
                    logger.info("MultiLanguage Country feature disabled, processing through old implementation...");
                    processGetCountryListV1(wsmodel);
                }
            }
            else{
                logger.info("MultiLanguage feature disabled, processing through old implementation...");
                processGetCountryListV1(wsmodel);
            }
        }
        catch (Exception e){
            logger.error("Exception caught while getting CountryList from DB, falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            processGetCountryListV1(wsmodel);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetCountryListV1(AppWsEntity wsmodel)
    {
        logger.info("Getting Countries from Db...");
//        String query = "from "+ Country.class.getName();
//        List<Country> list = GeneralDao.Instance.find(query);
//
//        if(list != null && list.size() > 0)
//        {
//            List<CountryListObj> countrylist = new ArrayList<>();
//            for(Country country : list)
//            {
//                //logger.info("Country [" + country.getName() + "]");
//                if(Util.hasText(wsmodel.getPartialflag()) && wsmodel.getPartialflag().equals("false"))
//                {
//                    CountryListObj c = new CountryListObj();
//                    if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
//                    {
//                        c.setName(country.getFra_name());
//                    }
//                    else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                        c.setName(country.getName());
//                    }
//                    else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                        c.setName(country.getName());
//                    }
//                    else
//                    {
//                        c.setName(country.getName());
//                    }
//                    c.setIsocode(country.getAlpha3());
//                    countrylist.add(c);
//                }
//                else
//                {
//                    ExecuteMWServiceRequest(wsmodel, false);
//                    return wsmodel;
//                    /*if(country.getImt_enabled() != null && country.getImt_enabled())
//                    {
//                        //logger.info("Country [" + country.getName() + "] IMT Enabled.");
//                        CountryListObj c = new CountryListObj();
//                        if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
//                        {
//                            c.setName(country.getFra_name());
//                        }
//                        else
//                        {
//                            c.setName(country.getName());
//                        }
//                        c.setIsocode(country.getAlpha3());
//                        countrylist.add(c);
//                    }*/
//                }
//            }
//            wsmodel.setCountrylist(countrylist);
//        }
//        else
//        {
//            logger.error("No country found in DB, replying OK...");
//        }
        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetCallCodeListbyCountry(AppWsEntity wsmodel)
    {
        logger.info("Getting CallCodes by Country Code from Db...");
//        String query = "from "+ Country.class.getName() + " c where c.alpha3 = :CNTRY ";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("CNTRY", wsmodel.getCountry());

//        Country country = (Country)GeneralDao.Instance.findObject(query, params);
//
//        if(country != null && Util.hasText(country.getCallcodes()))
//        {
//            List<CallCodeListObj> calllist = new ArrayList<>();
//
//            String[] dbcallcodelist = country.getCallcodes().split("\\s*,\\s*");
//            for(String s : dbcallcodelist)
//            {
//                CallCodeListObj c = new CallCodeListObj();
//                c.setCode(s);
//                calllist.add(c);
//            }
//            wsmodel.setCallcodelist(calllist);
//        }
//        else
//        {
//            logger.error("No CallCode found in DB against country [" + wsmodel.getCountry() + "], replying OK...");
//        }
        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetCurrencyList(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        logger.info("Getting Currencies from Db...");

        String query = "from " + Currency.class.getName() + " c ";
        Map<String, Object> param = new HashMap<String, Object>();
        List<Currency> list = GeneralDao.Instance.find(query, param);

        if(list != null && list.size() > 0)
        {
            List<CurrencyListObj> currencylist = new ArrayList<>();
            for(Currency curr : list)
            {
                CurrencyListObj c = new CurrencyListObj();
                c.setName(curr.getName());
                c.setIsocode(curr.getCode()+"");
                currencylist.add(c);
            }
            wsmodel.setCurrencylist(currencylist);
        }
        else
        {
            logger.error("No currency found in DB, replying OK...");
        }
        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        return wsmodel;
    }

    public static AppWsEntity processGetRemitCountries(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetRemitPayerTypes(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetRemitPayers(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
//        if(!Util.hasText(wsmodel.getDestcountry())){
//            logger.error("Dest Country [" + wsmodel.getDestcountry() + "] not found in request, rejecting...");
//            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//            return wsmodel;
//        }
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetRemitPayerForm(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        if(!Util.hasText(wsmodel.getDestcountry()) || !Util.hasText(wsmodel.getPayertype()) || !Util.hasText(wsmodel.getBillerid())){
            logger.error("Dest Country [" + wsmodel.getDestcountry() + "] or PayerType [" + wsmodel.getPayertype() + "] or BillerID [" + wsmodel.getBillerid() + "] not found in request, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processSetBeneficiary(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        if(Util.hasText(wsmodel.getApiversion()) && (wsmodel.getApiversion().equals(APIVersion.VERSION2) || wsmodel.getApiversion().equals(APIVersion.VERSION3))){
            return MWWSOperation.ExecuteDynSetBeneficiary(wsmodel);
        }
        else{
            return MWWSOperation.ExecuteSetBeneficiary(wsmodel);
        }
    }

    public static AppWsEntity processGetBeneficiaries(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        if(Util.hasText(wsmodel.getApiversion()) && (wsmodel.getApiversion().equals(APIVersion.VERSION2) || wsmodel.getApiversion().equals(APIVersion.VERSION3))){
            return MWWSOperation.ExecuteDynGetBeneficiaries(wsmodel);
        }
        else{
            return MWWSOperation.ExecuteGetBeneficiaries(wsmodel);
        }
    }

    public static AppWsEntity processDeleteBeneficiary(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        if(Util.hasText(wsmodel.getApiversion()) && (wsmodel.getApiversion().equals(APIVersion.VERSION2) || wsmodel.getApiversion().equals(APIVersion.VERSION3))){
            return MWWSOperation.ExecuteDynDeleteBeneficiary(wsmodel);
        }
        else{
            return MWWSOperation.ExecuteDeleteBeneficiary(wsmodel);
        }
    }

    public static AppWsEntity processSetLanguage(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteSetLanguage(wsmodel);
    }

    public static AppWsEntity processSetNotifLanguage(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteSetNotifLanguage(wsmodel);
    }

    public static AppWsEntity processGetBillPackageDetails(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processUpdateTermandCond(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteUpdateTermandCond(wsmodel);
    }

    public static AppWsEntity processGetErrorDescription(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        Util.setRespCodeDescription(wsmodel);
        return wsmodel;
    }

    public static AppWsEntity processGetAccountBalance(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    //// Muhammad Hamza -- MOBILE BANKING Feature -- 30-MAY-2024  -- START

    public static AppWsEntity processGetAccountOppositions(AppWsEntity wsmodel)
    {
        if(!(Util.hasText(wsmodel.getClientid())) && !(Util.hasText(wsmodel.getAccountnumber()))
                && !(Util.hasText(wsmodel.getBranchcode())) && !(Util.hasText(wsmodel.getAccountcurrency()))){
            logger.info("Client ID["+ wsmodel.getClientid() +"], AccountNumber["+ wsmodel.getAccountnumber() +"], AccountCurrency["+ wsmodel.getAccountcurrency() +"]" +
                    "and BranchCode["+ wsmodel.getBranchcode() +"]"+"Someone Field is missing, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processGetAccountRIB(AppWsEntity wsmodel)
    {
        if(!(Util.hasText(wsmodel.getClientid())) && !(Util.hasText(wsmodel.getAccountnumber()))
                && !(Util.hasText(wsmodel.getBranchcode())) && !(Util.hasText(wsmodel.getAccountcurrency()))){
            logger.info("Client ID["+ wsmodel.getClientid() +"], AccountNumber["+ wsmodel.getAccountnumber() +"], AccountCurrency["+ wsmodel.getAccountcurrency() +"]" +
                    "and BranchCode["+ wsmodel.getBranchcode() +"]"+"Someone Field is missing, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity processContactRM(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        //find is it email verified or not if yes then goto SOPRA if not then please firstly verifired your email
        // and reject his request...
        if(!wsmodel.getIsemailverified()){
            logger.error("Faild to Execute for Not verified Emails, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.EMAIL_NOT_VERIFIED);
            return wsmodel;
        }

        if(!(Util.hasText(wsmodel.getMobilenumber())) && wsmodel.getMobilenumber() == null){
            logger.info("Mobile Number["+ wsmodel.getMobilenumber() +"] is missing, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        logger.info("Getting RM Email Address and Verifying Mobile Number....");
        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
        params = new HashMap<String, Object>();
        params.put("MOBNO", wsmodel.getMobilenumber());

        MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

        if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE))
        {
           wsmodel.setFirstname(customer.getFirstname());
           wsmodel.setLastname(customer.getLastname());
           wsmodel.setEmailaddress(customer.getEmailaddress());
        }
        else{
            if(customer != null){
                logger.error("Inactive user found against mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
            }
            else{
                logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
            }
        }

        if(wsmodel.getIscmsaccoutlinked())
        {
            //For Bank Account Link Users
            MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);

            if(wsmodel.getRespcode().equals(ISOResponseCodes.MW_SESSION_EXPIRED)){
                logger.info("Session Expire["+ wsmodel.getRespcode() +"] PLease try to login again, rejecting...");
                return wsmodel;
            }

            String rmEmail = wsmodel.getDestemailaddress();
            wsmodel.setRmemailsource(ContactRMSubjectObj.CUSTOMER_PROFILE);
            wsmodel.setRmemail(rmEmail);

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Go to fetch emaillist from branch table
                //GetDestEmailFromBranchtable
                //then set into rmEmail.
                rmEmail = wsmodel.getDestemailaddress();
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.BRANCH_TABLE);
            }

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Set to Default Email into rmEmail
                rmEmail = ContactRMSubjectObj.SUPPORT_EMAIL;
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.GENERICS);
            }

        }
        else{
            //For Wallet Users Only
            wsmodel.setDestemailaddress(customer.getRmemail());

            String rmEmail = wsmodel.getDestemailaddress();
            wsmodel.setRmemail(rmEmail);
            wsmodel.setRmemailsource(ContactRMSubjectObj.CUSTOMER_PROFILE);

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Go to fetch emaillist from branch table
                //GetDestEmailFromBranchtable
                //then set into rmEmail.
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.BRANCH_TABLE);
            }

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Set to Default Email into rmEmail
                rmEmail = ContactRMSubjectObj.SUPPORT_EMAIL;
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.GENERICS);
            }

        }

        if(!(Util.hasText(wsmodel.getRmemail())) && wsmodel.getRmemail() == null){
            logger.info("RM Email["+ wsmodel.getRmemail() +"] is missing, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        return MWWSOperation.ExecuteContactRM(wsmodel);
    }

    public static AppWsEntity processGetRMEmails(AppWsEntity wsmodel)
    {
        //find is it email verified or not if yes then goto SOPRA if not then please firstly verifired your email
        // and reject his request...
        if(!wsmodel.getIsemailverified()){
            logger.error("Faild to Execute for Not verified Emails, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.EMAIL_NOT_VERIFIED);
            return wsmodel;
        }

        if(wsmodel.getIscmsaccoutlinked())
        {
            //For Bank Account Link Users
            MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
            String rmEmail = wsmodel.getDestemailaddress();
            wsmodel.setRmemailsource(ContactRMSubjectObj.CUSTOMER_PROFILE);
            wsmodel.setRmemail(rmEmail);

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Go to fetch emaillist from branch table
                //GetDestEmailFromBranchtable
                //then set into rmEmail.
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.BRANCH_TABLE);
            }

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Set to Default Email into rmEmail
                rmEmail = ContactRMSubjectObj.SUPPORT_EMAIL;
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.GENERICS);
            }

            wsmodel.setDestemailaddress(rmEmail);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

        }
        else{
            //For Wallet Users Only
//            wsmodel.setDestemailaddress(GlobalContext.getInstance().getSystemEmailAddress());
            MWWSOperation.ExecuteContactRM(wsmodel);
            String rmEmail = wsmodel.getDestemailaddress();
            wsmodel.setRmemailsource(ContactRMSubjectObj.CUSTOMER_PROFILE);
            wsmodel.setRmemail(rmEmail);

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Go to fetch emaillist from branch table
                //GetDestEmailFromBranchtable
                //then set into rmEmail.
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.BRANCH_TABLE);
            }

            if(!Util.hasText(rmEmail) && rmEmail == null){
                //Set to Default Email into rmEmail
                rmEmail = ContactRMSubjectObj.SUPPORT_EMAIL;
                wsmodel.setRmemail(rmEmail);
                wsmodel.setRmemailsource(ContactRMSubjectObj.GENERICS);
            }

            wsmodel.setDestemailaddress(rmEmail);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }

        return wsmodel;
    }

    public static AppWsEntity processCreateEmailOTP(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteCreateEmailOTPRequest(wsmodel);
    }

    public static AppWsEntity processSetOTPChannel(AppWsEntity wsmodel)
    {
        try{
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if(customer != null && customer.getStatus().equals(CustomerStatus.ACTIVE))
            {
                MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                    logger.info("Approved response received from OpenAPI for Setting OTP Channel, updateing customer OTP Channel on MW...");
                    customer.setOtpchannel(wsmodel.getOtpchannel());
                    GeneralDao.Instance.saveOrUpdate(customer);
                }
            }
            else{
                if(customer != null){
                    logger.error("Inactive user found against mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
                }
                else{
                    logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                }
            }
        }
        catch (Exception e){
            logger.error("Exception caught while executing [" + wsmodel.getServicename() + "], rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
        }
        return wsmodel;
    }

    public static AppWsEntity processConfirmEmailOTP(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteConfirmEmailOTPRequest(wsmodel);
    }

    public static AppWsEntity processGetSubjectList(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        List<SubjectList> dbsubjects = null;
        List<ContactRMSubjectObj> subjectlist = null;
        params = new HashMap<String, Object>();


        dbQuery = "from "+ SubjectList.class.getName() + " c where c.reasonenabled= :ENABLED";
        params.put("ENABLED", true);

        dbsubjects = GeneralDao.Instance.find(dbQuery,params);


        if(dbsubjects != null && dbsubjects.size() > 0)
        {
            subjectlist = new ArrayList<>();
            for(SubjectList r : dbsubjects)
            {
                ContactRMSubjectObj obj = new ContactRMSubjectObj();
                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
                {
                    obj.setSubject(r.getSubject());
                    obj.setDescription(r.getFradescription());
                }
                else
                {
                    obj.setSubject(r.getSubject());
                    obj.setDescription(r.getDescription());
                }
                subjectlist.add(obj);
            }

            wsmodel.setSubjectlist(subjectlist);
        }

        if(wsmodel.getSubjectlist() != null && wsmodel.getSubjectlist().size() > 0)
        {
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        else
        {
            logger.error("No Subject List found in DB, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
        }

        return wsmodel;
    }

    public static AppWsEntity processTransactionReasons(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        List<MWTransactionReasons> dbtransactionreason = null;
        List<TransactionReasons> transactionreasonlist = null;
        params = new HashMap<String, Object>();

        if(Util.hasText(wsmodel.getTransactiontype()) && wsmodel.getTransactiontype() != null)
        {
            dbQuery = "from " + MWTransactionReasons.class.getName() + " c where c.reasonenabled= :ENABLED and c.transactiontype= :TRANTYPE";
            params.put("ENABLED", true);
            params.put("TRANTYPE", wsmodel.getTransactiontype());

            dbtransactionreason = GeneralDao.Instance.find(dbQuery, params);
        }
        else{
            dbQuery = "from " + MWTransactionReasons.class.getName() + " c where c.reasonenabled= :ENABLED";
            params.put("ENABLED", true);

            dbtransactionreason = GeneralDao.Instance.find(dbQuery, params);
        }

        if(dbtransactionreason != null && dbtransactionreason.size() > 0)
        {
            transactionreasonlist = new ArrayList<>();
            for(MWTransactionReasons r : dbtransactionreason)
            {
                TransactionReasons obj = new TransactionReasons();
                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
                {
                    obj.setReason(r.getReasonname());
                    obj.setReasoncode(r.getReasoncode());
//                    obj.setTransactiontype(r.getTransactiontype());
                    obj.setFradescription(r.getFradescription());
                }
                else
                {
                    obj.setReason(r.getReasonname());
//                    obj.setTransactiontype(r.getTransactiontype());
                    obj.setDescription(r.getDescription());
                    obj.setReasoncode(r.getReasoncode());
                }
                transactionreasonlist.add(obj);
            }

            wsmodel.setTransactionReasons(transactionreasonlist);
        }

        if(wsmodel.getTransactionReasons() != null && wsmodel.getTransactionReasons().size() > 0)
        {
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        else
        {
            logger.error("No Transaction Reasons List found in DB, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
        }

        return wsmodel;
    }

    public static AppWsEntity processCreateBeneficiary(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteCreateBeneficiary(wsmodel);
    }

    public static AppWsEntity processGetBeneficiariesList(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetBeneficiariesList(wsmodel);
    }

    public static AppWsEntity processDeleteBeneficiaries(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteDeleteBeneficiaries(wsmodel);
    }
    public static AppWsEntity processUpdateBeneficiaries(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteUpdateBeneficiaries(wsmodel);
    }
    public static AppWsEntity processBeneficiariesDetails(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteBeneficiariesDetails(wsmodel);
    }

    public static AppWsEntity processBankForexPurchase(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }
    public static AppWsEntity processGetTransactionTypes(AppWsEntity wsmodel)
    {

        String dbQuery;
        Map<String, Object> params;
        List<TransactionTypes> dbtransactions = null;
        List<SupportedTransactionTypes> transactionlist = null;
        params = new HashMap<String, Object>();


        dbQuery = "from "+ TransactionTypes.class.getName() + " c where c.enabled= :ENABLED";
        params.put("ENABLED", true);

        dbtransactions = GeneralDao.Instance.find(dbQuery,params);


        if(dbtransactions != null && dbtransactions.size() > 0)
        {
            transactionlist = new ArrayList<>();
            for(TransactionTypes r : dbtransactions)
            {
                SupportedTransactionTypes obj = new SupportedTransactionTypes();
                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
                {
                    obj.setName(r.getFratransactiontype());
                }
                else
                {
                    obj.setName(r.getTransactiontype());
                }
                transactionlist.add(obj);
            }

            wsmodel.setTransactiontypeslist(transactionlist);
        }

        if(wsmodel.getTransactiontypeslist() != null && wsmodel.getTransactiontypeslist().size() > 0)
        {
//            logger.info("Supported Transaction Types:["+ wsmodel.getTransactiontypeslist().get(0).getName() +"]");
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        else
        {
            logger.error("No Transaction Type List found in DB, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
        }

        return wsmodel;

    }

    public static AppWsEntity processGetBankCodes(AppWsEntity wsmodel)
    {

        String dbQuery;
        Map<String, Object> params;
        List<BankDetailsList> bankdetailslist = null;
        List<MWBanksDetails> dbbankdetail = null;
        params = new HashMap<String, Object>();


        dbQuery = "from "+ MWBanksDetails.class.getName() + " c where c.enabled= :ENABLED";
        params.put("ENABLED", true);
        logger.info("Query: "+dbQuery+" Params: "+params);
        dbbankdetail = GeneralDao.Instance.find(dbQuery, params);


        if(dbbankdetail != null && dbbankdetail.size() > 0)
        {
            bankdetailslist = new ArrayList<>();
            for(MWBanksDetails r : dbbankdetail)
            {
                BankDetailsList obj = new BankDetailsList();
                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
                {
                    obj.setId(r.getId());
//                    obj.setName(r.getName());
                    obj.setFraname(r.getFraname());
                    obj.setBankcode(r.getBankcode());
                    obj.setBranchcode(r.getBranchcode());
                    obj.setSwiftcode(r.getSwiftcode());
                    obj.setCity(r.getCity());
                    obj.setState(r.getState());
                    obj.setCountry(r.getCountry());
                    obj.setDescription(r.getDescription());
                    obj.setCurrency(r.getCurrency());
                    obj.setEnabled(r.getEnabled());
                    obj.setFetchaccountsupported(r.getFetchaccountsupported());
                }
                else
                {
                    obj.setId(r.getId());
                    obj.setName(r.getName());
//                    obj.setFraname(r.getFraname());
                    obj.setBankcode(r.getBankcode());
                    obj.setBranchcode(r.getBranchcode());
                    obj.setSwiftcode(r.getSwiftcode());
                    obj.setCity(r.getCity());
                    obj.setState(r.getState());
                    obj.setCountry(r.getCountry());
                    obj.setDescription(r.getDescription());
                    obj.setCurrency(r.getCurrency());
                    obj.setEnabled(r.getEnabled());
                    obj.setFetchaccountsupported(r.getFetchaccountsupported());
                }

                bankdetailslist.add(obj);
            }

            wsmodel.setBankdetailslist(bankdetailslist);
        }

        if(wsmodel.getBankdetailslist() != null && wsmodel.getBankdetailslist().size() > 0)
        {
//            logger.info("Supported Transaction Types:["+ wsmodel.getTransactiontypeslist().get(0).getName() +"]");
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        else
        {
            logger.error("No Bank Details List found in DB, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
        }

        return wsmodel;

    }


    //// Muhammad Hamza -- MOBILE BANKING Feature -- 30-MAY-2024  -- END

    //// Muhammad Hamza -- E-Ticketing -- 10-SEP-2024  -- Start
    public static AppWsEntity processGetServices(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteGetServices(wsmodel);
    }
    public static AppWsEntity processGetServicesTags(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteGetServicesTags(wsmodel);
    }
    public static AppWsEntity processGetServiceDetails(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteGetServiceDetails(wsmodel);
    }
    public static AppWsEntity processGetEventInvoice(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteGetEventInvoice(wsmodel);
    }
    public static AppWsEntity processConfirmInvoicePayment(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteConfirmInvoicePayment(wsmodel);
    }
    public static AppWsEntity processGetInvoiceDetail(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteGetInvoiceDetail(wsmodel);
    }
    public static AppWsEntity processGetTicketList(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//        return MWWSOperation.ExecuteGetInvoiceDetail(wsmodel);
    }
    //// Muhammad Hamza -- E-Ticketing -- 10-SEP-2024  -- END


    //// Muhammad UMER -- TAP & PAY -- 23-SEP-2024  -- END
    public static AppWsEntity processGetCardId(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteGetCardId(wsmodel);
    }

    //// Muhammad UMER -- TAP & PAY -- 23-SEP-2024  -- END


    //Muhammad Hamza -- MNO - WalletToWallet --  Start

    public static AppWsEntity processGetMNOs(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }
    public static AppWsEntity processTitleFetchOut(AppWsEntity wsmodel)
    {
        try {
            if((!Util.hasText(wsmodel.getDestmobilenumber())) && (!Util.hasText(wsmodel.getBillerid()))){
                logger.info("Mandatory field DestMobileNumber["+wsmodel.getDestmobilenumber()+"] and BillerID["+wsmodel.getBillerid()+"] is missing, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;
            //new start
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer != null) {
                return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
            } else {
                logger.info("Customer not found in DB, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        }catch(Exception e){
            logger.info("Exception occur while getting customer detail...");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

    }

    public static AppWsEntity processMNOTransferOut(AppWsEntity wsmodel)
    {
        if((!Util.hasText(wsmodel.getDestmobilenumber())) && (!Util.hasText(wsmodel.getBillerid()))){
            logger.info("Mandatory field DestMobileNumber["+wsmodel.getDestmobilenumber()+"] and BillerID["+wsmodel.getBillerid()+"] is missing, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    //Muhammad Hamza -- MNO - WalletToWallet --  End

    public static AppWsEntity processD1SDKAccessToken(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteD1SDKAccessTokenRequest(wsmodel);
    }


    public static AppWsEntity processGetIMTReasonsV2(AppWsEntity wsmodel)
    {
        try{
            String dbQuery;
            Map<String, Object> params = new HashMap<String, Object>();
//            List<IMTReasons> dbReasons = null;
            List<IMTReasonsObj> reasonList = null;

            if(Util.isMultiLangEnabled()){
                if(Util.isMultiLangIMTReasonsEnabled()){
                    if(Util.hasText(wsmodel.getMobilenumber())) { //Raza enabling conditional session Validation 01-09-2025
                        logger.info("Validating Session...");
                        if (!ValidateUserandAppSession(wsmodel)) {
                            logger.error("Failed to Validate Session, rejecting...");
                            return wsmodel;
                        }
                    }

                    // Base query: fetch reasons + translations in a single query
                    if (Util.hasText(wsmodel.getBillerid())) {
                        dbQuery = "select distinct r from IMTReasons r " +
                                "left join fetch r.translations t " +
                                "where r.billerid = :BILLER and r.reasonenabled = :ENABLED";
                        params.put("BILLER", wsmodel.getBillerid());
                        params.put("ENABLED", true);
                    } else {
                        dbQuery = "select distinct r from IMTReasons r " +
                                "left join fetch r.translations t " +
                                "where r.reasonenabled = :ENABLED";
                        params.put("ENABLED", true);
                    }

//                    // Execute query with JOIN FETCH
//                    dbReasons = GeneralDao.Instance.find(dbQuery, params);
//
//                    if (dbReasons != null && !dbReasons.isEmpty()) {
//                        reasonList = new ArrayList<>();
////                        for (IMTReasons r : dbReasons) {
////                            IMTReasonsObj obj = new IMTReasonsObj();
////                            obj.setReason(r.getReason());
////
////                            // Default description from parent
////                            String description = null;
////                            if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
////                                description = r.getDescription();
////                            }
////                            else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA")){
////                                description = r.getFradescription();
////                            }
////                            else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
////                                description = r.getDescription();
////                            }
////                            else{
////                                description = r.getFradescription();
////                            }
////                            logger.info("Selected Desc [" + description + "] LangCode [" + wsmodel.getLanguage() + "] NotifLang [" + wsmodel.getNotiflanguage() + "]");
////
////                            // Override if translation exists for requested language
////                            if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
////                                Optional<IMTReasonsTranslation> match = r.getTranslations()
////                                        .stream()
////                                        .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
////                                        .findFirst();
////                                if (match.isPresent() && Util.hasText(match.get().getDescription())) {
////                                    description = match.get().getDescription();
////                                    logger.info("Updated Desc [" + description + "] LangCode [" + wsmodel.getLanguage() + "] NotifLang [" + wsmodel.getNotiflanguage() + "]");
////                                }
////                            }
////
////                            obj.setDescription(description);
////                            reasonList.add(obj);
////                        }
//
//                        wsmodel.setImtreasonlist(reasonList);
//
//                        if(wsmodel.getImtreasonlist() != null && wsmodel.getImtreasonlist().size() > 0)
//                        {
//                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                        }
//                        else
//                        {
//                            logger.error("No IMT Reason List found in DB, rejecting...");
//                            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//                        }
//
//                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                    } else {
//                        logger.error("No IMT Reason List found in DB, falling back to old implementation...");
//                        processGetIMTReasonsV1(wsmodel);
//                    }

                    return wsmodel;
                }
                else{
                    logger.info("MultiLanguage for IMTReasons feature disabled, processing through old implementation...");
                    processGetIMTReasonsV1(wsmodel);
                }
            }
            else{
                logger.info("MultiLanguage feature disabled, processing through old implementation...");
                processGetIMTReasonsV1(wsmodel);
            }
        }
        catch (Exception e){
            logger.error("Exception caught while prcessing [" + wsmodel.getServicename() + "], falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            processGetIMTReasonsV1(wsmodel);
        }
        return wsmodel;
    }

    public static AppWsEntity processGetIMTReasonsV1(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        String dbQuery;
        Map<String, Object> params;
//        List<IMTReasons> dbreasons = null;
        List<IMTReasonsObj> reasonlist = null;
        params = new HashMap<String, Object>();

//        if(Util.hasText(wsmodel.getBillerid()))
//        {
//            dbQuery = "from " + IMTReasons.class.getName() + " c where c.billerid= :BILLER and c.reasonenabled= :ENABLED";
//            params.put("BILLER", wsmodel.getBillerid());
//            params.put("ENABLED", true);
//            dbreasons = GeneralDao.Instance.find(dbQuery, params);
//        }
//        else
//        {
//            dbQuery = "from "+ IMTReasons.class.getName() + " c where c.reasonenabled= :ENABLED";
//            params.put("ENABLED", true);
//
//            dbreasons = GeneralDao.Instance.find(dbQuery,params);
//        }
//
//        if(dbreasons != null && dbreasons.size() > 0)
//        {
//            reasonlist = new ArrayList<>();
//            for(IMTReasons r : dbreasons)
//            {
//                IMTReasonsObj obj = new IMTReasonsObj();
//                if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("FRA"))
//                {
//                    obj.setReason(r.getReason());
//                    obj.setDescription(r.getFradescription());
//                }
//                else if(Util.hasText(wsmodel.getLanguage()) && wsmodel.getLanguage().equals("ENG")){
//                    obj.setReason(r.getReason());
//                    obj.setDescription(r.getDescription());
//                }
//                else if(Util.getDefaultMobileAppLanguage().equals("ENG")){
//                    obj.setReason(r.getReason());
//                    obj.setDescription(r.getDescription());
//                }
//                else
//                {
//                    obj.setReason(r.getReason());
//                    obj.setDescription(r.getFradescription());
//                }
//                reasonlist.add(obj);
//            }
//            wsmodel.setImtreasonlist(reasonlist);
//        }

        if(wsmodel.getImtreasonlist() != null && wsmodel.getImtreasonlist().size() > 0)
        {
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        else
        {
            logger.error("No IMT Reason List found in DB, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
        }

        return wsmodel;
    }


    public static boolean UpdateSecurityQuestions(AppWsEntity wsmodel, MWCustomer customer)
    {
        try {
            logger.info("Updating Security Questions...");


            if(!Util.hasText(wsmodel.getSecretquestion1()) || !Util.hasText(wsmodel.getSecretquestion2())
              || !Util.hasText(wsmodel.getSecretquestionanswer1()) || !Util.hasText(wsmodel.getSecretquestionanswer2()))
            {
                logger.error("Incomplete Secret Quest and Answer Data, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return false;
            }

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
            params = new HashMap<String, Object>();
            params.put("QUEST", wsmodel.getSecretquestion1());

            MWSecurQuestions dbquestion = (MWSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);

            if(dbquestion == null)
            {
                dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.frquestion= :QUEST  ";

                dbquestion = (MWSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbquestion == null) {

                    dbQuery = "from " + MWSecurQuestionsTranslation.class.getName() + " c where c.question= :QUEST  ";

                    MWSecurQuestionsTranslation xlatedbquestion = (MWSecurQuestionsTranslation)GeneralDao.Instance.findObject(dbQuery, params);


                    if(xlatedbquestion != null){
                        dbquestion = xlatedbquestion.getSecurquestion();
                    }
                    else{
                        logger.error("Question1 not found in DB to Update, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                        return false;
                    }
                }
            }

            //MWSecurQuestions dbquestion2 = null;
//            if(Util.hasText(wsmodel.getSecretquestion2()))
//            {
                dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
                params = new HashMap<String, Object>();
                params.put("QUEST", wsmodel.getSecretquestion2());

                MWSecurQuestions dbquestion2 = (MWSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbquestion2 == null)
                {
                    dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.frquestion= :QUEST  ";
                    dbquestion2 = (MWSecurQuestions)GeneralDao.Instance.findObject(dbQuery, params);

                    if(dbquestion2 == null) {

                        dbQuery = "from " + MWSecurQuestionsTranslation.class.getName() + " c where c.question= :QUEST  ";

                        MWSecurQuestionsTranslation xlatedbquestion = (MWSecurQuestionsTranslation)GeneralDao.Instance.findObject(dbQuery, params);

                        if(xlatedbquestion != null){
                            dbquestion = xlatedbquestion.getSecurquestion();
                        }
                        else{
                            logger.error("Question2 not found in DB to Update, rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                            return false;
                        }
                    }
                }
//            }


            dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUST  ";
            params = new HashMap<String, Object>();
            params.put("CUST", customer);

            List<MWCustSecurQuestions> customerquestionlist = GeneralDao.Instance.find(dbQuery,params);

//            if(customerquestionlist != null && customerquestionlist.size() > 0)
//            {
                if(customerquestionlist.size() == 1) //Raza adding for migrated customers with 1 secret question and answer
                {
                        logger.info("Customer found with One Secret Question only, processing and updating...");
                        MWCustSecurQuestions custquest = customerquestionlist.get(0);
                        if(custquest.getQuestionnumber() == 1)
                        {
                            custquest.setQuestion(dbquestion);
                            custquest.setQuestionnumber(1);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer1().trim());

                            MWCustSecurQuestions custquest2 = new MWCustSecurQuestions(2, dbquestion2, wsmodel.getSecretquestionanswer2().trim(), customer);
                            GeneralDao.Instance.save(custquest2);
                        }
                        else //it will always be 2 --  as make sure by Field validation
                        {
                            custquest.setQuestion(dbquestion2);
                            custquest.setQuestionnumber(2);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer2().trim());

                            MWCustSecurQuestions custquest1 = new MWCustSecurQuestions(1, dbquestion, wsmodel.getSecretquestionanswer1().trim(), customer);
                            GeneralDao.Instance.save(custquest1);
                        }
                        GeneralDao.Instance.saveOrUpdate(custquest);
                }
                else if(customerquestionlist.size() == 2) //Raza: else 2 or more
                {
                    for(MWCustSecurQuestions custquest : customerquestionlist)
                    {
                        if(custquest.getQuestionnumber() == 1)
                        {
                            custquest.setQuestion(dbquestion);
                            custquest.setQuestionnumber(1);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer1().trim());
                        }
                        else //it will always be 2 --  as make sure by Field validation
                        {
                            custquest.setQuestion(dbquestion2);
                            custquest.setQuestionnumber(2);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer2().trim());
                        }
                        GeneralDao.Instance.saveOrUpdate(custquest);
                    }
                }
                else //Waleed: No Questions available before
                {
//                    for(MWCustSecurQuestions custquest : customerquestionlist)
//                    {
                            //For 1st Question
                            MWCustSecurQuestions custquest = new MWCustSecurQuestions();
                            custquest.setQuestion(dbquestion);
                            custquest.setQuestionnumber(1);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer1().trim());
                            custquest.setCustomer(customer);
                            GeneralDao.Instance.saveOrUpdate(custquest);

                            //For 2nd Question
                            custquest = new MWCustSecurQuestions();
                            custquest.setQuestion(dbquestion2);
                            custquest.setQuestionnumber(2);
                            custquest.setAnswer(wsmodel.getSecretquestionanswer2().trim());
                            custquest.setCustomer(customer);
                            GeneralDao.Instance.saveOrUpdate(custquest);

                }



//            }
//            else
//            {
//                //logger.error("No Secret Questions found for Customer [" + WebServiceUtil.getMaskedValue(customer.getCustomerId()) + "] , rejecting...");
//                logger.error("No Secret Questions found for Customer [" + customer.getMobilenumber() + "] , rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//                return false;
//            }

            return true;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error("Exception caught while updating security questions!");
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
    }


    public static AppWsEntity processDisableMobileNumberForChange(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        try {
//            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getUserid())) {
                logger.info("Disabling Mobile Number...");
                AppWsEntity wsobj = MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                if (wsobj.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    String dbQuery;
                    Map<String, Object> params;
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getMobilenumber());

                    MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if(customer == null)
                {
                    logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], cannot disable MobileNumber, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }else {
                    //Raza adding 09-07-2022 start
                    logger.info("Csutomer Found, Finding Disabled Customers with similar Mobile Numbers....");

                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber like '%" + wsmodel.getMobilenumber().substring(5,wsmodel.getMobilenumber().length()) + "%'  ";

                    List<MWCustomer> similarcustomers = GeneralDao.Instance.find(dbQuery);

                    if(similarcustomers != null && similarcustomers.size() > 1)
                    {
                        logger.info("Multiple or previously disabled Customers Found, Disbaling Mobile with Different Strategy....");

                        logger.info("Disabling Mobile Number [" + wsmodel.getMobilenumber() + "]...");

                        customer.setMobilenumber(customer.getMobilenumber().substring(0, 5).replace("00243", "99999") + customer.getMobilenumber().substring(5, customer.getMobilenumber().length()) + customer.getId());
                        customer.setStatus(CustomerStatus.DISABLED_FOR_UPDATE_MOBIILE); //DISABLE CUSTOMER;
                        GeneralDao.Instance.update(customer);
                    }
                    else
                    {
                        logger.info("Disabling Mobile Number [" + wsmodel.getMobilenumber() + "]...");

                        customer.setMobilenumber(customer.getMobilenumber().substring(0, 5).replace("00243", "99999") + customer.getMobilenumber().substring(5, customer.getMobilenumber().length()));
                        customer.setStatus(CustomerStatus.DISABLED_FOR_UPDATE_MOBIILE); //DISABLE CUSTOMER;
                        GeneralDao.Instance.update(customer);
                    }

                    //Raza adding 09-07-2022 end
                    }

                } else {
                    logger.error("Not able to disable mobile number [" + wsmodel.getMobilenumber() + "],  rejecting...");

                }

                return wsobj;
//            }
//            else{
//                logger.error("Transaction Rejected because [" + wsmodel.getMobilenumber() + "] or User Id is missing [" + wsmodel.getUserid() + "],  rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                return wsmodel;
//            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            logger.error("Exception caught while disabling mobile number!");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processVerifyMobileNumberForChange(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        try {
            logger.info("Verifying New MobileNumber...");

//            String dbQuery;
//            Map<String, Object> params;
//            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
//            params = new HashMap<String, Object>();
//            params.put("MOB", wsmodel.getMobilenumber());
//
//            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);
//
//            if(customer != null)
//            {
//                logger.error("Customer found against MobileNumber [" + wsmodel.getMobilenumber() + "], cannot verify MobileNumber, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                return wsmodel;
//            }

            String mobilenum = "";

            if(Util.hasText(wsmodel.getMobilenumber()))
            {
                //Raza updating mobile number start
                mobilenum = wsmodel.getMobilenumber().replace(" ", "");

                if(mobilenum.length() == 9)
                {
                    mobilenum = "00243" + mobilenum;
                }
                else if(mobilenum.length() == 10)
                {
                    mobilenum = "00243" + mobilenum.substring(1,mobilenum.length());
                }
                else if(mobilenum.contains("+243") && mobilenum.substring(0,4).equals("+243"))
                {
                    //mobilenum = "00243" + mobilenum.substring(4, mobilenum.length());
                    mobilenum = mobilenum.replace("+", "00");
                }
                else if(mobilenum.contains("00243") && mobilenum.substring(0,5).equals("00243"))
                {
                    mobilenum = mobilenum;
                }
                else
                {
                    mobilenum = null;
                }
            }


            wsmodel.setMobilenumber(mobilenum);

            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getReason())) {
               return MWWSOperation.ExecuteCreateOTPRequest(wsmodel);
            }
            else{
                logger.error("Unable to generate OTP for new mobile number verification...");
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while verifying new mobile number!");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processRedeemWallet(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        try {
            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getUserid())) {
            logger.info("Redeeming Wallet...");
            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB and c.status= :STATUS and c.userid= :USR ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());
            params.put("USR", wsmodel.getUserid());
            params.put("STATUS", "10");

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(customer == null)
            {
                logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], cannot redeem wallet, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
            else {

                AppWsEntity wsobj = MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                if (wsobj.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    customer.setStatus("00"); // ENABLE CUSTOMER; //We will not update status on mobile updatation
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                } else {
                    logger.error("Not able to redeem wallet against mobile number [" + wsmodel.getMobilenumber() + "],  rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                }

                return wsobj;

            }

            }
            else{
                logger.error("Transaction Rejected because [" + wsmodel.getMobilenumber() + "] or UserId  [" + wsmodel.getUserid() + "] is missing,  rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while redeeming wallet against mobile number [" + wsmodel.getNewmobilenumber() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }


//    public static AppWsEntity processUpdateMerchantMobileNumber(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
//    {
//        try {
//            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getUserid())) {
//                logger.info("Updating Merchant MobileNumber...");
//                String dbQuery;
//                Map<String, Object> params;
//                dbQuery = "from " + MWMerchant.class.getName() + " c where c.mobilenumber= :MOB and c.userid= :USR ";
//                params = new HashMap<String, Object>();
//                params.put("MOB", wsmodel.getMobilenumber());
//                params.put("USR", wsmodel.getUserid());
//
//                MWMerchant mwmerchant = (MWMerchant)GeneralDao.Instance.findObject(dbQuery, params);
//
//                if(mwmerchant == null)
//                {
//                    logger.error("Merchant not found against MobileNumber [" + wsmodel.getMobilenumber() + "] or UserId  [" + wsmodel.getUserid() + "], cannot update MobileNumber, rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
//                    return wsmodel;
//                }
//                else {
//                    AppWsEntity wsobj = MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//                    if (wsobj.getRespcode().equals(ISOResponseCodes.APPROVED)) {
//                            mwmerchant.setMobilenumber(wsmodel.getNewmobilenumber());
//                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                            logger.info("New MobileNumber [" + wsmodel.getNewmobilenumber() + "] allocated to Merchant UserId [" + wsmodel.getUserid() + "]");
//                    } else {
//                        logger.error("Not able to update merchant new mobile number [" + wsmodel.getNewmobilenumber() + "] for Merchant UserId  [" + wsmodel.getUserid() + "],  rejecting...");
//                    }
//                    return wsobj;
//                }
//            }
//            else{
//                logger.error("Transaction Rejected because [" + wsmodel.getMobilenumber() + "] or User Id is missing [" + wsmodel.getUserid() + "],  rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                return wsmodel;
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            logger.error("Exception caught while updating mobile number!");
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//    }
//
//    public static AppWsEntity processUpdateMerchantAccountNumber(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
//    {
//        try {
//            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getUserid())) {
//                logger.info("Updating Merchant Account Number...");
//                String dbQuery;
//                Map<String, Object> params;
//                dbQuery = "from " + MWMerchant.class.getName() + " c where c.userid= :USR ";
//                params = new HashMap<String, Object>();
//                params.put("USR", wsmodel.getUserid());
//
//                MWMerchant mwmerchant = (MWMerchant)GeneralDao.Instance.findObject(dbQuery, params);
//
//                if(mwmerchant == null)
//                {
//                    logger.error("Merchant not found against UserId [" + wsmodel.getUserid() + "], cannot update Merchant's Account Number, rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
//                    return wsmodel;
//                }
//                else {
//                    AppWsEntity wsobj = MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//                    if (wsobj.getRespcode().equals(ISOResponseCodes.APPROVED)) {
//                        logger.info("Merchant's Account Updated Successfully UserId [" + wsmodel.getUserid() + "] ");
//                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                    }
//                    else {
//                        logger.error("Merchant's Account Updation Failed  UserId [" + wsmodel.getUserid() + "],  rejecting...");
//                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                    }
//                     return wsobj;
//                    }
//                }
//            else{
//                logger.error("Transaction Rejected because User Id is missing [" + wsmodel.getUserid() + "],  rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                return wsmodel;
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            logger.error("Exception caught while updating merchant account number!");
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//    }

    public static AppWsEntity processUpdateMobileNumber(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        try {

            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getNewmobilenumber()) && wsmodel.getMobilenumber().equals(wsmodel.getNewmobilenumber()))
            {
                logger.info("Update Mobile API called for Disbale Only and SignUp scenario, returning OK...");
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }

            if(Util.hasText(wsmodel.getMobilenumber()) && Util.hasText(wsmodel.getUserid())) {
            logger.info("Updating MobileNumber...");
            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB and c.userid= :USR ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());
            params.put("USR", wsmodel.getUserid());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(customer == null)
            {
                logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], cannot update MobileNumber, getting Customer with UserId only...");

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.userid= :USR ";
                params = new HashMap<String, Object>();
                params.put("USR", wsmodel.getUserid());

                customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

                if(customer == null)
                {
                    logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "] or UserId [" + wsmodel.getUserid() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }
                else
                {
                    AppWsEntity wsobj = MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                    if (wsobj.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                        logger.info("Checking if New MobileNumber [" + wsmodel.getNewmobilenumber() + "], is available.");
                        dbQuery = "";
                        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
                        params = new HashMap<String, Object>();
                        params.put("MOB", wsmodel.getNewmobilenumber());

                        MWCustomer newcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                        if (newcustomer == null) {
                            customer.setMobilenumber(wsmodel.getNewmobilenumber());
//                    customer.setStatus("00"); // ENABLE CUSTOMER; //We will not update status on mobile updatation
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        } else {
                            logger.error("New MobileNumber [" + wsmodel.getNewmobilenumber() + "] already allocated,  rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        }

//                return wsmodel;
                    } else {
                        logger.error("Not able to update new mobile number [" + wsmodel.getNewmobilenumber() + "],  rejecting...");
                    }

                    return wsobj;
                }
            }
            else {

                AppWsEntity wsobj = MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                if (wsobj.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    logger.info("Checking if New MobileNumber [" + wsmodel.getNewmobilenumber() + "], is available.");
                    dbQuery = "";
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getNewmobilenumber());

                    MWCustomer newcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                    if (newcustomer == null) {
                        customer.setMobilenumber(wsmodel.getNewmobilenumber());
//                    customer.setStatus("00"); // ENABLE CUSTOMER; //We will not update status on mobile updatation
                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    } else {
                        logger.error("New MobileNumber [" + wsmodel.getNewmobilenumber() + "] already allocated,  rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    }

//                return wsmodel;
                } else {
                    logger.error("Not able to update new mobile number [" + wsmodel.getNewmobilenumber() + "],  rejecting...");
                }

                return wsobj;

            }
          }
            else{
                logger.error("Transaction Rejected because [" + wsmodel.getMobilenumber() + "] or User Id is missing [" + wsmodel.getUserid() + "],  rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception caught while updating mobile number!");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processUpdateKYCStatus(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        try {
            logger.info("Updating Security Questions...");

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer)GeneralDao.Instance.findObject(dbQuery, params);

            if(customer == null)
            {
                logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], cannot update KYCStatus, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
            else
            {
                if(Util.hasText(wsmodel.getKycstatus()) || Util.hasText(wsmodel.getPermissions()))
                {
                    if(Util.hasText(wsmodel.getKycstatus()))
                    {
                        if(wsmodel.getKycstatus().equals(CMSKYCStatus.LIGHT))
                        {
                            customer.setKycstatus(CMSKYCStatus.LIGHT);
                        }
                        else if(wsmodel.getKycstatus().equals(CMSKYCStatus.STANDARD))
                        {
                            customer.setKycstatus(CMSKYCStatus.STANDARD);
                        }
                        else if(wsmodel.getKycstatus().equals(CMSKYCStatus.BANKSTAFF))
                        {
                            customer.setKycstatus(CMSKYCStatus.BANKSTAFF);
                        }
                        else if(wsmodel.getKycstatus().equals(CMSKYCStatus.MIGRATED))
                        {
                            customer.setKycstatus(CMSKYCStatus.MIGRATED);
                        }
                        else if(wsmodel.getKycstatus().equals(CMSKYCStatus.MIGRATEDBANKSTAFF))
                        {
                            customer.setKycstatus(CMSKYCStatus.MIGRATEDBANKSTAFF);
                        }
                        else if(wsmodel.getKycstatus().equals(CMSKYCStatus.MIGRATEDBANKSTAFF))
                        {
                            customer.setKycstatus(CMSKYCStatus.MIGRATEDBANKSTAFF);
                        }
                        else if(wsmodel.getKycstatus().equals(CMSKYCStatus.NGO))
                        {
                            customer.setKycstatus(CMSKYCStatus.NGO);
                        }
                        else
                        {
                            logger.error("Invalid KYC Status vaule[" + wsmodel.getKycstatus() + "], checking from DB...");

                            dbQuery = "from " + CMSKYCStatus.class.getName() + " c where c.kycstatus= :KYC  ";
                            params = new HashMap<String, Object>();
                            params.put("KYC", wsmodel.getKycstatus());

                            CMSKYCStatus dbkycstatus = (CMSKYCStatus)GeneralDao.Instance.findObject(dbQuery, params);

                            if(dbkycstatus != null){
                                customer.setKycstatus(dbkycstatus.getKycstatus());
                            }
                            else{
                                logger.error("KYC Status vaule [" + wsmodel.getKycstatus() + "] not found in DB, rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                                return wsmodel;
                            }
                        }
                    }


                    if(Util.hasText(wsmodel.getPermissions()))
                    {
                        customer.setPermissions(wsmodel.getPermissions());
                    }

                    if(Util.hasText(wsmodel.getProduct()))
                    {
                        customer.setProduct(wsmodel.getProduct());
                    }

                    GeneralDao.Instance.saveOrUpdate(customer);
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    return wsmodel;
                }
                else
                {
                    logger.error("KYCStatus and Permission not found to update KYCStatus, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                    return wsmodel;
                }
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            logger.error("Exception caught while updating KYCStatus!");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity getRespCodesList(AppWsEntity wsmodel) //Raza adding to Get RespCodes
    {
        if(!GetRespListV2(wsmodel))
        {
            logger.error("Failed to Set ResList, ignoring...");
            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
        }
        else
        {
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        return wsmodel;
    }

    public static AppWsEntity processTestSopra(AppWsEntity wsmodel) //Raza adding to Get Transaction Charge/Fee
    {
        return SendToOpenAPI(wsmodel);
    }
	
    public static AppWsEntity processMigrateUpdatedBalance(AppWsEntity wsmodel) //Raza adding to Get RespCodes
    {
		wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
		return wsmodel;
        //return BalanceMigration.ExecuteFinalAdjustmentForBalanceMigration(wsmodel);
    }


    public static AppWsEntity processAgentPOSVerifyOTPRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + SMSOTPLog.class.getName() + " c where c.agentmerchantid= :AGNT and c.otp= :OTP and c.mobilenumber= :MOB and c.isverified= :VERIF and c.isexpired= :EXP and c.expirydatetime > :EXPIRY ";
        params = new HashMap<String, Object>();
        params.put("AGNT", wsmodel.getAgentid());
        params.put("OTP",  WSEncryptionUtil.EncryptAppOTP(wsmodel.getOtp()));
        params.put("MOB", wsmodel.getDestmobilenumber());
        params.put("VERIF", false);
        params.put("EXP", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));


        SMSOTPLog otplog = (SMSOTPLog)GeneralDao.Instance.findObject(dbQuery, params);

        if(otplog != null)
        {
            logger.info("AgentPOSOtp Found and Verified against OTP [" + wsmodel.getOtp() + "], returning...");
            otplog.setIsverified(true);
            otplog.setVerifydatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
            GeneralDao.Instance.saveOrUpdate(otplog);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
        }
        else
        {
            logger.error("Failed to get and verify AgentPOSOtp against OTP [" + wsmodel.getOtp() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
        }
        otplog = null;
        return wsmodel;
    }

    public static AppWsEntity processGetAgentEnvoiCashListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAgentCashOutRequest(AppWsEntity wsmodel)
    {
        //Raza adding to stop EnvoiCash to own mobile number
        if(wsmodel.getServicename().equals("EnvoiCashRequest") && wsmodel.getMobilenumber().equals(wsmodel.getDestmobilenumber()))
        {
            logger.error("EnvoiCash not allowed on own mobile number [" + wsmodel.getDestmobilenumber() + "], rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return wsmodel;
        }

        /*if(Util.hasText(wsmodel.getDestmobilenumber()))
        {
            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getDestmobilenumber());
            MWCustomer destcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if(destcustomer != null)
            {
                logger.error("EnvoiCash not allowed to illico Cash User [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_TO_ACCOUNT);
                return wsmodel;
            }
        }*/


        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAgentEnvoiCashRequest(AppWsEntity wsmodel)
    {
        //Raza adding to stop EnvoiCash to own mobile number
        if((!Util.hasText(wsmodel.getDestmobilenumber())) || (wsmodel.getServicename().equals("AgentEnvoiCashRequest") && wsmodel.getMobilenumber().equals(wsmodel.getDestmobilenumber())))
        {
            logger.error("AgentEnvoiCashRequest not allowed on own mobile number [" + wsmodel.getDestmobilenumber() + "] or DestMobileNumber [" + wsmodel.getDestmobilenumber() + "] is missing, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
            return wsmodel;
        }


        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processAgentCancelCashOutRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processGetAgentCashOutListRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processS2MCall(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }
	
	// Added by Affan on 2-NOV-23 ADC Start
    public static AppWsEntity processCreateChannelPINRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
        params = new HashMap<String, Object>();
        params.put("MOBNO", wsmodel.getMobilenumber());

        MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

        if(customer != null)
        {
            customer.setAlternateChannelFlag("2"); // 0 = illico say signup, 1 = ADC say signup, 2 = dono par achuka hay
            GeneralDao.Instance.saveOrUpdate(customer);
        }

        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }
    // Added by Affan on 2-NOV-23 End

    //Hamza adding for Mobile Banking 27-08-2024 start
    public static AppWsEntity processTransferMoneyRequest(AppWsEntity wsmodel)
    {
        String servicename = wsmodel.getServicename();

        wsmodel.setApiversion(APIVersion.VERSION2);
        try{
            logger.info("Process TransferMoney Request...");

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {

                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :BENEID ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("BENEID", Long.parseLong("0"));

                if(Util.hasText(wsmodel.getBeneficiaryid())) {
//                    dbQuery += " and c.id= :BENEID ";
                    params.put("BENEID", Long.parseLong(wsmodel.getBeneficiaryid()));
                }

                List<MWBeneficiary> dbbeneflist = GeneralDao.Instance.find(dbQuery, params);

                if (dbbeneflist != null && dbbeneflist.size() > 0) {
                    for (MWBeneficiary bf : dbbeneflist) {
                        wsmodel.setFirstname(bf.getFirstname());
                        wsmodel.setLastname(bf.getLastname());
                        wsmodel.setBeneficiaryname(bf.getBeneficiaryname());
                        wsmodel.setDestusername(bf.getBeneficiaryname());
                        wsmodel.setBeneficiarytype(bf.getBeneficiarytype());
                        wsmodel.setDestaccounttitle(bf.getDestaccounttitle());
                        wsmodel.setDestaccountnumber(bf.getDestaccountnumber());
                        wsmodel.setDestaccount(bf.getDestaccountnumber());
                        wsmodel.setConsumerno(bf.getConsumerno());
                        wsmodel.setCountry(bf.getCountry());
                        wsmodel.setIban(bf.getIban());
                        wsmodel.setSwiftbiccode(bf.getSwiftcode());
                        wsmodel.setBiccode(bf.getBikcode());
                        wsmodel.setBikcode(Util.hasText(bf.getBikcode()) ? bf.getBikcode() : bf.getSwiftcode());
                        wsmodel.setBankcode(bf.getBankcode());
                        wsmodel.setDestbankcode(bf.getBankcode());
                        wsmodel.setBranchcode(bf.getBranchnumber());
                        wsmodel.setBankname(bf.getBankname());
                        wsmodel.setCity(bf.getCity());
                        wsmodel.setCurrency(bf.getCurrency());
                        wsmodel.setSourceaccount(bf.getSourceaccount());
                        wsmodel.setSourcetitle(bf.getSourcetitle());
                        wsmodel.setBeneficiarytype(bf.getBeneficiarytype());
                        wsmodel.setDestaddress(bf.getDestaddress());
                        wsmodel.setAddress(customer.getAddress());
                    }
                }
                else {
                    logger.error("No Beneficiary Found against this ID["+ wsmodel.getBeneficiaryid() +"] for customer [" + wsmodel.getMobilenumber() + "]");
                }

            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }

            String transactiontype = wsmodel.getTypefilter();
            switch (transactiontype)
            {
                case "IllicoToIllico":   //OLDAPI: WalletTransaction
                case "IllicoToAlias":
                {
                    wsmodel.setServicename("WalletTransaction");
                    MiddlewareWebServiceHandler.processWalletTransactionRequest(wsmodel);
                    break;
                }
                case "BankToIllico":
                {
                    wsmodel.setServicename("BankToWallet");
                    MiddlewareWebServiceHandler.processBankToWalletRequest(wsmodel);
                    break;
                }
                case "BankToAlias":
                {
                    wsmodel.setServicename("BankToAlias");
                    MiddlewareWebServiceHandler.processBankToAliasRequest(wsmodel);
                    break;
                }
                case "BankToOtherBank":
                case "BankToBank":
                {
                    wsmodel.setServicename("BankToBank");
                    MiddlewareWebServiceHandler.processBankToBankRequest(wsmodel);
                    break;
                }
                case "IllicoToBank":
                {
                    wsmodel.setServicename("SendMoneyBankAccount");
                    MiddlewareWebServiceHandler.processSendMoneyBankAccountRequest(wsmodel);
                    break;
                }
                case "LoadIllico":
                {
                    wsmodel.setServicename("LoadWallet");
                    MiddlewareWebServiceHandler.processLoadWalletRequest(wsmodel);
                    break;
                }
                case "LoadBank":
                {
                    wsmodel.setServicename("UnloadWallet");
                    MiddlewareWebServiceHandler.processUnloadWalletRequest(wsmodel);
                    break;
                }
                case "EnvoiCash":
                {
                    wsmodel.setServicename("EnvoiCashRequest");
                    MiddlewareWebServiceHandler.processCashOutReqRequest(wsmodel);
                    break;
                }
                default:
                {
                    logger.error("Unrecognized Type Filter [" + wsmodel.getTypefilter() + "] for [" + wsmodel.getServicename() + "] Transaction, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    break;
                }
            }
            return wsmodel;
        }
        catch (Exception e){
            logger.error("Exception caught while processing TransferMoney Request!");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            wsmodel.setServicename(servicename);
        }
    }
    //Hamza adding for Mobile Banking 27-08-2024 end

    //Hamza adding for Incoming MoneyGram Calls Start

    public static AppWsEntity processIncomingRemittanceInquiry(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    public static AppWsEntity processIncomingRemittance(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel,false);
    }

    //Hamza adding for Incoming MoneyGram Calls End

    //Justn for NOtification Checking
    public static  AppWsEntity ExecuteExternalNotifyCustomer(AppWsEntity wsmodel)
    {

        try {
            logger.info("Executing ExecuteSendNotifyCustomer Request...");

            logger.info("Validating Session...");
//            if (!ValidateUserandAppSession(wsmodel)) {
//                logger.error("Failed to Validate Session, rejecting...");
//                return wsmodel;
//            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null)
            {
                logger.info("Customer Not Null....");
                wsmodel.setLanguage(customer.getLanguage());
                wsmodel.setUserid(customer.getUserid());
                wsmodel.setCustomerid(customer.getCustomerId());
                wsmodel.setCdfacctid(customer.getCdfaccountid());
                wsmodel.setUsdacctid(customer.getUsdaccountid());
                wsmodel.setUsername(customer.getUsername());
                wsmodel.setEmailaddress(customer.getEmailaddress());
                wsmodel.setDateofbirth(customer.getDateofbirth());
                wsmodel.setLanguage(customer.getLanguage());
                wsmodel.setNotiflanguage(customer.getNotiflanguage());
                wsmodel.setFirstname(customer.getFirstname());
                wsmodel.setLastname(customer.getLastname());



                if (Util.hasText(wsmodel.getDeliverymode()))
                {
                    if (wsmodel.getDeliverymode().equals(DeliveryModes.InAPP_FLAG))
                    {
                        logger.info("APP Notifcation..");

                        if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, "E ticketing", wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            return wsmodel;
                        }
                        else {
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;

                    } else if (wsmodel.getDeliverymode().equals(DeliveryModes.EMAIL_FLAG))
                    {
                        logger.info("EMAIL Notifcation..");

                        if(!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getEmailaddress(), wsmodel))
                        {
                            logger.error("Failed to create & Send Email for EmailID [" + wsmodel.getEmailaddress() + "], rejecting...");
                            return wsmodel;

                        }
                        else {

                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;

                    } else if (wsmodel.getDeliverymode().equals(DeliveryModes.EMAIL_InAPP_FLAG))
                    {
                        logger.info("EMAIL APP Notifcation..");

                        if(!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getEmailaddress(), wsmodel))
                        {
                            logger.error("Failed to create & Send Email for EmailID [" + wsmodel.getEmailaddress() + "], rejecting...");
                            return wsmodel;

                        }
                        if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            return wsmodel;

                        }
                        else {
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;
                    }
                    else if (wsmodel.getDeliverymode().equals(DeliveryModes.SMS_FLAG))
                    {
                        logger.info("SMS Notifcation..");

                        if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            return wsmodel;

                        }
                        else {
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;
                    }
                    else if(wsmodel.getDeliverymode().equals(DeliveryModes.SMS_InAPP_FLAG))
                    {

                        logger.info("SMS APP Notifcation..");

                        if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            return wsmodel;

                        }
                        if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            return wsmodel;
                        }
                        else {
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;
                    }
                    else if(wsmodel.getDeliverymode().equals(DeliveryModes.SMS_EMAIL_FLAG))
                    {
                        logger.info("SMS EMAIL Notifcation..");

                        if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            return wsmodel;
                        }
                        if(!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER,wsmodel.getCategoryid(), wsmodel.getEmailaddress(), wsmodel))
                        {
                            logger.error("Failed to create & Send Email for EmailID [" + wsmodel.getEmailaddress() + "], rejecting...");
                            return wsmodel;
                        }
                        else {
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;

                    }
                    else if (wsmodel.getDeliverymode().equals(DeliveryModes.SMS_EMAIL_InAPP_FLAG))
                    {
                        logger.info("SMS EMAIL APP Notifcation..");
                        if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            return wsmodel;
                        }
                        if(!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getEmailaddress(), wsmodel))
                        {
                            logger.error("Failed to create & Send Email for EmailID [" + wsmodel.getEmailaddress() + "], rejecting...");
                            return wsmodel;
                        }
                        if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, wsmodel.getCategoryid(), wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            return wsmodel;
                        }
                        else
                        {
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                        }
                        return wsmodel;

                    }
                    else
                    {
                        logger.error("Invalid typefilter/DeliveryMode [" + wsmodel.getDeliverymode() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                        return wsmodel;
                    }

                }
                else{
                    logger.info("DeleiveryMode == Null");
                    wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                    return wsmodel;
                }
            }
            else{
                logger.error("Customer not found with mobilenumber [" + wsmodel.getMobilenumber() + "], ignoring...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }

        }
        catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity processDeviceUnbinding(AppWsEntity wsmodel) {
       return MWWSOperation.executeDeviceUnbinding(wsmodel);
    }

    public static AppWsEntity processConfirmDeviceUnbinding(AppWsEntity wsModel) {
        return MWWSOperation.executeConfirmDeviceUnbinding(wsModel, false);
    }

    public static AppWsEntity processBillRequest(AppWsEntity wsmodel) {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }
}



