package gateway.middlewarewebservice.component;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import pk.vaulsys.apigateway.base.config.*;
import pk.vaulsys.apigateway.beneficiary.entity.MWBeneficiary;
import pk.vaulsys.apigateway.beneficiary.entity.MWPayersConfig;
import pk.vaulsys.apigateway.beneficiary.entity.MWRemitBeneficiary;
import pk.vaulsys.apigateway.customer.*;
import pk.vaulsys.apigateway.email.base.EmailGatewayHandler;
import pk.vaulsys.apigateway.email.entity.EmailOtpLog;
import pk.vaulsys.apigateway.email.entity.UnverifiedEmailsLog;
import pk.vaulsys.apigateway.entity.SystemConfig;
import pk.vaulsys.apigateway.network.channel.base.Channel;
import pk.vaulsys.apigateway.notification.base.NotificationSettingCategory;
import pk.vaulsys.apigateway.notification.entity.NotificationSetting;
import pk.vaulsys.apigateway.notification.handler.NotificationHandler;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ChannelCodes;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.base.msgspecs.base.SwitchRespCodes;
import pk.vaulsys.apigateway.protocols.base.msgspecs.base.SwitchRespCodesTranslation;
import pk.vaulsys.apigateway.protocols.base.msgspecs.entity.SwitchTransactionCodes;
import pk.vaulsys.apigateway.protocols.webservice.base.CustomerType;
import pk.vaulsys.apigateway.protocols.webservice.base.D1JWTTokenUtility;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.CMSKYCStatus;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.FormTemplate;
import pk.vaulsys.apigateway.protocols.webservice.base.entity.WebServiceEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.*;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.handler.MWCustDeviceBindHandler;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.handler.MiddlewareWebServiceHandler;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.*;
import pk.vaulsys.apigateway.security.keystore.KeyType;
import pk.vaulsys.apigateway.security.securekey.SecureDESKey;
import pk.vaulsys.apigateway.security.securekey.SecureKey;
import pk.vaulsys.apigateway.smsgateway.base.OTPChannel;
import pk.vaulsys.apigateway.smsgateway.base.SMSCategory;
import pk.vaulsys.apigateway.smsgateway.entity.SMSOTPLog;
import pk.vaulsys.apigateway.smsgateway.handler.SMSGatewayHandler;
import pk.vaulsys.apigateway.terminal.impl.Terminal;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;
import pk.vaulsys.apigateway.wfe.GlobalContext;
import pk.vaulsys.apigateway.wfe.ProcessContext;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.handler.MWCustDeviceBindHandler.BindCustomerDevice;

/**
 * Created by Raza on 29-Jan-18.
 */
public class MWWSOperation {
    private static final Logger logger = LogManager.getLogger(MWWSOperation.class);



    public static AppWsEntity ExecuteSignUpRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing SignUp Request...");

            String dbQuery;
            Map<String, Object> params;

            //Verify MobileNumber
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
                logger.error("Customer already exists with MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_CUSTOMER);
                return wsmodel;
            }

            //Verify Email
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.emailaddress= :EMAIL ";
            params = new HashMap<String, Object>();
            params.put("EMAIL", wsmodel.getEmailaddress());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
                logger.error("Customer already exists with Email [" + wsmodel.getEmailaddress() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_CUSTOMER);
                return wsmodel;
            }


            //Now Create Customer & Register Device
            if (customer == null) {
                customer = new MWCustomer();
            }
            customer.setUsername(wsmodel.getUsername().trim());
            customer.setMobilenumber(wsmodel.getMobilenumber());
            if (Util.hasText(wsmodel.getPassword())) //Raza adding on 03032021 - SignUp Migrated no password from App
            {
                String temppass = WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim());

                if (!Util.hasText(temppass)) {
                    logger.error("Invalid Password from App, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                    return wsmodel;
                }


                customer.setPassword(temppass);
                //customer.setPassword(WSEncrptionUtil.EncryptAppPassword(wsmodel.getPassword().trim()));
            }
            customer.setEmailaddress(wsmodel.getEmailaddress());
            customer.setCnic(wsmodel.getIdentificationno().trim());
            customer.setIdentificationtype(wsmodel.getIdentificationtype());
            customer.setFirstlogin(true);
            customer.setHaswallet(false);
            customer.setResetcredentials(true);
            customer.setFirstname(wsmodel.getFirstname().trim());
            customer.setLastname(wsmodel.getLastname().trim());
            customer.setKycstatus(CMSKYCStatus.LIGHT);
            customer.setStatus("08");
//            //For Device Bind - Start
//            customer.setDevicebindingenabled(true);
//            //For Device Bind - End

            if (Util.hasText(wsmodel.getDateofbirth())) {
                customer.setDateofbirth(wsmodel.getDateofbirth());
            }
            //customer.setCustomerpicture(wsmodel.getCustomerpicture());


            MWDeviceLog device = new MWDeviceLog();
            device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
            device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
            device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
            device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
            device.setBaseintegrity(false);
            device.setCtsprofile(false);
            device.setIsrooted(false);

//            if we have CR to change Primary Device
//            device.setPrimaryDevice(true);

            device.setCustomer(customer);
            GeneralDao.Instance.saveOrUpdate(customer);
            GeneralDao.Instance.saveOrUpdate(device);

            MWDeviceSessionLog session = new MWDeviceSessionLog();
            session.setDevice(device);
            session.setCustomer(customer);
            session.setCreatedatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
            session.setExpiredatetime(Long.parseLong(WebServiceUtil.dateFormat.format(DateUtils.addMinutes(new Date(), Integer.parseInt(GetSessionExipreTime())))));
            session.setExpired(true);
            String tvalue = "";
            for (int i = 0; i < 12; i++) {
                Random rnd = new Random();
                int a = rnd.nextInt(10);
                tvalue += a;
            }
            session.setToken(tvalue);
            GeneralDao.Instance.saveOrUpdate(session);
            wsmodel.setToken(tvalue);

            if (wsmodel.getSecurityparams() != null) {
                customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
                customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
                GeneralDao.Instance.saveOrUpdate(customer);
            }

            logger.info("Customer Registered & Device Session Generated successfully!");

            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
            return wsmodel;

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }


//    public static AppWsEntity ExecuteGetCustomerByMobileNumberRequest(AppWsEntity wsmodel) {
//
//        try {
//            logger.info("Executing GetCustomerByMobileNumber Request...");
//            // Biller Start
//            String dbQuery;
//            Map<String, Object> params;
//
//            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBILENUMBER ";
//            params = new HashMap<String, Object>();
//            params.put("MOBILENUMBER", wsmodel.getMobilenumber());
//
//            MWCustomer mwCustomer =  (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
//            if(mwCustomer != null){
//                wsmodel.setUserid(mwCustomer.getUserid());
//                wsmodel.setCustomerId(mwCustomer.getCustomerId());
//            }else{
//
//
//            }
//            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//            return wsmodel;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//    }

    public static AppWsEntity ExecuteUpdateMerchantDashboardRequest(AppWsEntity wsmodel) {
            return wsmodel;
    }

    public static AppWsEntity ExecuteLogInRequest(AppWsEntity wsmodel, Boolean otpconfirmed, Boolean binddevice)
    {

        try {
            logger.info("Executing " + wsmodel.getServicename() + " Request...");

            String dbQuery;
            Map<String, Object> params;

            //Verify MobileNumber
//            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO " + " and c.password = :PASS ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//            params.put("PASS", WSEncrptionUtil.EncryptAppPassword(wsmodel.getPassword()));

            /*if(Util.hasText(wsmodel.getMobilenumber()))
            {
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
            }
            else if(Util.hasText(wsmodel.getUsername()))
            {
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :USER ";
                params = new HashMap<String, Object>();
                params.put("USER", wsmodel.getUsername());
            }
            else
            {
                logger.error("No Identifier found to LogIn, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }*/

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :MOBNO or c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            // Added by Affan on 2-NOV-23 - ADC Start
            if (customer != null && customer.getAlternateChannelFlag() != null && customer.getAlternateChannelFlag().equals("1")) {
                logger.info("Customer with Mobile No. [" + wsmodel.getMobilenumber() + "] is not an illicocash Customer, registration required returning.....");
                if(Util.hasText(customer.getLanguage()))
                {
                    wsmodel.setLanguage(customer.getLanguage());
                }
                if(Util.hasText(customer.getNotiflanguage()))
                {
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                }
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_ADC_EXIST);
                return wsmodel;
            }
            // Added by Affan on 2-NOV-23 - ADC End

            if (customer != null && customer.getHaswallet() != null && customer.getHaswallet() && customer.getIsmigratedactive() != null && !customer.getIsmigratedactive() && customer.getStatus().equals(CustomerStatus.ACTIVE))
            {
                logger.info("Verifying Prerequisites for Customer LogIn....");
                if(Util.hasText(customer.getLanguage()))
                {
                    wsmodel.setLanguage(customer.getLanguage());
                }
                if(Util.hasText(customer.getNotiflanguage()))
                {
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                }
                if (!VerifyLoginPrerequisites(wsmodel, customer)) {

                    logger.error("Failed to Verify Login Prerequisites");

                    wsmodel.setRespcode(!(Util.hasText(wsmodel.getRespcode())) ? ISOResponseCodes.MW_PERMISSION_DENIED : wsmodel.getRespcode());
                    return wsmodel;
                }


                logger.info("Prerequisites verified for Customer LogIn....");

                wsmodel.setMobilenumber(customer.getMobilenumber());
                wsmodel.setEmailaddress(customer.getEmailaddress());
                wsmodel.setFirstname(customer.getFirstname());
                wsmodel.setLastname(customer.getLastname());

                //Customer can LogIn but cannot get wallets etc. if Inactive/Blocked on Wallet
                //if(!customer.getStatus().equals("00")){
                //    //The Customer whose wallet is blocked, so he should not be allowed to login.
                //    logger.error("Customer [" + customer.getFirstname() + " " + customer.getLastname() + "]  wallet is blocked, because the mobile number [" + wsmodel.getMobilenumber().replace("9999","00243") + "] is allocated to another user, rejecting...");
                //    wsmodel.setRespcode(ISOResponseCodes.MW_CUST_BLOCKED);
                //    return wsmodel;

                //}
                if (!customer.getPassword().equals(WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim()))) {
                    logger.error("Invalid Password [" + (wsmodel.getPassword().trim()) + "] for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                    customer.setRemainingloginretries((Long.parseLong(customer.getRemainingloginretries()) - 1) + "");
                    wsmodel.setRemretries(customer.getRemainingloginretries());     // Hritik Adding Login Atempts to App Screen on 13-July-23

                    if (Integer.parseInt(customer.getRemainingloginretries()) < 1) {
                        customer.setStatus(CustomerStatus.FAILED_LOGIN_ATTEMPTS_BLOCK);

                        /*String servicename = wsmodel.getServicename();
                        wsmodel.setServicename("UpdateProfile");
                        wsmodel.setStatus(CustomerStatus.FAILED_LOGIN_ATTEMPTS_BLOCK);
                        wsmodel.setUserid(customer.getUserid());
                        SendToOpenAPI(wsmodel);
                        wsmodel.setServicename(servicename);

                        if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
                        {
                            logger.info("Customer Login retries exhuasted Status updated on Wallet!");
                        }
                        else
                        {
                            logger.error("Failed to update Customer Login retries exhuasted Status on Wallet, ignoring...");
                        }*/
                    }
                    GeneralDao.Instance.saveOrUpdate(customer);
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                    return wsmodel;
                }


                customer.setRemainingloginretries(wsmodel.getMaxloginattempts());
                GeneralDao.Instance.saveOrUpdate(customer);

                logger.info("Customer found, getting session...");
                dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                params = new HashMap<String, Object>();
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

                if (sessionlist != null && sessionlist.size() > 0)
                {

                    /*logger.error("User already have a active session token, cannot allow ReLogin, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
                    return wsmodel;*/

                    //Added by Affan on 03-OCT-2023 Start
                    if (sessionlist.get(0).getDevice().getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid())) {
                        logger.info("User active Session found on Device, re-issuing session...");

                        if (Util.hasText(wsmodel.getSecurityparams().getFirebasetoken())
                                && Util.hasText(sessionlist.get(0).getDevice().getCustomer().getFirebasetoken())
                                && !wsmodel.getSecurityparams().getFirebasetoken().equals(sessionlist.get(0).getDevice().getCustomer().getFirebasetoken())) {
                            logger.error("Firebase token mismatch for Login Request, received against active device session, rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
                            return wsmodel;
                        }
                    } else {
                        logger.error("User already have a active session token on another Device, cannot allow ReLogin, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
                        return wsmodel;
                    }
                    //Added by Affan on 03-OCT-2023 End

//                    logger.error("User already have a active session token w.r.t. device, checking session for Mobile...");
//
//                    if(sessionlist.size() > 1)
//                    {
//                        logger.error("Multiple active sessions found on device, marking all expire...");
//                        for(MWDeviceSessionLog sess : sessionlist)
//                        {
//                            sess.setExpired(true);
//                            GeneralDao.Instance.saveOrUpdate(sess);
//                        }
//                        GeneralDao.Instance.endTransaction();  //Raza close multiple sessions and commit in DB before checking session against Mobile Number...
//                        GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
//                    }
//                    else
//                    {
//                        if(!sessionlist.get(0).getCustomer().getMobilenumber().equals(wsmodel.getMobilenumber()))
//                        {
//                            logger.error("LogIn request recveied from deivce with active session of Customer [" +  sessionlist.get(0).getCustomer().getMobilenumber() + "], cannot allow LogIn to Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
//                            wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
//                            return wsmodel;
//                        }
//                        else
//                        {
//                            logger.error("ForceLogOut case, allowing Re-LogIn to Customer [" +  wsmodel.getMobilenumber() + "]...");
//                            sessionlist.get(0).setExpired(true);
//                            GeneralDao.Instance.saveOrUpdate(sessionlist.get(0));
//                        }
//                    }
                }

                else {
                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.customer.mobilenumber= :MOB " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getMobilenumber());
                    params.put("ISEXPIRED", false);
                    params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                    sessionlist = GeneralDao.Instance.find(dbQuery, params);

                    if (sessionlist != null && sessionlist.size() > 0) {
                        logger.error("Customer already LoggedIn from another device, rejecting...");
                        //wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
                        wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION); //94 - Permission Denied
                        return wsmodel;
//                        logger.error("Customer already LoggedIn from another device, assuming forcelogout and login from another device...");
//                        if(sessionlist.size() > 1)
//                        {
//                            logger.error("Multiple active sessions found on mobilenumber, marking all expire...");
//                            for(MWDeviceSessionLog sess : sessionlist)
//                            {
//                                sess.setExpired(true);
//                                GeneralDao.Instance.saveOrUpdate(sess);
//                            }
//                            GeneralDao.Instance.endTransaction();  //Raza close multiple sessions and commit in DB before checking session against Mobile Number...
//                            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
//                        }
//                        else
//                        {
//                            logger.info("Customer [" + wsmodel.getMobilenumber() + "] trying to LogIn with same Device or other, while having an aactive session, assuming forcelogout case...");
//                            sessionlist.get(0).setExpired(true);
//                            GeneralDao.Instance.saveOrUpdate(sessionlist.get(0));

////                            if(!sessionlist.get(0).getDevice().getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid()))
////                            {
////                                logger.error("LogIn request recveied from deivce with active session of Customer [" +  sessionlist.get(0).getCustomer().getMobilenumber() + "], cannot allow LogIn to Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
////                                wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
////                                return wsmodel;
////                            }
////                            else
////                            {
////                                logger.error("ForceLogOut case, allowing Re-LogIn to Customer [" +  wsmodel.getMobilenumber() + "]...");
////                                sessionlist.get(0).setExpired(true);
////                                GeneralDao.Instance.saveOrUpdate(sessionlist.get(0));
////                            }
//                        }
                    }

                    else {
                        String firebasetoken = wsmodel.getSecurityparams().getFirebasetoken();

                        if (!Util.hasText(firebasetoken)) {
                            firebasetoken = customer.getFirebasetoken();
                        }

                        if (Util.hasText(firebasetoken)) {
                            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.firebasetoken= :FIRE " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                            params = new HashMap<String, Object>();
                            params.put("FIRE", firebasetoken);
                            params.put("ISEXPIRED", false);
                            params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                            sessionlist = GeneralDao.Instance.find(dbQuery, params);

                            if (sessionlist != null && sessionlist.size() > 0) {
                                logger.error("Device already LoggedIn w.r.t. Firebase token, rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION); //94 - Permission Denied
                                return wsmodel;
                            }
                        }


                    }
                }

                MarkSessionsExpire(wsmodel);
                /*
                //TODO: Raza update expired logic
                dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                params = new HashMap<String, Object>();
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
                params.put("ISEXPIRED", false);

                sessionlist = GeneralDao.Instance.find(dbQuery, params);
                for(MWDeviceSessionLog sess : sessionlist)
                {
                    sess.setExpired(true);
                    GeneralDao.Instance.saveOrUpdate(sess);
                }
                //TODO: Raza update expired logic
                */


                String firebasetoken = wsmodel.getSecurityparams().getFirebasetoken();

                if (!Util.hasText(firebasetoken)) {
                    firebasetoken = customer.getFirebasetoken();
                }

                if (Util.hasText(firebasetoken)) {
                    //TODO: Raza update FireBase Token Update Logic ; Old customer that was using that device
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.firebasetoken= :FIRE ";
                    params = new HashMap<String, Object>();
                    params.put("FIRE", firebasetoken);
                    List<MWCustomer> firebasecustomers = GeneralDao.Instance.find(dbQuery, params);
                    if (firebasecustomers != null && firebasecustomers.size() > 0) {
                        for (MWCustomer mwcust : firebasecustomers) {
                            if (!mwcust.getMobilenumber().equals(customer.getMobilenumber())) {
                                mwcust.setFirebasetoken(null);
                                GeneralDao.Instance.saveOrUpdate(mwcust);
                            }
                        }
                    }

                    dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.firebasetoken= :FIRE ";
                    params = new HashMap<String, Object>();
                    params.put("FIRE", firebasetoken);
                    List<MWDeviceLog> firebasedevices = GeneralDao.Instance.find(dbQuery, params);
                    if (firebasedevices != null && firebasedevices.size() > 0) {
                        for (MWDeviceLog dev : firebasedevices) {
                            if (!dev.getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid())) {
                                dev.setFirebasetoken(null);
                                GeneralDao.Instance.saveOrUpdate(dev);
                            }
                        }
                    }
                    //TODO: Raza update FireBase Token Update Logic ; Old customer that was using that device
                }


                customer.setFirebasetoken(firebasetoken);
                customer.setLastrequesttime(WebServiceUtil.TransDateTimeFormat.format(new Date()));//Raza adding for Idle TimeOut...

//                //Muhammad Hamza
//                //Check whether device is already binded to another customer
                /*if (!customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) {
                    dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid= :DEVC ";
                    params = new HashMap<String, Object>();
                    params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

                    MWCustDeviceBindingLog dbbindlog = (MWCustDeviceBindingLog) GeneralDao.Instance.findObject(dbQuery, params);


//                dbbindlog.getCustomer().getMobilenumber().equals(wsmodel.getMobilenumber())
                    if (dbbindlog != null && dbbindlog.getCustomer() != null && dbbindlog.getCustomer().getMobilenumber() != customer.getMobilenumber()) {
                        logger.error("Device Bind record found in DB for DeviceId [" + wsmodel.getSecurityparams().getDeviceid() + "] with Customer [" + dbbindlog.getCustomer().getMobilenumber() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_DEVC_BIND_WITH_ANOTHER_CUSTOMER);
                        return wsmodel;
                    }
                }*/


                //Muhammad Hamza: adding for Device Bind Checking... Start
                if (!customer.getDevicebindingenabled() && systemDeviceBindingEnabled())
                {
                    if(!otpconfirmed){

                        logger.info("Device Binding Enabled but Customer has it disabled, verifying OTP Reqiured...");

                        try {
                            if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_BIND_DEVICE, customer.getMobilenumber(), wsmodel)) {
                                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                    //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    //return wsmodel;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Exception caght while sending SMS, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }

                        try {
                            if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                    && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_BIND_DEVICE, customer.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                            }
                        }
                        catch (Exception e){
                            logger.error("Exception caght while sending email, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }

                        //TODO: Raza REMOVE ME TESTING ONLY
                        if (Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007")) {
                            if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                            }
                        } else if (wsmodel.getSecurityparams() != null && Util.hasText(wsmodel.getSecurityparams().getDeviceid()) && wsmodel.getSecurityparams().getDeviceid().equals("10cbb8712e76027e")) {
                            if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {

                                logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                            }
                        }
                        //TODO: Raza REMOVE ME TESTING ONLY

                        wsmodel.setRespcode(ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED);
                        return wsmodel;

                    }

                }

                //Muhammad Hamza: adding for Device Bind Checking... End


                GeneralDao.Instance.saveOrUpdate(customer);

                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
                params = new HashMap<String, Object>();
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

                MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);


                if (device == null) {
                    logger.info("New Device Found, using it....");

                    //Raza New Illico Device case start
                    dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.customer.mobilenumber = :MOB ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getMobilenumber());

                    MWDeviceLog olddevice = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

                    if (olddevice != null) {
                        logger.info("Customer used new device, OTP required...");

                        olddevice.setCustomer(null);
                        GeneralDao.Instance.saveOrUpdate(olddevice);
                        //Raza save new device first...
                        device = new MWDeviceLog();
                        device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                        device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                        device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                        device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                        device.setBaseintegrity(false);
                        device.setCtsprofile(false);
                        device.setIsrooted(false);
                        //device.setCustomer(customer);
                        device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                        GeneralDao.Instance.saveOrUpdate(customer);
                        GeneralDao.Instance.saveOrUpdate(device);

                        //Raza generating and sending SMS for LogIn from NewDevice start
                        wsmodel.setNotiflanguage(customer.getNotiflanguage());
                        wsmodel.setFirstname(customer.getFirstname());
                        wsmodel.setLastname(customer.getLastname());

                        try {
                            if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                    //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    //return wsmodel;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Exception caght while sending SMS, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }
                        //Raza geenrating and sending SMS for LogIn from NewDevice end
                        try {
                            if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                    && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                            }
                        }
                        catch (Exception e){
                            logger.error("Exception caght while sending email, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }

						//TODO: Raza REMOVE ME TESTING ONLY
                        if (Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007")) {
                            if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                            }
                        }
                        //TODO: Raza REMOVE ME TESTING ONLY


                        wsmodel.setRespcode((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled()) ? ISOResponseCodes.MW_OTP_REQUIRED : ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED );
                        return wsmodel;
                    }

                    //Raza New Illico Device case end


                    device = new MWDeviceLog();
                    device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                    device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                    device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                    device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                    device.setBaseintegrity(false);
                    device.setCtsprofile(false);
                    device.setIsrooted(false);
                    //device.setCustomer(customer);
                    device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                    GeneralDao.Instance.saveOrUpdate(customer);
                    GeneralDao.Instance.saveOrUpdate(device);
                }
                //Hamza Added for Device Bind Enabled
                else if (device.getCustomer() == null && !otpconfirmed) {
                    logger.info("Device found with no Customer, OTP Required!");

                    //Raza geenrating and sending SMS for LogIn from NewDevice start
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    wsmodel.setFirstname(customer.getFirstname());
                    wsmodel.setLastname(customer.getLastname());

                    try {
                        if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                            if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                //return wsmodel;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception caght while sending SMS, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }
                    //Raza geenrating and sending SMS for LogIn from NewDevice end

                    try {
                        if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getEmailaddress(), wsmodel)) // Added by Affan on 10-Nov-23 Email OTP Format
                        {
                            logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                        }
                    }
                    catch (Exception e){
                        logger.error("Exception caght while sending email, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }


                    //TODO: Raza REMOVE ME TESTING ONLY
                    if (Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007")) {
                        if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                        }
                    } else if (wsmodel.getSecurityparams() != null && Util.hasText(wsmodel.getSecurityparams().getDeviceid()) && wsmodel.getSecurityparams().getDeviceid().equals("10cbb8712e76027e")) {
                        if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                        }
                    }
                    //TODO: Raza REMOVE ME TESTING ONLY


                    wsmodel.setRespcode((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled()) ? ISOResponseCodes.MW_OTP_REQUIRED : ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED);
                    return wsmodel;
                }
                else if (device.getCustomer() != null && !device.getCustomer().getMobilenumber().equals(customer.getMobilenumber()) && !otpconfirmed) {
                    logger.info("Customer Logging In from new device, OTP Required!");

                    //Raza geenrating and sending SMS for LogIn from NewDevice start
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    wsmodel.setFirstname(customer.getFirstname());
					wsmodel.setLastname(customer.getLastname());

                    try {
                        if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                            if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_MOBILE_DEVICE, customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                return wsmodel;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception caght while sending SMS, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }
                    //Raza geenrating and sending SMS for LogIn from NewDevice end

                    try {
                        if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getEmailaddress(), wsmodel)) {
                            logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                        }
                    }
                    catch (Exception e){
                        logger.error("Exception caght while sending email, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }


                    //TODO: Raza REMOVE ME TESTING ONLY
                    if (Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007")) {
                        if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                        }
                    } else if (wsmodel.getSecurityparams() != null && Util.hasText(wsmodel.getSecurityparams().getDeviceid()) && wsmodel.getSecurityparams().getDeviceid().equals("10cbb8712e76027e")) {
                        if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {

                            logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                        }
                    }
                    //TODO: Raza REMOVE ME TESTING ONLY

                    wsmodel.setRespcode(((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled()) ? ISOResponseCodes.MW_OTP_REQUIRED : ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED));
                    return wsmodel;
                }
                else if (otpconfirmed) {
                    logger.info("Updating customer for device after otp confirmed");

                    dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.customer.mobilenumber = :MOB ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getMobilenumber());

                    MWDeviceLog olddevice = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

                    if (olddevice != null) {
                        olddevice.setCustomer(null);
                        GeneralDao.Instance.saveOrUpdate(olddevice);
                    }

                    device.setCustomer(customer);
                    GeneralDao.Instance.saveOrUpdate(device);
                }

                if (binddevice && systemDeviceBindingEnabled())
                {
                        if (BindCustomerDevice(customer, device, MWCustDevcBindReasons.MANUAL)) {
                            logger.info("Customer Device binding successfully!");
                            customer.setDevicebindingenabled(true);
                        } else {
                            logger.error("Failed to Bind Device [" + wsmodel.getSecurityparams().getDeviceid() + "] with Customer [" + customer.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                            return wsmodel;
                        }
                    GeneralDao.Instance.saveOrUpdate(customer);
                }

                MWDeviceSessionLog session = new MWDeviceSessionLog();
                session.setDevice(device);
                session.setCustomer(customer);
                session.setCreatedatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                session.setExpiredatetime(Long.parseLong(WebServiceUtil.dateFormat.format(DateUtils.addMinutes(new Date(), Integer.parseInt(GetSessionExipreTime())))));
                session.setExpired(false);
                String tvalue = "";
                for (int i = 0; i < 12; i++) {
                    Random rnd = new Random();
                    int a = rnd.nextInt(10);
                    tvalue += a;
                }
                session.setToken(tvalue);
                GeneralDao.Instance.saveOrUpdate(session);
                wsmodel.setToken(tvalue);
                if (wsmodel.getSecurityparams() != null) {
                    customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
                    customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
                    GeneralDao.Instance.saveOrUpdate(customer);
                }


                logger.info("Customer Verified & Device Session Generated successfully!");
                String age = "";
                if (Util.hasText(customer.getDateofbirth())) {
                    Date dob = WebServiceUtil.dobFormat.parse(customer.getDateofbirth());
                    age = "" + WebServiceUtil.calculateAge(dob);
                }

                //AdvertisementHandler.getAdvertisements(CustomerType.CUSTOMER, AdvertisementCategory.LOGIN,age, customer.getProduct(),   wsmodel); // Added by Affan on 5-May-23
                wsmodel.setTotalcount((customer.getUnreadnotifcount() != null) ? customer.getUnreadnotifcount() + "" : "0");
                wsmodel.setKycstatus(GlobalContext.getInstance().getKYCStatusDetails(customer.getKycstatus()).getDescription());
                wsmodel.setQrid(customer.getUserid());
                wsmodel.setLanguage(customer.getLanguage());
                wsmodel.setNotiflanguage(customer.getNotiflanguage());
                wsmodel.setPermissions(customer.getPermissions());
                wsmodel.setUsername(customer.getFirstname() + " " + customer.getLastname());

                if (Util.hasText(customer.getTermsandcondition()) && customer.getTermsandcondition().equals("1")) {
                    customer.setFirstlogin(false);
                    GeneralDao.Instance.saveOrUpdate(customer);
                    wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                } else {
                    logger.info("Customer LoggedIn, should accept Terms&Conditions to proceed...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_TERMS_CONDITIONS_REQ);
                }


                return wsmodel;
            }
            else {
                if (customer != null && (customer.getHaswallet() != null && customer.getHaswallet()) && (customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) && customer.getStatus().equals(CustomerStatus.ACTIVE)) {
                    wsmodel.setMobilenumber(customer.getMobilenumber());
                    wsmodel.setFirstname(customer.getFirstname());
                    wsmodel.setLastname(customer.getLastname());
                    wsmodel.setEmailaddress(customer.getEmailaddress());
                    if(Util.hasText(customer.getLanguage()))
                    {
                        wsmodel.setLanguage(customer.getLanguage());
                    }
                    if(Util.hasText(customer.getNotiflanguage()))
                    {
                        wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    }

                    logger.info("Verifying Prerequisites for Customer LogIn....");

                    if (!VerifyLoginPrerequisites(wsmodel, customer)) {

                        logger.error("Failed to Verify Login Prerequisites");
                        wsmodel.setRespcode(!(Util.hasText(wsmodel.getRespcode())) ? ISOResponseCodes.MW_PERMISSION_DENIED : wsmodel.getRespcode());
                        return wsmodel;
                    }


                    logger.info("Prerequisites verified for Customer LogIn....");

                    if (!customer.getPassword().equals(WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim()))) //Raza adding 03032021
                    {
                        logger.error("Migrated Customer Invalid Password [" + wsmodel.getPassword().trim() + "] for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");

                        customer.setRemainingloginretries((Long.parseLong(customer.getRemainingloginretries()) - 1) + "");
                        if (Integer.parseInt(customer.getRemainingloginretries()) < 1) {
                            customer.setStatus(CustomerStatus.FAILED_LOGIN_ATTEMPTS_BLOCK);

                        /*String servicename = wsmodel.getServicename();
                        wsmodel.setServicename("UpdateProfile");
                        wsmodel.setStatus(CustomerStatus.FAILED_LOGIN_ATTEMPTS_BLOCK);
                        wsmodel.setUserid(customer.getUserid());
                        SendToOpenAPI(wsmodel);
                        wsmodel.setServicename(servicename);

                        if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
                        {
                            logger.info("Customer Login retries exhuasted Status updated on Wallet!");
                        }
                        else
                        {
                            logger.error("Failed to update Customer Login retries exhuasted Status on Wallet, ignoring...");
                        }*/
                        }
                        GeneralDao.Instance.saveOrUpdate(customer);

                        wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                        return wsmodel;
                    }
                    logger.error("Migrated Customer found with wallet, RE-KYC required...");

                    customer.setRemainingloginretries(wsmodel.getMaxloginattempts());
                    GeneralDao.Instance.saveOrUpdate(customer);
                    //customer.setPassword(WSEncrptionUtil.EncryptAppPassword(customer.getPassword()));
                    //GeneralDao.Instance.saveOrUpdate(customer);

                    if(!otpconfirmed)
                    {
                        try {
                            if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    return wsmodel;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Exception caght while sending SMS, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }
                        try {
                            if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                    && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                            }
                        } catch (Exception e) {
                            logger.error("Exception caght while sending email, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }
                        wsmodel.setRespcode(((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled()) ? ISOResponseCodes.MW_OTP_REQUIRED : ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED));
                    } else {
                        if (!GenerateAppSession(wsmodel, customer)) {
                            logger.error("Failed to generate Session for Migrated RE-KYC, rejecting...");
                            return wsmodel;
                        }
                        customer.setIsmigratedactive(false);
                        customer.setTermsandcondition("1");
                        customer.setRemainingloginretries(Util.hasText(wsmodel.getMaxloginattempts()) ? wsmodel.getMaxloginattempts() : "3");
                        GeneralDao.Instance.saveOrUpdate(customer);
                        String age = "";
                        if (Util.hasText(customer.getDateofbirth())) {
                            Date dob = WebServiceUtil.dobFormat.parse(customer.getDateofbirth());
                            age = "" + WebServiceUtil.calculateAge(dob);
                        }

                        // AdvertisementHandler.getAdvertisements(CustomerType.CUSTOMER, AdvertisementCategory.LOGIN,age, customer.getProduct(),   wsmodel); // Added by Affan on 5-May-23
                        wsmodel.setTotalcount((customer.getUnreadnotifcount() != null) ? customer.getUnreadnotifcount() + "" : "0");
                        wsmodel.setKycstatus(Util.hasText(customer.getKycstatus()) ? GlobalContext.getInstance().getKYCStatusDetails(customer.getKycstatus()).getDescription() : GlobalContext.getInstance().getKYCStatusDetails(CMSKYCStatus.LIGHT).getDescription());
                        wsmodel.setQrid(Util.hasText(customer.getUserid()) ? customer.getUserid() : wsmodel.getMobilenumber());
                        wsmodel.setLanguage(Util.hasText(customer.getLanguage()) ? customer.getLanguage() : Util.getDefaultMobileAppLanguage());
                        wsmodel.setNotiflanguage(Util.hasText(customer.getNotiflanguage()) ? customer.getNotiflanguage() : Util.getDefaultMobileAppLanguage());
                        wsmodel.setPermissions(Util.hasText(customer.getPermissions()) ? customer.getPermissions() : "10111111111");
                        wsmodel.setUsername(customer.getFirstname() + " " + customer.getLastname());


                        if(systemDeviceBindingEnabled() && customer.getDevicebindingenabled()){

                        }
                        else{
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                            //wsmodel.setRespcode(ISOResponseCodes.MIGRATED_REKYC_REQ);

                            if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //Raza not rejecting on failure
                                //return wsmodel;
                            }

                            if (!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                            }

                            if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //Raza not rejecting on failure
                                //return wsmodel;
                            }
                        }




                    }

                    return wsmodel;
                }
                else if (customer != null && (customer.getHaswallet() != null && !customer.getHaswallet()) && (customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) && customer.getStatus().equals(CustomerStatus.ACTIVE))
                {
                    wsmodel.setMobilenumber(customer.getMobilenumber());
                    wsmodel.setFirstname(customer.getFirstname());
                    wsmodel.setLastname(customer.getLastname());
                    wsmodel.setMiddlename(Util.hasText(customer.getMiddlename()) ? customer.getMiddlename() : customer.getFirstname());
                    wsmodel.setEmailaddress(customer.getEmailaddress());
                    if (!customer.getPassword().equals(wsmodel.getPassword().trim())) //WALEED adding FOR CLEAR PASSWORD VERIFICATION
                    {
                        logger.error("Migrated Customer Invalid Password [" + wsmodel.getPassword().trim() + "] for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                        return wsmodel;
                    }
                    logger.error("Migrated Customer found without wallet, Registeration required...");
                    //customer.setPassword(WSEncrptionUtil.EncryptAppPassword(customer.getPassword()));
                    //GeneralDao.Instance.saveOrUpdate(customer);

                    if (customer.getMigratebalanceenabled() != null && customer.getMigratebalanceenabled()) //Raza adding for Migration Utility...
                    {
                        logger.info("Skipping OTP of Customer [" + customer.getMobilenumber() + "] for Migration....");
                        otpconfirmed = true;
                    }

                    if (!otpconfirmed) {
                        try {
                            if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                    //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    //return wsmodel;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Exception caght while sending SMS, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }
                        try {
                            if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                    && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                    && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                            }
                        } catch (Exception e) {
                            logger.error("Exception caght while sending email, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }

                        wsmodel.setRespcode(((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled()) ? ISOResponseCodes.MW_OTP_REQUIRED : ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED));
                    }
                    else {
                        wsmodel.setPartialflag("true");
                        wsmodel.setAdvanceflag("true");
                        wsmodel.setFirstname(customer.getFirstname());
                        wsmodel.setLastname(customer.getLastname());
                        wsmodel.setAddress(customer.getAddress());
                        wsmodel.setDateofbirth(customer.getDateofbirth());
                        wsmodel.setGender(customer.getGender());
                        if (Util.hasText(customer.getCnic())) {
                            wsmodel.setCnic(customer.getCnic());
                        }
//                        else{
////                          wsmodel.setCnic(customer.getMobilenumber()); //Commented by waleed
//                            wsmodel.setCnic(" ");
//                        }

                        if (Util.hasText(customer.getIdentificationtype())) {
                            wsmodel.setIdentificationtype(customer.getIdentificationtype());
                        } else {
//                          wsmodel.setIdentificationtype(customer.getIdentificationtype()); //Commented by waleed
                            wsmodel.setIdentificationtype(null);
                        }
                        wsmodel.setEmailaddress(customer.getEmailaddress());
                        wsmodel.setCnicpicturefront(customer.getCnicpicturefront());
                        wsmodel.setCnicpictureback(customer.getCnicpictureback());
                        wsmodel.setCountry(customer.getCountry());
                        //wsmodel.setProvince(customer.getpro());
                        //wsmodel.setCity(customer.getCnicpictureback());
                        //wsmodel.setMunicipality(customer.getCnicpictureback());
                        //wsmodel.setNationality(customer.getCnicpictureback());

                        //Waleed adding these fields only for migration.
                        wsmodel.setMonthlyincome(customer.getMonthlyincome());
                        wsmodel.setMonthlyexpenditure(customer.getMonthlyexpenditure());
                        wsmodel.setOccupation(customer.getOccupation());
                        wsmodel.setCustcomments(customer.getCustcomments());
                        wsmodel.setRelationshipcode(customer.getRelationshipcode());
                        if (Util.hasText(customer.getMig_cdfbalance()) && Util.hasText(customer.getMig_usdbalance())) {
                            wsmodel.setMig_cdfbalance(Util.getISOAmountFromDecimal(customer.getMig_cdfbalance()));
                            wsmodel.setMig_usdbalance(Util.getISOAmountFromDecimal(customer.getMig_usdbalance()));
                        }

                        //Waleed adding these fields only for migration.

                        wsmodel.setUsername(customer.getUsername());
                        wsmodel.setLanguage(customer.getLanguage());
                        wsmodel.setNotiflanguage(customer.getNotiflanguage());
                        String servicename = wsmodel.getServicename();
                        wsmodel.setServicename("CreateWallet");
                        MWWSOperation.ExecuteCreateWalletRequest(wsmodel);
                        wsmodel.setServicename(servicename);
                        if (!wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                            logger.error("Failed to Create Wallet Profile for migrated customer, rejecting...");
                            return wsmodel;
                        }
                        logger.info("Migrated Customer Wallet Profile Created, moving forward...");

                        /*if(!GenerateAppSession(wsmodel, customer))
                        {
                            logger.error("Failed to generate Session For Migrated SignUp, rejecting...");
                            return wsmodel;
                        }*/
                        customer.setIsmigratedactive(false);
                        customer.setTermsandcondition("1");
                        GeneralDao.Instance.saveOrUpdate(customer);
                        String age = "";
                        if (Util.hasText(customer.getDateofbirth())) {
                            Date dob = WebServiceUtil.dobFormat.parse(customer.getDateofbirth());
                            age = "" + WebServiceUtil.calculateAge(dob);
                        }
                        //AdvertisementHandler.getAdvertisements(CustomerType.CUSTOMER, AdvertisementCategory.LOGIN,age, customer.getProduct(),   wsmodel); // Added by Affan on 5-May-23
                        wsmodel.setTotalcount((customer.getUnreadnotifcount() != null) ? customer.getUnreadnotifcount() + "" : "0");
                        wsmodel.setKycstatus(Util.hasText(customer.getKycstatus()) ? GlobalContext.getInstance().getKYCStatusDetails(customer.getKycstatus()).getDescription() : GlobalContext.getInstance().getKYCStatusDetails(CMSKYCStatus.LIGHT).getDescription());
                        wsmodel.setQrid(Util.hasText(customer.getUserid()) ? customer.getUserid() : wsmodel.getMobilenumber());
                        wsmodel.setLanguage(Util.hasText(customer.getLanguage()) ? customer.getLanguage() : Util.getDefaultMobileAppLanguage());
                        wsmodel.setNotiflanguage(Util.hasText(customer.getNotiflanguage()) ? customer.getNotiflanguage() : Util.getDefaultMobileAppLanguage());
                        wsmodel.setPermissions(Util.hasText(customer.getPermissions()) ? customer.getPermissions() : "10111111111");
                        wsmodel.setUsername(customer.getFirstname() + " " + customer.getLastname());
                        //wsmodel.setRespcode(ISOResponseCodes.MIGRATED_SIGNUP_REQUIRED);

                        if(!customer.getDevicebindingenabled() && systemDeviceBindingEnabled())
                        {
                            if (otpconfirmed) {

                            }
                            else{
                            logger.info("Device Binding Enabled but Customer has it disabled, verifying OTP...");
                                try {
                                    if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                            && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                        if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_BIND_DEVICE, customer.getMobilenumber(), wsmodel)) {
                                            logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
                                            //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                            //return wsmodel;
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.error("Exception caght while sending SMS, ignoring...");
                                    logger.error(WebServiceUtil.getStrException(e));
                                }
                            //Raza geenrating and sending SMS for LogIn from NewDevice end
                                try {
                                    if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                            && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                            && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_BIND_DEVICE, customer.getEmailaddress(), wsmodel)) {
                                        logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                                    }
                                } catch (Exception e) {
                                    logger.error("Exception caght while sending email, ignoring...");
                                    logger.error(WebServiceUtil.getStrException(e));
                                }

							//TODO: Raza REMOVE ME TESTING ONLY
                            if (Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007")) {
                                if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {
                                    logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                                }
                            } else if (wsmodel.getSecurityparams() != null && Util.hasText(wsmodel.getSecurityparams().getDeviceid()) && wsmodel.getSecurityparams().getDeviceid().equals("10cbb8712e76027e")) {
                                if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, ((customer.getDevicebindingenabled() != null && customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) ? SMSCategory.VERIFY_MOBILE_DEVICE : SMSCategory.VERIFY_BIND_DEVICE), customer.getMobilenumber(), wsmodel)) {

                                    logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                                }
                            }
                            //TODO: Raza REMOVE ME TESTING ONLY

                            wsmodel.setRespcode(ISOResponseCodes.MW_DEVC_BIND_OTP_REQUIRED);
                            return wsmodel;
                        }
                        }

                        else{
                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                            if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "] Migrated LogIn, ignoring...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //Raza not rejecting on failure
                                //return wsmodel;
                            }

                            if (!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                            }

                            if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "] Migrated LogIn, ignoring...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //Raza not rejecting on failure
                                //return wsmodel;
                            }

                            try {
                                if (!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, SMSCategory.LOGIN_MIGRATED, customer.getEmailaddress(), wsmodel)) {
                                    logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");
                                }
                            }
                            catch (Exception e){
                                logger.error("Exception caght while sending email, ignoring...");
                                logger.error(WebServiceUtil.getStrException(e));
                            }
                        }

                    }

                    return wsmodel;
                }
                else if (customer != null && !customer.getStatus().equals(CustomerStatus.ACTIVE))
                {
                    if (customer.getStatus().equals(CustomerStatus.FAILED_LOGIN_ATTEMPTS_BLOCK)) {
                        logger.error("Customer [" + customer.getMobilenumber() + "] temp blocked due to login failed attempts, rejecting...");
                    } else {
                        logger.error("Customer [" + customer.getMobilenumber() + "] found with inactive status, rejecting...");
                    }
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
                    return wsmodel;
                }
                logger.error("Inactive or Customer not Found with MobileNumber [" + wsmodel.getMobilenumber() + "] or UserName [" + wsmodel.getUsername() + "], Password [" + (wsmodel.getPassword().trim()) + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteTitleFetchRequest(AppWsEntity wsmodel) {
        try {
            logger.info("Executing [{}] request", wsmodel.getServicename());

            return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
        } catch (Exception e) {
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity executePrivacyPolicy(AppWsEntity wsmodel)
    {

        try {
            logger.info("Executing " + wsmodel.getServicename() + " Request...");

            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            String lang;

            if(Util.hasText(wsmodel.getLanguage())){
                lang = wsmodel.getLanguage();
            } else if (Util.hasText(wsmodel.getNotiflanguage())) {
                lang = wsmodel.getNotiflanguage();
            } else {
                lang = "ENG";
            }

            dbQuery = "from " + PrivacyPolicy.class.getName() + " pp where pp.lang= :LANG";
            params = new HashMap<String, Object>();
            params.put("LANG", lang);

            PrivacyPolicy privacyPolicy = (PrivacyPolicy) GeneralDao.Instance.findObject(dbQuery, params);

            if(privacyPolicy == null){
                wsmodel.setRespcode(ISOResponseCodes.DATA_NOT_FOUND);
            } else {
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }

            wsmodel.setPrivacypolicy(privacyPolicy);

            return wsmodel;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }


    public static AppWsEntity ExecuteLogOutRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing " + wsmodel.getServicename() + " Request...");

            String dbQuery;
            Map<String, Object> params;

            //Verify MobileNumber
            if (Util.hasText(wsmodel.getMobilenumber())) {
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (customer != null) {
                    logger.info("Customer found, getting session...");
                    if(Util.hasText(customer.getLanguage()))
                    {
                        wsmodel.setLanguage(customer.getLanguage());
                    }
                    if(Util.hasText(customer.getNotiflanguage()))
                    {
                        wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    }
//                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + /*" and c.expiredatetime > :EXPIRY " +*/ " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where ";
                    params = new HashMap<String, Object>();
                    if (wsmodel.getSecurityparams().getDeviceid() != null) {
                        dbQuery += "c.device.deviceid= :DEVC " + /*" and c.expiredatetime > :EXPIRY " +*/ " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
                    } else {
                        dbQuery += "c.customer= :CUST " + /*" and c.expiredatetime > :EXPIRY " +*/ " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                        params.put("CUST", customer);
                    }
                    params.put("ISEXPIRED", false);
                    //params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                    List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

                    if (sessionlist != null && sessionlist.size() > 0) {
                        logger.info("User active session found, signing out...");

                        for (MWDeviceSessionLog sess : sessionlist) {
                            sess.setExpired(true);
                            GeneralDao.Instance.saveOrUpdate(sess);
                        }
                        wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                        return wsmodel;
                    } else {
                        logger.error("No Session Found for MobileNumber [" + wsmodel.getMobilenumber() + "] with Token [" + wsmodel.getToken() + "] cannot LogOut, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                        return wsmodel;
                    }
                } else {
                    logger.error("Customer not Found with MobileNumber [" + wsmodel.getMobilenumber() + "], Password [" + (wsmodel.getPassword().trim()) + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }
            } else if (wsmodel.getSecurityparams() != null && Util.hasText(wsmodel.getSecurityparams().getDeviceid())) {
                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
                params = new HashMap<String, Object>();
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

                MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);
                if (device != null) {
                    logger.info("Device found, getting session...");
                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + /*" and c.expiredatetime > :EXPIRY " +*/ " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                    params = new HashMap<String, Object>();
                    params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
                    params.put("ISEXPIRED", false);
                    //params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                    List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

                    if (sessionlist != null && sessionlist.size() > 0) {
                        logger.info("User active session found, signing out...");

                        for (MWDeviceSessionLog sess : sessionlist) {
                            sess.setExpired(true);
                            GeneralDao.Instance.saveOrUpdate(sess);
                        }
                        wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                        return wsmodel;
                    } else {
                        logger.error("No Session Found for Device Id [" + wsmodel.getSecurityparams().getDeviceid() + "] with Token [" + wsmodel.getToken() + "] cannot LogOut, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                        return wsmodel;
                    }
                } else {
                    logger.error("Device not Found with DeviceId [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }
            } else {
                logger.error("No Identifier found for logout, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                return wsmodel;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteChangePasswordRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ChangePassword Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<String, Object>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session
            if (customer != null) {
                logger.info("Customer found, verifying session...");
                if (!ValidateUserSession(wsmodel, customer)) {
                    logger.error("Failed to Validate Session, rejecting...");
                    return wsmodel;
                }

                if (!customer.getPassword().equals(WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim()))) {
                    logger.error("Invalid Old Password, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OLDPASSWORD);
                    return wsmodel;
                }

//                if(customer.getPassword().equals(WSEncrptionUtil.DecryptandEncryptAppPassword(wsmodel.getNewpassword().trim())))
//                {
//                    logger.error("New Password cannot be same as Old Password, rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_PASSWORD);
//                    return wsmodel;
//                }

                logger.info("Customer Change Password prerequisite verified, changing password...");
                String temppass = WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getNewpassword().trim());

                if (!Util.hasText(temppass)) {
                    logger.error("Invalid password receveid from App, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                    return wsmodel;
                } else if (customer.getPassword().equals(temppass)) {
                    logger.error("New Password cannot be same as Old Password, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_PASSWORD);
                    return wsmodel;
                }

                customer.setPassword(temppass);
                customer.setFirstlogin(false);
                customer.setResetcredentials(false);
                GeneralDao.Instance.saveOrUpdate(customer);

                CheckandSendSMS(wsmodel);

                CheckandSendNotification(wsmodel);

                /*if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return wsmodel;
                }
                //TODO: Raza REMOVE ME TESTING ONLY
                if(Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007"))
                {
                    if(!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                    {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY
                */

                //Marking previous session expired, User should LogIn with new credentials
                if (!CloseUserSessions(wsmodel, customer)) {
                    logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Invalid UserName or Password, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteChangeUserNameRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ChangeUserName Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<String, Object>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session
            if (customer != null) {
                logger.info("Customer found, verifying session...");
                if (!ValidateUserSession(wsmodel, customer)) {
                    logger.error("Failed to Validate Session, rejecting...");
                    return wsmodel;
                }

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :USER ";
                params = new HashMap<String, Object>();
                params.put("USER", wsmodel.getNewusername());

                MWCustomer tempcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (tempcustomer != null) {
                    logger.error("Duplicate UserName found, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_USERNAME);
                    return wsmodel;
                }


                customer.setUsername(wsmodel.getNewusername());
                GeneralDao.Instance.saveOrUpdate(customer);

                CheckandSendSMS(wsmodel);

                CheckandSendNotification(wsmodel);

                /*if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.USERNAME_CHANGED, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return wsmodel;
                }
                //TODO: Raza REMOVE ME TESTING ONLY
                if(Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007"))
                {
                    if(!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.USERNAME_CHANGED, customer.getMobilenumber(), wsmodel))
                    {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY*/

                //Marking previous session expired, User should LogIn with new credentials
                if (!CloseUserSessions(wsmodel, customer)) {
                    logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Invalid UserName or Password, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteChangeResetPasswordRequest(AppWsEntity wsmodel) {
        try {
            logger.info("Executing ChangeResetPassword Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session

            if (customer == null) {
                logger.error("Invalid UserName or Password, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }


            logger.info("Customer found, verifying session...");

            //TODO: Raza also verify if no active sesison ; update this Logic
            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
            params = new HashMap<>();
            params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
            params.put("ISEXPIRED", false);
            params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

            List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

            if (sessionlist != null && sessionlist.size() > 0) {
                logger.info("Active session token found for device cannot ChangeResetPassword, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
                return wsmodel;
            } else {
                dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.customer.mobilenumber= :MOB " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                params = new HashMap<>();
                params.put("MOB", wsmodel.getMobilenumber());
                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                sessionlist = GeneralDao.Instance.find(dbQuery, params);
                if (sessionlist != null && sessionlist.size() > 0) {
                    logger.info("Active session token found for customer cannot ChangeResetPassword, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
                    return wsmodel;
                }
            }
            logger.info("Customer and device verifies, changing reset password....");
            //TODO: Raza also verify if no active sesison ; update this Logic

            if (customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) {
                if (customer.getPassword().equals(WSEncryptionUtil.DecryptAppPassword(wsmodel.getNewpassword().trim()))) {
                    logger.error("New Password cannot be same as Old Password, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_PASSWORD);
                    return wsmodel;
                }
            }

            String temppass = WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getNewpassword().trim());

            if (!Util.hasText(temppass)) {
                logger.error("Invalid New Password received from App, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                return wsmodel;
            }

            if (customer.getPassword().equals(temppass)) {
                {
                    logger.error("New Password cannot be same as Old Password, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_PASSWORD);
                    return wsmodel;
                }
            }

            if (customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) {
                customer.setPassword(wsmodel.getNewpassword().trim());
            } else {
                customer.setPassword(temppass);
            }

            customer.setFirstlogin(false);
            customer.setResetcredentials(false);
            GeneralDao.Instance.saveOrUpdate(customer);

                /*if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return wsmodel;
                }
                //TODO: Raza REMOVE ME TESTING ONLY
                if(Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007"))
                {
                    if(!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                    {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY*/

            CheckandSendSMS(wsmodel);

            CheckandSendNotification(wsmodel);

            //Marking previous session expired, User should LogIn with new credentials
            if (!CloseUserSessions(wsmodel, customer)) {
                logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
            }

            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
            return wsmodel;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteChangeResetUserNameRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ChangeResetUserName Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<String, Object>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session
            if (customer != null) //&& customer.getResetcredentials())
            {
                logger.info("Customer found, verifying session...");

                //TODO: Raza also verify if no active sesison ; update this Logic
                dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                params = new HashMap<String, Object>();
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

                if (sessionlist != null && sessionlist.size() > 0) {
                    logger.info("Active session token found for device cannot ChangeResetUserName, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
                    return wsmodel;
                } else {
                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.customer.mobilenumber= :MOB " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getMobilenumber());
                    params.put("ISEXPIRED", false);
                    params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                    sessionlist = GeneralDao.Instance.find(dbQuery, params);
                    if (sessionlist != null && sessionlist.size() > 0) {
                        logger.info("Active session token found for customer cannot ChangeResetUserName, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
                        return wsmodel;
                    }
                }
                logger.info("Customer and device verifies, changing reset UserName....");
                //TODO: Raza also verify if no active sesison ; update this Logic

                customer.setUsername(wsmodel.getNewusername());
                customer.setFirstlogin(false);
                customer.setResetcredentials(false);
                GeneralDao.Instance.saveOrUpdate(customer);

                /*if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return wsmodel;
                }
                //TODO: Raza REMOVE ME TESTING ONLY
                if(Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007"))
                {
                    if(!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                    {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY*/

                CheckandSendSMS(wsmodel);

                CheckandSendNotification(wsmodel);

                //Marking previous session expired, User should LogIn with new credentials
                if (!CloseUserSessions(wsmodel, customer)) {
                    logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer not found against Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteChangeUserNamePasswordRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ChangeUserNamePassword Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<String, Object>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session
            if (customer != null) {
                logger.info("Customer found, verifying session...");
                if (!ValidateUserSession(wsmodel, customer)) {
                    logger.error("Failed to Validate Session, rejecting...");
                    return wsmodel;
                }

                if (!customer.getPassword().equals(WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim()))) {
                    logger.error("Invalid Old Password, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OLDPASSWORD);
                    return wsmodel;
                }

                logger.info("Customer Change UserName & Password prerequisite verified, changing password...");
                String temppass = WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getNewpassword().trim());
                if (!Util.hasText(temppass)) {
                    logger.error("Invalid New password received from App, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                    return wsmodel;
                } else if (customer.getPassword().equals(temppass)) {
                    logger.error("New Password cannot be same as old password, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_PASSWORD);
                    return wsmodel;
                }

                customer.setPassword(temppass);
                customer.setUsername(wsmodel.getUsername().trim());
                customer.setFirstlogin(false);
                customer.setResetcredentials(false);
                GeneralDao.Instance.saveOrUpdate(customer);

                CheckandSendSMS(wsmodel);

                CheckandSendNotification(wsmodel);

                /*if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return wsmodel;
                }
                //TODO: Raza REMOVE ME TESTING ONLY
                if(Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007"))
                {
                    if(!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PASSWORD_CHANGED, customer.getMobilenumber(), wsmodel))
                    {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY*/

                //Marking previous session expired, User should LogIn with new credentials
                if (!CloseUserSessions(wsmodel, customer)) {
                    logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Invalid UserName or Password, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteResetPasswordRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ResetPassword Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            if (!ValidateUserandDeviceforReset(wsmodel)) {
                logger.error("Failed to validate prerequisite for Reset password, rejecting...");
                return wsmodel;
            }

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<String, Object>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session
            if (customer != null) // && !customer.getResetcredentials())
            {
                logger.info("Customer found, resetting...");
                customer.setResetcredentials(true);
                GeneralDao.Instance.saveOrUpdate(customer);
                wsmodel.setLanguage(customer.getLanguage());
                wsmodel.setFirstname(customer.getFirstname());
                wsmodel.setLastname(customer.getLastname());
                wsmodel.setNotiflanguage(customer.getNotiflanguage());
                if(!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, SMSCategory.FORGOT_PASSWORD, customer.getMobilenumber(), wsmodel))
                {
                    logger.error("Failed to create & Send OTP for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return wsmodel;
                }

                // Added by Affan on 12-June-23
                try {
                    if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                            && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                            && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, SMSCategory.FORGOT_PASSWORD, customer.getEmailaddress(), wsmodel)) {
                        logger.error("Failed to create & Send Email for Address [" + wsmodel.getDestemailaddress() + "], rejecting...");
                        return wsmodel;
                    }
                } catch (Exception e) {
                    logger.error("Exception caght while sending email, ignoring...");
                    logger.error(WebServiceUtil.getStrException(e));
                }
                // Added by Affan on 12-June-23

                //TODO: Raza REMOVE ME TESTING ONLY
                if (Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007")) {
                    if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.FORGOT_PASSWORD, customer.getMobilenumber(), wsmodel)) {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY


                //Raza closing session //TODO: have to verify if deivce is changes or other scenarios
                if (!CloseUserSessions(wsmodel, customer)) {
                    logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                if (customer != null) {
                    logger.error("Customer not allowed to reset, already in Reset State, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                    return wsmodel;
                }
                logger.error("Customer not found against mobilenumber [" + wsmodel.getMobilenumber() + ", rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteResetUserNameRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ResetUserName Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            if (!ValidateUserandDeviceforReset(wsmodel)) {
                logger.error("Failed to validate prerequisite for Reset UserName, rejecting...");
                return wsmodel;
            }

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";
            params = new HashMap<String, Object>();
            params.put("USER", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params); //Raza not validating password, will validate session
            if (customer != null) // && !customer.getResetcredentials())
            {
                logger.info("Customer found, resetting...");
                customer.setResetcredentials(true);
                //wsmodel.setUsername(customer.getUsername());
                GeneralDao.Instance.saveOrUpdate(customer);
                wsmodel.setNotiflanguage(customer.getNotiflanguage());
//                if(!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.FORGOT_USERNAME, customer.getMobilenumber(), wsmodel))
//                {
//                    logger.error("Failed to create & Send OTP for Mobile [" + customer.getMobilenumber() + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
//                    return wsmodel;
//                }

                /*//TODO: Raza REMOVE ME TESTING ONLY
                if(Util.hasText(customer.getMobilenumber()) && customer.getMobilenumber().contains("007007"))
                {
                    if(!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.FORGOT_USERNAME, customer.getMobilenumber(), wsmodel))
                    {
                        logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], rejecting...");
                    }
                }
                //TODO: Raza REMOVE ME TESTING ONLY*/


                //Raza closing session //TODO: have to verify if deivce is changes or other scenarios
                if (!CloseUserSessions(wsmodel, customer)) {
                    logger.error("Failed to close existing session for customer [" + customer.getMobilenumber() + "], ignoring...");
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                /*if(customer != null)
                {
                    logger.error("Customer not allowed to reset, already in Reset State, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                    return wsmodel;
                }*/
                logger.error("Customer not found against mobilenumber [" + wsmodel.getMobilenumber() + ", rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity executeBulkCreateWalletRequest(AppWsEntity wsModel) {
        try {
            logger.info("Executing bulk create wallet request...");


            if (!Util.hasText(wsModel.getFilename())) {
                logger.error("Filename not found in {} request, rejecting...", wsModel.getServicename());
                wsModel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsModel;
            }

            if (isDuplicateFileForBulkOnboard(wsModel)) {
                logger.error("Duplicate FileName [{}] found, rejecting...", wsModel.getFilename());
                wsModel.setRespcode(ISOResponseCodes.DUPLICATE_TRANSACTION);
                return wsModel;
            }

            if (wsModel.getOnboardCustList() == null && !wsModel.getOnboardCustList().isEmpty()) {
                logger.error("No customer list found in request, rejecting...");
                wsModel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsModel;
            }

            List<String> allowedKYC = Arrays.asList(
                    CMSKYCStatus.STANDARD,
                    CMSKYCStatus.LIGHT
            );

            if (!Util.hasText(wsModel.getKycstatus()) || !allowedKYC.contains(wsModel.getKycstatus())) {
                logger.error("KYC Status not found, rejecting...");
                wsModel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsModel;
            }

//            Thread bulkThread = new Thread(new BulkOnBoardCustomer(wsModel));
//            bulkThread.setName("BulkOnBoardingCustomerHandler: " + wsModel.getTranrefnumber());
//            bulkThread.setDaemon(false);
//            logger.info("running thread..");
//            bulkThread.start();
            logger.info("ended thread..");
            logger.info("Sending response to user without waiting for bulk wallet creation to complete...");
            wsModel.setRespcode(ISOResponseCodes.APPROVED);
            return wsModel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while executing bulk create wallet request [{}]", wsModel.getServicename());
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }
    }

    public static AppWsEntity executeBulkDisbursementRequest(AppWsEntity wsModel) {
        String serviceName = wsModel.getServicename();
        String fileName = wsModel.getFilename();

        try {
            logger.info("Executing {} request...", serviceName);

            if (!Util.hasText(fileName)) {
                logger.error("Filename not found in {} request, rejecting...", serviceName);
                wsModel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsModel;
            }

            if (isDuplicateFile(wsModel)) {
                logger.error("Duplicate FileName [{}] found, rejecting...", fileName);
                wsModel.setRespcode(ISOResponseCodes.DUPLICATE_TRANSACTION);
                return wsModel;
            }

            if (wsModel.getBulkPayTransactionList() == null && !wsModel.getBulkPayTransactionList().isEmpty()) {
                logger.error("No bulk pay user list found in request, rejecting...");
                wsModel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsModel;
            }

//            Thread bulkPayThread = new Thread(new BulkPayCustomerThread(wsModel));
//            bulkPayThread.setName("BulkPayThread: " + wsModel.getTranrefnumber());
//            bulkPayThread.setDaemon(false);
//            bulkPayThread.start();
            logger.info("Sending response to user without waiting for {} to complete...", serviceName);
            wsModel.setRespcode(ISOResponseCodes.APPROVED);
            return wsModel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while executing {} request", serviceName);
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }
    }

    public static boolean isDuplicateFile(AppWsEntity wsModel) {
        try {
            logger.info("Checking if duplicate file exist with name [{}]...", wsModel.getFilename());

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + BulkPaySummary.class.getName() + " w where w.fileName = :FILENAME ";
            params = new HashMap<>();
            params.put("FILENAME", wsModel.getFilename());

            List<BulkPaySummary> summarylist = GeneralDao.Instance.find(dbQuery, params);

            return !summarylist.isEmpty();
        } catch (Exception e) {
            logger.error("Exception caught while checking duplicate filename [{}], rejecting...", wsModel.getFilename());
            logger.error(WebServiceUtil.getStrException(e));
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return false;
        }
    }

    public static boolean isDuplicateFileForBulkOnboard(AppWsEntity wsModel) {
        try {
            logger.info("Checking if duplicate file exist with name [{}]...", wsModel.getFilename());

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + BulkOnBoardingCustomerSummary.class.getName() + " w where w.filename = :FILENAME ";
            params = new HashMap<>();
            params.put("FILENAME", wsModel.getFilename());

            List<BulkPaySummary> summarylist = GeneralDao.Instance.find(dbQuery, params);

            return !summarylist.isEmpty();
        } catch (Exception e) {
            logger.error("Exception caught while checking duplicate filename [{}], rejecting...", wsModel.getFilename());
            logger.error(WebServiceUtil.getStrException(e));
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return false;
        }
    }

    public static AppWsEntity ExecuteBulkPayTransaction(AppWsEntity wsModel){
        String serviceName = wsModel.getServicename();
        try{
            String dbQuery;
            Map<String, Object> params;

            //Verify MobileNumber
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<>();
            params.put("MOBNO", wsModel.getDestmobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer == null && customer.getHaswallet() == null) {
                logger.error("Customer or wallet not found, rejecting...");
                wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsModel;
            }

            wsModel.setUserid(customer.getUserid());

            SendToOpenAPI(wsModel);

            return wsModel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while executing {} request", serviceName);
            wsModel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsModel;
        }
    }

    public static AppWsEntity ExecuteCreateWalletRequest(AppWsEntity wsmodel) {
        try {
            logger.info("Executing CreateWallet/SignUp Request...");

            if(!Util.hasText(wsmodel.getJourneyid())){
                logger.error("No journey ID found");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

//            wsmodel = TruIdHandler.fetchSession(wsmodel);

            if(wsmodel == null || !wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                logger.error("Error fetching truID session [{}]", wsmodel.getJourneyid());
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            String kyc = wsmodel.getApiversion(); // Checking for v2 only
            logger.info("KYC: [{}]",kyc);

            List<String> missingFields = Util.validateKYCRequiredFields(wsmodel, kyc);

            if (!missingFields.isEmpty()) {
                logger.error("Missing required fields for {} onboarding {}, rejecting...", kyc, missingFields);
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            if (wsmodel.getSecurityparams() != null && !Util.hasText(wsmodel.getSecurityparams().getDeviceid())){
                logger.error("Device ID not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            wsmodel.setTermsandcondition("true");

            MWCustomer customer = null;

            Boolean isValid = SystemConfig.isValidMobileNumber(wsmodel.getMobilenumber());

            if (!isValid) {
                logger.error("Rejecting Transaction due to Invalid format Mobile Number [" + wsmodel.getMobilenumber() + "]...");
                wsmodel.setRespcode(ISOResponseCodes.ERROR_INVALIDMOBILENUMBER);
                return wsmodel;
            }

            //Verify MobileNumber
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());


            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
                logger.error("Customer already exists with MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_MOBILE);
                return wsmodel;
            }


            if (Util.hasText(wsmodel.getUsername())) //Raza updating 25052021 - Duplicate Customer issue
            {
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :USR ";
                params = new HashMap<String, Object>();
                params.put("USR", wsmodel.getUsername());

                MWCustomer tmepcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (tmepcustomer != null && !tmepcustomer.getMobilenumber().equals(wsmodel.getMobilenumber())) {
                    logger.error("Customer already exists with Username [" + wsmodel.getUsername() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_USERNAME);
                    return wsmodel;
                }
            }


            if(customer == null)
            {
                //Verify Email
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.emailaddress= :EMAIL ";
                params = new HashMap<String, Object>();
                params.put("EMAIL", wsmodel.getEmailaddress());

                customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if(customer != null && customer.getHaswallet() != null && customer.getHaswallet())
                {
                    logger.error("Customer already exists with Email [" + wsmodel.getEmailaddress() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_EMAIL);
                    return wsmodel;
                }
            }

//            if (customer == null) {
//                //Verify Cnic
//                dbQuery = "from " + MWCustomer.class.getName() + " c where c.cnic= :CNIC ";
//                params = new HashMap<String, Object>();
//                params.put("CNIC", wsmodel.getIdentificationno().trim());
//
//                customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
//                if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
//                    logger.error("Customer already exists with Cnic [" + wsmodel.getIdentificationno() + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_IDENTIFICATIONNO);
//                    return wsmodel;
//                }
//            }

            //Verify Device
            //1) If it belongs to someone else --> RAW BANK says overwrite
            //2) If it belongs to someone else & have active session, reject
            //3) If device is changed, register new device against customer
            dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DVC ";
            params = new HashMap<String, Object>();
            params.put("DVC", wsmodel.getSecurityparams().getDeviceid()); //Raza if null it should'nt have come this far
            MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

            if (device != null) {
                logger.info("Existing Device [" + wsmodel.getSecurityparams().getDeviceid() + "] found for SignUp, verifying sessions...");
                if (!ValidateRegisterdDeviceForSignUp(wsmodel) && (customer != null && !customer.getIsmigratedactive())) {
                    logger.error("Cannot allow SignUp on device with Active Session, rejecting...");
                    return wsmodel;
                }
            }


            //TODO: Verify THIS start...
            //Raza adding if multiple devices comes with Same firebase, consider lastone as legit and mark rest of them as null...
            if (Util.hasText(wsmodel.getSecurityparams().getFirebasetoken())) {
                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.firebasetoken= :FIRE ";
                params = new HashMap<String, Object>();
                params.put("FIRE", wsmodel.getSecurityparams().getFirebasetoken());
                List<MWDeviceLog> firebasedevices = GeneralDao.Instance.find(dbQuery, params);

                if (firebasedevices != null && firebasedevices.size() > 0) {
                    for (MWDeviceLog dev : firebasedevices) {
                        dev.setFirebasetoken(null);
                        GeneralDao.Instance.saveOrUpdate(dev); //Raza Updating Firebase for Previous Devices if any; Will be updated on LogIn...
                    }
                }
            }
            //TODO: Verify THIS end...


            //Now Create Customer & Register Device
            if (customer == null) {
                customer = new MWCustomer();

                if (Util.hasText(wsmodel.getCustomerpicture())) {
                    customer.setCustomerpicture(wsmodel.getCustomerpicture());
                }
                if (Util.hasText(wsmodel.getCnicpicturefront())) {
                    customer.setCnicpicturefront(wsmodel.getCnicpicturefront());
                }
                if (Util.hasText(wsmodel.getCnicpictureback())) {
                    customer.setCnicpictureback(wsmodel.getCnicpictureback());
                }
                GeneralDao.Instance.save(customer.getPictures());
            }
            if (Util.hasText(wsmodel.getUsername())) //Raza adding 03032021
            {
                customer.setUsername(wsmodel.getUsername().trim());
            }
            customer.setMobilenumber(wsmodel.getMobilenumber());
            if (Util.hasText(wsmodel.getPassword())) //Raza adding 03032021
            {
                String temppass = WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim());
                if (!Util.hasText(temppass)) {
                    logger.error("Invalid password received from App, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                    return wsmodel;
                }
                customer.setPassword(temppass);
            }
            customer.setEmailaddress(wsmodel.getEmailaddress());
            customer.setCnic(wsmodel.getIdentificationno());
            customer.setIdentificationtype(wsmodel.getIdentificationtype());
            customer.setFirstlogin(true);
            customer.setHaswallet(false);
            customer.setResetcredentials(false);
            customer.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
            customer.setGender(wsmodel.getGender());
            customer.setTermsandcondition(Util.hasText(wsmodel.getTermsandcondition()) ? (wsmodel.getTermsandcondition().equals("true") ? "1" : "0") : "0");
            if (Util.hasText(wsmodel.getCustomerpicture())) {
                customer.setCustomerpicture(wsmodel.getCustomerpicture());
            }
            if (Util.hasText(wsmodel.getCnicpicturefront())) {
                customer.setCnicpicturefront(wsmodel.getCnicpicturefront());
            }
            if (Util.hasText(wsmodel.getCnicpictureback())) {
                customer.setCnicpictureback(wsmodel.getCnicpictureback());
            }

            if (Util.hasText(wsmodel.getDateofbirth())) {
                String date = Util.convertDate(wsmodel.getDateofbirth());
                wsmodel.setDateofbirth(date);
                customer.setDateofbirth(date);
            }

            if (Util.hasText(wsmodel.getIdexpiry())) {
                String date = Util.convertDate(wsmodel.getIdexpiry());
                wsmodel.setIdexpiry(date);
            }

            if (Util.hasText(wsmodel.getFirstname())) {
                customer.setFirstname(wsmodel.getFirstname().trim());
            }

            if (Util.hasText(wsmodel.getLastname())) {
                customer.setLastname(wsmodel.getLastname().trim());
            }

            if (Util.hasText(wsmodel.getMiddlename())) {
                customer.setMiddlename(wsmodel.getMiddlename().trim());
            }

            customer.setAddress(wsmodel.getAddress());
            customer.setKycstatus(wsmodel.getKycstatus());
            customer.setPermissions("101111111");
            customer.setLanguage(wsmodel.getLanguage());
            customer.setNotiflanguage(wsmodel.getLanguage());
            customer.setStatus("08");
            customer.setJourneyid(wsmodel.getJourneyid());
            customer.setLastrequesttime(WebServiceUtil.TransDateTimeFormat.format(new Date()));

            if (Util.hasText(wsmodel.getPartialflag()) && wsmodel.getPartialflag().equals("true")) {
                customer.setIsmigratedactive(false);
                customer.setIsmigrated(true);
            }

            if (device == null) {
                device = new MWDeviceLog();
                device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                device.setBaseintegrity(false);
                device.setCtsprofile(false);
                device.setIsrooted(false);
                device.setImei(wsmodel.getSecurityparams().getImei());
                device.setImsi(wsmodel.getSecurityparams().getImsi());
                device.setIccid(wsmodel.getSecurityparams().getIccid());
                device.setUuid(wsmodel.getSecurityparams().getUuid());
            }
            device.setCustomer(customer); //Update Customer of Device
            customer.setAlternateChannelFlag("0"); // Added by Affan on 2-NOV-23 - ADC
//            customer.setDevicebindingenabled(true);
            customer.setIccidbindingenabled(false);
            customer.setIccid(wsmodel.getSecurityparams().getIccid());
            GeneralDao.Instance.saveOrUpdate(customer);
            GeneralDao.Instance.saveOrUpdate(device);
            GeneralDao.Instance.flush(); //Raza flush to save Data before moving forward...

            if (Util.hasText(wsmodel.getSecretquestion1()) && Util.hasText(wsmodel.getSecretquestion2())) {
                logger.info("Adding Security Questions...");
                if (!AddSecurityQuestions(wsmodel, customer)) {
                    logger.error("Unable to Add Security Questions for cnic [" + wsmodel.getIdentificationno() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return wsmodel;
                }
            }
            else if (Util.hasText(wsmodel.getSecretquestion1()) || Util.hasText(wsmodel.getSecretquestion2())) {
                logger.info("Adding Security Questions for Migrated Customer...");
                if (!AddSecurityQuestions(wsmodel, customer)) {
                    logger.error("Unable to Add Security Questions for cnic [" + wsmodel.getIdentificationno() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return wsmodel;
                }
            }


            logger.info("Customer Registered on Middleware, will update Data on API Response...");

            ExecuteMWServiceRequest(wsmodel, false);

            if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                logger.info("Approved Response received from OpenAPI, updating Customer...");
                customer.setHaswallet(true);
                customer.setResetcredentials(false);
                customer.setFirstlogin(false);
                customer.setUserid(wsmodel.getUserid());
                customer.setCdfaccountid(wsmodel.getCdfacctid());
                customer.setUsdaccountid(wsmodel.getUsdacctid());
                customer.setStatus(CustomerStatus.ACTIVE); //12
                customer.setPermissions(wsmodel.getPermissions());
                customer.setStatus("00");
                if (Util.hasText(wsmodel.getProduct())) {
                    customer.setProduct(wsmodel.getProduct());
                }
                if (Util.hasText(wsmodel.getKycstatus())) {
                    customer.setKycstatus(wsmodel.getKycstatus());
                }

                if (Util.hasText(wsmodel.getCustomerid())) {
                    customer.setCustomerId(wsmodel.getCustomerid());
                }

                if (Util.hasText(wsmodel.getTermsandcondition()) && wsmodel.getTermsandcondition().equals("true")) {
                    customer.setTermsandcondition("1");
                } else {
                    customer.setTermsandcondition("0");
                }

                if (customer.getIsmigratedactive() == null || (customer.getIsmigratedactive() != null && !customer.getIsmigratedactive())) {
                    if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PROFILE_CREATED, customer.getMobilenumber(), wsmodel)) {
                        logger.error("Failed to create & Send SMS for Mobile [ {} ], rejecting...", customer.getMobilenumber());
                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        return wsmodel;
                    }

                    if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PROFILE_CREATED, customer.getMobilenumber(), wsmodel)) {
                        logger.error("Failed to create & Send Notification for Mobile [ {} ], rejecting...", customer.getMobilenumber());
                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        return wsmodel;
                    }

                    if (!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, SMSCategory.PROFILE_CREATED, customer.getEmailaddress(), wsmodel)) {
                        logger.error("Failed to create & Send Notification for Mobile [ {} ], rejecting...", customer.getMobilenumber());
                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        return wsmodel;
                    }
                }

                customer.setIsmigratedactive(false);
                if (wsmodel.getSecurityparams() != null) {
                    customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
                    customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
//                    customer.setDevicebindingenabled(true);
                }
                GeneralDao.Instance.saveOrUpdate(customer);


                String age = "";
                if (Util.hasText(customer.getDateofbirth())) {
                    Date dob = WebServiceUtil.dobFormat.parse(customer.getDateofbirth());
                    age = "" + WebServiceUtil.calculateAge(dob);
                }
                //AdvertisementHandler.getAdvertisements(CustomerType.CUSTOMER, AdvertisementCategory.PROFILECREATION,age, customer.getProduct(),   wsmodel); // Added by Affan on 5-May-23

                //17-05-2024
                //M.Hamza: Adding for whenever customer signup with new device then he gives session
                //but whenever customer signup through agent with old device then customer signup successfully but
                //he'll not to login because we'll not give a single session.

                String tvalue = "";
                for (int i = 0; i < 12; i++) {
                    Random rnd = new Random();
                    int a = rnd.nextInt(10);
                    tvalue += a;
                }

//                if(device == null) {
//
//                    MWDeviceSessionLog session = new MWDeviceSessionLog();
//                    session.setDevice(device);
//                    session.setCustomer(customer);
//                    session.setCreatedatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
//                    session.setExpiredatetime(Long.parseLong(WebServiceUtil.dateFormat.format(DateUtils.addMinutes(new Date(), Integer.parseInt(GetSessionExipreTime())))));
//                    if (Util.hasText(wsmodel.getPartialflag()) && wsmodel.getPartialflag().equals("true")) {
//                        session.setExpired(false);
//                    } else {
//                        session.setExpired(true);
//                    }
//
//                    session.setToken(tvalue);
//                    GeneralDao.Instance.saveOrUpdate(session);
//
//                }

                wsmodel.setToken(tvalue);
                wsmodel.setQrid(wsmodel.getUserid());

                if (wsmodel.getSecurityparams() != null) {
                    customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
                    customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
                    GeneralDao.Instance.saveOrUpdate(customer);
                }

                logger.info("Customer Registered & Device Session Generated successfully!");

                wsmodel.setCustomerpicture(null);
                wsmodel.setCnicpicturefront(null);
                wsmodel.setCnicpictureback(null);

//                //TODO: Abdul Khaliq, control (enable/disable) below implmentation with a System_config Flag
//                try{
//                    logger.info("Checking for already received Session ID Webhook from TruID...");
//
//                    dbQuery = "from " + MWPendingTruIdSessionLog.class.getName() + " c where c.sessionid= :SESS and c.status= :STATUS ";
//                    params = new HashMap<String, Object>();
//                    params.put("SESS", wsmodel.getJourneyid());
//                    params.put("STATUS", MWPendingTruIdSessionLog.PENDING);
//                    MWPendingTruIdSessionLog dbrecord = (MWPendingTruIdSessionLog)GeneralDao.Instance.findObject(dbQuery, params);
//
//                    if(dbrecord != null){
//                        logger.info("Webhook already received from TruId for SessionID [" + wsmodel.getJourneyid() + "], processing...");
//
//                        Thread truIDSessionThread = new Thread(new TruIdHandler( TruIDEntity.GetNewObject(wsmodel), customer, dbrecord));
//                        truIDSessionThread.setName("SignUpTruIDGetSessionProcess: " + wsmodel.getRrn());
//                        truIDSessionThread.setDaemon(false);
//                        logger.info("starting thread..");
//                        truIDSessionThread.start();
//                        logger.info("thread started..");
//                    }
//                    else{
//                        logger.info("No Webhook yet received from TruId for SessionID [" + wsmodel.getJourneyid() + "]");
//                    }
//
//                }
//                catch(Exception e){
//                    logger.error("Exception caught while executing Pending Session ID flow, ignoring...");
//                    logger.error(WebServiceUtil.getStrException(e));
//                }
//
                
                if(MWWSOperation.systemDeviceBindingEnabled()){
                    if (BindCustomerDevice(customer, device, MWCustDevcBindReasons.SIGNUP)) {
                        logger.error("Customer Device Binding successful.!");
                        customer.setDevicebindingenabled(true);
                        GeneralDao.Instance.saveOrUpdate(customer);
                    } else {
                        logger.error("Failed to bind Customer Device on SignUp, ignoring...");
                        customer.setDevicebindingenabled(false);
                    }
                }

                //wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                wsmodel.setCustomerpicture(null);
                wsmodel.setCnicpicturefront(null);
                wsmodel.setCnicpictureback(null);
                return wsmodel;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static void executeBackOfficeCreateWallet(AppWsEntity wsmodel) {
        try {
            logger.info("Executing backoffice create wallet required...");

            String dbQuery;
            Map<String, Object> params;

            String kyc = wsmodel.getKycstatus();

            List<String> allowedKYC = Arrays.asList(
                    CMSKYCStatus.STANDARD,
                    CMSKYCStatus.LIGHT
            );

            if (!Util.hasText(kyc) || !allowedKYC.contains(kyc)) {
                logger.error("KYC Status not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return;
            }

            List<String> missingFields = Util.validateKYCRequiredFields(wsmodel, kyc);

            if (!missingFields.isEmpty()) {
                logger.error("Missing required fields for {} onboarding {}, rejecting...", kyc, missingFields);
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return;
            }

            if (kyc.equals(CMSKYCStatus.LIGHT)) {
                wsmodel.setMobilenumber(wsmodel.getIdentificationno());
                wsmodel.setDateofbirth(null);
                wsmodel.setNationality(null);
                wsmodel.setProvince(null);
                wsmodel.setCity(null);
            }

            if (kyc.equals(CMSKYCStatus.STANDARD)) {
                Boolean isValidMobileNumber = SystemConfig.isValidMobileNumber(wsmodel.getMobilenumber());

                if (!isValidMobileNumber) {
                    logger.error("Invalid format mobile number [{}], rejecting...", wsmodel.getMobilenumber());
                    wsmodel.setRespcode(ISOResponseCodes.ERROR_INVALIDMOBILENUMBER);
                    return;
                }
            }

            wsmodel.setIdentificationno(wsmodel.getIdentificationno());
            wsmodel.setUsername(wsmodel.getIdentificationno());
            wsmodel.setTermsandcondition("true");

            logger.info("now registering...");

            MWCustomer customer = null;

            //Verify MobileNumber
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<>();
            params.put("MOBNO", wsmodel.getMobilenumber());


            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
                logger.error("Customer already exists with MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_MOBILE);
                return;
            }


            if (Util.hasText(wsmodel.getUsername())) {
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :USR ";
                params = new HashMap<>();
                params.put("USR", wsmodel.getUsername());

                MWCustomer tmepcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (tmepcustomer != null && !tmepcustomer.getMobilenumber().equals(wsmodel.getMobilenumber())) {
                    logger.error("Customer already exists with Username [" + wsmodel.getUsername() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_USERNAME);
                    return;
                }
            }

            if (customer == null) {
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.cnic= :CNIC ";
                params = new HashMap<>();
                params.put("CNIC", wsmodel.getIdentificationno().trim());

                customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
                    logger.error("Customer already exists with Cnic [" + wsmodel.getIdentificationno() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_IDENTIFICATIONNO);
                    return;
                }
            }

            //Verify Device
            //1) If it belongs to someone else --> RAW BANK says overwrite
            //2) If it belongs to someone else & have active session, reject
            //3) If device is changed, register new device against customer
            dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DVC ";
            params = new HashMap<>();
            params.put("DVC", wsmodel.getSecurityparams().getDeviceid()); //Raza if null it should'nt have come this far
            MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

            if (device != null) {
                logger.info("Existing Device [" + wsmodel.getSecurityparams().getDeviceid() + "] found for SignUp, verifying sessions...");
                if (!ValidateRegisterdDeviceForSignUp(wsmodel) && (customer != null && !customer.getIsmigratedactive())) {
                    logger.error("Cannot allow SignUp on device with Active Session, rejecting...");
                    return;
                }
            }


            //TODO: Verify THIS start...
            //Raza adding if multiple devices comes with Same firebase, consider lastone as legit and mark rest of them as null...
            if (Util.hasText(wsmodel.getSecurityparams().getFirebasetoken())) {
                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.firebasetoken= :FIRE ";
                params = new HashMap<>();
                params.put("FIRE", wsmodel.getSecurityparams().getFirebasetoken());
                @SuppressWarnings("unchecked")
                List<MWDeviceLog> firebaseDevices = (List<MWDeviceLog>)GeneralDao.Instance.find(dbQuery, params);

                if (firebaseDevices != null && !firebaseDevices.isEmpty()) {
                    for (MWDeviceLog dev : firebaseDevices) {
                        dev.setFirebasetoken(null);
                        GeneralDao.Instance.saveOrUpdate(dev); //Raza Updating Firebase for Previous Devices if any; Will be updated on LogIn...
                    }
                }
            }
            //TODO: Verify THIS end...


            //Now Create Customer & Register Device
            if (customer == null) {
                customer = new MWCustomer();

                if (Util.hasText(wsmodel.getCustomerpicture())) {
                    customer.setCustomerpicture(wsmodel.getCustomerpicture());
                }
                if (Util.hasText(wsmodel.getCnicpicturefront())) {
                    customer.setCnicpicturefront(wsmodel.getCnicpicturefront());
                }
                if (Util.hasText(wsmodel.getCnicpictureback())) {
                    customer.setCnicpictureback(wsmodel.getCnicpictureback());
                }
                GeneralDao.Instance.save(customer.getPictures());
            }

            if (Util.hasText(wsmodel.getUsername()))
            {
                customer.setUsername(wsmodel.getUsername().trim());
            }

            customer.setMobilenumber(wsmodel.getMobilenumber());

            if (Util.hasText(wsmodel.getPassword()))
            {
                String tempPass = WSEncryptionUtil.DecryptandEncryptAppPassword(wsmodel.getPassword().trim());
                if (!Util.hasText(tempPass)) {
                    logger.error("Invalid password received from App, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_USERNAME_PASS);
                    return ;
                }
                customer.setPassword(tempPass);
            }
            customer.setEmailaddress(wsmodel.getEmailaddress());
            customer.setCnic(Util.hasText(wsmodel.getIdentificationno()) ? wsmodel.getIdentificationno().trim() : null);
            customer.setIdentificationtype(wsmodel.getIdentificationtype());
            customer.setFirstlogin(true);
            customer.setHaswallet(false);
            customer.setResetcredentials(false);
            customer.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
            customer.setGender(wsmodel.getGender());
            customer.setTermsandcondition(Util.hasText(wsmodel.getTermsandcondition()) ? (wsmodel.getTermsandcondition().equals("true") ? "1" : "0") : "0");
            if (Util.hasText(wsmodel.getCustomerpicture())) {
                customer.setCustomerpicture(wsmodel.getCustomerpicture());
            }
            if (Util.hasText(wsmodel.getCnicpicturefront())) {
                customer.setCnicpicturefront(wsmodel.getCnicpicturefront());
            }
            if (Util.hasText(wsmodel.getCnicpictureback())) {
                customer.setCnicpictureback(wsmodel.getCnicpictureback());
            }

            if (Util.hasText(wsmodel.getDateofbirth())) {
                customer.setDateofbirth(wsmodel.getDateofbirth());
            }
            customer.setFirstname(wsmodel.getFirstname().trim());
            customer.setLastname(wsmodel.getLastname().trim());

            if (Util.hasText(wsmodel.getMiddlename())) {
                customer.setMiddlename(wsmodel.getMiddlename().trim());
            }

            customer.setAddress(wsmodel.getAddress());
            customer.setKycstatus(CMSKYCStatus.LIGHT);
            customer.setPermissions("101111111");
            customer.setLanguage(wsmodel.getLanguage());
            customer.setNotiflanguage(wsmodel.getLanguage());
            customer.setStatus("08");

            if (Util.hasText(wsmodel.getPartialflag()) && wsmodel.getPartialflag().equals("true")) {
                customer.setIsmigratedactive(false);
                customer.setIsmigrated(true);
            }

            if (device == null) {
                device = new MWDeviceLog();
                device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                device.setBaseintegrity(false);
                device.setCtsprofile(false);
                device.setIsrooted(false);
                device.setImei(wsmodel.getSecurityparams().getImei());
                device.setImsi(wsmodel.getSecurityparams().getImsi());
                device.setIccid(wsmodel.getSecurityparams().getIccid());
                device.setUuid(wsmodel.getSecurityparams().getUuid());
            }
            device.setCustomer(customer);
            customer.setAlternateChannelFlag("0");
            customer.setIccidbindingenabled(false);
            customer.setIccid(wsmodel.getSecurityparams().getIccid());
            GeneralDao.Instance.saveOrUpdate(customer);
            GeneralDao.Instance.saveOrUpdate(device);
            GeneralDao.Instance.flush();

            if (Util.hasText(wsmodel.getSecretquestion1()) && Util.hasText(wsmodel.getSecretquestion2())) {
                logger.info("Adding Security Questions...");
                if (!AddSecurityQuestions(wsmodel, customer)) {
                    logger.error("Unable to Add Security Questions for cnic [" + wsmodel.getIdentificationno() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return;
                }
            } else if (Util.hasText(wsmodel.getSecretquestion1()) || Util.hasText(wsmodel.getSecretquestion2())) {
                logger.info("Adding Security Questions for Migrated Customer...");
                if (!AddSecurityQuestions(wsmodel, customer)) {
                    logger.error("Unable to Add Security Questions for cnic [" + wsmodel.getIdentificationno() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return;
                }
            }


            logger.info("Customer Registered on Middleware, will update Data on API Response...");

            ExecuteMWServiceRequest(wsmodel, false);

            if (!wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                wsmodel.setCustomerpicture(null);
                wsmodel.setCnicpicturefront(null);
                wsmodel.setCnicpictureback(null);
                return;
            }


            logger.info("Approved Response received from OpenAPI, updating Customer...");
            customer.setHaswallet(true);
            customer.setResetcredentials(false);
            customer.setFirstlogin(false);
            customer.setUserid(wsmodel.getUserid());
            customer.setCdfaccountid(wsmodel.getCdfacctid());
            customer.setUsdaccountid(wsmodel.getUsdacctid());
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setPermissions(wsmodel.getPermissions());
            customer.setStatus("00");
            if (Util.hasText(wsmodel.getProduct())) {
                customer.setProduct(wsmodel.getProduct());
            }
            if (Util.hasText(wsmodel.getKycstatus())) {
                customer.setKycstatus(wsmodel.getKycstatus());
            }

            if (Util.hasText(wsmodel.getCustomerid())) {
                customer.setCustomerId(wsmodel.getCustomerid());
            }

            if (Util.hasText(wsmodel.getTermsandcondition()) && wsmodel.getTermsandcondition().equals("true")) {
                customer.setTermsandcondition("1");
            } else {
                customer.setTermsandcondition("0");
            }

            if (customer.getIsmigratedactive() == null || (customer.getIsmigratedactive() != null && !customer.getIsmigratedactive())) {
                if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PROFILE_CREATED, customer.getMobilenumber(), wsmodel)) {
                    logger.error("Failed to create & Send SMS for Mobile [ {} ], rejecting...", customer.getMobilenumber());
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return;
                }

                if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PROFILE_CREATED, customer.getMobilenumber(), wsmodel)) {
                    logger.error("Failed to create & Send Notification for Mobile [ {} ], rejecting...", customer.getMobilenumber());
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    return;
                }
            }

            customer.setIsmigratedactive(false);
            if (wsmodel.getSecurityparams() != null) {
                customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
                customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
            }
            GeneralDao.Instance.saveOrUpdate(customer);


            String age = "";
            if (Util.hasText(customer.getDateofbirth())) {
                Date dob = WebServiceUtil.dobFormat.parse(customer.getDateofbirth());
                age = "" + WebServiceUtil.calculateAge(dob);
            }
            //AdvertisementHandler.getAdvertisements(CustomerType.CUSTOMER, AdvertisementCategory.PROFILECREATION,age, customer.getProduct(),   wsmodel); // Added by Affan on 5-May-23

            //17-05-2024
            //M.Hamza: Adding for whenever customer signup with new device then he gives session
            //but whenever customer signup through agent with old device then customer signup successfully but
            //he'll not to login because we'll not give a single session.

            String tvalue = "";
            for (int i = 0; i < 12; i++) {
                Random rnd = new Random();
                int a = rnd.nextInt(10);
                tvalue += a;
            }

            wsmodel.setToken(tvalue);
            wsmodel.setQrid(wsmodel.getUserid());

            if (wsmodel.getSecurityparams() != null) {
                customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
                customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
                GeneralDao.Instance.saveOrUpdate(customer);
            }

            logger.info("Customer Registered & Device Session Generated successfully!");

            wsmodel.setCustomerpicture(null);
            wsmodel.setCnicpicturefront(null);
            wsmodel.setCnicpictureback(null);

            if (MWWSOperation.systemDeviceBindingEnabled()) {
                if (BindCustomerDevice(customer, device, MWCustDevcBindReasons.SIGNUP)) {
                    logger.error("Customer Device Binding successful.!");
                    customer.setDevicebindingenabled(true);
                    GeneralDao.Instance.saveOrUpdate(customer);
                } else {
                    logger.error("Failed to bind Customer Device on SignUp, ignoring...");
                    customer.setDevicebindingenabled(false);
                }
            }

            //wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);

        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
        }
    }

    public static AppWsEntity ExecuteUpdateProfileRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing UpdateProfile Request...");

            String dbQuery;
            Map<String, Object> params;

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            //Verify MobileNumber
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null && customer.getHaswallet() != null && customer.getHaswallet() && customer.getStatus().equals(CustomerStatus.ACTIVE)) {
                if (Util.hasText(wsmodel.getEmailaddress())) {
                    //Verify Email
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.emailaddress= :EMAIL ";
                    params = new HashMap<String, Object>();
                    params.put("EMAIL", wsmodel.getEmailaddress().trim());

                    MWCustomer tempcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                    if (tempcustomer != null && !customer.getId().equals(tempcustomer.getId())) {
                        logger.error("Customer already exists with Email [" + wsmodel.getEmailaddress() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_EMAIL);
                        return wsmodel;
                    }
                }

                /*if(Util.hasText(wsmodel.getUsername()))
                {
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.username= :USR ";
                    params = new HashMap<String, Object>();
                    params.put("USR", wsmodel.getUsername());

                    MWCustomer tempcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                    if(tempcustomer != null)
                    {
                        logger.error("Customer already exists with Username [" + wsmodel.getUsername() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_CUSTOMER);
                        return wsmodel;
                    }

                }*/
                if (Util.hasText(wsmodel.getIdentificationno())) {
                    //Verify CNIC
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.cnic= :CNIC ";
                    params = new HashMap<String, Object>();
                    params.put("CNIC", wsmodel.getIdentificationno().trim());

                    MWCustomer tempcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                    if (tempcustomer != null && !customer.getId().equals(tempcustomer.getId())) {
//                        logger.info("CustomerId:" + customer.getId());
//                        logger.info("TempCustomerId:" + tempcustomer.getId());

                        logger.error("Customer already exists with IdentificationNumber [" + wsmodel.getIdentificationno() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_IDENTIFICATIONNO);
                        return wsmodel;
                    }
                }

                ExecuteMWServiceRequest(wsmodel, false);
                if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    logger.info("Approved Response received from OpenAPI, updating Customer...");

                    if (Util.hasText(wsmodel.getEmailaddress())) {
                        customer.setEmailaddress(wsmodel.getEmailaddress().trim());
                    }

                    if (Util.hasText(wsmodel.getCustomerpicture())) {
                        customer.setCustomerpicture(wsmodel.getCustomerpicture());
                    }

                    if (Util.hasText(wsmodel.getFirstname())) {
                        customer.setFirstname(wsmodel.getFirstname().trim());
                    }

                    if (Util.hasText(wsmodel.getLastname())) {
                        customer.setLastname(wsmodel.getLastname().trim());
                    }
                    if (Util.hasText(wsmodel.getMiddlename())) {
                        customer.setMiddlename(wsmodel.getMiddlename().trim());
                    }
                    if (Util.hasText(wsmodel.getNationality())) {
                        customer.setNationality(wsmodel.getNationality());
                    }

                    if (Util.hasText(wsmodel.getDateofbirth())) {
                        customer.setDateofbirth(wsmodel.getDateofbirth());
                    }

                    if (Util.hasText(wsmodel.getGender())) {
                        customer.setGender(wsmodel.getGender());
                    }

                    if (Util.hasText(wsmodel.getCountry())) {
                        customer.setCountry(wsmodel.getCountry());
                    }

                    if (Util.hasText(wsmodel.getAddress())) {
                        customer.setAddress(wsmodel.getAddress());
                    }


                    if (Util.hasText(customer.getKycstatus()) && (customer.getKycstatus().equals(CMSKYCStatus.LIGHT) || customer.getKycstatus().equals(CMSKYCStatus.MIGRATED))) {
                        if (Util.hasText(wsmodel.getCnicpicturefront())) {
                            customer.setCnicpicturefront(wsmodel.getCnicpicturefront());
                        }

                        if (Util.hasText(wsmodel.getCnicpictureback())) {
                            customer.setCnicpictureback(wsmodel.getCnicpictureback());
                        }

                        if (Util.hasText(wsmodel.getIdentificationno())) {
                            customer.setCnic(wsmodel.getIdentificationno().trim());
                        }

                        if (Util.hasText(wsmodel.getIdentificationtype())) {
                            customer.setIdentificationtype(wsmodel.getIdentificationtype());
                        }
                    }
                    customer.setIsmigratedactive(false);

                    GeneralDao.Instance.saveOrUpdate(customer);

                    if (GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()) != null &&
                            GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()).getSmsenabled() != null &&
                            GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()).getSmsenabled()) {
                        if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.PROFILE_UPDATED, customer.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");
//                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
//                            return wsmodel;
                        }
                    }

                    if (GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()) != null &&
                            GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()).getNotificationenabled() != null &&
                            GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename()).getNotificationenabled()) {
                        if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.PROFILE_UPDATED, customer.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + customer.getMobilenumber() + "], ignoring...");
//                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
//                            return wsmodel;
                        }
                    }

                    GeneralDao.Instance.saveOrUpdate(customer);


                    logger.info("Customer Profile Updated successfully!");

                    return wsmodel;
                }
                else {
                    logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                    return wsmodel;
                }
            } else if (customer != null && !customer.getStatus().equals(CustomerStatus.ACTIVE) && !customer.getStatus().equals(CustomerStatus.DISABLED_FOR_UPDATE_MOBIILE) && Util.hasText(wsmodel.getStatus()))  //10 For UpdateMobileNumberWork
            {
                logger.info("Update Profile for Status [" + wsmodel.getStatus() + "] recevied for Customer [" + customer.getMobilenumber() + "], processing...");
                ExecuteMWServiceRequest(wsmodel, false);
                if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    customer.setStatus(wsmodel.getStatus());
                    GeneralDao.Instance.saveOrUpdate(customer);
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                } else {
                    logger.error("Failed to Update Customer Status on Wallet, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                }
                return wsmodel;
            } else {
                logger.error("Customer not allowed to Update Profile, wallet not created, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_INACTIVE);
                return wsmodel;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteUpdateProfileRequestForTruID(AppWsEntity wsmodel, MWCustomer customer) {
        try {
            logger.info("Executing UpdateProfile Request...");

            String dbQuery;
            Map<String, Object> params;

            ExecuteMWServiceRequest(wsmodel, false);

            if (!wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                return wsmodel;
            }

            logger.info("Approved Response received from OpenAPI, updating Customer...");

            if (Util.hasText(wsmodel.getFirstname())) {
                customer.setFirstname(wsmodel.getFirstname());
            }

            if (Util.hasText(wsmodel.getLastname())) {
                customer.setLastname(wsmodel.getLastname());
            }

            if (Util.hasText(wsmodel.getGender())) {
                customer.setGender(wsmodel.getGender());
            }

            if (Util.hasText(wsmodel.getDateofbirth())) {
                customer.setDateofbirth(wsmodel.getDateofbirth());
            }

            if (Util.hasText(wsmodel.getNationality())) {
                customer.setNationality(wsmodel.getNationality());
            }

            if (Util.hasText(wsmodel.getCnic())) {
                customer.setCnic(wsmodel.getCnic());
            }

            if (Util.hasText(wsmodel.getOccupation())) {
                customer.setOccupation(wsmodel.getOccupation());
            }

            if (Util.hasText(wsmodel.getAddress())) {
                customer.setAddress(wsmodel.getAddress());
            }

            if (Util.hasText(wsmodel.getEmailaddress())) {
                customer.setEmailaddress(wsmodel.getEmailaddress().trim());
            }

            if (Util.hasText(wsmodel.getEmailaddress())) {
                customer.setEmailaddress(wsmodel.getEmailaddress().trim());
            }

            if (Util.hasText(wsmodel.getGender())) {
                customer.setGender(wsmodel.getGender());
            }

            if (Util.hasText(wsmodel.getIdentificationtype())) {
                customer.setIdentificationtype(wsmodel.getIdentificationtype());
            }

            // setting up pictures
            MWCustPictures mwCustPictures = new MWCustPictures();

            if (Util.hasText(wsmodel.getCustomerpicture())) {
                mwCustPictures.setCustomerpicture(wsmodel.getCustomerpicture());
            }

            if (Util.hasText(wsmodel.getCnicpictureback())) {
                mwCustPictures.setCnicpictureback(wsmodel.getCnicpictureback());
            }

            if (Util.hasText(wsmodel.getCnicpicturefront())) {
                mwCustPictures.setCnicpicturefront(wsmodel.getCnicpicturefront());
            }

            customer.setPictures(mwCustPictures);

            customer.setStatus("00");

            GeneralDao.Instance.saveOrUpdate(customer);

            logger.info("Customer Profile Updated successfully!");

            return wsmodel;

        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCreateOTPRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing CreateOTP Request...");

            if (Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals(SMSCategory.VERIFY_MOBILE_REGISTRATION)) {
                String dbQuery;
                Map<String, Object> params;
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (customer != null && ((customer.getHaswallet() != null && customer.getHaswallet()) ||
                        (customer.getIsmigrated() != null && customer.getIsmigrated()) ||
                        (customer.getIsmigratedactive() != null && customer.getIsmigratedactive()))) {
                    logger.error("Customer already exists with mobile [" + wsmodel.getMobilenumber() + "], cannot allow signup, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_DUPLICATE_CUSTOMER);
                    return wsmodel;
                }
            }

            List<String> allowedReasons = Arrays.asList(
                    SMSCategory.VERIFY_MOBILE_REGISTRATION,
                    SMSCategory.OTP_CONFIRMATION_UPDATEMOBILE,
                    SMSCategory.LOGOUT_ALL_SESSIONS
            );

            String reason = (Util.hasText(wsmodel.getReason()) && allowedReasons.contains(wsmodel.getReason()))
                    ? wsmodel.getReason()
                    : SMSCategory.OTP_CONFIRMATION;

            if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, reason, wsmodel.getMobilenumber(), wsmodel)) {
                logger.error("Failed to create & Send OTP for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                return wsmodel;
            }


            //TODO: Raza REMOVE ME TESTING ONLY
            if (Util.hasText(wsmodel.getMobilenumber()) && wsmodel.getMobilenumber().contains("007007")) {
                if (!NotificationHandler.TestCreateandSendNotification(CustomerType.CUSTOMER, (Util.hasText(wsmodel.getReason()) && (wsmodel.getReason().equals(SMSCategory.VERIFY_MOBILE_REGISTRATION) || wsmodel.getReason().equals(SMSCategory.OTP_CONFIRMATION_UPDATEMOBILE))) ? wsmodel.getReason() : SMSCategory.OTP_CONFIRMATION, wsmodel.getMobilenumber(), wsmodel)) {
                    logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                }
            }
            //TODO: Raza REMOVE ME TESTING ONLY

            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
            return wsmodel;


        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }



    public static AppWsEntity ExecuteConfirmOTPRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing ConfirmOtp Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer = null;
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null && Util.hasText(customer.getEmailaddress())) {
                logger.info("Firstly, Check into Email OTP logs, processing...");

                dbQuery = "from " + EmailOtpLog.class.getName() + " c where c.toemail= :TO " + " and c.customertype = :CUSTTYPE " + " and c.isexpired = :ISEXPIRED and c.expirydatetime > :EXPIRY";
                params = new HashMap<String, Object>();
                params.put("TO", customer.getEmailaddress());
                params.put("CUSTTYPE", CustomerType.CUSTOMER);
                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                List<EmailOtpLog> emailOtpLogList = GeneralDao.Instance.find(dbQuery, params);

                if (emailOtpLogList != null && emailOtpLogList.size() > 0) {
                    for (EmailOtpLog otplog : emailOtpLogList) {
                        if (otplog.getOtp().equals(WSEncryptionUtil.EncryptAppOTP(wsmodel.getOtp())) && !otplog.getIsverified()) {
                            otplog.setIsverified(true);
                            otplog.setVerifydatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                            GeneralDao.Instance.saveOrUpdate(otplog);

                            if (otplog.getReason().equals(SMSCategory.VERIFY_MOBILE_DEVICE)) {
                                logger.info("OTP Confimation for LogIn with new device received, processing...");
                                return ExecuteLogInRequest(wsmodel, true, false);
                            }
                            else if (otplog.getReason().equals(SMSCategory.VERIFY_BIND_DEVICE))
                            {
                                logger.info("OTP Confimation for LogIn with bind device received, processing...");

                                logger.info("If this is new Device Insert before Device Bind...");

                                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
                                params = new HashMap<String, Object>();
                                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

                                MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);


                                if (device == null)
                                {
                                    logger.info("New Device Found before Device Bind, using it....");

                                    //Raza New Illico Device case start
                                    dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.customer.mobilenumber = :MOB ";
                                    params = new HashMap<String, Object>();
                                    params.put("MOB", wsmodel.getMobilenumber());

                                    MWDeviceLog olddevice = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

                                    if (olddevice != null)
                                    {
                                        logger.info("Customer used new device, OTP rquired...");

                                        olddevice.setCustomer(null);
                                        GeneralDao.Instance.saveOrUpdate(olddevice);
                                        //Raza save new device first...
                                        device = new MWDeviceLog();
                                        device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                                        device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                                        device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                                        device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                                        device.setBaseintegrity(false);
                                        device.setCtsprofile(false);
                                        device.setIsrooted(false);
                                        //device.setCustomer(customer);
                                        device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                                        GeneralDao.Instance.saveOrUpdate(customer);
                                        GeneralDao.Instance.saveOrUpdate(device);


                                    }

                                    //Raza New Illico Device case end

                                    else {
                                        device = new MWDeviceLog();
                                        device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                                        device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                                        device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                                        device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                                        device.setBaseintegrity(false);
                                        device.setCtsprofile(false);
                                        device.setIsrooted(false);
                                        //device.setCustomer(customer);
                                        device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                                        GeneralDao.Instance.saveOrUpdate(customer);
                                        GeneralDao.Instance.saveOrUpdate(device);
                                    }
                                }
                                return ExecuteLogInRequest(wsmodel, true, true);

                            }

                            else if (otplog.getReason().equals(SMSCategory.VERIFY_UNBIND_DEVICE)) {
                                logger.info("OTP Confirmation for Device Unbinding Confirmation Verified...");
                                executeConfirmDeviceUnbinding(wsmodel, true);
                                return wsmodel;
                            }

                            else if (otplog.getReason().equals(SMSCategory.VERIFY_MOBILE_REGISTRATION)) {
                                logger.info("OTP Confirmation for Mobile Device Registeration Processed and Verified, replying...");
                                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                                return wsmodel;
                            }
                            else if (otplog.getReason().equals(SMSCategory.OTP_CONFIRMATION_UPDATEMOBILE)) {
                                logger.info("OTP Confirmation for Update Mobile Processed and Verified, replying...");
                                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                                return wsmodel;
                            }

                             if (Util.hasText(otplog.getTxnrefnum())) {
                                String origservicename = GetandExecuteOriginalTxn(otplog.getTxnrefnum(), wsmodel);

                                if (!Util.hasText(origservicename)) {
                                    logger.error("Failed to get and validate orig txn for OTP Confirmation, rejecting...");
                                    return wsmodel;
                                }
                                else if(origservicename.equals("BankForexPurchase")){ //Hamza adding for Mobile Banking 20-09-2024
                                    return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                                }
                                else if (origservicename.equals("TransferMoney")) {
                                    if (wsmodel.getTypefilter().equals("BankToBank") || wsmodel.getTypefilter().equals("BankToOtherBank")) {
                                        logger.info("OrigServiceName["+ origservicename +"], TypeFilter["+wsmodel.getTypefilter()+"]");
                                        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                                    } else {
                                        return MiddlewareWebServiceHandler.processTransferMoneyRequest(wsmodel);
                                    }
                                }
                                else if (GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(origservicename).getIsfinancial()) {
                                    return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                                } else if (origservicename.equals("LinkBankAccount")) {
                                    wsmodel.setServicename("LinkBankAccountOTP");
                                    return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                                } else {
                                    logger.info("Standalone Otp Confirmation Request Processed and Verified, replying...");
                                    wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                                    return wsmodel;
                                }
                            }
                             else {
                                logger.info("Standalone Otp Confirmation Request Processed and Verified, replying...");
                                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                                return wsmodel;
                            }
                        }
                    }
                    logger.error("No EMAIL Otp matched with provided [" + wsmodel.getOtp() + "] for customer [" + customer.getEmailaddress() + "] in Email OTP logs, further processing...");
                    //wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                    //return wsmodel;
                } else {
                    logger.error("No EMAIL Otp found against mobilenumber [" + wsmodel.getMobilenumber() + "] and Email["+customer.getEmailaddress()+"], further processing...");
                    //wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                   // return wsmodel;
                }
            }
            // Added by Affan on 06-September-2023 End

            /* //TODO: Raza commenting due to Confirm Otp in case of SignUp, verify THIS
            if(!ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }*/
            logger.info("Secondly, Check into SMS OTP logs, processing...");

            dbQuery = "from " + SMSOTPLog.class.getName() + " c where c.mobilenumber= :MOB " + " and c.expirydatetime > :EXPIRY " + " and c.isexpired = :ISEXPIRED ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());
            params.put("ISEXPIRED", false);
            params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

            List<SMSOTPLog> otploglist = GeneralDao.Instance.find(dbQuery, params);

            if (otploglist != null && otploglist.size() > 0) {
                for (SMSOTPLog otplog : otploglist) {
                    if (otplog.getOtp().equals(WSEncryptionUtil.EncryptAppOTP(wsmodel.getOtp())) && !otplog.getIsverified()) {
                        logger.info("SMS OTP verified successfully for customer [" + wsmodel.getMobilenumber() + "], replying...");
                        otplog.setIsverified(true);
                        otplog.setVerifydatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                        GeneralDao.Instance.saveOrUpdate(otplog);


                        if (otplog.getReason().equals(SMSCategory.VERIFY_MOBILE_DEVICE)) {
                            logger.info("OTP Confimation for LogIn with new device received, processing...");
                            return ExecuteLogInRequest(wsmodel, true, false);
                        }
                        else if (otplog.getReason().equals(SMSCategory.VERIFY_BIND_DEVICE)) {
                            logger.info("OTP Confimation for LogIn with bind device received, processing...");

                            logger.info("If this is new Device Insert before Device Bind...");

                            dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
                            params = new HashMap<String, Object>();
                            params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

                            MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);


                            if (device == null)
                            {
                                logger.info("New Device Found before Device Bind, using it....");

                                //Raza New Illico Device case start
                                dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.customer.mobilenumber = :MOB ";
                                params = new HashMap<String, Object>();
                                params.put("MOB", wsmodel.getMobilenumber());

                                MWDeviceLog olddevice = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

                                if (olddevice != null)
                                {
                                    logger.info("Customer used new device, OTP rquired...");

                                    olddevice.setCustomer(null);
                                    GeneralDao.Instance.saveOrUpdate(olddevice);
                                    //Raza save new device first...
                                    device = new MWDeviceLog();
                                    device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                                    device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                                    device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                                    device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                                    device.setBaseintegrity(false);
                                    device.setCtsprofile(false);
                                    device.setIsrooted(false);
                                    //device.setCustomer(customer);
                                    device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                                    GeneralDao.Instance.saveOrUpdate(customer);
                                    GeneralDao.Instance.saveOrUpdate(device);


                                }

                                //Raza New Illico Device case end

                                else {
                                    device = new MWDeviceLog();
                                    device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
                                    device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
                                    device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
                                    device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
                                    device.setBaseintegrity(false);
                                    device.setCtsprofile(false);
                                    device.setIsrooted(false);
                                    //device.setCustomer(customer);
                                    device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
                                    GeneralDao.Instance.saveOrUpdate(customer);
                                    GeneralDao.Instance.saveOrUpdate(device);
                                }
                            }
                            return ExecuteLogInRequest(wsmodel, true, true);

                        }
                        else if (otplog.getReason().equals(SMSCategory.VERIFY_UNBIND_DEVICE)) {
                            logger.info("OTP Confirmation for Device Unbinding Confirmation Verified...");
                            executeConfirmDeviceUnbinding(wsmodel, true);
                            return wsmodel;
                        }
                        else if (otplog.getReason().equals(SMSCategory.VERIFY_MOBILE_REGISTRATION)) {
                            logger.info("OTP Confirmation for Mobile Device Registeration Processed and Verified, replying...");
                            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                            return wsmodel;
                        }
                        else if (otplog.getReason().equals(SMSCategory.OTP_CONFIRMATION_UPDATEMOBILE)) {
                            logger.info("OTP Confirmation for Update Mobile Processed and Verified, replying...");
                            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                            return wsmodel;
                        }
                        else if (otplog.getReason().equals(SMSCategory.LOGOUT_ALL_SESSIONS)) {
                            logger.info("OTP Confirmation for logout all sessions and verified...");

                            String logoutOtpTimeDiffMin = SystemConfig.getConfigByIdentifier("LOGOUT_OTP_TIME_DIFF_MIN");

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                            Long startDateTime = Long.parseLong(LocalDateTime.now().minusMinutes(Long.parseLong(logoutOtpTimeDiffMin)).format(formatter));
                            Long endDateTime = Long.parseLong(LocalDateTime.now().format(formatter));

                            dbQuery = "from " + AppWsEntity.class.getName() +
                                    " c where c.mobilenumber= :MOB " +
                                    " and c.respcode in (:RESPCODES) " +
                                    " and c.servicename = :SERVICE_NAME" +
                                    " and TO_NUMBER(c.transdatetime) between :START and :END " +
                                    " order by c.transdatetime desc";

                            params = new HashMap<String, Object>();

                            List<String> respcodes = Arrays.asList(
                                    ISOResponseCodes.MW_INVALID_MOBILE_SESSION,
                                    ISOResponseCodes.MW_CUST_BIND_WITH_ANOTHER_DEVC
                            );

                            params.put("RESPCODES", respcodes);
                            params.put("MOB", wsmodel.getMobilenumber());
                            params.put("SERVICE_NAME", "LogIn");
                            params.put("START", startDateTime);
                            params.put("END", endDateTime);

                            AppWsEntity log = (AppWsEntity)GeneralDao.Instance.findObject(dbQuery, params);

                            if (log == null) {
                                logger.error("Recent log not found for LogIn, rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                                return wsmodel;
                            }

                            logger.info("Log found for LogIn RRN [{}]", log.getRrn());

                            if(!log.getSecurityparams().getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid()))
                            {
                                logger.error("Device should not be same, rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.DEVICE_SAME);
                                return wsmodel;
                            }

                            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                            return wsmodel;
                        }
                        else if (Util.hasText(otplog.getTxnrefnum())) {
                            String origservicename = GetandExecuteOriginalTxn(otplog.getTxnrefnum(), wsmodel);

                            if (!Util.hasText(origservicename)) {
                                logger.error("Failed to get and validate orig txn for OTP Confirmation, rejecting...");
                                return wsmodel;
                            }
                            else if(origservicename.equals("BankForexPurchase")){ //Hamza adding for Mobile Banking 20-09-2024
                                return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                            }
//                            else if (origservicename.equals("TransferMoney") && (!wsmodel.getTypefilter().equals("BankToBank")
//                                    || !wsmodel.getTypefilter().equals("BankToOtherBank"))) { //Hamza adding for Mobile Banking 27-08-2024
//                                return MiddlewareWebServiceHandler.processTransferMoneyRequest(wsmodel);
//                            }
//                            else if (origservicename.equals("TransferMoney") && (wsmodel.getTypefilter().equals("BankToBank")
//                            || wsmodel.getTypefilter().equals("BankToOtherBank"))) { //Hamza adding for Mobile Banking 27-08-2024
////                                wsmodel.setOriginalapi("BankToBank");
//                                logger.info("OrigServiceName["+ origservicename +"], TypeFilter["+wsmodel.getTypefilter()+"]");
//                                return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
////                                return MiddlewareWebServiceHandler.processTransferMoneyRequest(wsmodel);
//                            }
                            else if (origservicename.equals("TransferMoney")) {
                                if (Util.hasText(wsmodel.getTypefilter()) && ((wsmodel.getTypefilter().equals("BankToBank") || wsmodel.getTypefilter().equals("BankToOtherBank")))) {
                                    logger.info("OrigServiceName["+ origservicename +"], TypeFilter["+wsmodel.getTypefilter()+"]");
                                    return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                                } else {
                                    return MiddlewareWebServiceHandler.processTransferMoneyRequest(wsmodel);
                                }
                            }
                            else if (GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(origservicename).getIsfinancial()) {
                                return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                            } else if (origservicename.equals("LinkBankAccount")) {
                                wsmodel.setServicename("LinkBankAccountOTP");
                                return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                            } else {
                                logger.info("Standalone Otp Confirmation Request Processed and Verified, replying...");
                                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                                return wsmodel;
                            }
                        }
                        else {
                            logger.info("Standalone Otp Confirmation Request Processed and Verified, replying...");
                            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                            return wsmodel;
                        }
                    }
                }
                logger.error("No SMS Otp matched with provided [" + wsmodel.getOtp() + "] for customer [" + wsmodel.getMobilenumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                return wsmodel;
            } else {
                logger.error("No SMS Otp found against mobilenumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                return wsmodel;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }


    public static AppWsEntity ExecuteGetRegisteredContacts(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetRegisteredContacts Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }


            if (wsmodel.getContactlist() != null && wsmodel.getContactlist().size() > 0) {
                String dbQuery;
                Map<String, Object> params;
                List<AppContact> cntctlst = new ArrayList<>();
                for (AppContact contact : wsmodel.getContactlist()) {
                    //Raza updating mobile number start
                    if (Util.hasText(contact.getMobilenumber())) {
                        String mobilenum = contact.getMobilenumber().replace(" ", "");
                        if (mobilenum.contains("+243")) {
                            //mobilenum = "00243" + mobilenum.substring(4, mobilenum.length());
                            mobilenum = mobilenum.replace("+", "00");
                        } else if (!mobilenum.contains("00243")) {
                            mobilenum = "00243" + mobilenum;
                        } else if (mobilenum.contains("+")) {
                            mobilenum = mobilenum.replace("+", "00");
                        } else if (mobilenum.length() == 9) {
                            mobilenum = "00243" + mobilenum;
                        }
                        //Raza updating mobile number end


                        MWCustomer customer;
                        params = new HashMap<String, Object>();
                        params.put("USER", mobilenum);

                        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";

                        MWCustomer cust = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                        if (cust != null && cust.getHaswallet() != null && cust.getHaswallet()) {
                            AppContact newcontact = new AppContact();
                            newcontact.setMobilenumber(contact.getMobilenumber());
                            cntctlst.add(newcontact);
                        }
                    }
                }
                if (cntctlst != null && cntctlst.size() > 0) {
                    logger.error("Updating contacts and returning...");
                    wsmodel.setContactlist(cntctlst);
                } else {
                    wsmodel.setContactlist(cntctlst);
                    logger.error("No Contact found in registerd contacts, returning no contacts...");
                    //logger.error("No Contact found in registerd contacts, returning all contacts...");
                }
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("ContactList not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetInviteContacts(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetInviteContacts Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }


            if (wsmodel.getContactlist() != null && wsmodel.getContactlist().size() > 0) {
                String dbQuery;
                Map<String, Object> params;
                List<AppContact> cntctlst = new ArrayList<>();
                for (AppContact contact : wsmodel.getContactlist()) {
                    MWCustomer customer;
                    Boolean addmob = false;
                    params = new HashMap<String, Object>();

                    if (Util.hasText(contact.getMobilenumber())) {
                        //Raza updating mobile number start
                        String mobilenum = contact.getMobilenumber().replace(" ", "");

                        if (mobilenum.length() == 9) {
                            mobilenum = "00243" + mobilenum;
                        } else if (mobilenum.length() == 10) {
                            mobilenum = "00243" + mobilenum.substring(1, mobilenum.length());
                        } else if (mobilenum.contains("+243") && mobilenum.substring(0, 4).equals("+243")) {
                            //mobilenum = "00243" + mobilenum.substring(4, mobilenum.length());
                            mobilenum = mobilenum.replace("+", "00");
                        } else if (mobilenum.contains("00243") && mobilenum.substring(0, 5).equals("00243")) {
                            mobilenum = mobilenum;
                        } else {
                            mobilenum = null;
                        }
                        /*else if(!mobilenum.contains("00243"))
                        {
                            mobilenum = "00243" + mobilenum;
                        }
                        else if(mobilenum.contains("+"))
                        {
                            mobilenum = mobilenum.replace("+", "00");
                        }*/
                        //Raza updating mobile number end
                        if (Util.hasText(mobilenum)) {
                            params.put("USER", mobilenum);
                            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";

                            MWCustomer cust = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                            if (cust == null) {
                                AppContact newcontact = new AppContact();
                                newcontact.setMobilenumber(contact.getMobilenumber());
                                cntctlst.add(newcontact);
                            } else //Raza adding for EnvoiCash Testing.. Remove ME
                            {
                                AppContact newcontact = new AppContact();
                                newcontact.setMobilenumber(contact.getMobilenumber());
                                cntctlst.add(newcontact);
                            }
                        }
                    }
                }
                if (cntctlst != null && cntctlst.size() > 0) {
                    logger.error("Updating contacts and returning...");
                    wsmodel.setContactlist(cntctlst);
                } else {
                    logger.error("No Contact found in registerd contacts, returning all contacts...");
                }
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("ContactList not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetDRCContacts(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetDRCContacts Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }


            if (wsmodel.getContactlist() != null && wsmodel.getContactlist().size() > 0) {
                String dbQuery;
                Map<String, Object> params;
                List<AppContact> cntctlst = new ArrayList<>();
                for (AppContact contact : wsmodel.getContactlist()) {
                    MWCustomer customer;
                    Boolean addmob = false;
                    params = new HashMap<String, Object>();

                    if (Util.hasText(contact.getMobilenumber())) {
                        //Raza updating mobile number start
                        String mobilenum = contact.getMobilenumber().replace(" ", "");

                        if (mobilenum.length() == 9) {
                            mobilenum = "00243" + mobilenum;
                        } else if (mobilenum.length() == 10) {
                            mobilenum = "00243" + mobilenum.substring(1, mobilenum.length());
                        } else if (mobilenum.contains("+243") && mobilenum.substring(0, 4).equals("+243")) {
                            //mobilenum = "00243" + mobilenum.substring(4, mobilenum.length());
                            mobilenum = mobilenum.replace("+", "00");
                        } else if (mobilenum.contains("00243") && mobilenum.substring(0, 5).equals("00243")) {
                            mobilenum = mobilenum;
                        } else {
                            mobilenum = null;
                        }
                        /*else if(!mobilenum.contains("00243"))
                        {
                            mobilenum = "00243" + mobilenum;
                        }
                        else if(mobilenum.contains("+"))
                        {
                            mobilenum = mobilenum.replace("+", "00");
                        }*/
                        //Raza updating mobile number end
                        if (Util.hasText(mobilenum)) {
                            params.put("USER", mobilenum);
                            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :USER ";

                            MWCustomer cust = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                            if (cust == null) {
                                AppContact newcontact = new AppContact();
                                newcontact.setMobilenumber(contact.getMobilenumber());
                                cntctlst.add(newcontact);
                            } else //Raza adding for EnvoiCash Testing.. Remove ME
                            {
                                AppContact newcontact = new AppContact();
                                newcontact.setMobilenumber(contact.getMobilenumber());
                                cntctlst.add(newcontact);
                            }
                        }
                    }
                }
                if (cntctlst != null && cntctlst.size() > 0) {
                    logger.error("Updating contacts and returning...");
                    wsmodel.setContactlist(cntctlst);
                } else {
                    logger.error("No Contact found in registerd contacts, returning all contacts...");
                }
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("ContactList not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteSendInvite(AppWsEntity wsmodel) {

        try {
            logger.info("Executing SendInvite Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            if (Util.hasText(wsmodel.getDestmobilenumber())) {
                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
                params = new HashMap<String, Object>();
                params.put("MOB", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (customer == null) {
                    logger.error("Customer not found against mobilenumber [" + wsmodel.getMobilenumber() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
                params = new HashMap<String, Object>();
                params.put("MOB", wsmodel.getDestmobilenumber());

                MWCustomer invitedcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (invitedcustomer != null) {
                    logger.error("Customer with mobile [" + wsmodel.getDestmobilenumber() + "] already registered, cannot invite!");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                    return wsmodel;
                }
                return wsmodel;
            } else {
                logger.error("Destination Mobile Number not present to Invite, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetNotifications(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetNotifications Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                wsmodel.setNotiflanguage(customer.getNotiflanguage());
                dbQuery = "from " + NotificationSetting.class.getName() + " c where c.customertype= :CUSTTYPE and c.category= :CAT ";
                params = new HashMap<String, Object>();
                params.put("CUSTTYPE", CustomerType.CUSTOMER);
                params.put("CAT", NotificationSettingCategory.NOTIFICATION_COUNT);
                NotificationSetting notifconfig = (NotificationSetting) GeneralDao.Instance.findObject(dbQuery, params);
                int notifsize = 50;
                if (notifconfig != null) {
                    //logger.info("NotifConfig found!"); //Raza REMOVE ME
                    try {
                        if (Util.hasText(notifconfig.getMaxnotificationcount())) {
                            notifsize = Integer.parseInt(notifconfig.getMaxnotificationcount());
                        }
                    } catch (Exception e) {
                        logger.error("Exception caught while parsing Notification Size, using size 50...");
                        //e.printStackTrace();
                        logger.error(WebServiceUtil.getStrException(e));
                    }
                }


                dbQuery = "SELECT * FROM (SELECT a.ID,a.NOTIFICATION_TITLE,a.TRANTYPE,a.NOTIFICATION_BODY,a.FRA_NOTIFICATION_TITLE,a.FRA_NOTIFICATION_BODY,a.CREATION_DATE_TIME,a.IS_ENCRYPTED,a.EXT_NOTIF,a.XLATE_LANG,a.XLATE_TITLE,a.XLATE_BODY from NOTIFICATION_LOG a where a.customer = :CUST ORDER BY TO_NUMBER(CREATION_DATE_TIME) DESC) where rownum <= " + notifsize;
                params = new HashMap<String, Object>();
                params.put("CUST", customer.getId());

                List<Object[]> dbnotiflist = GeneralDao.Instance.executeSqlQuery(dbQuery, params);

                if (dbnotiflist != null && dbnotiflist.size() > 0) {
                    //logger.info("DB Notif Found"); //Raza REMOVE ME


                    List<NotificationObj> notificationlist = new ArrayList<>();
                    for (int i = 0; i < dbnotiflist.size(); i++) {
                        logger.info("En-Flag [" + ((dbnotiflist.get(i)[7] != null) ? dbnotiflist.get(i)[7] + "" : "null") + "]"); //Raza REMOVE ME
                        NotificationObj notobj = new NotificationObj();
                        notobj.setId((dbnotiflist.get(i)[0] != null) ? dbnotiflist.get(i)[0] + "" : null);

                        if (Util.hasText(wsmodel.getNotiflanguage()) && wsmodel.getNotiflanguage().equals("FRA")) {
                            if (dbnotiflist.get(i)[7] != null && dbnotiflist.get(i)[7].equals("1")) {
                                notobj.setTitle((dbnotiflist.get(i)[4] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[4] + "") : null);
                                notobj.setNotification((dbnotiflist.get(i)[5] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[5] + "") : null);
                            } else {
                                notobj.setTitle((dbnotiflist.get(i)[4] != null) ? dbnotiflist.get(i)[4] + "" : null);
                                notobj.setNotification((dbnotiflist.get(i)[5] != null) ? dbnotiflist.get(i)[5] + "" : null);
                            }
                        } else if(Util.hasText(wsmodel.getNotiflanguage()) && wsmodel.getNotiflanguage().equals("ENG")) {
                            if (dbnotiflist.get(i)[7] != null && dbnotiflist.get(i)[7].equals("1")) {
                                notobj.setTitle((dbnotiflist.get(i)[1] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[1] + "") : null);
                                notobj.setNotification((dbnotiflist.get(i)[3] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[3] + "") : null);
                            } else {
                                notobj.setTitle((dbnotiflist.get(i)[1] != null) ? dbnotiflist.get(i)[1] + "" : null);
                                notobj.setNotification((dbnotiflist.get(i)[3] != null) ? dbnotiflist.get(i)[3] + "" : null);
                            }
                        }
                        else if(Util.hasText(wsmodel.getNotiflanguage()) && Util.hasText(dbnotiflist.get(i)[9]+"") && wsmodel.getNotiflanguage().equals(dbnotiflist.get(i)[9])) {
                            notobj.setTitle((dbnotiflist.get(i)[10] != null) ? dbnotiflist.get(i)[10] + "" : null);
                            notobj.setNotification((dbnotiflist.get(i)[11] != null) ? dbnotiflist.get(i)[11] + "" : null);
                        }
                        else if(Util.getDefaultMobileAppLanguage().equals("ENG")) {
                            if (dbnotiflist.get(i)[7] != null && dbnotiflist.get(i)[7].equals("1")) {
                                notobj.setTitle((dbnotiflist.get(i)[1] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[1] + "") : null);
                                notobj.setNotification((dbnotiflist.get(i)[3] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[3] + "") : null);
                            } else {
                                notobj.setTitle((dbnotiflist.get(i)[1] != null) ? dbnotiflist.get(i)[1] + "" : null);
                                notobj.setNotification((dbnotiflist.get(i)[3] != null) ? dbnotiflist.get(i)[3] + "" : null);
                            }
                        }
                        else{
                            if (dbnotiflist.get(i)[7] != null && dbnotiflist.get(i)[7].equals("1")) {
                                notobj.setTitle((dbnotiflist.get(i)[4] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[4] + "") : null);
                                notobj.setNotification((dbnotiflist.get(i)[5] != null) ? WSEncryptionUtil.decryptSmsNotifContent(dbnotiflist.get(i)[5] + "") : null);
                            } else {
                                notobj.setTitle((dbnotiflist.get(i)[4] != null) ? dbnotiflist.get(i)[4] + "" : null);
                                notobj.setNotification((dbnotiflist.get(i)[5] != null) ? dbnotiflist.get(i)[5] + "" : null);
                            }
                        }
                        notobj.setCategory((dbnotiflist.get(i)[2] != null) ? dbnotiflist.get(i)[2] + "" : null);
                        notobj.setCreationdatetime((dbnotiflist.get(i)[6] != null) ? dbnotiflist.get(i)[6] + "" : null);
                        notobj.setTicketObjectFromString((dbnotiflist.get(i)[8] != null) ? dbnotiflist.get(i)[8] + "" : null);
                        notificationlist.add(notobj);
                    }
                    wsmodel.setNotificationlist(notificationlist);
                } else {
                    logger.error("No Notification found for customer [" + wsmodel.getMobilenumber() + "]");
                }



                    /*dbQuery = "from " + NotificationLog.class.getName() + " c where c.customer= :CUST and rownum <=" + notifsize;
                    params = new HashMap<String, Object>();
                    params.put("CUST", customer);
                    List<NotificationLog> dbnotifications = GeneralDao.Instance.find(dbQuery, params);

                    if(dbnotifications != null && dbnotifications.size() > 0)
                    {
                        List<NotificationObj> notificationlist = new ArrayList<>();
                        for(NotificationLog dbnot : dbnotifications)
                        {
                            NotificationObj notobj = new NotificationObj();
                            notobj.setTitle(dbnot.getTitle());
                            notobj.setCategory(dbnot.getCategory());
                            notobj.setNotification(dbnot.getBody());
                            notificationlist.add(notobj);
                        }
                        wsmodel.setNotificationlist(notificationlist);
                    }*/

                customer.setUnreadnotifcount(0L);
                GeneralDao.Instance.saveOrUpdate(customer);

                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer not found with mobilenumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetAcceptedInvites(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetAcceptedInvites Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetCancelInvites(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetCancelInvites Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetSessionState(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetSessionState Request...");

            if (!GetRespListV2(wsmodel)) {
                logger.error("Failed to Set ResList, ignoring...");
            }

            if (ValidateUserandAppSessionForStartUp(wsmodel)) {
                logger.info("Customer and Device Session verified for Startup, replying...");
                if (Util.hasText(wsmodel.getAdvanceflag())) {
                    String dbQuery = "";
                    Map<String, Object> params;

                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
                    params = new HashMap<String, Object>();
                    params.put("MOBNO", wsmodel.getMobilenumber());

                    MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                    if (customer != null) {

                        String age = "";
                        if (Util.hasText(customer.getDateofbirth())) {
                            Date dob = WebServiceUtil.dobFormat.parse(customer.getDateofbirth());
                            age = "" + WebServiceUtil.calculateAge(dob);
                        }


//                        AdvertisementHandler.getAdvertisements(CustomerType.CUSTOMER, AdvertisementCategory.LOGIN, age, customer.getProduct(), wsmodel);


                    } else {
                        logger.error("Customer [" + wsmodel.getMobilenumber() + "] not found while validating session, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                        return wsmodel;
                    }
                }
                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            } else {
                logger.error("Failed to validate Session StartUp session, replying as per scenario...");
                return wsmodel;
            }


        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteSetBeneficiary(AppWsEntity wsmodel) {

        try {
            logger.info("Executing SetBeneficiary Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.utilcompany= :UTILCOMP " + " and c.consumerno= :BENEFICIARY ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                String consumerno = wsmodel.getMobilenumber();
                if (Util.hasText(wsmodel.getConsumerno()) && !wsmodel.getConsumerno().equals(wsmodel.getMobilenumber())) {
                    consumerno = wsmodel.getConsumerno();
                }
                else {
                    //if(Util.hasText(wsmodel.getDestfirstname()))
                    //{
                    dbQuery += " and c.firstname= :FSTNAME ";
                    params.put("FSTNAME", wsmodel.getDestfirstname());
                    //}
                    //if(Util.hasText(wsmodel.getDestlastname()))
                    //{
                    dbQuery += " and c.lastname= :LSTNAME ";
                    params.put("LSTNAME", wsmodel.getDestlastname());
                    //}

                    /*if(Util.hasText(wsmodel.getCountry()))
                    {
                        dbQuery += " and c.country= :CNTRY ";
                        params.put("CNTRY", wsmodel.getCountry());
                    }
                    if(Util.hasText(wsmodel.getIban()))
                    {
                        dbQuery += " and c.iban= :IBAN ";
                        params.put("IBAN", wsmodel.getIban());
                    }
                    if(Util.hasText(wsmodel.getDestaccount()))
                    {
                        dbQuery += " and c.account= :ACT ";
                        params.put("ACT", wsmodel.getDestaccount());
                    }
                    if(Util.hasText(wsmodel.getSwiftbiccode()))
                    {
                        dbQuery += " and c.swiftcode= :SWFTCDE ";
                        params.put("SWFTCDE", wsmodel.getSwiftbiccode());
                    }
                    if(Util.hasText(wsmodel.getIfscode()))
                    {
                        dbQuery += " and c.ifscode= :IFSCODE ";
                        params.put("IFSCODE", wsmodel.getIfscode());
                    }
                    if(Util.hasText(wsmodel.getIdentificationtype()))
                    {
                        dbQuery += " and c.idtype= :IDTYPE ";
                        params.put("IDTYPE", wsmodel.getIdentificationtype());
                    }
                    if(Util.hasText(wsmodel.getCnic()))
                    {
                        dbQuery += " and c.idcode= :IDNUM ";
                        params.put("IDNUM", wsmodel.getCnic());
                    }
                    if(Util.hasText(wsmodel.getBankcode()))
                    {
                        dbQuery += " and c.sortcode= :SRTCODE ";
                        params.put("SRTCODE", wsmodel.getBankcode());
                    }
                    if(Util.hasText(wsmodel.getCardnumber()))
                    {
                        dbQuery += " and c.cardnumber= :CRDNUM ";
                        params.put("CRDNUM", wsmodel.getCardnumber());
                    }*/
                }
                params.put("BENEFICIARY", consumerno);
                params.put("UTILCOMP", wsmodel.getUtilcompanyid());

                MWBeneficiary dbbenef = (MWBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbbenef != null) {
                    dbbenef.setFirstname(wsmodel.getDestfirstname().trim());
                    dbbenef.setLastname(wsmodel.getDestlastname().trim());
                    dbbenef.setCountry(wsmodel.getDestcountry());
                    dbbenef.setMobilenumber(wsmodel.getDestmobilenumber());
                    dbbenef.setIban(wsmodel.getIban());
                    dbbenef.setAccount(wsmodel.getDestaccount());
                    dbbenef.setSwiftcode(wsmodel.getSwiftbiccode());
                    dbbenef.setIfscode(wsmodel.getIfscode());
                    dbbenef.setPackagecode(wsmodel.getPackagecode());
                    dbbenef.setIdtype(wsmodel.getIdentificationtype());
                    dbbenef.setIdcode(Util.hasText(wsmodel.getIdentificationno()) ? wsmodel.getIdentificationno().trim() : null);
                    dbbenef.setSortcode(wsmodel.getBankcode());
                    dbbenef.setCardnumber(wsmodel.getCardnumber());
                    dbbenef.setCreditaccount(wsmodel.getCreditaccountnumber());
                    dbbenef.setClabe(wsmodel.getClabe());
                    dbbenef.setCbu(wsmodel.getCbu());
                    dbbenef.setCbualias(wsmodel.getCbualias());
                    dbbenef.setBikcode(wsmodel.getBikcode());
                    dbbenef.setAbaroutingnumber(wsmodel.getAbaroutingnumber());
                    dbbenef.setBsbnumber(wsmodel.getBsbnumber());
                    dbbenef.setRoutingcode(wsmodel.getRoutingcode());
                    dbbenef.setEntityttid(wsmodel.getEntityttid());
                    dbbenef.setAccounttype(wsmodel.getAccounttype());
                    dbbenef.setDestaddress(wsmodel.getDestaddress());
                    dbbenef.setPayerid(wsmodel.getPayerid());
                    dbbenef.setPayertype(wsmodel.getPayertype());
                    dbbenef.setBranchnumber(wsmodel.getBranchcode());
                    dbbenef.setEmail(wsmodel.getEmailaddress());
                    GeneralDao.Instance.saveOrUpdate(dbbenef);
                } else {
                    dbbenef = new MWBeneficiary();
                    dbbenef.setConsumerno(consumerno);
                    dbbenef.setUtilcompany(wsmodel.getUtilcompanyid());
                    dbbenef.setFirstname(wsmodel.getDestfirstname().trim());
                    dbbenef.setLastname(wsmodel.getDestlastname().trim());
                    dbbenef.setCountry(wsmodel.getDestcountry());
                    dbbenef.setMobilenumber(wsmodel.getDestmobilenumber());
                    dbbenef.setIban(wsmodel.getIban());
                    dbbenef.setAccount(wsmodel.getDestaccount());
                    dbbenef.setSwiftcode(wsmodel.getSwiftbiccode());
                    dbbenef.setIfscode(wsmodel.getIfscode());
                    dbbenef.setPackagecode(wsmodel.getPackagecode());
                    dbbenef.setIdtype(wsmodel.getIdentificationtype());
                    dbbenef.setIdcode(Util.hasText(wsmodel.getIdentificationno()) ? wsmodel.getIdentificationno().trim() : null);
                    dbbenef.setSortcode(wsmodel.getBankcode());
                    dbbenef.setCardnumber(wsmodel.getCardnumber());
                    dbbenef.setCustomer(customer);
                    dbbenef.setCreditaccount(wsmodel.getCreditaccountnumber());
                    dbbenef.setClabe(wsmodel.getClabe());
                    dbbenef.setCbu(wsmodel.getCbu());
                    dbbenef.setCbualias(wsmodel.getCbualias());
                    dbbenef.setBikcode(wsmodel.getBikcode());
                    dbbenef.setAbaroutingnumber(wsmodel.getAbaroutingnumber());
                    dbbenef.setBsbnumber(wsmodel.getBsbnumber());
                    dbbenef.setRoutingcode(wsmodel.getRoutingcode());
                    dbbenef.setEntityttid(wsmodel.getEntityttid());
                    dbbenef.setAccounttype(wsmodel.getAccounttype());
                    dbbenef.setDestaddress(wsmodel.getDestaddress());
                    dbbenef.setPayerid(wsmodel.getPayerid());
                    dbbenef.setPayertype(wsmodel.getPayertype());

                    GeneralDao.Instance.saveOrUpdate(dbbenef);
                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetBeneficiaries(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetBeneficiaries Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.utilcompany= :UTILCOMP ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("UTILCOMP", wsmodel.getUtilcompanyid());

                List<MWBeneficiary> dbbeneflist = GeneralDao.Instance.find(dbQuery, params);

                if (dbbeneflist != null && dbbeneflist.size() > 0) {
                    List<BenefciaryObj> beneflist = new ArrayList<>();
                    for (MWBeneficiary bf : dbbeneflist) {
                        BenefciaryObj obj = new BenefciaryObj();
                        obj.setConsumerno(bf.getConsumerno());
                        if (Util.hasText(bf.getDestaddress())) {
                            obj.setDestaddress(bf.getDestaddress());
                        }
                        if (Util.hasText(bf.getPayerid())) {
                            obj.setPayerid(bf.getPayerid());
                        }
                        if (Util.hasText(bf.getPayertype())) {
                            obj.setPayertype(bf.getPayertype());
                        }
                        obj.setFirstname(bf.getFirstname());
                        obj.setLastname(bf.getLastname());
                        obj.setCountry(bf.getCountry());
                        obj.setAccount(bf.getAccount());
                        obj.setIban(bf.getIban());
                        obj.setMobile(bf.getMobilenumber());
                        obj.setSwiftcode(bf.getSwiftcode());
                        obj.setIfscode(bf.getIfscode());
                        obj.setPackagecode(bf.getPackagecode());
                        obj.setIdtype(bf.getIdtype());
                        obj.setIdcode(bf.getIdcode());
                        obj.setSortcode(bf.getSortcode());
                        obj.setCardnumber(bf.getCardnumber());
                        obj.setCreditaccount(bf.getCreditaccount());
                        //Raza adding on 13-05-2022 start
                        obj.setUtilcompany(bf.getUtilcompany());
                        obj.setClabe(bf.getClabe());
                        obj.setCbu(bf.getCbu());
                        obj.setCbualias(bf.getCbualias());
                        obj.setBikcode(bf.getBikcode());
                        obj.setAbaroutingnumber(bf.getAbaroutingnumber());
                        obj.setBsbnumber(bf.getBsbnumber());
                        obj.setRoutingcode(bf.getRoutingcode());
                        obj.setEntityttid(bf.getEntityttid());
                        obj.setAccounttype(bf.getAccounttype());
                        //Raza adding on 13-05-2022 end

                        beneflist.add(obj);
                    }
                    wsmodel.setBeneficiarylist(beneflist);
                } else {
                    logger.error("No Beneficiary Found for UtilCompnay [" + wsmodel.getUtilcompanyid() + "] customer [" + wsmodel.getMobilenumber() + "]");
                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED); //Raza return OK always
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteDeleteBeneficiary(AppWsEntity wsmodel) {

        try {
            logger.info("Executing DeleteBeneficiary Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.utilcompany= :UTILCOMP ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                if (Util.hasText(wsmodel.getConsumerno()) && !wsmodel.getConsumerno().equals(wsmodel.getMobilenumber())) {
                    dbQuery += " and c.consumerno= :BENEFICIARY ";
                    params.put("BENEFICIARY", wsmodel.getConsumerno());
                } else {
                    dbQuery += " and c.firstname= :FIRSTNAME and c.lastname= :LASTNAME ";
                    params.put("FIRSTNAME", wsmodel.getDestfirstname());
                    params.put("LASTNAME", wsmodel.getDestlastname());
                    //params.put("BENEFICIARY", wsmodel.getMobilenumber());
                }
                params.put("UTILCOMP", wsmodel.getUtilcompanyid());

                MWBeneficiary dbbenef = (MWBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbbenef != null) {
                    logger.info("Beneficiary found, deleting...");
                    dbbenef.setCustomer(null);
                    GeneralDao.Instance.saveOrUpdate(dbbenef);
                    GeneralDao.Instance.delete(dbbenef);
                } else {
                    logger.error("Beneficiary not found against consumerno. [" + wsmodel.getConsumerno() + "], UtilCompnay [" + wsmodel.getUtilcompanyid() + "] of Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                    return wsmodel;
                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteSetLanguage(AppWsEntity wsmodel) {

        try {
            logger.info("Executing SetLanguage Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                logger.info("Customer language [" + wsmodel.getLanguage() + "] mobile [" + customer.getMobilenumber() + "]");
                customer.setLanguage(wsmodel.getLanguage());
                customer.setNotiflanguage(wsmodel.getLanguage()); //Raza adding 10-02-2021
                GeneralDao.Instance.saveOrUpdate(customer);

                wsmodel.setRespcode(ISOResponseCodes.APPROVED); //Raza return OK always
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteSetNotifLanguage(AppWsEntity wsmodel) {

        try {
            logger.info("Executing SetNotifLanguage Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                customer.setNotiflanguage(wsmodel.getLanguage());
                GeneralDao.Instance.saveOrUpdate(customer);

                wsmodel.setRespcode(ISOResponseCodes.APPROVED); //Raza return OK always
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteUpdateTermandCond(AppWsEntity wsmodel) {

        try {
            logger.info("Executing UpdateTermandCond Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                if (Util.hasText(wsmodel.getTermsandcondition()) && wsmodel.getTermsandcondition().equals("true")) {
                    customer.setTermsandcondition("1");
                } else {
                    customer.setTermsandcondition("0");
                }
                GeneralDao.Instance.saveOrUpdate(customer);

                SendToOpenAPI(wsmodel);
                if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    logger.info("Approved Response received from OpenAPI, replying...");

                    return wsmodel;
                } else {
                    logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                    return wsmodel;
                }

            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    // Helper method to check if a string is an integer
    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static AppWsEntity ExecuteDynSetBeneficiary(AppWsEntity wsmodel) {

        try {
            logger.info("Executing SetDynBeneficiary Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            JSONObject benefjson = new JSONObject(wsmodel.getBeneficiaryobj());
//            String benefid = (benefjson.has("id") && Util.hasText(benefjson.getString("id"))) ? benefjson.getString("id") : null;

            //Muhammad Hamza Added: Validation on Save Beneficiary
            // Check if id exists and is a string
            String benefid = null;

            if (benefjson.has("id")) {
                if (Util.hasText(benefjson.getString("id"))) {
                    benefid = benefjson.getString("id");
                   //if(isInteger(benefid)){
                      // logger.error("Beneficiary id is an integer, rejecting...");
                      // wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                       //return wsmodel;
                  // }
                   //else{
                    //   logger.info("Beneficiary id is not an Integer!!");
                   //}
                } else {
                    logger.error("Beneficiary id is empty, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                    return wsmodel;
                }
            }

//            // Ensure JSON object contains only this fields
//            Iterator<String> keys = benefjson.keys();
//            while (keys.hasNext()) {
//                String key = keys.next();
//                if (!key.equals("details") && !key.equals("addressdetails") && !key.equals("id")
//                        && !key.equals("firstname") && !key.equals("lastname") && !key.equals("payerid")
//                        && !key.equals("destcountry") && !key.equals("destmobilenumber") && !key.equals("payertype")
//                        && !key.equals("payertypeoption")) {
//                    logger.error("JSON object contains invalid key [" + key + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//                    return wsmodel;
//                }
//            }

            MWRemitBeneficiary dbbenef = null;

            //if(benefjson.has("id") && Util.hasText(benefjson.getString("id"))){
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                logger.info("customer not null..");
                if (Util.hasText(benefid)) {
                    dbQuery = "from " + MWRemitBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.id= :ID ";
                    params = new HashMap<String, Object>();
                    params.put("MOBNO", wsmodel.getMobilenumber());
                    params.put("ID", Long.parseLong(benefjson.getString("id")));

                    // Muhammad Hamza: adding for Remove ID from JSON Object. 07-03-2024
                    if (benefjson.has("id")) {
                        benefjson.remove("id");
                        wsmodel.setBeneficiaryobj(benefjson.toString());
                    }

                    dbbenef = (MWRemitBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                }

                //Muhammad Hamza adding for "Cannot Add an Additional beneficiary form ID" //06-03-2024
                if (dbbenef == null && Util.hasText(benefid)) {
                    logger.info("you cannot add an additional ID...");
                    logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                    return wsmodel;
                }

                if (dbbenef == null) {
                    dbbenef = new MWRemitBeneficiary();
                }


                dbbenef.setCustomer(customer);
                dbbenef.setBeneficiaryinfo(wsmodel.getBeneficiaryobj());

                GeneralDao.Instance.saveOrUpdate(dbbenef);

                GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.beginTransaction();

                GeneralDao.Instance.refresh(dbbenef);

                benefjson.put("id", dbbenef.getId());
                wsmodel.setBeneficiaryobj(benefjson.toString());

                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
            }
//            }
//            else{
//                logger.error("Failed to get ID for Beneficiary, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
//            }

            return wsmodel;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            //wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteDynGetBeneficiaries(AppWsEntity wsmodel) {

        try {
            logger.info("Executing DynGetBeneficiaries Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWRemitBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());

                List<MWRemitBeneficiary> dbbeneflist = GeneralDao.Instance.find(dbQuery, params);

                if (dbbeneflist != null && dbbeneflist.size() > 0) {
                    logger.info("DynBeneficiaries found in DB...");
                    List<String> beneflist = new ArrayList<>();
                    for (MWRemitBeneficiary bf : dbbeneflist) {
                        try {
                            JSONObject benefinfo = new JSONObject(bf.getBeneficiaryinfo());

                            if (benefinfo != null) {
                                if (!benefinfo.has("id") || !Util.hasText(benefinfo.getString("id"))) {
                                    benefinfo.put("id", bf.getId() + "");
                                }

                                //Check if benefobj exist then process payerid as sub json object

                               //For PayerID Checker
                                /*
                                if (benefinfo.has("payerid") && Util.hasText(benefinfo.getString("payerid"))){ //&& !benefinfo.has("payername")) {
                                    String payertype = benefinfo.getString("payerid");
                                    String payeridName = getPayertypeName(payertype, wsmodel.getLanguage());
                                    logger.info("PayerType: "+payertype);
                                    logger.info("PayerName: "+payeridName);
                                    benefinfo.put("payername", payeridName);
                                }*/
                                if (benefinfo.has("payerid") && !benefinfo.has("payername") ||
                                        (benefinfo.has("payername") && !Util.hasText(benefinfo.optString("payername")))) {
                                    String payertype = benefinfo.getString("payerid");
                                    String payeridName = getPayertypeName(payertype, wsmodel.getLanguage());
                                    logger.info("PayerType: "+payertype);
                                    logger.info("PayerName: "+payeridName);
                                    benefinfo.put("payername", payeridName);
                                }

                                beneflist.add(benefinfo.toString());
                            } else {
                                logger.error("Failed to get BenefObj against ID [" + bf.getId() + "], ignoring...");
                            }
                        } catch (Exception e) {
                            logger.error("Exception caught while getting Beneficiary ID [" + bf.getId() + "], ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }

                    }
                    wsmodel.setBeneficiariesdata(beneflist);
                } else {
                    logger.error("No Beneficiary Found for MobileNumber [" + wsmodel.getMobilenumber() + "]");
                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED); //Raza return OK always
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static String getPayertypeName(String payerid, String lang) {
        try{
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWPayersConfig.class.getName() + " c where c.payer_id= :payerid ";
            params = new HashMap<String, Object>();
            params.put("payerid", payerid);

            logger.info("DBQuery: "+dbQuery);
            logger.info("Params: "+params);
            MWPayersConfig payeridnames = (MWPayersConfig) GeneralDao.Instance.findObject(dbQuery, params);

            if(payeridnames != null){
                return (Util.hasText(lang) && lang.equals("FRA")) ? payeridnames.getFrpayer_name() : payeridnames.getPayer_name();
            } else {
                return "Unknown Payer";
            }
        }
        catch (Exception e){
            logger.error("Exception caught while getting PayerType agaisnt Payer Id [" + payerid + "], returning Unknown Payertype");
            logger.error(WebServiceUtil.getStrException(e));
            return "Unknown Payer";
        }
    }



    public static AppWsEntity ExecuteDynDeleteBeneficiary(AppWsEntity wsmodel) {

        try {
            logger.info("Executing DeleteDynBeneficiary Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                JSONObject benefobj = new JSONObject(wsmodel.getBeneficiaryobj());

                if (benefobj != null && benefobj.has("id") && Util.hasText(benefobj.getString("id"))) {
                    dbQuery = "from " + MWRemitBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.id= :ID ";
                    params = new HashMap<String, Object>();
                    params.put("MOBNO", wsmodel.getMobilenumber());
                    params.put("ID", Long.parseLong(benefobj.getString("id")));

                    MWRemitBeneficiary dbbenef = (MWRemitBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                    if (dbbenef != null) {
                        logger.info("Beneficiary found, deleting...");
                        dbbenef.setCustomer(null);
                        GeneralDao.Instance.saveOrUpdate(dbbenef);
                        GeneralDao.Instance.delete(dbbenef);

                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                    } else {
                        logger.error("Beneficiary not found against ID [" + benefobj.getString("id") + "] of Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                    }
                } else {
                    logger.error("Failed to get BeneficiaryObj from request, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                }
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
        }
        return wsmodel;
    }
    //Muhammad Hamza: Mobile Banking feature -- Start
    public static AppWsEntity ExecuteContactRM(AppWsEntity wsmodel) {

        try {
            logger.info("Executing " + wsmodel.getServicename() + " Request...");

            if(!Util.hasText(wsmodel.getSubject()) && wsmodel.getSubject() != null){
                logger.info("Subject["+ wsmodel.getSubject() +"] is missing in the Request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            if(!Util.hasText(wsmodel.getMessage()) && wsmodel.getMessage() != null){
                logger.info("Message["+ wsmodel.getMessage() +"] is missing in the Request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            try {
                if (!EmailGatewayHandler.CreateandSendEmailContactRM(CustomerType.CUSTOMER, SMSCategory.CONTACT_RM, wsmodel.getRmemail(), wsmodel)) {
                    logger.error("Failed to create & Send Email for EmailID [" + wsmodel.getEmailaddress() + "], ignoring...");
                    wsmodel.setRespcode(ISOResponseCodes.EMAIL_GATEWAY_DOWN);
                }
            }
            catch (Exception e){
                logger.error("Exception caght while sending email, ignoring...");
                logger.error(WebServiceUtil.getStrException(e));
            }

            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;

        } catch (Exception e) {

            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing ServiceRequest [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCreateEmailOTPRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing CreateEmailOTP Request...");

            if (Util.hasText(wsmodel.getReason()) && SystemConfig.SIGNUP_EMAIL_OTP.equals(wsmodel.getReason())) {
                logger.info("Direct email OTP in case of SignUp, skipping customer verification");

                if (!Util.hasText(wsmodel.getEmailaddress())) {
                    logger.error("Mandatory Field is missing in the request, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                    return wsmodel;
                }

                String dbQuery;
                Map<String, Object> params;
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.emailaddress= :EMAIL ";
                params = new HashMap<String, Object>();
                params.put("EMAIL", wsmodel.getEmailaddress());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (
                        customer != null &&
                        ((customer.getHaswallet() != null && customer.getHaswallet()) ||
                        (customer.getIsmigrated() != null && customer.getIsmigrated()) ||
                        (customer.getIsmigratedactive() != null && customer.getIsmigratedactive()))
                )
                {
                    logger.error(
                            "Email: {} already exists, cannot allow signup, rejecting...",
                            wsmodel.getEmailaddress()
                    );
                    wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_EMAIL);
                    return wsmodel;
                }

                if (!EmailGatewayHandler.CreateandSendEmailOTP
                        (
                            CustomerType.CUSTOMER,
                            Util.hasText(wsmodel.getReason()) ? wsmodel.getReason() : SMSCategory.OTP_CONFIRMATION,
                            wsmodel.getEmailaddress(),
                            wsmodel
                        )
                ) {
                    logger.error("Failed to create & Send OTP for Email [" + wsmodel.getEmailaddress() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.EMAIL_GATEWAY_DOWN);
                    return wsmodel;
                }

                wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;
            logger.info("Getting Email Address and Verifying Mobile Number....");
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer != null) {
                if (!Util.hasText(wsmodel.getEmailaddress())) {
                    wsmodel.setEmailaddress(customer.getEmailaddress());
                }
                wsmodel.setUserid(customer.getUserid());

                logger.info("Currently OTP Sending on this email[" + wsmodel.getEmailaddress() + "] instead of this[" + customer.getEmailaddress() + "], processing...");
            }

            if (!Util.hasText(wsmodel.getEmailaddress()) || !(Util.hasText(wsmodel.getUserid()))) {
                logger.error("Email Address[" + wsmodel.getEmailaddress() + "] or UserId[" + wsmodel.getUserid() + "] not found against Customer or Request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_DEST_EMAIL);
                return wsmodel;
            }

            String servicename = wsmodel.getServicename();
            try {
                wsmodel.setServicename("VerifyPin");
                MWWSOperation.SendToOpenAPI(wsmodel);
            } catch (Exception e) {
                logger.error("Exception caught while sending VerifyPin for checking Email Verified Status to OpenAPI, rejecting...");
                logger.error(WebServiceUtil.getStrException(e));
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            } finally {
                wsmodel.setServicename(servicename);
            }

            if (!wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                logger.error("Failed to get Email Status or verify PIN from Wallet, rejecting...");
                return wsmodel;
            }

            if (!EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, Util.hasText(wsmodel.getReason()) ? wsmodel.getReason() : SMSCategory.OTP_CONFIRMATION, wsmodel.getEmailaddress(), wsmodel)) {
                logger.error("Failed to create & Send OTP for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.EMAIL_GATEWAY_DOWN); //TODO: Raza Update This
                return wsmodel;
            }

            //Muhammad Hamza: Set Unverified Email Address logs
            logger.info("Setting Unverified Email Address feilds....");
            dbQuery = "from " + UnverifiedEmailsLog.class.getName() + " c where c.mobilenumber= :MOBNO and c.txnrefnum= :TXNTYPE";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());
            params.put("TXNTYPE", wsmodel.getTranrefnumber());

            UnverifiedEmailsLog emaillog = (UnverifiedEmailsLog) GeneralDao.Instance.findObject(dbQuery, params);

            if (emaillog != null) {
                logger.info("Update the previose fields, processing...");
                emaillog.setMobilenumber(wsmodel.getMobilenumber());
                emaillog.setOldemail(customer.getEmailaddress());
                emaillog.setUpdatedemail(Util.hasText(wsmodel.getEmailaddress()) ? wsmodel.getEmailaddress() : null);
                if (Util.hasText(wsmodel.getEmailaddress()) && wsmodel.getEmailaddress().equals(customer.getEmailaddress())) {
                    emaillog.setOldpreemailflag(true);
                } else {
                    emaillog.setOldpreemailflag(false);
                }
                emaillog.setIsexpired(false);
                emaillog.setIsverified(false);
                emaillog.setCreatedatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                emaillog.setVerifydatetime(null);
                emaillog.setUserid(customer.getUserid());
                emaillog.setTxnrefnum(wsmodel.getTranrefnumber());

                try{
                    GeneralDao.Instance.saveOrUpdate(emaillog);
                } catch (Exception e){
                    logger.error(WebServiceUtil.getStrException(e));
                }
            } else {
                logger.info("Set new fields for email Unverified log, processing...");
                UnverifiedEmailsLog unverifiedemailslog = new UnverifiedEmailsLog();
                unverifiedemailslog.setMobilenumber(wsmodel.getMobilenumber());
                unverifiedemailslog.setOldemail(customer.getEmailaddress());
                unverifiedemailslog.setUpdatedemail(Util.hasText(wsmodel.getEmailaddress()) ? wsmodel.getEmailaddress() : customer.getEmailaddress());
                if (Util.hasText(wsmodel.getEmailaddress()) && wsmodel.getEmailaddress().equals(customer.getEmailaddress())) {
                    unverifiedemailslog.setOldpreemailflag(true);
                }
                unverifiedemailslog.setIsexpired(false);
                unverifiedemailslog.setIsverified(false);
                unverifiedemailslog.setCreatedatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                unverifiedemailslog.setVerifydatetime(null);
                unverifiedemailslog.setUserid(customer.getUserid());
                unverifiedemailslog.setTxnrefnum(wsmodel.getTranrefnumber());

                try{
                    GeneralDao.Instance.saveOrUpdate(unverifiedemailslog);
                } catch (Exception e){
                    logger.error(WebServiceUtil.getStrException(e));
                }
            }


            wsmodel.setRespcode(ISOResponseCodes.MW_APPROVED);
            return wsmodel;


        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteConfirmEmailOTPRequest(AppWsEntity wsmodel) {

        String servicename = wsmodel.getServicename();
        try {
            logger.info("Executing ConfirmOtp Request...");

            String dbQuery;
            Map<String, Object> params;
            MWCustomer customer;

            // Added by Affan on 06-September-2023 Start
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer != null) {
                dbQuery = "from " + EmailOtpLog.class.getName() + " c where c.userid= :TO " + " and c.customertype = :CUSTTYPE " + " and c.isexpired = :ISEXPIRED and c.expirydatetime > :EXPIRY";
                params = new HashMap<String, Object>();
                params.put("TO", Util.hasText(customer.getUserid()) ? customer.getUserid() : wsmodel.getMobilenumber());
                params.put("CUSTTYPE", CustomerType.CUSTOMER);
                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                List<EmailOtpLog> emailOtpLogList = GeneralDao.Instance.find(dbQuery, params);

                if (emailOtpLogList != null && emailOtpLogList.size() > 0) {
                    for (EmailOtpLog otplog : emailOtpLogList) {
                        if (otplog.getOtp().equals(WSEncryptionUtil.EncryptAppOTP(wsmodel.getOtp())) && !otplog.getIsverified()) {
                            otplog.setIsverified(true);
                            otplog.setVerifydatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                            GeneralDao.Instance.saveOrUpdate(otplog);

                            if (otplog.getReason().equals(SMSCategory.VERIFY_EMAIL) || otplog.getReason().equals(SMSCategory.VEIRFY_CHANGE_EMAIL)) {
                                logger.info("OTP Confirmation for Email verified is complete, replying...");
                                wsmodel.setIsemailverified(true);

                                logger.info("Setting Pending Unverified Email Address feilds....");
                                dbQuery = "from " + UnverifiedEmailsLog.class.getName() + " c where c.txnrefnum= :TXNTYPE and c.userid= :USER";
                                params = new HashMap<String, Object>();
                                params.put("USER", otplog.getUserid());
                                params.put("TXNTYPE", otplog.getTxnrefnum());

                                UnverifiedEmailsLog emaillog = (UnverifiedEmailsLog) GeneralDao.Instance.findObject(dbQuery, params);

                                if (emaillog != null) {
                                    logger.info("Update the previose Status in Unverified Email Logs, processing...");

                                    emaillog.setIsexpired(true);
                                    emaillog.setIsverified(otplog.getIsverified());
                                    emaillog.setVerifydatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                                    if (!emaillog.getOldpreemailflag()) {
                                        wsmodel.setUpdateemailaddress(emaillog.getUpdatedemail());
                                        wsmodel.setEmailaddress(emaillog.getUpdatedemail());
                                    }
                                    GeneralDao.Instance.saveOrUpdate(emaillog);
                                } else {
                                    logger.info("There's no record found for Verified Email, ignoring...");
                                }
                                logger.info("Standalone Otp Confirmation Request Processed and Verified, replying...");
                                wsmodel.setServicename("UpdateEmail");
                                MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                                wsmodel.setServicename(servicename);

                                customer.setEmailaddress(emaillog.getUpdatedemail());
                                customer.setIsemailverified(true);
                                GeneralDao.Instance.saveOrUpdate(customer);

                                wsmodel.setRespcode(Util.hasText(wsmodel.getRespcode()) ? wsmodel.getRespcode() : ISOResponseCodes.APPROVED);
                                return wsmodel;
                            } else if (otplog.getReason().equals(SystemConfig.SIGNUP_EMAIL_OTP)) {
                                otplog.setIsexpired(false);
                                GeneralDao.Instance.saveOrUpdate(otplog);
                                logger.info("StandAlone OTP find for this email[" + wsmodel.getEmailaddress() + "] and MobileNumber[" + wsmodel.getMobilenumber() + "] for Reason[" + wsmodel.getReason() + "], successfully...");
                                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                return wsmodel;
                            }

                        } else {
                            logger.error("No Otp matched with provided [" + wsmodel.getOtp() + "] for customer [" + wsmodel.getEmailaddress() + "]");
                            wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                            return wsmodel;
                        }
                    }
                } else {
                    logger.error("No Otp found against Email [" + wsmodel.getEmailaddress() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                    return wsmodel;
                }

            } else {
                if (Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals(SystemConfig.SIGNUP_EMAIL_OTP)) {
                    logger.info("StandAlone OTP find for this email[" + wsmodel.getEmailaddress() + "] and MobileNumber[" + wsmodel.getMobilenumber() + "] for Reason[" + wsmodel.getReason() + "], processing...");

                    dbQuery = "from " + EmailOtpLog.class.getName() + " c where c.userid= :TO " + " and c.customertype = :CUSTTYPE " + " and c.isexpired = :ISEXPIRED and c.expirydatetime > :EXPIRY";
                    params = new HashMap<String, Object>();
                    params.put("TO", wsmodel.getMobilenumber());
                    params.put("CUSTTYPE", CustomerType.CUSTOMER);
                    params.put("ISEXPIRED", false);
                    params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                    List<EmailOtpLog> emailOtpLogList = GeneralDao.Instance.find(dbQuery, params);

                    if (emailOtpLogList != null && emailOtpLogList.size() > 0) {
                        for (EmailOtpLog otplog : emailOtpLogList) {
                            if (otplog.getOtp().equals(WSEncryptionUtil.EncryptAppOTP(wsmodel.getOtp())) && !otplog.getIsverified()) {
                                otplog.setIsverified(true);
                                otplog.setIsexpired(false);
                                otplog.setVerifydatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
                                GeneralDao.Instance.saveOrUpdate(otplog);
                                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                return wsmodel;
                            } else {
                                logger.error("No Otp matched with provided [" + wsmodel.getOtp() + "] for customer [" + wsmodel.getEmailaddress() + "]");
                                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_OTP);
                                return wsmodel;
                            }
                        }
                    } else {
                        logger.error("NO OTP match for this email, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.EMAIL_NOT_VERIFIED);
                    }
                } else {
                    logger.error("Customer Mobile[" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }
            }

        } catch (Exception e) {
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setServicename(servicename);
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        } finally {
            wsmodel.setServicename(servicename);
        }
        return wsmodel;
    }

    public static AppWsEntity ExecuteCreateBeneficiary(AppWsEntity wsmodel) {

        try {
            logger.info("Executing CreateBeneficiary Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
//                dbQuery += " and c.id= :ID";
//                params.put("ID", Long.parseLong(wsmodel.getBeneficiaryid()));
                String consumerno = wsmodel.getMobilenumber();

                if (Util.hasText(wsmodel.getDestaccountnumber())) {
                    dbQuery += " and c.destaccountnumber= :DESTACCT";
                    params.put("DESTACCT", wsmodel.getDestaccountnumber());
                }
                if (Util.hasText(wsmodel.getTransactiontype())) {
                    dbQuery += " and c.transactiontype= :TRANTYPE";
                    params.put("TRANTYPE", wsmodel.getTransactiontype());
                }
                if (Util.hasText(wsmodel.getUtilcompanyid())) {
                    dbQuery += " and c.utilcompany= :BILLERID";
                    params.put("BILLERID", wsmodel.getUtilcompanyid());
                }

//                if (Util.hasText(wsmodel.getBeneficiaryid())) {
//                    dbQuery += " and c.id= :ID";
//                    params.put("ID", Long.parseLong(wsmodel.getBeneficiaryid()));
//                }
//
//                if (Util.hasText(wsmodel.getBeneficiaryname())) {
//                    dbQuery += " and c.beneficiaryname= :BENENAME";
//                    params.put("BENENAME", wsmodel.getBeneficiaryname());
//                }
//                if(Util.hasText(wsmodel.getDestfirstname()) && Util.hasText(wsmodel.getDestlastname())){
//                    dbQuery += " and c.firstname= :FSTNAME ";
//                    params.put("FSTNAME", wsmodel.getDestfirstname());
//
//                    dbQuery += " and c.lastname= :LSTNAME ";
//                    params.put("LSTNAME", wsmodel.getDestlastname());
//                }

                MWBeneficiary dbbenef = (MWBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbbenef != null) {
                    logger.error("Beneficiary ALready Exist with TransactionType[" + wsmodel.getTransactiontype() + "]and DestAccountNumber["+wsmodel.getDestaccountnumber() +"]found in DB, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.BEENFICIARY_ALREADY_EXISTS);
                    return wsmodel;

                } else {
/*
                    if (Util.hasText(wsmodel.getDestaccountnumber())) {
                        dbQuery += " and c.destaccountnumber= :DESTACCT";
                        params.put("DESTACCT", wsmodel.getDestaccountnumber());
                    }
                    if (Util.hasText(wsmodel.getTransactiontype())) {
                        dbQuery += " and c.transactiontype= :TRANTYPE";
                        params.put("TRANTYPE", wsmodel.getTransactiontype());
                    }
                    dbbenef = (MWBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);
                    if (dbbenef != null) {
                        logger.error("Beneficiary ALready Exist with TransactionType[" + wsmodel.getTransactiontype() + "]and DestAccountNumber["+wsmodel.getDestaccountnumber() +"]found in DB, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.BEENFICIARY_ALREADY_EXISTS);
                        return wsmodel;
                    } else {*/
                        if (!wsmodel.getBeneficiarytype().equals(wsmodel.getTransactiontype())) {
                            logger.error("Beneficiary type and transaction type should be the same.");
                            wsmodel.setRespcode(ISOResponseCodes.BENE_TRAN_TYPE_SAME);
                            return wsmodel;
                        }
                        dbbenef = new MWBeneficiary();
                        dbbenef.setConsumerno(consumerno);
                        dbbenef.setUtilcompany(wsmodel.getUtilcompanyid());
                        dbbenef.setFirstname((Util.hasText(wsmodel.getDestfirstname())) ? wsmodel.getDestfirstname().trim() : null);
                        dbbenef.setLastname((Util.hasText(wsmodel.getDestlastname())) ? wsmodel.getDestlastname().trim() : null);
                        dbbenef.setCountry(wsmodel.getDestcountry());
                        dbbenef.setMobilenumber(wsmodel.getDestmobilenumber());
                        dbbenef.setIban(wsmodel.getIban());
                        dbbenef.setAccount(wsmodel.getDestaccount());
                        dbbenef.setSwiftcode(wsmodel.getSwiftbiccode());
                        dbbenef.setIfscode(wsmodel.getIfscode());
                        dbbenef.setPackagecode(wsmodel.getPackagecode());
                        dbbenef.setIdtype(wsmodel.getIdentificationtype());
                        dbbenef.setIdcode(Util.hasText(wsmodel.getIdentificationno()) ? wsmodel.getIdentificationno().trim() : null);
                        dbbenef.setSortcode(wsmodel.getBankcode());
                        dbbenef.setCardnumber(wsmodel.getCardnumber());
                        dbbenef.setCustomer(customer);
                        dbbenef.setCreditaccount(wsmodel.getCreditaccountnumber());
                        dbbenef.setClabe(wsmodel.getClabe());
                        dbbenef.setCbu(wsmodel.getCbu());
                        dbbenef.setCbualias(wsmodel.getCbualias());
                        dbbenef.setBikcode(wsmodel.getBikcode());
                        dbbenef.setAbaroutingnumber(wsmodel.getAbaroutingnumber());
                        dbbenef.setBsbnumber(wsmodel.getBsbnumber());
                        dbbenef.setRoutingcode(wsmodel.getRoutingcode());
                        dbbenef.setEntityttid(wsmodel.getEntityttid());
                        dbbenef.setAccounttype(wsmodel.getAccounttype());
                        dbbenef.setDestaddress(wsmodel.getDestaddress());
                        dbbenef.setPayerid(wsmodel.getPayerid());
                        dbbenef.setPayertype(wsmodel.getPayertype());

                        //Mobile Banking Features --Start
                        dbbenef.setSourceaccount(wsmodel.getSourceaccount());
                        dbbenef.setSourcetitle(wsmodel.getSourcetitle());
                        dbbenef.setDestaccountnumber(wsmodel.getDestaccountnumber());
                        dbbenef.setDestaccounttitle(wsmodel.getDestaccounttitle());
                        dbbenef.setBeneficiaryname(wsmodel.getBeneficiaryname());
                        dbbenef.setBeneficiarytype(wsmodel.getBeneficiarytype());
                        dbbenef.setTransactiontype(wsmodel.getTransactiontype());
                        dbbenef.setCurrency(wsmodel.getCurrency());
                        dbbenef.setBankcode(wsmodel.getBankcode());
                        dbbenef.setBankname(wsmodel.getBankname());
                        dbbenef.setCity(wsmodel.getCity());
                        //Mobile Banking Features -- End

                        GeneralDao.Instance.saveOrUpdate(dbbenef);

                        //for Beneficiary ID show in Response
                        wsmodel.setBeneficiaryid(String.valueOf(dbbenef.getId()));

                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteUpdateBeneficiaries(AppWsEntity wsmodel) {

        try {
            logger.info("Executing UpdateBeneficiary Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                dbQuery += " and c.id= :ID";
                params.put("ID", Long.parseLong(wsmodel.getBeneficiaryid()));

                MWBeneficiary dbbenef = (MWBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbbenef != null) {
                    dbbenef.setFirstname((Util.hasText(wsmodel.getDestfirstname())) ? wsmodel.getDestfirstname().trim() : null);
                    dbbenef.setLastname((Util.hasText(wsmodel.getDestlastname())) ? wsmodel.getDestlastname().trim() : null);
                    dbbenef.setCountry(wsmodel.getDestcountry());
                    dbbenef.setMobilenumber(wsmodel.getDestmobilenumber());
                    dbbenef.setIban(wsmodel.getIban());
                    dbbenef.setAccount(wsmodel.getDestaccount());
                    dbbenef.setSwiftcode(wsmodel.getSwiftbiccode());
                    dbbenef.setIfscode(wsmodel.getIfscode());
                    dbbenef.setPackagecode(wsmodel.getPackagecode());
                    dbbenef.setIdtype(wsmodel.getIdentificationtype());
                    dbbenef.setIdcode(Util.hasText(wsmodel.getIdentificationno()) ? wsmodel.getIdentificationno().trim() : null);
                    dbbenef.setSortcode(wsmodel.getBankcode());
                    dbbenef.setCardnumber(wsmodel.getCardnumber());
                    dbbenef.setCreditaccount(wsmodel.getCreditaccountnumber());
                    dbbenef.setClabe(wsmodel.getClabe());
                    dbbenef.setCbu(wsmodel.getCbu());
                    dbbenef.setCbualias(wsmodel.getCbualias());
                    dbbenef.setBikcode(wsmodel.getBikcode());
                    dbbenef.setAbaroutingnumber(wsmodel.getAbaroutingnumber());
                    dbbenef.setBsbnumber(wsmodel.getBsbnumber());
                    dbbenef.setRoutingcode(wsmodel.getRoutingcode());
                    dbbenef.setEntityttid(wsmodel.getEntityttid());
                    dbbenef.setAccounttype(wsmodel.getAccounttype());
                    dbbenef.setDestaddress(wsmodel.getDestaddress());
                    dbbenef.setPayerid(wsmodel.getPayerid());
                    dbbenef.setPayertype(wsmodel.getPayertype());
                    dbbenef.setBranchnumber(wsmodel.getBranchcode());
                    dbbenef.setEmail(wsmodel.getEmailaddress());
                    dbbenef.setUtilcompany(wsmodel.getUtilcompanyid());

                    //Mobile Banking Features -- Start
                    dbbenef.setSourceaccount(wsmodel.getSourceaccount());
                    dbbenef.setSourcetitle(wsmodel.getSourcetitle());
                    dbbenef.setDestaccountnumber(wsmodel.getDestaccountnumber());
                    dbbenef.setDestaccounttitle(wsmodel.getDestaccounttitle());
                    dbbenef.setBeneficiaryname(wsmodel.getBeneficiaryname());
//                    dbbenef.setBeneficiarytype(wsmodel.getBeneficiarytype());
                    dbbenef.setTransactiontype(wsmodel.getTransactiontype());
                    dbbenef.setCurrency(wsmodel.getCurrency());
                    dbbenef.setBankcode(wsmodel.getBankcode());
                    dbbenef.setBankname(wsmodel.getBankname());
                    dbbenef.setCity(wsmodel.getCity());
                    //Mobile Banking Features -- End

                    GeneralDao.Instance.saveOrUpdate(dbbenef);
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                } else {
                    logger.error("No Beneficiary Found for customer [" + wsmodel.getMobilenumber() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                }

                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetBeneficiariesList(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetBeneficiaries Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());

                if(Util.hasText(wsmodel.getTransactiontype()) && wsmodel.getTransactiontype() != null){
                    dbQuery += " and c.transactiontype= :TRANTYPE ";
                    params.put("TRANTYPE", wsmodel.getTransactiontype());
                }
//                if(Util.hasText(wsmodel.getBeneficiaryid())){
//                    dbQuery += " and c.id= :ID";
//                    params.put("ID", Long.parseLong(wsmodel.getBeneficiaryid()));
//                }

                if (Util.hasText(wsmodel.getUtilcompanyid())) {
                    dbQuery += " and c.utilcompany= :BILLERID";
                    params.put("BILLERID", wsmodel.getUtilcompanyid());
                }

                if (Util.hasText(wsmodel.getBeneficiaryname())) {
                    dbQuery += " and c.beneficiaryname= :BENENAME";
                    params.put("BENENAME", wsmodel.getBeneficiaryname());
                }

                if(Util.hasText(wsmodel.getBeneficiaryid())){
                    dbQuery += " and c.id= :BENEID ";
                    params.put("BENEID", Long.parseLong(wsmodel.getBeneficiaryid()));
                }

                List<MWBeneficiary> dbbeneflist = GeneralDao.Instance.find(dbQuery, params);

                if (dbbeneflist != null && dbbeneflist.size() > 0) {
                    List<BenefciaryObj> beneflist = new ArrayList<>();
                    for (MWBeneficiary bf : dbbeneflist) {
                        BenefciaryObj obj = new BenefciaryObj();
                        obj.setConsumerno(bf.getConsumerno());
                        if (Util.hasText(bf.getDestaddress())) {
                            obj.setDestaddress(bf.getDestaddress());
                        }
                        if (Util.hasText(bf.getPayerid())) {
                            obj.setPayerid(bf.getPayerid());
                        }
                        if (Util.hasText(bf.getPayertype())) {
                            obj.setPayertype(bf.getPayertype());
                        }
                        obj.setFirstname(bf.getFirstname());
                        obj.setLastname(bf.getLastname());
                        obj.setCountry(bf.getCountry());
                        obj.setAccount(bf.getAccount());
                        obj.setIban(bf.getIban());
                        obj.setMobile(bf.getMobilenumber());
                        obj.setSwiftcode(bf.getSwiftcode());
                        obj.setIfscode(bf.getIfscode());
                        obj.setPackagecode(bf.getPackagecode());
                        obj.setIdtype(bf.getIdtype());
                        obj.setIdcode(bf.getIdcode());
                        obj.setSortcode(bf.getSortcode());
                        obj.setCardnumber(bf.getCardnumber());
                        obj.setCreditaccount(bf.getCreditaccount());
                        //Raza adding on 13-05-2022 start
                        obj.setUtilcompany(bf.getUtilcompany());
                        obj.setClabe(bf.getClabe());
                        obj.setCbu(bf.getCbu());
                        obj.setCbualias(bf.getCbualias());
                        obj.setBikcode(bf.getBikcode());
                        obj.setAbaroutingnumber(bf.getAbaroutingnumber());
                        obj.setBsbnumber(bf.getBsbnumber());
                        obj.setRoutingcode(bf.getRoutingcode());
                        obj.setEntityttid(bf.getEntityttid());
                        obj.setAccounttype(bf.getAccounttype());
                        //Raza adding on 13-05-2022 end

                        //Muhammad Hamza: Mobile Banking features --Start
                        obj.setBeneficiaryname(bf.getBeneficiaryname());
//                        obj.setBeneficiarytype(bf.getBeneficiarytype());
                        obj.setDestaccountnumber(bf.getDestaccountnumber());
                        obj.setDestaccounttitle(bf.getDestaccounttitle());
                        obj.setSourceaccount(bf.getSourceaccount());
                        obj.setSourcetitle(bf.getSourcetitle());
                        obj.setTransactiontype(bf.getTransactiontype());
                        obj.setId(String.valueOf(bf.getId()));
                        obj.setCurrency(bf.getCurrency());
                        obj.setBankcode(bf.getBankcode());
                        obj.setBankname(bf.getBankname());
                        obj.setCity(bf.getCity());
                        //Muhammad Hamza: Mobile Banking features --End


                        beneflist.add(obj);
                    }
                    wsmodel.setBeneficiarylist(beneflist);
                }
                else {
                    logger.error("No Beneficiary Found for customer [" + wsmodel.getMobilenumber() + "]");
                }
                if(wsmodel.getBeneficiarylist() != null && wsmodel.getBeneficiarylist().size() > 0)
                {
//            logger.info("Supported Transaction Types:["+ wsmodel.getTransactiontypeslist().get(0).getName() +"]");
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                }
                else
                {
                    logger.error("No Transaction Type List found in DB, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                }
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteDeleteBeneficiaries(AppWsEntity wsmodel) {

        try {
            logger.info("Executing DeleteBeneficiaries Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {
                dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO " + " and c.id= :BENEFID ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("BENEFID", Long.parseLong(wsmodel.getBeneficiaryid()));

//                if (Util.hasText(wsmodel.getConsumerno()) && !wsmodel.getConsumerno().equals(wsmodel.getMobilenumber()))
//                {
//                    dbQuery += " and c.consumerno= :BENEFICIARY ";
//                    params.put("BENEFICIARY", wsmodel.getConsumerno());
//                }
//                if(Util.hasText(wsmodel.getDestfirstname()) && Util.hasText(wsmodel.getDestlastname())){
//                    dbQuery += " and c.firstname= :FIRSTNAME and c.lastname= :LASTNAME ";
//                    params.put("FIRSTNAME", wsmodel.getDestfirstname());
//                    params.put("LASTNAME", wsmodel.getDestlastname());
//                }

                MWBeneficiary dbbenef = (MWBeneficiary) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbbenef != null) {
                    logger.info("Beneficiary found, deleting...");
                    dbbenef.setCustomer(null);
                    GeneralDao.Instance.saveOrUpdate(dbbenef);
                    GeneralDao.Instance.delete(dbbenef);
                } else {
                    logger.error("No Beneficiary Found for"+ (Util.hasText(wsmodel.getBeneficiaryid()) ? " ID[" + wsmodel.getBeneficiaryid() + "]": "") + " customer [" + wsmodel.getMobilenumber() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                    return wsmodel;
                }
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteBeneficiariesDetails(AppWsEntity wsmodel) {

        try {
            logger.info("Executing BeneficiariesDetails Request...");

            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) {

                if (Util.hasText(wsmodel.getAccountnumber()) && (Util.hasText(wsmodel.getTransactiontype()) && wsmodel.getTransactiontype().equals("Rawbank Account")))
                {
                    MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
                }
                else if (Util.hasText(wsmodel.getAccountnumber()) && (Util.hasText(wsmodel.getTransactiontype()) && wsmodel.getTransactiontype().equals("Wallet")))
                {

                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
                    params = new HashMap<String, Object>();
                    params.put("MOB", wsmodel.getAccountnumber());

                    MWCustomer customertwo = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                    if (customertwo != null)
                    {
                            List<BenefciaryObj> beneflist = new ArrayList<>();
                                BenefciaryObj obj = new BenefciaryObj();
                                //obj.setConsumerno(bf.getConsumerno());
                                if (Util.hasText(customertwo.getAddress())) {
                                    obj.setDestaddress(customertwo.getAddress());
                                }
//                                if (Util.hasText(bf.getPayerid())) {
//                                    obj.setPayerid(bf.getPayerid());
//                                }
//                                if (Util.hasText(bf.getPayertype())) {
//                                    obj.setPayertype(bf.getPayertype());
//                                }
                                obj.setFirstname(customertwo.getFirstname());
                                obj.setLastname(customertwo.getLastname());
                                obj.setCountry(customertwo.getCountry());
                                //obj.setAccount(customertwo.getAccount());
                                //obj.setIban(bf.getIban());
                                obj.setMobile(customertwo.getMobilenumber());
                                obj.setEmail(customertwo.getEmailaddress());
                                //obj.setSwiftcode(bf.getSwiftcode());
                                //obj.setIfscode(bf.getIfscode());
                                //obj.setPackagecode(bf.getPackagecode());
                                //obj.setIdtype(bf.getIdtype());
                               //obj.setIdcode(bf.getIdcode());
                                //obj.setSortcode(bf.getSortcode());
                                //obj.setCardnumber(bf.getCardnumber());
                                //setCreditaccount(bf.getCreditaccount());
                                //Raza adding on 13-05-2022 start
                                //obj.setUtilcompany(bf.getUtilcompany());
                                //obj.setClabe(bf.getClabe());
                                //obj.setCbu(bf.getCbu());
                                //obj.setCbualias(bf.getCbualias());
                                //obj.setBikcode(bf.getBikcode());
                                //obj.setAbaroutingnumber(bf.getAbaroutingnumber());
                                //obj.setBsbnumber(bf.getBsbnumber());
                                //obj.setRoutingcode(bf.getRoutingcode());
                                //obj.setEntityttid(bf.getEntityttid());
                                //obj.setAccounttype(bf.getAccounttype());
                                //Raza adding on 13-05-2022 end

                                //Muhammad Hamza: Mobile Banking features --Start
                                obj.setBeneficiaryname(customertwo.getFirstname()+" "+customertwo.getLastname());
                                //obj.setBeneficiarytype(bf.getBeneficiarytype());
                                obj.setDestaccountnumber(customertwo.getMobilenumber());
                                obj.setDestaccounttitle(customertwo.getUsername());
//                                obj.setSourceaccount(bf.getSourceaccount());
//                                obj.setSourcetitle(bf.getSourcetitle());
//                                obj.setTransactiontype(bf.getTransactiontype());
//                                obj.setId(String.valueOf(bf.getId()));
//                                obj.setCurrency(bf.getCurrency());
//                                obj.setBankcode(bf.getBankcode());
//                                obj.setBankname(bf.getBankname());
//                                obj.setCity(bf.getCity());
                                //Muhammad Hamza: Mobile Banking features --End


                                beneflist.add(obj);

                            wsmodel.setBeneficiarylist(beneflist);

                    }
                    else{
                        logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                        return wsmodel;
                    }
                }
                else
                {
                    dbQuery = "from " + MWBeneficiary.class.getName() + " c where c.customer.mobilenumber= :MOBNO ";
                    params = new HashMap<String, Object>();
                    params.put("MOBNO", wsmodel.getMobilenumber());

                    if(Util.hasText(wsmodel.getTransactiontype()) && wsmodel.getTransactiontype() != null){
                        dbQuery += " and c.transactiontype= :TRANTYPE ";
                        params.put("TRANTYPE", wsmodel.getTransactiontype());
                    }

                    List<MWBeneficiary> dbbeneflist = GeneralDao.Instance.find(dbQuery, params);

                    if (dbbeneflist != null && dbbeneflist.size() > 0) {
                        List<BenefciaryObj> beneflist = new ArrayList<>();
                        for (MWBeneficiary bf : dbbeneflist) {
                            BenefciaryObj obj = new BenefciaryObj();
                            obj.setConsumerno(bf.getConsumerno());
                            if (Util.hasText(bf.getDestaddress())) {
                                obj.setDestaddress(bf.getDestaddress());
                            }
                            if (Util.hasText(bf.getPayerid())) {
                                obj.setPayerid(bf.getPayerid());
                            }
                            if (Util.hasText(bf.getPayertype())) {
                                obj.setPayertype(bf.getPayertype());
                            }
                            obj.setFirstname(bf.getFirstname());
                            obj.setLastname(bf.getLastname());
                            obj.setCountry(bf.getCountry());
                            obj.setAccount(bf.getAccount());
                            obj.setIban(bf.getIban());
                            obj.setMobile(bf.getMobilenumber());
                            obj.setSwiftcode(bf.getSwiftcode());
                            obj.setIfscode(bf.getIfscode());
                            obj.setPackagecode(bf.getPackagecode());
                            obj.setIdtype(bf.getIdtype());
                            obj.setIdcode(bf.getIdcode());
                            obj.setSortcode(bf.getSortcode());
                            obj.setCardnumber(bf.getCardnumber());
                            obj.setCreditaccount(bf.getCreditaccount());
                            //Raza adding on 13-05-2022 start
                            obj.setUtilcompany(bf.getUtilcompany());
                            obj.setClabe(bf.getClabe());
                            obj.setCbu(bf.getCbu());
                            obj.setCbualias(bf.getCbualias());
                            obj.setBikcode(bf.getBikcode());
                            obj.setAbaroutingnumber(bf.getAbaroutingnumber());
                            obj.setBsbnumber(bf.getBsbnumber());
                            obj.setRoutingcode(bf.getRoutingcode());
                            obj.setEntityttid(bf.getEntityttid());
                            obj.setAccounttype(bf.getAccounttype());
                            //Raza adding on 13-05-2022 end

                            //Muhammad Hamza: Mobile Banking features --Start
                            obj.setBeneficiaryname(bf.getBeneficiaryname());
                            obj.setBeneficiarytype(bf.getBeneficiarytype());
                            obj.setDestaccountnumber(bf.getDestaccountnumber());
                            obj.setDestaccounttitle(bf.getDestaccounttitle());
                            obj.setSourceaccount(bf.getSourceaccount());
                            obj.setSourcetitle(bf.getSourcetitle());
                            obj.setTransactiontype(bf.getTransactiontype());
                            obj.setId(String.valueOf(bf.getId()));
                            obj.setCurrency(bf.getCurrency());
                            obj.setBankcode(bf.getBankcode());
                            obj.setBankname(bf.getBankname());
                            obj.setCity(bf.getCity());
                            //Muhammad Hamza: Mobile Banking features --End


                            beneflist.add(obj);
                        }
                        wsmodel.setBeneficiarylist(beneflist);
                    }
                    else {
                        logger.error("No Beneficiary Found for" + (Util.hasText(wsmodel.getAccountnumber()) ? " AccountNumber [" + wsmodel.getAccountnumber() + "]" : "") + " customer [" + wsmodel.getMobilenumber() + "], and" + (Util.hasText(wsmodel.getTransactiontype()) ? " Transaction Type [" + wsmodel.getTransactiontype() + "]" : ""));
                    }
                }

                if (wsmodel.getBeneficiarylist() != null && wsmodel.getBeneficiarylist().size() > 0) {
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                } else {
                    logger.error("No Beneficairy List found in DB, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_BENEFICIARY);
                }
                return wsmodel;
            } else {
                logger.error("Customer with mobile [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return wsmodel;
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    //Muhammad Hamza: Mobile Banking feature -- End

    //Muhammad Hamza: E-Ticketing -- Start
    public static AppWsEntity ExecuteGetServices(AppWsEntity wsmodel){

        try {
            String servicename = wsmodel.getServicename();
            Channel eticketserver = GlobalContext.getInstance().getChannelbyId(ChannelCodes.ETICKETING_INTERGRATION);
            if (eticketserver == null) {
                logger.error("E-Ticketing Server [" + ChannelCodes.ETICKETING_INTERGRATION + "] Not found in DB..");
                wsmodel.setRespcode(ISOResponseCodes.BANK_LINK_DOWN);
                return wsmodel;
            }
            logger.info("Eticket Server: "+eticketserver);
//            wsmodel.setServicename("GetEventList");
//            ETicketingHandler.executeRequest(wsmodel, eticketserver);
//            ETicketingOperation.GetEventList(wsmodel, eticketserver);

            wsmodel.setServicename(servicename);
            wsmodel.setRespcode(Util.hasText(wsmodel.getRespcode()) ? wsmodel.getRespcode() : ISOResponseCodes.APPROVED);

        }
        catch(Exception e)
        {
            logger.error("Exception caught while getting Events List, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

        }

        return wsmodel;
    }

    public static AppWsEntity ExecuteGetServicesTags(AppWsEntity wsmodel){

        try {
            String servicename = wsmodel.getServicename();
            Channel eticketserver = GlobalContext.getInstance().getChannelbyId(ChannelCodes.ETICKETING_INTERGRATION);
            if (eticketserver == null) {
                logger.error("E-Ticketing Server [" + ChannelCodes.ETICKETING_INTERGRATION + "] Not found in DB..");
                wsmodel.setRespcode(ISOResponseCodes.BANK_LINK_DOWN);
                return wsmodel;
            }
            logger.info("Eticket Server: "+eticketserver);
//            wsmodel.setServicename("GetEventList");
//            ETicketingHandler.executeRequest(wsmodel, eticketserver);
//            ETicketingOperation.GetEventListTags(wsmodel, eticketserver);

            wsmodel.setServicename(servicename);
            wsmodel.setRespcode(Util.hasText(wsmodel.getRespcode()) ? wsmodel.getRespcode() : ISOResponseCodes.APPROVED);

        }
        catch(Exception e)
        {
            logger.error("Exception caught while getting Events List, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

        }

        return wsmodel;
    }
    public static AppWsEntity ExecuteGetServiceDetails(AppWsEntity wsmodel){

        try {
            String servicename = wsmodel.getServicename();
            Channel eticketserver = GlobalContext.getInstance().getChannelbyId(ChannelCodes.ETICKETING_INTERGRATION);
            if (eticketserver == null) {
                logger.error("E-Ticketing Server [" + ChannelCodes.ETICKETING_INTERGRATION + "] Not found in DB..");
                wsmodel.setRespcode(ISOResponseCodes.BANK_LINK_DOWN);
                return wsmodel;
            }
            logger.info("Eticket Server: "+eticketserver);
//            wsmodel.setServicename("GetEventList");
//            ETicketingHandler.executeRequest(wsmodel, eticketserver);
//            ETicketingOperation.GetEventsDetails(wsmodel, eticketserver);

            wsmodel.setServicename(servicename);
            wsmodel.setRespcode(Util.hasText(wsmodel.getRespcode()) ? wsmodel.getRespcode() : ISOResponseCodes.APPROVED);

        }
        catch(Exception e)
        {
            logger.error("Exception caught while getting Events List, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

        }

        return wsmodel;
    }
    public static AppWsEntity ExecuteGetEventInvoice(AppWsEntity wsmodel)
    {
        try {
            String servicename = wsmodel.getServicename();
            Channel eticketserver = GlobalContext.getInstance().getChannelbyId(ChannelCodes.ETICKETING_INTERGRATION);
            if (eticketserver == null) {
                logger.error("E-Ticketing Server [" + ChannelCodes.ETICKETING_INTERGRATION + "] Not found in DB..");
                wsmodel.setRespcode(ISOResponseCodes.BANK_LINK_DOWN);
                return wsmodel;
            }
            logger.info("Eticket Server: "+eticketserver);
//            wsmodel.setServicename("GetEventList");
//            ETicketingHandler.executeRequest(wsmodel, eticketserver);
//            ETicketingOperation.BookTicket(wsmodel, eticketserver);

            wsmodel.setServicename(servicename);
            wsmodel.setRespcode(Util.hasText(wsmodel.getRespcode()) ? wsmodel.getRespcode() : ISOResponseCodes.APPROVED);

        }
        catch(Exception e)
        {
            logger.error("Exception caught while getting Events List, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);

        }

        return wsmodel;
    }
    public static AppWsEntity ExecuteConfirmInvoicePayment(AppWsEntity wsmodel){
        return wsmodel;
    }
    public static AppWsEntity ExecuteGetInvoiceDetail(AppWsEntity wsmodel){
        return wsmodel;
    }

    //Muhammad Hamza: E-Ticketing -- End



    public static AppWsEntity ExecuteMWServiceRequest(AppWsEntity wsmodel, boolean ifxreq) {

        try {
            logger.info("Executing " + wsmodel.getServicename() + " Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            if(Util.hasText(wsmodel.getServicename()) && !wsmodel.getServicename().equals("GetTitleFetchOut"))
            {
                if (Util.hasText(wsmodel.getDestmobilenumber()) && !Util.hasText(wsmodel.getDestuserid())) {
                    String dbQuery;
                    Map<String, Object> params;
                    logger.info("Getting and verifying destination Mobile Number....");
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
                    params = new HashMap<String, Object>();
                    params.put("MOBNO", wsmodel.getDestmobilenumber());

                    MWCustomer destcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                    //TODO: Raza not verifying dest customer state or status, wallet will decide...
                    if (destcustomer != null) //&& destcustomer.getHaswallet() && destcustomer.getStatus().equals(CustomerStatus.ACTIVE))
                    {
                        wsmodel.setDestuserid(destcustomer.getUserid());
                        wsmodel.setDestusername(destcustomer.getUsername());
                        if (!Util.hasText(wsmodel.getDestfirstname())) {
                            wsmodel.setDestfirstname(destcustomer.getFirstname());
                        }
                        if (!Util.hasText(wsmodel.getDestlastname())) {
                            wsmodel.setDestlastname(destcustomer.getLastname());
                        }
                        wsmodel.setDestlanguage(destcustomer.getNotiflanguage());
                        if (!wsmodel.getServicename().equals("BuyVCNGiftCard")) {
                            wsmodel.setDestemailaddress(destcustomer.getEmailaddress());
                        }
                    }
                }
            }

            if (Util.hasText(wsmodel.getQrid()) && !Util.hasText(wsmodel.getDestuserid())) {
                String dbQuery;
                Map<String, Object> params;
                logger.info("Getting and verifying destination User by QRID....");
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.userid= :QRID ";
                params = new HashMap<String, Object>();
                params.put("QRID", wsmodel.getQrid());

                MWCustomer destcustomer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                //TODO: Raza not verifying dest customer state or status, wallet will decide...
                if (destcustomer != null) //&& destcustomer.getHaswallet() && destcustomer.getStatus().equals(CustomerStatus.ACTIVE))
                {
                    if (destcustomer.getMobilenumber().equals(wsmodel.getMobilenumber())) {
                        logger.error("QRSendMoney not allowed on own QR [" + wsmodel.getQrid() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                        return wsmodel;
                    }


                    wsmodel.setDestuserid(destcustomer.getUserid()); //TODO: Raza can also set received QRID??
                    wsmodel.setDestmobilenumber(destcustomer.getMobilenumber());
                    wsmodel.setDestusername(destcustomer.getUsername());
                    if (!Util.hasText(wsmodel.getDestfirstname())) {
                        wsmodel.setDestfirstname(destcustomer.getFirstname());
                    }
                    if (!Util.hasText(wsmodel.getDestlastname())) {
                        wsmodel.setDestlastname(destcustomer.getLastname());
                    }
                    wsmodel.setDestlanguage(destcustomer.getNotiflanguage());
                    if (!wsmodel.getServicename().equals("BuyVCNGiftCard")) {
                        wsmodel.setDestemailaddress(destcustomer.getEmailaddress());
                    }
                }
            }


            SendToOpenAPI(wsmodel);

            if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                if (Util.hasText(wsmodel.getServicename()) && (wsmodel.getServicename().equals("QRSendMoney") && Util.hasText(wsmodel.getMerchantcode()))) {
                    logger.error("Changing service from " + wsmodel.getServicename() + " to MerchantQRSendMoney"); // waleed changing service name on identification of message
                    wsmodel.setServicename("MerchantQRSendMoney");
                }

                if (Util.hasText(wsmodel.getTrantype())) {
                    wsmodel.setApiname(GlobalContext.getInstance().getAPIByTranType(wsmodel.getTrantype(), "ENG"));
                    wsmodel.setFrapiname(GlobalContext.getInstance().getAPIByTranType(wsmodel.getTrantype(), "FRA"));
                }
                logger.info("Approved Response received from OpenAPI, replying...");

                if (!wsmodel.getServicename().equals("CreateWallet")
                        || (wsmodel.getServicename().equals("CreateWallet")
                        && Util.hasText(wsmodel.getPartialflag())
                        && !wsmodel.getPartialflag().equals("true"))) {
                    //Raza updating ConfirmBankOTP flow 06-05-2021
                    if (wsmodel.getServicename().equals("ConfirmBankOtp") && Util.hasText(wsmodel.getOriginalapi())) {
                        String servicename = wsmodel.getServicename();
                        wsmodel.setServicename(wsmodel.getOriginalapi());
                        CheckandSendSMS(wsmodel);
                        CheckandSendEmail(wsmodel);
                        wsmodel.setServicename(servicename);
                    }
                    else if (wsmodel.getServicename().equals("BankToWallet")
                            || wsmodel.getServicename().equals("BankToAlias")
                            || wsmodel.getServicename().equals("LoadWallet")
                            || wsmodel.getServicename().equals("BankToBank") || wsmodel.getServicename().equals("BankForexPurchase"))
                    {
                        String servicename = wsmodel.getServicename();
                        wsmodel.setServicename(wsmodel.getServicename() + "Request");
                        CheckandSendSMS(wsmodel);
                        CheckandSendEmail(wsmodel);
                        wsmodel.setServicename(servicename);
                    }
                    else {
                        CheckandSendSMS(wsmodel);
                        CheckandSendEmail(wsmodel);
                    }


                    //TODO: Raza 21-04-2021 Update below Logic for not sending notifications on which OTP is required
                    if (wsmodel.getServicename().equals("ConfirmBankOtp")
                            && Util.hasText(wsmodel.getOriginalapi())
                            && !wsmodel.getOriginalapi().equals("ConfirmBankOtp")) {
                        String servicename = wsmodel.getServicename();
                        wsmodel.setServicename(wsmodel.getOriginalapi());
                        CheckandSendNotification(wsmodel);
                        CheckandSendEmail(wsmodel);
                        wsmodel.setServicename(servicename);
                    }
                    else if (!wsmodel.getServicename().equals("BankToWallet") &&
                            !wsmodel.getServicename().equals("BankToAlias") &&
                            !wsmodel.getServicename().equals("LoadWallet") &&
                            !wsmodel.getServicename().equals("BankToBank") &&
                            !wsmodel.getServicename().equals("GetBankDetails") &&
                            !wsmodel.getServicename().equals("BankForexPurchase") &&
                            !wsmodel.getServicename().equals("ConfirmBankOtp")) {
                        CheckandSendNotification(wsmodel);
                        CheckandSendEmail(wsmodel);
                    }
                    else {
                        if (!Util.hasText(wsmodel.getOriginalapi()) || (Util.hasText(wsmodel.getOriginalapi()) && !wsmodel.getOriginalapi().equals("ConfirmBankOtp"))) {
                            if (!wsmodel.getServicename().equals("BankToWallet")
                                    && !wsmodel.getServicename().equals("BankToAlias")
                                    && !wsmodel.getServicename().equals("BankToBank")
                                    && !wsmodel.getServicename().equals("LoadWallet")
                                    && !wsmodel.getServicename().equals("GetBankDetails")
                                    && !wsmodel.getServicename().equals("BankForexPurchase")  ) // Added by Affan on 12-June-23
                            {
                                CheckandSendNotification(wsmodel);
                                CheckandSendEmail(wsmodel);
                            }

                        }

                    }

                }


                if (Util.hasText(wsmodel.getServicename()) && wsmodel.getServicename().equals("ConfirmBankOtp")
                        && Util.hasText(wsmodel.getOriginalapi()) && wsmodel.getOriginalapi().equals("LoadWallet")) {
                    wsmodel.setApiname("LoadWallet");
                    wsmodel.setFrapiname(GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif("LoadWallet").getFrapiname());
                } else if (Util.hasText(wsmodel.getServicename()) && wsmodel.getServicename().equals("ConfirmBankOtp")
                        && Util.hasText(wsmodel.getOriginalapi()) && wsmodel.getOriginalapi().equals("UnloadWallet")) {
                    wsmodel.setApiname("UnloadWallet");
                    wsmodel.setFrapiname(GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif("UnloadWallet").getFrapiname());
                }


                return wsmodel;
            }
            else if (wsmodel.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                if (wsmodel.getServicename().equals("SendMoneyInternational")) {
                    try {
                        if (!SMSGatewayHandler.CreateandSendSMS(CustomerType.CUSTOMER, SMSCategory.OUTGOING_REMIT_PARKED, wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            return wsmodel;
                        }
                        if (!NotificationHandler.CreateandSendNotification(CustomerType.CUSTOMER, SMSCategory.OUTGOING_REMIT_PARKED, wsmodel.getMobilenumber(), wsmodel)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            return wsmodel;
                        }

                        try {
                            if (!EmailGatewayHandler.CreateandSendEmail(CustomerType.CUSTOMER, SMSCategory.OUTGOING_REMIT_PARKED, wsmodel.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for EmailId [" + wsmodel.getEmailaddress() + "], rejecting...");
                                return wsmodel;
                            }
                        }
                        catch (Exception e){
                            logger.error("Exception caght while sending email, ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                        }

                        return wsmodel;
                    } catch (Exception e) {
                        logger.error("Exception caught while sending pending remit rejected message, rejecting...");
                        //e.printStackTrace();
                        logger.error(WebServiceUtil.getStrException(e));
                        //wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                        return wsmodel;
                    }
                } else {
                    logger.error("ServiceName [" + wsmodel.getServicename() + "] not registered for Accepted Waiting Approval, ignoring...");
                    return wsmodel;
                }
            } else {
                logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                return wsmodel;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing ServiceRequest [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetCardId(AppWsEntity wsmodel)
    {
        try {
            logger.info("Executing ExecuteGetCardId Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }
            /* ---------- Naveed validate + decrypt BOTH fields ---------- */
            if (!Util.hasText(wsmodel.getCardnumber())
                    || !Util.hasText(wsmodel.getCardexpiry())) {

                logger.error("cardnumber or cardexpiry missing/blank");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            logger.info("Encrypted cardnumber: {}", wsmodel.getCardnumber());
            logger.info("Encrypted cardexpiry: {}", wsmodel.getCardexpiry());

            //Hamza Added for TAP n PAY - START
            String servicename = wsmodel.getServicename();
            try{
                wsmodel.setServicename("VerifyPin");
                MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//                MWWSOperation.SendToOpenAPI(wsmodel);
            }
            catch (Exception e){
                logger.error("Exception caught while sending VerifyPin for checking Email Verified Status to OpenAPI, rejecting...");
                logger.error(WebServiceUtil.getStrException(e));
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }
            finally {
                wsmodel.setServicename(servicename);
            }

            if(Util.hasText(wsmodel.getRespcode()) && !wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                logger.error("Negative Response Revcieve from OpenAPI while Verify Pin, rejecting...");
                wsmodel.setRespcode(wsmodel.getRespcode());
                return wsmodel;
            }
            //Hamza Added for TAP n PAY - END

            try {

                String clearPan = WSEncryptionUtil.decryptAppThalesD1Card(wsmodel.getCardnumber());
                String clearExp = WSEncryptionUtil.decryptAppThalesD1Card(wsmodel.getCardexpiry());

                if (StringUtils.isBlank(clearPan) || StringUtils.isBlank(clearExp)) {
                    logger.error("Decryption returned blank values");
                    wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
                    return wsmodel;
                }
                logger.info("Decrypted cardnumber: {}", WSEncryptionUtil.maskCardNumber(clearPan));
                logger.info("Decrypted cardnumber: {}", clearPan);
                logger.info("Decrypted cardexpiry: {}", clearExp);

                wsmodel.setCardnumber(clearPan);
                wsmodel.setCardexpiry(clearExp);

            } catch (Exception ex) {
                logger.error("Exception during card field decryption", ex);
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }

            if (Util.hasText(wsmodel.getDestmobilenumber())) {
                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
                params = new HashMap<String, Object>();
                params.put("MOB", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (customer == null) {
                    logger.error("Customer not found against mobilenumber [" + wsmodel.getMobilenumber() + "]");
                    wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }
            } else {
                logger.info("Customer found with mobile number: [" + wsmodel.getMobilenumber() + "] Sending request to OPENAPI.......");
                String temp = wsmodel.getServicename();
                wsmodel.setServicename("GetCardId"); // CustomerCheck is the API at OpenApi
                SendToOpenAPI(wsmodel);
                wsmodel.setServicename(temp);
                logger.info("Response Code received from OPENAPI: [" + wsmodel.getRespcode() + "] which says: " + wsmodel.getRespcodedesc());
                if(!(Util.hasText(wsmodel.getRespcode())))
                {
                    logger.info("Response code NULL received from OPENAPI.... ");
                    return wsmodel;

                }
                //Naveed adding to route for tokenization to Thales
                if(wsmodel.getRespcode().equalsIgnoreCase(ISOResponseCodes.APPROVED))
                {
                    logger.info("Processing for Tokenization... ");
                    wsmodel.setServicename("ThalesD1RegisterConsumerWithCards");
                    SendToMiddlewareTokenization(wsmodel);
                }


                if(!wsmodel.getRespcode().equalsIgnoreCase(ISOResponseCodes.APPROVED))
                {
                    logger.info("process failed... Respcode description: " + wsmodel.getRespcodedesc() + " ....... Rejecting ");
                    return wsmodel;
                }

            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        return wsmodel;
    }

    public static AppWsEntity ExecuteD1SDKAccessTokenRequest(AppWsEntity wsmodel){

        try {
            //validate login if needed
            logger.info("Executing " + wsmodel.getServicename() + " Request...");

            logger.info("Validating Session...");
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }
           // ISSUER_ID = "RAW___CO_1";

            String issuer = "RAW___CO_1";
            String subject = wsmodel.getCustomerid();
            logger.info("Consumer Id = " + wsmodel.getCustomerid());
            String[] audience = new String[]{"https://client-api.d1-stg.thalescloud.io/oidc/RAW___CO_1"}; //Please check for PROD
            String scope = "thales:d1";

            //String jwt = D1JWTTokenUtility.generateAccessToken(issuer, subject, audience, scope);
            //String jwt = D1JWTTokenUtility.generateAccessToken(issuer, subject, audience, scope);
            D1JWTTokenUtility.generateAccessToken(issuer, subject, audience, scope, wsmodel);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            wsmodel.setRespcodedesc("Access Token Generated Successfully");
//            wsmodel.setToken(jwt);
//            wsmodel.setExpiry(String.valueOf(expirationTime.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            wsmodel.setRespcodedesc("Token Generation Failed");
        }

        return wsmodel;
    }


    public static AppWsEntity SendToOpenAPI (AppWsEntity wsmodel){
        try {
            //send to wallet for wallet creation
            wsmodel.setFwdchannelid(ChannelCodes.MIDDLEWARE); //Raza always set FWD Channel ID from Middleware

            Channel destchannel = null;

            if (GlobalContext.getInstance().getTransactionCodeDescbyAPI(wsmodel.getServicename()).getFraudenabled()
                    && Util.hasText(wsmodel.getMobilenumber()) && wsmodel.getMobilenumber().equals("00243111111111")) {

                SendToFraud(wsmodel);

                if (!wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    logger.error("Failed/Negative response [" + wsmodel.getRespcode() + "] received from Fraud Management system for service [" + wsmodel.getServicename() + "], rejecting...");
                    return wsmodel;
                }
                wsmodel.setRespcode(null); //Remove response code before forwarding it for processing...
            }

            destchannel = GlobalContext.getInstance().getChannel(GlobalContext.getInstance().getTransactionCodeDescbyAPI(wsmodel.getServicename()).getDestchannel()); //Raza should be done through Routing

            if (destchannel == null) {
                destchannel = GlobalContext.getInstance().getChannelbyId(ChannelCodes.OPENAPI); //Raza should be done through Routing
            }

            if (destchannel == null) {
                logger.error("OpenAPI Server Not found in DB..");
                wsmodel.setRespcode(ISOResponseCodes.HOST_LINK_DOWN);
                return wsmodel;
            }

            logger.info("Calling Open API Sys...");

            if (!BuildMsgForOpenAPI(wsmodel)) {
                logger.error("Unable to Build Message For OpenAPI, rejecting...");
                return wsmodel;
            }

            Client client = Client.create();
            client.setConnectTimeout(destchannel.getConnecttimeout() * 1000);
            client.setReadTimeout(destchannel.getReadtimeout() * 1000);
            WebResource webResource = null;
            AppWsEntity resp;
            try {


                if (wsmodel.getServicename().equals("GetAccountBalance") || wsmodel.getServicename().equals("GetBankTransactionList")
                        || wsmodel.getServicename().equals("GetAccountOpposition") || wsmodel.getServicename().equals("GetAccountRIB")
                        || wsmodel.getServicename().equals("ContactRM") || wsmodel.getServicename().equals("BeneficiariesDetails")) {
                    logger.info("Calling URL [" + ((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + "9009" + destchannel.getWebserviceURL() + "app/mobileapp" + "]");
                    webResource = client.resource(((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + "9009" + destchannel.getWebserviceURL() + "app/mobileapp"); //wsmodel.getServicename().toLowerCase());
                } else {
                    logger.info("Calling URL [" + ((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + destchannel.getPort() + destchannel.getWebserviceURL() + "app/mobileapp" + "]");
                    webResource = client.resource(((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + destchannel.getPort() + destchannel.getWebserviceURL() + "app/mobileapp"); //wsmodel.getServicename().toLowerCase());
                }

                //Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
                };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                resp = webResource.type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        //.header("Content-Type", "application/json")
                        .post(AppWsEntity.class, wsmodel);

                logger.info("Response Received [" + resp.getRespcode() + "], Replying to Acquirer...");
                wsmodel.setRespcode(resp.getRespcode());
                if (resp.getRespcode().equals(ISOResponseCodes.APPROVED) || resp.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                    if (Util.hasText(resp.getAmounttransaction())) {
                        wsmodel.setAmounttransaction(resp.getAmounttransaction());
                    }

                    if (Util.hasText(resp.getUsername())) {
                        wsmodel.setUsername(resp.getUsername());
                    }

                    if (resp.getBanks() != null) {
                        wsmodel.setBanks(resp.getBanks());
                    }

                    if (Util.hasText(resp.getIdentificationno())) {
                        wsmodel.setIdentificationno(resp.getIdentificationno());
                    }


                    //Muhammad Hamza Adding for Money Gram -- Start
                    if (Util.hasText(resp.getTransactionsessionid())) {
                        wsmodel.setTransactionsessionid(resp.getTransactionsessionid());
                    }
                    if (Util.hasText(resp.getReferencenumber())) {
                        wsmodel.setReferencenumber(resp.getReferencenumber());
                    }
                    if (Util.hasText(resp.getReceiveragentid())) {
                        wsmodel.setReceiveragentid(resp.getReceiveragentid());
                    }
                    if (Util.hasText(resp.getDeliveryoption())) {
                        wsmodel.setDeliveryoption(resp.getDeliveryoption());
                    }
                    if (Util.hasText(resp.getSecretquestionflag())) {
                        wsmodel.setSecretquestionflag(resp.getSecretquestionflag());
                    }
                    //Muhammad Hamza Adding for Money Gram -- Start

                    if (Util.hasText(resp.getCardid())) {
                        wsmodel.setCardid(resp.getCardid());
                    }

                    //Muhammad Hamza Added for MNO - Wallet to Wallet --START
                    if (resp.getMnoslist() != null) {
                        wsmodel.setMnoslist(resp.getMnoslist());
                    }
                    if (Util.hasText(resp.getServicename()) && resp.getServicename().equals("GetTitleFetchOut")) {
                        if (resp.getDestusername() != null) {
                            wsmodel.setDestusername(resp.getDestusername());
                        }
                        if (resp.getDestcountry() != null) {
                            wsmodel.setDestcountry(resp.getDestcountry());
                        }
                        if (resp.getDestaddress() != null) {
                            wsmodel.setDestaddress(resp.getDestaddress());
                        }
                    }
                    //Muhammad Hamza Added for MNO - Wallet to Wallet --End

                    //Muhammad Hamza Added for E-Ticketing for RAWBank --START


                    if (resp.getQrcode() != null) {
                        wsmodel.setQrcode(resp.getQrcode());
                    }

                    if (resp.getEventid() != null) {
                        wsmodel.setEventid(resp.getEventid());
                    }

                    if (resp.getBookid() != null) {
                        wsmodel.setBookid(resp.getBookid());
                    }
                    if (resp.getTickettypeid() != null) {
                        wsmodel.setTickettypeid(resp.getTickettypeid());
                    }
                    if (Util.hasText(resp.getQuantity())) {
                        wsmodel.setQuantity(resp.getQuantity());
                    }
                    //Muhammad Hamza Added for E-Ticketing for RAWBank --END

                    if (resp.getDestaccounttitle() != null) {
                        wsmodel.setDestaccounttitle(resp.getDestaccounttitle());
                    }
                    if (resp.getBeneficiarylist() != null) {
                        wsmodel.setBeneficiarylist(resp.getBeneficiarylist());
                    }
                    if (Util.hasText(String.valueOf(resp.getIsemailverified()))) {
                        wsmodel.setIsemailverified(resp.getIsemailverified());
                    }
                    if (Util.hasText(String.valueOf(resp.getIscmsaccoutlinked()))) {
                        wsmodel.setIscmsaccoutlinked(resp.getIscmsaccoutlinked());
                    }
                    if (Util.hasText(resp.getDestemailaddress())) {
                        wsmodel.setDestemailaddress(resp.getDestemailaddress());
                    }
                    if (Util.hasText(resp.getBranchname())) {
                        wsmodel.setBranchname(resp.getBranchname());
                    }
                    if (Util.hasText(resp.getBranchcode())) {
                        wsmodel.setBranchcode(resp.getBranchcode());
                    }
                    if (Util.hasText(resp.getClientid())) {
                        wsmodel.setClientid(resp.getClientid());
                    }
                    if (Util.hasText(resp.getAccountholdername())) {
                        wsmodel.setAccountholdername(resp.getAccountholdername());
                    }
                    if (Util.hasText(resp.getAccountholderaddress())) {
                        wsmodel.setAccountholderaddress(resp.getAccountholderaddress());
                    }
                    if (Util.hasText(resp.getAccountcurrency())) {
                        wsmodel.setAccountcurrency(resp.getAccountcurrency());
                    }
                    if (Util.hasText(resp.getAccountkey())) {
                        wsmodel.setAccountkey(resp.getAccountkey());
                    }
                    if (Util.hasText(resp.getAccountnumber())) {
                        wsmodel.setAccountnumber(resp.getAccountnumber());
                    }

                    if (Util.hasText(resp.getBankcode())) {
                        wsmodel.setBankcode(resp.getBankcode());
                    }
                    if (resp.getAccountopposition() != null) {
                        wsmodel.setAccountopposition(resp.getAccountopposition());
                    }
                    if (resp.getCustomeropposition() != null) {
                        wsmodel.setCustomeropposition(resp.getCustomeropposition());
                    }
                    if (Util.hasText(resp.getBiccode())) {
                        wsmodel.setBiccode(resp.getBiccode());
                    }
                    //Muhammad Hamza Added for Mobile banking feature for RAWBank --END

                    if (Util.hasText(resp.getDestaccount())) //Raza adding for Meezan Load/Unload txns
                    {
                        wsmodel.setDestaccount(resp.getDestaccount());
                    }
                    if (Util.hasText(resp.getUserid())) {
                        wsmodel.setUserid(resp.getUserid());
                    }
                    if (Util.hasText(resp.getCnic())) {
                        wsmodel.setCnic(resp.getCnic().trim());
                        wsmodel.setIdentificationno(resp.getCnic().trim());
                    }
                    if (Util.hasText(resp.getAccountnumber())) //Raza adding for Meezan Delink API
                    {
                        wsmodel.setAccountnumber(resp.getAccountnumber());
                    }
                    if (Util.hasText(resp.getCustomername())) {
                        wsmodel.setCustomername(resp.getCustomername());
                    }
                    if (Util.hasText(resp.getMiddlename())) {
                        wsmodel.setMiddlename(resp.getMiddlename().trim());
                    }
                    if (Util.hasText(resp.getNationality())) {
                        wsmodel.setNationality(resp.getNationality());
                    }
                    if (Util.hasText(resp.getFathername())) {
                        wsmodel.setFathername(resp.getFathername());
                    }
                    if (Util.hasText(resp.getDateofbirth())) {
                        wsmodel.setDateofbirth(resp.getDateofbirth());
                    }
                    if (Util.hasText(resp.getPlaceofbirth())) {
                        wsmodel.setPlaceofbirth(resp.getPlaceofbirth());
                    }
                    if (Util.hasText(resp.getMothername())) {
                        wsmodel.setMothername(resp.getMothername());
                    }
                    if (Util.hasText(resp.getAddress())) {
                        wsmodel.setAddress(resp.getAddress());
                    }
                    if (Util.hasText(resp.getCity())) {
                        wsmodel.setCity(resp.getCity());
                    }
                    if (Util.hasText(resp.getProvince())) {
                        wsmodel.setProvince(resp.getProvince());
                    }
                    if (Util.hasText(resp.getCountry())) {
                        wsmodel.setCountry(resp.getCountry());
                    }
                    if (Util.hasText(resp.getCreationdate())) {
                        wsmodel.setCreationdate(resp.getCreationdate());
                    }
                    if (Util.hasText(resp.getAccesstoken())) {
                        wsmodel.setAccesstoken(resp.getAccesstoken());
                    }
                    if (resp.getAccountlist() != null) {
                        wsmodel.setAccountlist(resp.getAccountlist());
                    }
                    if (Util.hasText(resp.getAcctlimit())) {
                        wsmodel.setAcctlimit(resp.getAcctlimit());
                    }
                    if (Util.hasText(resp.getActivationtime())) {
                        wsmodel.setActivationtime(resp.getActivationtime());
                    }
                    if (Util.hasText(resp.getAllowed())) {
                        wsmodel.setAllowed(resp.getAllowed());
                    }
                    if (Util.hasText(resp.getAvaillimit())) {
                        wsmodel.setAvaillimit(resp.getAvaillimit());
                    }
                    if (Util.hasText(resp.getAvaillimitfreq())) {
                        wsmodel.setAvaillimitfreq(resp.getAvaillimitfreq());
                    }
                    if (Util.hasText(resp.getAvaillimitfreq())) {
                        wsmodel.setAvaillimitfreq(resp.getAvaillimitfreq());
                    }
                    if (Util.hasText(resp.getDailylimit())) {
                        wsmodel.setDailylimit(resp.getDailylimit());
                    }
                    if (resp.getLinkedaccounts() != null) {
                        wsmodel.setLinkedaccounts(resp.getLinkedaccounts());
                    }
                    if (Util.hasText(resp.getMonthlylimit())) {
                        wsmodel.setMonthlylimit(resp.getMonthlylimit());
                    }
                    if (Util.hasText(resp.getSrcchargeamount())) {
                        wsmodel.setSrcchargeamount(resp.getSrcchargeamount());
                    }
                    if (Util.hasText(resp.getDestchargeamount())) {
                        wsmodel.setDestchargeamount(resp.getDestchargeamount());
                    }
                    if (Util.hasText(resp.getTaxamount())) {
                        wsmodel.setTaxamount(resp.getTaxamount());
                    }
                    if (resp.getAccountlimits() != null) {
                        wsmodel.setAccountlimits(resp.getAccountlimits());
                    }
                    if (resp.getProvisionalwallets() != null) {
                        wsmodel.setProvisionalwallets(resp.getProvisionalwallets());
                    }
                    if (Util.hasText(resp.getRequesttime())) {
                        wsmodel.setRequesttime(resp.getRequesttime());
                    }
                    if (Util.hasText(resp.getActivationtime())) {
                        wsmodel.setActivationtime(resp.getActivationtime());
                    }
                    if (Util.hasText(resp.getStan())) {
                        wsmodel.setStan(resp.getStan());
                    }
                    if (Util.hasText(resp.getRrn())) {
                        wsmodel.setRrn(resp.getRrn());
                    }
//                    if (Util.hasText(resp.getStatus())) {
//                        wsmodel.setStatus(resp.getStatus());
//                    }
                    if (resp.getTransactions() != null) {
                        wsmodel.setTransactions(resp.getTransactions());
                    }
                    if (resp.getUsertransactions() != null) {
                        wsmodel.setUsertransactions(resp.getUsertransactions());
                    }
                    if (resp.getTransactionDetail() != null) {
                        wsmodel.setTransactionDetail(resp.getTransactionDetail());
                    }
                    if (Util.hasText(resp.getCurrency())) {
                        wsmodel.setCurrency(resp.getCurrency());
                    }
                    if (Util.hasText(resp.getProvince())) {
                        wsmodel.setProvince(resp.getProvince());
                    }
                    if (Util.hasText(resp.getEnableflag())) {
                        wsmodel.setEnableflag(resp.getEnableflag());
                    }
                    if (Util.hasText(resp.getYearlylimit())) {
                        wsmodel.setYearlylimit(resp.getYearlylimit());
                    }
                    if (Util.hasText(resp.getAccountbalance())) {
                        wsmodel.setAccountbalance(resp.getAccountbalance());
                    }
                    if (Util.hasText(resp.getAccountbalance())) {
                        wsmodel.setAccountbalance(resp.getAccountbalance());
                    }
                    if (Util.hasText(resp.getCardnumber())) {
                        wsmodel.setCardnumber(resp.getCardnumber());
                    }
                    if (Util.hasText(resp.getCardexpiry())) {
                        wsmodel.setCardexpiry(resp.getCardexpiry());
                    }
                    if (Util.hasText(resp.getBankcode())) {
                        wsmodel.setBankcode(resp.getBankcode());
                    }
                    if (Util.hasText(resp.getNewpindata())) {
                        wsmodel.setNewpindata(resp.getNewpindata());
                    }
                    if (Util.hasText(resp.getSecretquestion1())) {
                        wsmodel.setSecretquestion1(resp.getSecretquestion1());
                    }
                    if (Util.hasText(resp.getSecretquestionanswer1())) {
                        wsmodel.setSecretquestionanswer1(resp.getSecretquestionanswer1().trim());
                    }
                    if (Util.hasText(resp.getSecretquestion2())) {
                        wsmodel.setSecretquestion2(resp.getSecretquestion2());
                    }
                    if (Util.hasText(resp.getSecretquestionanswer2())) {
                        wsmodel.setSecretquestionanswer2(resp.getSecretquestionanswer2().trim());
                    }
                    if (Util.hasText(resp.getTotalcount())) {
                        wsmodel.setTotalcount(resp.getTotalcount());
                    }
                    if (Util.hasText(resp.getBlockedflag())) {
                        wsmodel.setBlockedflag(resp.getBlockedflag());
                    }
                    if (Util.hasText(resp.getTempblockflag())) {
                        wsmodel.setTempblockflag(resp.getTempblockflag());
                    }
                    if (Util.hasText(resp.getAgentid())) {
                        wsmodel.setAgentid(resp.getAgentid());
                    }
                    if (Util.hasText(resp.getCdfaccountnumber())) {
                        wsmodel.setCdfaccountnumber(resp.getCdfaccountnumber());
                    }
                    if (Util.hasText(resp.getUsdaccountnumber())) {
                        wsmodel.setUsdaccountnumber(resp.getUsdaccountnumber());
                    }
                    if (Util.hasText(resp.getCdfacctid())) {
                        wsmodel.setCdfacctid(resp.getCdfacctid());
                    }
                    if (Util.hasText(resp.getUsdacctid())) {
                        wsmodel.setUsdacctid(resp.getUsdacctid());
                    }
                    if (resp.getWalletsAndLinkedAccounts() != null) {
                        wsmodel.setWalletsAndLinkedAccounts(resp.getWalletsAndLinkedAccounts());
                    }
                    if (Util.hasText(resp.getBusinessname())) {
                        wsmodel.setBusinessname(resp.getBusinessname());
                    }
                    if (Util.hasText(resp.getFirstname())) {
                        wsmodel.setFirstname(resp.getFirstname().trim());
                    }
                    if (Util.hasText(resp.getLastname())) {
                        wsmodel.setLastname(resp.getLastname().trim());
                    }
                    if (Util.hasText(resp.getMerchanttype())) {
                        wsmodel.setMerchanttype(resp.getMerchanttype());
                    }
                    if (Util.hasText(resp.getEmailaddress())) {
                        wsmodel.setEmailaddress(resp.getEmailaddress());
                    }
                    if (Util.hasText(resp.getAlternatenumber())) {
                        wsmodel.setAlternatenumber(resp.getAlternatenumber());
                    }
                    if (Util.hasText(resp.getAlternateemail())) {
                        wsmodel.setAlternateemail(resp.getAlternateemail());
                    }
                    if (Util.hasText(resp.getIdentificationtype())) {
                        wsmodel.setIdentificationtype(resp.getIdentificationtype());
                    }
                    if (Util.hasText(resp.getCreatoruser())) {
                        wsmodel.setCreatoruser(resp.getCreatoruser());
                    }
                    if (Util.hasText(resp.getCnicpicturefront())) {
                        wsmodel.setCnicpicturefront(resp.getCnicpicturefront());
                    }
                    if (Util.hasText(resp.getCnicpictureback())) {
                        wsmodel.setCnicpictureback(resp.getCnicpictureback());
                    }
                    if (Util.hasText(resp.getCustomerpicture())) {
                        wsmodel.setCustomerpicture(resp.getCustomerpicture());
                    }
                    if (Util.hasText(resp.getCommcdfacctid())) {
                        wsmodel.setCommcdfacctid(resp.getCommcdfacctid());
                    }
                    if (Util.hasText(resp.getCommusdacctid())) {
                        wsmodel.setCommusdacctid(resp.getCommusdacctid());
                    }
                    if (Util.hasText(resp.getDestmerchantid())) {
                        wsmodel.setAgentid(resp.getAgentid());
                    }
                    if (Util.hasText(resp.getMemo())) {
                        wsmodel.setMemo(resp.getMemo());
                    }
                    if (Util.hasText(resp.getQrid())) {
                        wsmodel.setQrid(resp.getQrid());
                    }
                    if (Util.hasText(resp.getAdvertisement1())) {
                        wsmodel.setAdvertisement1(resp.getAdvertisement1());
                    }
                    if (Util.hasText(resp.getAdvertisement2())) {
                        wsmodel.setAdvertisement1(resp.getAdvertisement2());
                    }
                    if (Util.hasText(resp.getAdvertisement3())) {
                        wsmodel.setAdvertisement3(resp.getAdvertisement3());
                    }
                    if (Util.hasText(resp.getCashoutpin())) {
                        logger.info("CashOutPin [" + resp.getCashoutpin() + "]");
                        wsmodel.setCashoutpin(resp.getCashoutpin());
                    }
                    if (Util.hasText(resp.getCurrencyrate())) {
                        wsmodel.setCurrencyrate(resp.getCurrencyrate());
                    }
                    if (Util.hasText(resp.getRequestmoneyref())) {
                        wsmodel.setRequestmoneyref(resp.getRequestmoneyref());
                    }
                    if (Util.hasText(resp.getExpiry())) {
                        wsmodel.setExpiry(resp.getExpiry());
                    }
                    if (Util.hasText(resp.getAuthorizationnumber())) {
                        wsmodel.setAuthorizationnumber(resp.getAuthorizationnumber());
                    }
                    if (Util.hasText(resp.getBillamount())) {
                        wsmodel.setBillamount(resp.getBillamount());
                    }
                    if (Util.hasText(resp.getWalletcurrency())) {
                        wsmodel.setWalletcurrency(resp.getWalletcurrency());
                    }
                    if (Util.hasText(resp.getWalletstatus())) {
                        wsmodel.setWalletstatus(resp.getWalletstatus());
                    }
                    if (Util.hasText(resp.getProduct())) {
                        wsmodel.setProduct(resp.getProduct());
                    }
                    if (Util.hasText(resp.getPoolaccountnumber())) {
                        wsmodel.setPoolaccountnumber(resp.getPoolaccountnumber());
                    }
                    if (Util.hasText(resp.getPoolaccountcurrency())) {
                        wsmodel.setPoolaccountcurrency(resp.getPoolaccountcurrency());
                    }
                    if (resp.getAliaslist() != null) {
                        wsmodel.setAliaslist(resp.getAliaslist());
                    }
                    if (resp.getWalletlist() != null) {
                        wsmodel.setWalletlist(resp.getWalletlist());
                    }
                    if (Util.hasText(resp.getAccountcurrency())) {
                        wsmodel.setAccountcurrency(resp.getAccountcurrency());
                    }
                    //if(Util.hasText(resp.getStatus()))
                    //{
                    //    wsmodel.setStatus(resp.getStatus());
                    //}
                    if (Util.hasText(resp.getState())) {
                        wsmodel.setState(resp.getState());
                    }
                    if (Util.hasText(resp.getAcctStatus())) {
                        wsmodel.setAcctStatus(resp.getAcctStatus());
                    }
                    if (Util.hasText(resp.getOriginalapi())) {
                        wsmodel.setOriginalapi(resp.getOriginalapi());
                    }
                    if (Util.hasText(resp.getDestmobilenumber())) {
                        wsmodel.setDestmobilenumber(resp.getDestmobilenumber());
                    }
                    if (Util.hasText(resp.getAmounttransaction())) {
                        wsmodel.setAmounttransaction(resp.getAmounttransaction());
                    }
                    if (Util.hasText(resp.getDestacctid())) {
                        wsmodel.setDestacctid(resp.getDestacctid());
                    }
                    if (Util.hasText(resp.getDestcurrency())) {
                        wsmodel.setDestcurrency(resp.getDestcurrency());
                    }
                    if (Util.hasText(resp.getAmountcbill())) {
                        wsmodel.setAmountcbill(resp.getAmountcbill());
                    }
                    if (Util.hasText(resp.getAmountcommissioncdf())) {
                        wsmodel.setAmountcommissioncdf(resp.getAmountcommissioncdf());
                    }
                    if (Util.hasText(resp.getAmountcommissionusd())) {
                        wsmodel.setAmountcommissionusd(resp.getAmountcommissionusd());
                    }
                    if (resp.getRequestmoneylist() != null) {
                        wsmodel.setRequestmoneylist(resp.getRequestmoneylist());
                    }
                    if (resp.getCashoutlist() != null) {
                        wsmodel.setCashoutlist(resp.getCashoutlist());
                    }
                    if (resp.getPendingremitloglist() != null) {
                        wsmodel.setPendingremitloglist(resp.getPendingremitloglist());
                    }
                    if (resp.getMerchantDashboardDataObjList() != null) {
                        wsmodel.setMerchantDashboardDataObjList(resp.getMerchantDashboardDataObjList());
                    }
                    if (resp.getMerchantDashboardDataObjListForChild() != null) {
                        wsmodel.setMerchantDashboardDataObjListForChild(resp.getMerchantDashboardDataObjListForChild());
                    }
                    if (Util.hasText(resp.getPaymentmethod())) {
                        wsmodel.setPaymentmethod(resp.getPaymentmethod());
                    }
                    if (resp.getTransactionportallist() != null) {
                        wsmodel.setTransactionportallist(resp.getTransactionportallist());
                    }
                    if (Util.hasText(resp.getMobilenumber())) {
                        wsmodel.setMobilenumber(resp.getMobilenumber());
                    }
                    if (Util.hasText(resp.getDestmobilenumber())) {
                        wsmodel.setDestmobilenumber(resp.getDestmobilenumber());
                    }
                    if (Util.hasText(resp.getFilename())) {
                        wsmodel.setFilename(resp.getFilename());
                    }
                    if (Util.hasText(resp.getNewfilename())) {
                        wsmodel.setNewfilename(resp.getNewfilename());
                    }
                    if (resp.getBillpackages() != null) {
                        wsmodel.setBillpackages(resp.getBillpackages());
                    }
                    if (resp.getAmtwithinduedate() != null) {
                        wsmodel.setAmtwithinduedate(resp.getAmtwithinduedate());
                    }
                    if (resp.getAmtafterduedate() != null) {
                        wsmodel.setAmtafterduedate(resp.getAmtafterduedate());
                    }
                    if (resp.getDuedate() != null) {
                        wsmodel.setDuedate(resp.getDuedate());
                    }
                    if (Util.hasText(resp.getBillid())) {
                        wsmodel.setBillid(resp.getBillid());
                    }
                    if (Util.hasText(resp.getTrancurrency())) {
                        wsmodel.setTrancurrency(resp.getTrancurrency());
                    }
                    if (Util.hasText(resp.getWalletcurrency())) {
                        wsmodel.setWalletcurrency(resp.getWalletcurrency());
                    }
                    if (Util.hasText(resp.getTranauthid())) {
                        wsmodel.setTranauthid(resp.getTranauthid());
                    }
                    if (resp.getAppgraphlist() != null) {
                        wsmodel.setAppgraphlist(resp.getAppgraphlist());
                    }
                    if (resp.getSecretquestionslist() != null) {
                        wsmodel.setSecretquestionslist(resp.getSecretquestionslist());
                    }
                    if (Util.hasText(resp.getApiname())) {
                        wsmodel.setApiname(resp.getApiname());
                    }
                    if (Util.hasText(resp.getFrapiname())) {
                        wsmodel.setFrapiname(resp.getFrapiname());
                    }
                    if (resp.getPayerslist() != null) {
                        wsmodel.setPayerslist(resp.getPayerslist());
                    }
                    if (Util.hasText(resp.getPayerid())) {
                        wsmodel.setPayerid(resp.getPayerid());
                    }
                    if (Util.hasText(resp.getPayertype())) {
                        wsmodel.setPayertype(resp.getPayertype());
                    }
                    if (Util.hasText(resp.getQuotationextid())) {
                        wsmodel.setQuotationextid(resp.getQuotationextid());
                    }
                    if (Util.hasText(resp.getQuotationid())) {
                        wsmodel.setQuotationid(resp.getQuotationid());
                    }
                    if (Util.hasText(resp.getTransactionid())) {
                        wsmodel.setTransactionid(resp.getTransactionid());
                    }
                    if (Util.hasText(resp.getTransactionextid())) {
                        wsmodel.setTransactionextid(resp.getTransactionextid());
                    }
                    if (Util.hasText(resp.getIban())) {
                        wsmodel.setIban(resp.getIban());
                    }
                    if (Util.hasText(resp.getKycstatus())) {
                        wsmodel.setKycstatus(resp.getKycstatus());
                    }
                    if (Util.hasText(resp.getDuration())) {
                        wsmodel.setDuration(resp.getDuration());
                    }
                    if (Util.hasText(resp.getSubscriberno())) {
                        wsmodel.setSubscriberno(resp.getSubscriberno());
                    }
                    if (resp.getChargedetails() != null) {
                        wsmodel.setChargedetails(resp.getChargedetails());
                    }
                    if (Util.hasText(resp.getPermissions())) {
                        wsmodel.setPermissions(resp.getPermissions());
                    }
                    if (Util.hasText(resp.getGender())) {
                        wsmodel.setGender(resp.getGender());
                    }
                    if (Util.hasText(resp.getMonthlyincome())) {
                        wsmodel.setMonthlyincome(resp.getMonthlyincome());
                    }
                    if (Util.hasText(resp.getOccupation())) {
                        wsmodel.setOccupation(resp.getOccupation());
                    }
                    if (Util.hasText(resp.getMonthlyexpenditure())) {
                        wsmodel.setMonthlyexpenditure(resp.getMonthlyexpenditure());
                    }
                    if (Util.hasText(resp.getCustcomments())) {
                        wsmodel.setCustcomments(resp.getCustcomments());
                    }
                    if (Util.hasText(resp.getBillerid()) && (wsmodel.getServicename().equals("InitSendMoneyInternational") || wsmodel.getServicename().equals("SendMoneyInternational"))) {
                        logger.info("Setting BillerID [" + resp.getBillerid() + "] ...");
                        wsmodel.setBillerid(resp.getBillerid());
                    }
                    if (Util.hasText(resp.getBillername())) {
                        logger.info("Setting BillerName [" + resp.getBillername() + "] ...");
                        wsmodel.setBillername(resp.getBillername());
                    }
                    if (Util.hasText(resp.getAvailablebalance())) {
                        wsmodel.setAvailablebalance(resp.getAvailablebalance());
                    }
                    if (Util.hasText(resp.getMunicipality())) {
                        wsmodel.setMunicipality(resp.getMunicipality());
                    }
                    if (Util.hasText(resp.getDestfirstname())) {
                        wsmodel.setDestfirstname(resp.getDestfirstname());
                    }
                    if (Util.hasText(resp.getDestlastname())) {
                        wsmodel.setDestlastname(resp.getDestlastname());
                    }
                    if (Util.hasText(resp.getBillerrespcode())) {
                        wsmodel.setBillerrespcode(resp.getBillerrespcode());
                    }
                    if (Util.hasText(resp.getBillerrespcodedesc())) {
                        wsmodel.setBillerrespcodedesc(resp.getBillerrespcodedesc());
                    }
                    if (Util.hasText(resp.getTrantype())) {
                        wsmodel.setTrantype(resp.getTrantype());
                    }
                    if (resp.getUnreadapprovalcount() != null) {
                        wsmodel.setUnreadapprovalcount(resp.getUnreadapprovalcount());
                    }
                    if (Util.hasText(resp.getMerchantname())) {
                        wsmodel.setMerchantname(resp.getMerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantname())) {
                        wsmodel.setDestmerchantname(resp.getDestmerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantcode())) {
                        wsmodel.setDestmerchantcode(resp.getDestmerchantcode());
                    }
                    if (Util.hasText(resp.getMerchantname())) {
                        wsmodel.setMerchantname(resp.getMerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantname())) {
                        wsmodel.setDestmerchantname(resp.getDestmerchantname());
                    }
                    if (Util.hasText(resp.getMerchantcode())) {
                        wsmodel.setMerchantcode(resp.getMerchantcode());
                    }
                    if (Util.hasText(resp.getDestmerchantcode())) {
                        wsmodel.setDestmerchantcode(resp.getDestmerchantcode());
                    }
                    if (wsmodel.getIs_merchant() != null) {
                        wsmodel.setIs_merchant(resp.getIs_merchant());
                    }
                    if (resp.getCountrylist() != null) {
                        wsmodel.setCountrylist(resp.getCountrylist());
                    }
                    if (Util.hasText(resp.getClabe())) {
                        wsmodel.setClabe(resp.getClabe());
                    }
                    if (Util.hasText(resp.getCbu())) {
                        wsmodel.setCbu(resp.getCbu());
                    }
                    if (Util.hasText(resp.getCbualias())) {
                        wsmodel.setCbualias(resp.getCbualias());
                    }
                    if (Util.hasText(resp.getBikcode())) {
                        wsmodel.setBikcode(resp.getBikcode());
                    }
                    if (Util.hasText(resp.getAbaroutingnumber())) {
                        wsmodel.setAbaroutingnumber(resp.getAbaroutingnumber());
                    }
                    if (Util.hasText(resp.getBsbnumber())) {
                        wsmodel.setBsbnumber(resp.getBsbnumber());
                    }
                    if (Util.hasText(resp.getRoutingcode())) {
                        wsmodel.setRoutingcode(resp.getRoutingcode());
                    }
                    if (Util.hasText(resp.getEntityttid())) {
                        wsmodel.setEntityttid(resp.getEntityttid());
                    }
                    if (Util.hasText(resp.getAccounttype())) {
                        wsmodel.setAccounttype(resp.getAccounttype());
                    }
                    if (resp.getCreditaccountnumber() != null) {
                        wsmodel.setCreditaccountnumber(resp.getCreditaccountnumber());
                    }
                    if (resp.getBankMnemonic() != null) {
                        wsmodel.setBankMnemonic(resp.getBankMnemonic());
                    }
                    if (Util.hasText(resp.getRelationshipcode())) {
                        wsmodel.setRelationshipcode(resp.getRelationshipcode());
                    }
                    if (Util.hasText(resp.getMig_cdfbalance())) {
                        wsmodel.setMig_cdfbalance(resp.getMig_cdfbalance());
                    }
                    if (Util.hasText(resp.getMig_usdbalance())) {
                        wsmodel.setMig_usdbalance(resp.getMig_usdbalance());
                    }
                    if (Util.hasText(resp.getOrigdataelement())) {
                        wsmodel.setOrigdataelement(resp.getOrigdataelement());
                    }
                    if (Util.hasText(resp.getCustomerid())) {
                        wsmodel.setCustomerid(resp.getCustomerid());
                    }
                    if (Util.hasText(resp.getNewmobilenumber())) {
                        wsmodel.setNewmobilenumber(resp.getNewmobilenumber());
                    }
                    if (resp.getS2mcardslist() != null) {
                        wsmodel.setS2mcardslist(resp.getS2mcardslist());
                    }
                    if (resp.getTranObjList() != null) {
                        wsmodel.setTranObjList(resp.getTranObjList());
                    }
                    if (resp.getS2mBalance() != null) {
                        wsmodel.setS2mBalance(resp.getS2mBalance());
                    }
                    if (Util.hasText(resp.getOrderid())) {
                        wsmodel.setOrderid(resp.getOrderid());
                    }
                    if (Util.hasText(resp.getRedirecthtml())) {
                        wsmodel.setRedirecthtml(resp.getRedirecthtml());
                    }
                    if (Util.hasText(resp.getCardsecuritycode())) {
                        wsmodel.setCardsecuritycode(resp.getCardsecuritycode());
                    }
                    if (Util.hasText(resp.getThreedsenabled())) {
                        wsmodel.setThreedsenabled(resp.getThreedsenabled());
                    }
                    if (Util.hasText(resp.getThreedsversion())) {
                        wsmodel.setThreedsversion(resp.getThreedsversion());
                    }
                    if (Util.hasText(resp.getThreedsacceptedversion())) {
                        wsmodel.setThreedsacceptedversion(resp.getThreedsacceptedversion());
                    }
                    if (Util.hasText(resp.getScheme())) {
                        wsmodel.setScheme(resp.getScheme());
                    }
                    if (Util.hasText(resp.getBrand())) {
                        wsmodel.setBrand(resp.getBrand());
                    }
                    if (Util.hasText(resp.getSelfflag())) //Raza adding for Till Notification 01-12-2022
                    {
                        wsmodel.setSelfflag(resp.getSelfflag());
                    }
                    if (Util.hasText(resp.getExchangerate())) {
                        wsmodel.setExchangerate(resp.getExchangerate());
                    }
                    if (resp.getCard() != null) {
                        wsmodel.setCard(resp.getCard());
                    }
                    if (resp.getCardlist() != null) {
                        wsmodel.setCardlist(resp.getCardlist());
                    }
                    // Added by Affan on 26-July-23 Start
                    if (resp.getCscBalanceResp() != null) {
                        wsmodel.setCscBalanceResp(resp.getCscBalanceResp());
                    }
                    if (resp.getCscgetamountdueresp() != null) {
                        wsmodel.setCscgetamountdueresp(resp.getCscgetamountdueresp());
                    }
                    if (resp.getCscTransactionList() != null) {
                        wsmodel.setCscTransactionList(resp.getCscTransactionList());
                    }
                    if (resp.getCscGetClientDetailsResp() != null) {
                        wsmodel.setCscGetClientDetailsResp(resp.getCscGetClientDetailsResp());
                    }
                    if (resp.getCscCard() != null) {
                        wsmodel.setCscCard(resp.getCscCard());
                    }
                    if (resp.getCscGetCardsRespList() != null) {
                        wsmodel.setCscGetCardsRespList(resp.getCscGetCardsRespList());
                    }
                    if (resp.getCscLoadCard() != null) {
                        wsmodel.setCscLoadCard(resp.getCscLoadCard());
                    }
                    // Added by Affan on 26-July-23 End
                    if (resp.getForexrates() != null) {
                        wsmodel.setForexrates(resp.getForexrates());
                    }
                    if (Util.hasText(resp.getDestdateofbirth())) {
                        wsmodel.setDestdateofbirth(resp.getDestdateofbirth());
                    }
                    if (Util.hasText(resp.getBillertxnid())) {
                        wsmodel.setBillertxnid(resp.getBillertxnid());
                    }
                    if (Util.hasText(resp.getSvfeissuerinstid())) {
                        wsmodel.setSvfeissuerinstid(resp.getSvfeissuerinstid());
                    }
                    if (Util.hasText(resp.getPurposeoftransaction())) {
                        wsmodel.setPurposeoftransaction(resp.getPurposeoftransaction());
                    }
                    if (Util.hasText(resp.getConversionrateaccount())) {
                        wsmodel.setConversionrateaccount(resp.getConversionrateaccount());
                    }
                    if (Util.hasText(resp.getConversionratecardholderbilling())) {
                        wsmodel.setConversionratecardholderbilling(resp.getConversionratecardholderbilling());
                    }
                }

                wsmodel.setStatus(resp.getStatus());
                wsmodel.setIcccarddata(resp.getIcccarddata());

                return wsmodel;
            } catch (Exception e) {
                logger.error("Exception caught while sending [" + wsmodel.getServicename() + "] to Switch System");
                //e.printStackTrace();
                logger.error(WebServiceUtil.getStrException(e));
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            } finally {
                logger.info("Destroying client for API call, deleting request...");
                if (client != null) {
                    client.destroy();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing ServiceRequest [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

//Naveed
    public static AppWsEntity SendToMiddlewareTokenization(AppWsEntity wsmodel) {
        try {
            wsmodel.setFwdchannelid(ChannelCodes.MIDDLEWARE); //Raza always set FWD Channel ID from Middleware

            Channel destchannel = null;

            destchannel = GlobalContext.getInstance().getChannel(GlobalContext.getInstance().getTransactionCodeDescbyAPI(wsmodel.getServicename()).getDestchannel()); //Raza should be done through Routing

            if (destchannel == null) {
                destchannel = GlobalContext.getInstance().getChannelbyId(ChannelCodes.THALESD1IN); //Raza should be done through Routing
            }

            if (destchannel == null) {
                logger.error("Middleware Tokenization Server Not found in DB..");
                wsmodel.setRespcode(ISOResponseCodes.HOST_LINK_DOWN);
                return wsmodel;
            }

            logger.info("Calling Middleware Tokenization Sys...");

            if (!BuildMsgForOpenAPI(wsmodel)) {
                logger.error("Unable to Build Message For Middleware Tokenization, rejecting...");
                return wsmodel;
            }


            Client client = Client.create();
            client.setConnectTimeout(destchannel.getConnecttimeout() * 1000);
            client.setReadTimeout(destchannel.getReadtimeout() * 1000);
            WebResource webResource = null;
            AppWsEntity resp;
            try {



                    logger.info("Calling URL [" + ((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + destchannel.getPort() + destchannel.getWebserviceURL()  + wsmodel.getServicename().toLowerCase() + "]");
                    webResource = client.resource(((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + destchannel.getPort() + destchannel.getWebserviceURL() + wsmodel.getServicename().toLowerCase());


                //Create a trust manager that does not validate certificate chains
//                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//
//                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                    }
//
//                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                    }
//                }
//                };
//                SSLContext sc = SSLContext.getInstance("SSL");
//                sc.init(null, trustAllCerts, new java.security.SecureRandom());
//                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//                // Create all-trusting host name verifier
//                HostnameVerifier allHostsValid = new HostnameVerifier() {
//                    public boolean verify(String hostname, SSLSession session) {
//                        return true;
//                    }
//                };
//
//                // Install the all-trusting host verifier
//                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                resp = webResource.type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        //.header("Content-Type", "application/json")
                        .post(AppWsEntity.class, wsmodel);

                logger.info("Response Received [" + resp.getRespcode() + "], Replying to Acquirer...");
                wsmodel.setRespcode(resp.getRespcode());
                if (resp.getRespcode().equals(ISOResponseCodes.APPROVED) || resp.getRespcode().equals(ISOResponseCodes.ACCEPTED_WAITING_APPROVAL)) {
                    if (Util.hasText(resp.getAmounttransaction())) {
                        wsmodel.setAmounttransaction(resp.getAmounttransaction());
                    }

                    //Muhammad Hamza Adding for Money Gram -- Start
                    if(Util.hasText(resp.getTransactionsessionid())){
                        wsmodel.setTransactionsessionid(resp.getTransactionsessionid());
                    }
                    if(Util.hasText(resp.getReferencenumber())){
                        wsmodel.setReferencenumber(resp.getReferencenumber());
                    }
                    if(Util.hasText(resp.getReceiveragentid())){
                        wsmodel.setReceiveragentid(resp.getReceiveragentid());
                    }
                    if(Util.hasText(resp.getDeliveryoption())) {
                        wsmodel.setDeliveryoption(resp.getDeliveryoption());
                    }
                    if(Util.hasText(resp.getSecretquestionflag())){
                        wsmodel.setSecretquestionflag(resp.getSecretquestionflag());
                    }
                    //Muhammad Hamza Adding for Money Gram -- Start

                    if(Util.hasText(resp.getCardid())){
                        wsmodel.setCardid(resp.getCardid());
                    }

                    //Muhammad Hamza Added for MNO - Wallet to Wallet --START
                    if(resp.getMnoslist() != null){
                        wsmodel.setMnoslist(resp.getMnoslist());
                    }
                    //Muhammad Hamza Added for MNO - Wallet to Wallet --End

                    //Muhammad Hamza Added for E-Ticketing for RAWBank --START


                    if(resp.getQrcode() != null)
                    {
                        wsmodel.setQrcode(resp.getQrcode());
                    }

                    if(resp.getEventid() != null)
                    {
                        wsmodel.setEventid(resp.getEventid());
                    }

                    if(resp.getBookid() != null)
                    {
                        wsmodel.setBookid(resp.getBookid());
                    }
                    if(resp.getTickettypeid() != null)
                    {
                        wsmodel.setTickettypeid(resp.getTickettypeid());
                    }
                    if(Util.hasText(resp.getQuantity()))
                    {
                        wsmodel.setQuantity(resp.getQuantity());
                    }
                    //Muhammad Hamza Added for E-Ticketing for RAWBank --END


                    //Muhammad Hamza Added for Mobile banking feature for RAWBank --START
                    if(resp.getDestaccounttitle() != null)
                    {
                        wsmodel.setDestaccounttitle(resp.getDestaccounttitle());
                    }
                    if(resp.getBeneficiarylist() != null)
                    {
                        wsmodel.setBeneficiarylist(resp.getBeneficiarylist());
                    }
                    if(Util.hasText(String.valueOf(resp.getIsemailverified())))
                    {
                        wsmodel.setIsemailverified(resp.getIsemailverified());
                    }
                    if(Util.hasText(String.valueOf(resp.getIscmsaccoutlinked())))
                    {
                        wsmodel.setIscmsaccoutlinked(resp.getIscmsaccoutlinked());
                    }
                    if(Util.hasText(resp.getDestemailaddress()))
                    {
                        wsmodel.setDestemailaddress(resp.getDestemailaddress());
                    }
                    if(Util.hasText(resp.getBranchname()))
                    {
                        wsmodel.setBranchname(resp.getBranchname());
                    }
                    if(Util.hasText(resp.getBranchcode()))
                    {
                        wsmodel.setBranchcode(resp.getBranchcode());
                    }
                    if(Util.hasText(resp.getClientid())){
                        wsmodel.setClientid(resp.getClientid());
                    }
                    if(Util.hasText(resp.getAccountholdername())){
                        wsmodel.setAccountholdername(resp.getAccountholdername());
                    }
                    if(Util.hasText(resp.getAccountholderaddress())){
                        wsmodel.setAccountholderaddress(resp.getAccountholderaddress());
                    }
                    if(Util.hasText(resp.getAccountcurrency())){
                        wsmodel.setAccountcurrency(resp.getAccountcurrency());
                    }
                    if(Util.hasText(resp.getAccountkey())){
                        wsmodel.setAccountkey(resp.getAccountkey());
                    }
                    if(Util.hasText(resp.getAccountnumber())){
                        wsmodel.setAccountnumber(resp.getAccountnumber());
                    }

                    if(Util.hasText(resp.getBankcode())){
                        wsmodel.setBankcode(resp.getBankcode());
                    }
                    if(resp.getAccountopposition() != null){
                        wsmodel.setAccountopposition(resp.getAccountopposition());
                    }
                    if(resp.getCustomeropposition() != null){
                        wsmodel.setCustomeropposition(resp.getCustomeropposition());
                    }
                    if(Util.hasText(resp.getBiccode())){
                        wsmodel.setBiccode(resp.getBiccode());
                    }
                    //Muhammad Hamza Added for Mobile banking feature for RAWBank --END

                    if (Util.hasText(resp.getDestaccount())) //Raza adding for Meezan Load/Unload txns
                    {
                        wsmodel.setDestaccount(resp.getDestaccount());
                    }
                    if (Util.hasText(resp.getUserid())) {
                        wsmodel.setUserid(resp.getUserid());
                    }
                    if (Util.hasText(resp.getIdentificationno())) {
                        wsmodel.setIdentificationno(resp.getIdentificationno().trim());
                    }
                    if (Util.hasText(resp.getAccountnumber())) //Raza adding for Meezan Delink API
                    {
                        wsmodel.setAccountnumber(resp.getAccountnumber());
                    }
                    if (Util.hasText(resp.getCustomername())) {
                        wsmodel.setCustomername(resp.getCustomername());
                    }
                    if (Util.hasText(resp.getMiddlename())) {
                        wsmodel.setMiddlename(resp.getMiddlename().trim());
                    }
                    if (Util.hasText(resp.getNationality())) {
                        wsmodel.setNationality(resp.getNationality());
                    }
                    if (Util.hasText(resp.getFathername())) {
                        wsmodel.setFathername(resp.getFathername());
                    }
                    if (Util.hasText(resp.getDateofbirth())) {
                        wsmodel.setDateofbirth(resp.getDateofbirth());
                    }
                    if (Util.hasText(resp.getPlaceofbirth())) {
                        wsmodel.setPlaceofbirth(resp.getPlaceofbirth());
                    }
                    if (Util.hasText(resp.getMothername())) {
                        wsmodel.setMothername(resp.getMothername());
                    }
                    if (Util.hasText(resp.getAddress())) {
                        wsmodel.setAddress(resp.getAddress());
                    }
                    if (Util.hasText(resp.getCity())) {
                        wsmodel.setCity(resp.getCity());
                    }
                    if (Util.hasText(resp.getProvince())) {
                        wsmodel.setProvince(resp.getProvince());
                    }
                    if (Util.hasText(resp.getCountry())) {
                        wsmodel.setCountry(resp.getCountry());
                    }
                    if (Util.hasText(resp.getCreationdate())) {
                        wsmodel.setCreationdate(resp.getCreationdate());
                    }
                    if (Util.hasText(resp.getAccesstoken())) {
                        wsmodel.setAccesstoken(resp.getAccesstoken());
                    }
                    if (resp.getAccountlist() != null) {
                        wsmodel.setAccountlist(resp.getAccountlist());
                    }
                    if (Util.hasText(resp.getAcctlimit())) {
                        wsmodel.setAcctlimit(resp.getAcctlimit());
                    }
                    if (Util.hasText(resp.getActivationtime())) {
                        wsmodel.setActivationtime(resp.getActivationtime());
                    }
                    if (Util.hasText(resp.getAllowed())) {
                        wsmodel.setAllowed(resp.getAllowed());
                    }
                    if (Util.hasText(resp.getAvaillimit())) {
                        wsmodel.setAvaillimit(resp.getAvaillimit());
                    }
                    if (Util.hasText(resp.getAvaillimitfreq())) {
                        wsmodel.setAvaillimitfreq(resp.getAvaillimitfreq());
                    }
                    if (Util.hasText(resp.getAvaillimitfreq())) {
                        wsmodel.setAvaillimitfreq(resp.getAvaillimitfreq());
                    }
                    if (Util.hasText(resp.getDailylimit())) {
                        wsmodel.setDailylimit(resp.getDailylimit());
                    }
                    if (resp.getLinkedaccounts() != null) {
                        wsmodel.setLinkedaccounts(resp.getLinkedaccounts());
                    }
                    if (Util.hasText(resp.getMonthlylimit())) {
                        wsmodel.setMonthlylimit(resp.getMonthlylimit());
                    }
                    if (Util.hasText(resp.getSrcchargeamount())) {
                        wsmodel.setSrcchargeamount(resp.getSrcchargeamount());
                    }
                    if (Util.hasText(resp.getDestchargeamount())) {
                        wsmodel.setDestchargeamount(resp.getDestchargeamount());
                    }
                    if (Util.hasText(resp.getTaxamount())) {
                        wsmodel.setTaxamount(resp.getTaxamount());
                    }
                    if (resp.getAccountlimits() != null) {
                        wsmodel.setAccountlimits(resp.getAccountlimits());
                    }
                    if (resp.getProvisionalwallets() != null) {
                        wsmodel.setProvisionalwallets(resp.getProvisionalwallets());
                    }
                    if (Util.hasText(resp.getRequesttime())) {
                        wsmodel.setRequesttime(resp.getRequesttime());
                    }
                    if (Util.hasText(resp.getActivationtime())) {
                        wsmodel.setActivationtime(resp.getActivationtime());
                    }
                    if (Util.hasText(resp.getStan())) {
                        wsmodel.setStan(resp.getStan());
                    }
                    if (Util.hasText(resp.getRrn())) {
                        wsmodel.setRrn(resp.getRrn());
                    }
//                    if (Util.hasText(resp.getStatus())) {
//                        wsmodel.setStatus(resp.getStatus());
//                    }
                    if (resp.getTransactions() != null) {
                        wsmodel.setTransactions(resp.getTransactions());
                    }
                    if (resp.getUsertransactions() != null) {
                        wsmodel.setUsertransactions(resp.getUsertransactions());
                    }
                    if (resp.getTransactionDetail() != null) {
                        wsmodel.setTransactionDetail(resp.getTransactionDetail());
                    }
                    if (Util.hasText(resp.getCurrency())) {
                        wsmodel.setCurrency(resp.getCurrency());
                    }
                    if (Util.hasText(resp.getProvince())) {
                        wsmodel.setProvince(resp.getProvince());
                    }
                    if (Util.hasText(resp.getEnableflag())) {
                        wsmodel.setEnableflag(resp.getEnableflag());
                    }
                    if (Util.hasText(resp.getYearlylimit())) {
                        wsmodel.setYearlylimit(resp.getYearlylimit());
                    }
                    if (Util.hasText(resp.getAccountbalance())) {
                        wsmodel.setAccountbalance(resp.getAccountbalance());
                    }
                    if (Util.hasText(resp.getAccountbalance())) {
                        wsmodel.setAccountbalance(resp.getAccountbalance());
                    }
                    if (Util.hasText(resp.getCardnumber())) {
                        wsmodel.setCardnumber(resp.getCardnumber());
                    }
                    if (Util.hasText(resp.getCardexpiry())) {
                        wsmodel.setCardexpiry(resp.getCardexpiry());
                    }
                    if (Util.hasText(resp.getBankcode())) {
                        wsmodel.setBankcode(resp.getBankcode());
                    }
                    if (Util.hasText(resp.getNewpindata())) {
                        wsmodel.setNewpindata(resp.getNewpindata());
                    }
                    if (Util.hasText(resp.getSecretquestion1())) {
                        wsmodel.setSecretquestion1(resp.getSecretquestion1());
                    }
                    if (Util.hasText(resp.getSecretquestionanswer1())) {
                        wsmodel.setSecretquestionanswer1(resp.getSecretquestionanswer1().trim());
                    }
                    if (Util.hasText(resp.getSecretquestion2())) {
                        wsmodel.setSecretquestion2(resp.getSecretquestion2());
                    }
                    if (Util.hasText(resp.getSecretquestionanswer2())) {
                        wsmodel.setSecretquestionanswer2(resp.getSecretquestionanswer2().trim());
                    }
                    if (Util.hasText(resp.getTotalcount())) {
                        wsmodel.setTotalcount(resp.getTotalcount());
                    }
                    if (Util.hasText(resp.getBlockedflag())) {
                        wsmodel.setBlockedflag(resp.getBlockedflag());
                    }
                    if (Util.hasText(resp.getTempblockflag())) {
                        wsmodel.setTempblockflag(resp.getTempblockflag());
                    }
                    if (Util.hasText(resp.getAgentid())) {
                        wsmodel.setAgentid(resp.getAgentid());
                    }
                    if (Util.hasText(resp.getCdfaccountnumber())) {
                        wsmodel.setCdfaccountnumber(resp.getCdfaccountnumber());
                    }
                    if (Util.hasText(resp.getUsdaccountnumber())) {
                        wsmodel.setUsdaccountnumber(resp.getUsdaccountnumber());
                    }
                    if (Util.hasText(resp.getCdfacctid())) {
                        wsmodel.setCdfacctid(resp.getCdfacctid());
                    }
                    if (Util.hasText(resp.getUsdacctid())) {
                        wsmodel.setUsdacctid(resp.getUsdacctid());
                    }
                    if (resp.getWalletsAndLinkedAccounts() != null) {
                        wsmodel.setWalletsAndLinkedAccounts(resp.getWalletsAndLinkedAccounts());
                    }
                    if (Util.hasText(resp.getBusinessname())) {
                        wsmodel.setBusinessname(resp.getBusinessname());
                    }
                    if (Util.hasText(resp.getFirstname())) {
                        wsmodel.setFirstname(resp.getFirstname().trim());
                    }
                    if (Util.hasText(resp.getLastname())) {
                        wsmodel.setLastname(resp.getLastname().trim());
                    }
                    if (Util.hasText(resp.getMerchanttype())) {
                        wsmodel.setMerchanttype(resp.getMerchanttype());
                    }
                    if (Util.hasText(resp.getEmailaddress())) {
                        wsmodel.setEmailaddress(resp.getEmailaddress());
                    }
                    if (Util.hasText(resp.getAlternatenumber())) {
                        wsmodel.setAlternatenumber(resp.getAlternatenumber());
                    }
                    if (Util.hasText(resp.getAlternateemail())) {
                        wsmodel.setAlternateemail(resp.getAlternateemail());
                    }
                    if (Util.hasText(resp.getIdentificationtype())) {
                        wsmodel.setIdentificationtype(resp.getIdentificationtype());
                    }
                    if (Util.hasText(resp.getCreatoruser())) {
                        wsmodel.setCreatoruser(resp.getCreatoruser());
                    }
                    if (Util.hasText(resp.getCnicpicturefront())) {
                        wsmodel.setCnicpicturefront(resp.getCnicpicturefront());
                    }
                    if (Util.hasText(resp.getCnicpictureback())) {
                        wsmodel.setCnicpictureback(resp.getCnicpictureback());
                    }
                    if (Util.hasText(resp.getCustomerpicture())) {
                        wsmodel.setCustomerpicture(resp.getCustomerpicture());
                    }
                    if (Util.hasText(resp.getCommcdfacctid())) {
                        wsmodel.setCommcdfacctid(resp.getCommcdfacctid());
                    }
                    if (Util.hasText(resp.getCommusdacctid())) {
                        wsmodel.setCommusdacctid(resp.getCommusdacctid());
                    }
                    if (Util.hasText(resp.getDestmerchantid())) {
                        wsmodel.setAgentid(resp.getAgentid());
                    }
                    if (Util.hasText(resp.getMemo())) {
                        wsmodel.setMemo(resp.getMemo());
                    }
                    if (Util.hasText(resp.getQrid())) {
                        wsmodel.setQrid(resp.getQrid());
                    }
                    if (Util.hasText(resp.getAdvertisement1())) {
                        wsmodel.setAdvertisement1(resp.getAdvertisement1());
                    }
                    if (Util.hasText(resp.getAdvertisement2())) {
                        wsmodel.setAdvertisement1(resp.getAdvertisement2());
                    }
                    if (Util.hasText(resp.getAdvertisement3())) {
                        wsmodel.setAdvertisement3(resp.getAdvertisement3());
                    }
                    if (Util.hasText(resp.getCashoutpin())) {
                        logger.info("CashOutPin [" + resp.getCashoutpin() + "]");
                        wsmodel.setCashoutpin(resp.getCashoutpin());
                    }
                    if (Util.hasText(resp.getCurrencyrate())) {
                        wsmodel.setCurrencyrate(resp.getCurrencyrate());
                    }
                    if (Util.hasText(resp.getRequestmoneyref())) {
                        wsmodel.setRequestmoneyref(resp.getRequestmoneyref());
                    }
                    if (Util.hasText(resp.getExpiry())) {
                        wsmodel.setExpiry(resp.getExpiry());
                    }
                    if (Util.hasText(resp.getAuthorizationnumber())) {
                        wsmodel.setAuthorizationnumber(resp.getAuthorizationnumber());
                    }
                    if (Util.hasText(resp.getBillamount())) {
                        wsmodel.setBillamount(resp.getBillamount());
                    }
                    if (Util.hasText(resp.getWalletcurrency())) {
                        wsmodel.setWalletcurrency(resp.getWalletcurrency());
                    }
                    if (Util.hasText(resp.getWalletstatus())) {
                        wsmodel.setWalletstatus(resp.getWalletstatus());
                    }
                    if (Util.hasText(resp.getProduct())) {
                        wsmodel.setProduct(resp.getProduct());
                    }
                    if (Util.hasText(resp.getPoolaccountnumber())) {
                        wsmodel.setPoolaccountnumber(resp.getPoolaccountnumber());
                    }
                    if (Util.hasText(resp.getPoolaccountcurrency())) {
                        wsmodel.setPoolaccountcurrency(resp.getPoolaccountcurrency());
                    }
                    if (resp.getAliaslist() != null) {
                        wsmodel.setAliaslist(resp.getAliaslist());
                    }
                    if (resp.getWalletlist() != null) {
                        wsmodel.setWalletlist(resp.getWalletlist());
                    }
                    if (Util.hasText(resp.getAccountcurrency())) {
                        wsmodel.setAccountcurrency(resp.getAccountcurrency());
                    }
                    //if(Util.hasText(resp.getStatus()))
                    //{
                    //    wsmodel.setStatus(resp.getStatus());
                    //}
                    if (Util.hasText(resp.getState())) {
                        wsmodel.setState(resp.getState());
                    }
                    if (Util.hasText(resp.getAcctStatus())) {
                        wsmodel.setAcctStatus(resp.getAcctStatus());
                    }
                    if (Util.hasText(resp.getOriginalapi())) {
                        wsmodel.setOriginalapi(resp.getOriginalapi());
                    }
                    if (Util.hasText(resp.getDestmobilenumber())) {
                        wsmodel.setDestmobilenumber(resp.getDestmobilenumber());
                    }
                    if (Util.hasText(resp.getAmounttransaction())) {
                        wsmodel.setAmounttransaction(resp.getAmounttransaction());
                    }
                    if (Util.hasText(resp.getDestacctid())) {
                        wsmodel.setDestacctid(resp.getDestacctid());
                    }
                    if (Util.hasText(resp.getDestcurrency())) {
                        wsmodel.setDestcurrency(resp.getDestcurrency());
                    }
                    if (Util.hasText(resp.getAmountcbill())) {
                        wsmodel.setAmountcbill(resp.getAmountcbill());
                    }
                    if (Util.hasText(resp.getAmountcommissioncdf())) {
                        wsmodel.setAmountcommissioncdf(resp.getAmountcommissioncdf());
                    }
                    if (Util.hasText(resp.getAmountcommissionusd())) {
                        wsmodel.setAmountcommissionusd(resp.getAmountcommissionusd());
                    }
                    if (resp.getRequestmoneylist() != null) {
                        wsmodel.setRequestmoneylist(resp.getRequestmoneylist());
                    }
                    if (resp.getCashoutlist() != null) {
                        wsmodel.setCashoutlist(resp.getCashoutlist());
                    }
                    if (resp.getPendingremitloglist() != null) {
                        wsmodel.setPendingremitloglist(resp.getPendingremitloglist());
                    }
                    if (resp.getMerchantDashboardDataObjList() != null) {
                        wsmodel.setMerchantDashboardDataObjList(resp.getMerchantDashboardDataObjList());
                    }
                    if (resp.getMerchantDashboardDataObjListForChild() != null) {
                        wsmodel.setMerchantDashboardDataObjListForChild(resp.getMerchantDashboardDataObjListForChild());
                    }
                    if (Util.hasText(resp.getPaymentmethod())) {
                        wsmodel.setPaymentmethod(resp.getPaymentmethod());
                    }
                    if (resp.getTransactionportallist() != null) {
                        wsmodel.setTransactionportallist(resp.getTransactionportallist());
                    }
                    if (Util.hasText(resp.getMobilenumber())) {
                        wsmodel.setMobilenumber(resp.getMobilenumber());
                    }
                    if (Util.hasText(resp.getDestmobilenumber())) {
                        wsmodel.setDestmobilenumber(resp.getDestmobilenumber());
                    }
                    if (Util.hasText(resp.getFilename())) {
                        wsmodel.setFilename(resp.getFilename());
                    }
                    if (Util.hasText(resp.getNewfilename())) {
                        wsmodel.setNewfilename(resp.getNewfilename());
                    }
                    if (resp.getBillpackages() != null) {
                        wsmodel.setBillpackages(resp.getBillpackages());
                    }
                    if (resp.getAmtwithinduedate() != null) {
                        wsmodel.setAmtwithinduedate(resp.getAmtwithinduedate());
                    }
                    if (resp.getAmtafterduedate() != null) {
                        wsmodel.setAmtafterduedate(resp.getAmtafterduedate());
                    }
                    if (resp.getDuedate() != null) {
                        wsmodel.setDuedate(resp.getDuedate());
                    }
                    if (Util.hasText(resp.getBillid())) {
                        wsmodel.setBillid(resp.getBillid());
                    }
                    if (Util.hasText(resp.getTrancurrency())) {
                        wsmodel.setTrancurrency(resp.getTrancurrency());
                    }
                    if (Util.hasText(resp.getWalletcurrency())) {
                        wsmodel.setWalletcurrency(resp.getWalletcurrency());
                    }
                    if (Util.hasText(resp.getTranauthid())) {
                        wsmodel.setTranauthid(resp.getTranauthid());
                    }
                    if (resp.getAppgraphlist() != null) {
                        wsmodel.setAppgraphlist(resp.getAppgraphlist());
                    }
                    if (resp.getSecretquestionslist() != null) {
                        wsmodel.setSecretquestionslist(resp.getSecretquestionslist());
                    }
                    if (Util.hasText(resp.getApiname())) {
                        wsmodel.setApiname(resp.getApiname());
                    }
                    if (Util.hasText(resp.getFrapiname())) {
                        wsmodel.setFrapiname(resp.getFrapiname());
                    }
                    if (resp.getPayerslist() != null) {
                        wsmodel.setPayerslist(resp.getPayerslist());
                    }
                    if (Util.hasText(resp.getPayerid())) {
                        wsmodel.setPayerid(resp.getPayerid());
                    }
                    if (Util.hasText(resp.getPayertype())) {
                        wsmodel.setPayertype(resp.getPayertype());
                    }
                    if (Util.hasText(resp.getQuotationextid())) {
                        wsmodel.setQuotationextid(resp.getQuotationextid());
                    }
                    if (Util.hasText(resp.getQuotationid())) {
                        wsmodel.setQuotationid(resp.getQuotationid());
                    }
                    if (Util.hasText(resp.getTransactionid())) {
                        wsmodel.setTransactionid(resp.getTransactionid());
                    }
                    if (Util.hasText(resp.getTransactionextid())) {
                        wsmodel.setTransactionextid(resp.getTransactionextid());
                    }
                    if (Util.hasText(resp.getIban())) {
                        wsmodel.setIban(resp.getIban());
                    }
                    if (Util.hasText(resp.getKycstatus())) {
                        wsmodel.setKycstatus(resp.getKycstatus());
                    }
                    if (Util.hasText(resp.getDuration())) {
                        wsmodel.setDuration(resp.getDuration());
                    }
                    if (Util.hasText(resp.getSubscriberno())) {
                        wsmodel.setSubscriberno(resp.getSubscriberno());
                    }
                    if (resp.getChargedetails() != null) {
                        wsmodel.setChargedetails(resp.getChargedetails());
                    }
                    if (Util.hasText(resp.getPermissions())) {
                        wsmodel.setPermissions(resp.getPermissions());
                    }
                    if (Util.hasText(resp.getGender())) {
                        wsmodel.setGender(resp.getGender());
                    }
                    if (Util.hasText(resp.getMonthlyincome())) {
                        wsmodel.setMonthlyincome(resp.getMonthlyincome());
                    }
                    if (Util.hasText(resp.getOccupation())) {
                        wsmodel.setOccupation(resp.getOccupation());
                    }
                    if (Util.hasText(resp.getMonthlyexpenditure())) {
                        wsmodel.setMonthlyexpenditure(resp.getMonthlyexpenditure());
                    }
                    if (Util.hasText(resp.getCustcomments())) {
                        wsmodel.setCustcomments(resp.getCustcomments());
                    }
                    if (Util.hasText(resp.getBillerid()) && (wsmodel.getServicename().equals("InitSendMoneyInternational") || wsmodel.getServicename().equals("SendMoneyInternational"))) {
                        logger.info("Setting BillerID [" + resp.getBillerid() + "] ...");
                        wsmodel.setBillerid(resp.getBillerid());
                    }
                    if (Util.hasText(resp.getBillername())) {
                        logger.info("Setting BillerName [" + resp.getBillername() + "] ...");
                        wsmodel.setBillername(resp.getBillername());
                    }
                    if (Util.hasText(resp.getAvailablebalance())) {
                        wsmodel.setAvailablebalance(resp.getAvailablebalance());
                    }
                    if (Util.hasText(resp.getMunicipality())) {
                        wsmodel.setMunicipality(resp.getMunicipality());
                    }
                    if (Util.hasText(resp.getDestfirstname())) {
                        wsmodel.setDestfirstname(resp.getDestfirstname());
                    }
                    if (Util.hasText(resp.getDestlastname())) {
                        wsmodel.setDestlastname(resp.getDestlastname());
                    }
                    if (Util.hasText(resp.getBillerrespcode())) {
                        wsmodel.setBillerrespcode(resp.getBillerrespcode());
                    }
                    if (Util.hasText(resp.getBillerrespcodedesc())) {
                        wsmodel.setBillerrespcodedesc(resp.getBillerrespcodedesc());
                    }
                    if (Util.hasText(resp.getTrantype())) {
                        wsmodel.setTrantype(resp.getTrantype());
                    }
                    if (resp.getUnreadapprovalcount() != null) {
                        wsmodel.setUnreadapprovalcount(resp.getUnreadapprovalcount());
                    }
                    if (Util.hasText(resp.getMerchantname())) {
                        wsmodel.setMerchantname(resp.getMerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantname())) {
                        wsmodel.setDestmerchantname(resp.getDestmerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantcode())) {
                        wsmodel.setDestmerchantcode(resp.getDestmerchantcode());
                    }
                    if (Util.hasText(resp.getMerchantname())) {
                        wsmodel.setMerchantname(resp.getMerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantname())) {
                        wsmodel.setDestmerchantname(resp.getDestmerchantname());
                    }
                    if (Util.hasText(resp.getMerchantcode())) {
                        wsmodel.setMerchantcode(resp.getMerchantcode());
                    }
                    if (Util.hasText(resp.getDestmerchantcode())) {
                        wsmodel.setDestmerchantcode(resp.getDestmerchantcode());
                    }
                    if (wsmodel.getIs_merchant() != null) {
                        wsmodel.setIs_merchant(resp.getIs_merchant());
                    }
                    if (resp.getCountrylist() != null) {
                        wsmodel.setCountrylist(resp.getCountrylist());
                    }
                    if (Util.hasText(resp.getClabe())) {
                        wsmodel.setClabe(resp.getClabe());
                    }
                    if (Util.hasText(resp.getCbu())) {
                        wsmodel.setCbu(resp.getCbu());
                    }
                    if (Util.hasText(resp.getCbualias())) {
                        wsmodel.setCbualias(resp.getCbualias());
                    }
                    if (Util.hasText(resp.getBikcode())) {
                        wsmodel.setBikcode(resp.getBikcode());
                    }
                    if (Util.hasText(resp.getAbaroutingnumber())) {
                        wsmodel.setAbaroutingnumber(resp.getAbaroutingnumber());
                    }
                    if (Util.hasText(resp.getBsbnumber())) {
                        wsmodel.setBsbnumber(resp.getBsbnumber());
                    }
                    if (Util.hasText(resp.getRoutingcode())) {
                        wsmodel.setRoutingcode(resp.getRoutingcode());
                    }
                    if (Util.hasText(resp.getEntityttid())) {
                        wsmodel.setEntityttid(resp.getEntityttid());
                    }
                    if (Util.hasText(resp.getAccounttype())) {
                        wsmodel.setAccounttype(resp.getAccounttype());
                    }
                    if (resp.getCreditaccountnumber() != null) {
                        wsmodel.setCreditaccountnumber(resp.getCreditaccountnumber());
                    }
                    if (resp.getBankMnemonic() != null) {
                        wsmodel.setBankMnemonic(resp.getBankMnemonic());
                    }
                    if (Util.hasText(resp.getRelationshipcode())) {
                        wsmodel.setRelationshipcode(resp.getRelationshipcode());
                    }
                    if (Util.hasText(resp.getMig_cdfbalance())) {
                        wsmodel.setMig_cdfbalance(resp.getMig_cdfbalance());
                    }
                    if (Util.hasText(resp.getMig_usdbalance())) {
                        wsmodel.setMig_usdbalance(resp.getMig_usdbalance());
                    }
                    if (Util.hasText(resp.getOrigdataelement())) {
                        wsmodel.setOrigdataelement(resp.getOrigdataelement());
                    }
                    if (Util.hasText(resp.getCustomerid())) {
                        wsmodel.setCustomerid(resp.getCustomerid());
                    }
                    if (Util.hasText(resp.getNewmobilenumber())) {
                        wsmodel.setNewmobilenumber(resp.getNewmobilenumber());
                    }
                    if (resp.getS2mcardslist() != null) {
                        wsmodel.setS2mcardslist(resp.getS2mcardslist());
                    }
                    if (resp.getTranObjList() != null) {
                        wsmodel.setTranObjList(resp.getTranObjList());
                    }
                    if (resp.getS2mBalance() != null) {
                        wsmodel.setS2mBalance(resp.getS2mBalance());
                    }
                    if (Util.hasText(resp.getOrderid())) {
                        wsmodel.setOrderid(resp.getOrderid());
                    }
                    if (Util.hasText(resp.getRedirecthtml())) {
                        wsmodel.setRedirecthtml(resp.getRedirecthtml());
                    }
                    if (Util.hasText(resp.getCardsecuritycode())) {
                        wsmodel.setCardsecuritycode(resp.getCardsecuritycode());
                    }
                    if (Util.hasText(resp.getThreedsenabled())) {
                        wsmodel.setThreedsenabled(resp.getThreedsenabled());
                    }
                    if (Util.hasText(resp.getThreedsversion())) {
                        wsmodel.setThreedsversion(resp.getThreedsversion());
                    }
                    if (Util.hasText(resp.getThreedsacceptedversion())) {
                        wsmodel.setThreedsacceptedversion(resp.getThreedsacceptedversion());
                    }
                    if (Util.hasText(resp.getScheme())) {
                        wsmodel.setScheme(resp.getScheme());
                    }
                    if (Util.hasText(resp.getBrand())) {
                        wsmodel.setBrand(resp.getBrand());
                    }
                    if (Util.hasText(resp.getSelfflag())) //Raza adding for Till Notification 01-12-2022
                    {
                        wsmodel.setSelfflag(resp.getSelfflag());
                    }
                    if (Util.hasText(resp.getExchangerate())) {
                        wsmodel.setExchangerate(resp.getExchangerate());
                    }
                    if (resp.getCard() != null) {
                        wsmodel.setCard(resp.getCard());
                    }
                    if (resp.getCardlist() != null) {
                        wsmodel.setCardlist(resp.getCardlist());
                    }
                    // Added by Affan on 26-July-23 Start
                    if (resp.getCscBalanceResp() != null) {
                        wsmodel.setCscBalanceResp(resp.getCscBalanceResp());
                    }
                    if (resp.getCscgetamountdueresp() != null) {
                        wsmodel.setCscgetamountdueresp(resp.getCscgetamountdueresp());
                    }
                    if (resp.getCscTransactionList() != null) {
                        wsmodel.setCscTransactionList(resp.getCscTransactionList());
                    }
                    if (resp.getCscGetClientDetailsResp() != null) {
                        wsmodel.setCscGetClientDetailsResp(resp.getCscGetClientDetailsResp());
                    }
                    if (resp.getCscCard() != null) {
                        wsmodel.setCscCard(resp.getCscCard());
                    }
                    if (resp.getCscGetCardsRespList() != null) {
                        wsmodel.setCscGetCardsRespList(resp.getCscGetCardsRespList());
                    }
                    if (resp.getCscLoadCard() != null) {
                        wsmodel.setCscLoadCard(resp.getCscLoadCard());
                    }
                    // Added by Affan on 26-July-23 End
                    if (resp.getForexrates() != null) {
                        wsmodel.setForexrates(resp.getForexrates());
                    }
                    if (Util.hasText(resp.getDestdateofbirth())) {
                        wsmodel.setDestdateofbirth(resp.getDestdateofbirth());
                    }
                    if (Util.hasText(resp.getBillertxnid())) {
                        wsmodel.setBillertxnid(resp.getBillertxnid());
                    }
                }

                wsmodel.setStatus(resp.getStatus());
                wsmodel.setIcccarddata(resp.getIcccarddata());

                return wsmodel;
            } catch (Exception e) {
                logger.error("Exception caught while sending [" + wsmodel.getServicename() + "] to Switch System");
                //e.printStackTrace();
                logger.error(WebServiceUtil.getStrException(e));
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            } finally {
                logger.info("Destroying client for API call, deleting request...");
                if (client != null) {
                    client.destroy();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing ServiceRequest [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static Boolean BuildMsgForOpenAPI(AppWsEntity wsmodel) {
        try {
            if (Util.hasText(wsmodel.getAmounttransaction())) {
                if (wsmodel.getAmounttransaction().contains(".")) {
                    wsmodel.setAmounttransaction(wsmodel.getAmounttransaction().replace(".", ""));
                }
            }

            //Raza only generating & sending TxnRefNum & DateTime before sending to OpenAPI, middleware failed calls should be verified through logs.
            if (!Util.hasText(wsmodel.getTranrefnumber())) {
                wsmodel.setTranrefnumber(Util.generateTxnRefNumber(60));
            }
            if (!Util.hasText(wsmodel.getTransdatetime())) {
                wsmodel.setTransdatetime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
            }

//            Terminal endPointTerminal = null;
//
//            if (GlobalContext.getInstance().getChannelbyId(ChannelCodes.OPENAPI) == null) {
//                logger.error("Open API Channel not Found with ChannelID [" + ChannelCodes.OPENAPI + "], rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.HOST_LINK_DOWN);
//                return false;
//            }
//
//
//            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.OPENAPI)).getInstitutionId();
//            ProcessContext processContext = new ProcessContext();
//            processContext.init();
//            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
//            Set<SecureKey> incomingKeySet = endPointTerminal.getKeySet();
//            SecureDESKey AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES_KEY, incomingKeySet);
//            SecureDESKey AESData = SecureDESKey.getKeyByType(KeyType.TYPE_AES_VALUE, incomingKeySet);

//            String EncryptedData = Base64.getEncoder().encodeToString(WSEncryptionUtil.AES256Encrypt(AESData.getKeyBytes(), AESKey.getKeyBytes()));
            String EncryptedData = "";

            logger.info("Encrypted Key generated for ChannelOPI!");
            wsmodel.setEncryptkey(EncryptedData);
            logger.info("Encrypted Key Updated for ChannelOPI!");
            return true;

        } catch (Exception e) {
            logger.error("Exception caught while updating encrypt key for Channel OpenAPI, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE); //75 Unknown Txn Sorce for OpenAPI
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
    }


    public static String generateEncryptKey(String channelID) {
        try {
            Terminal endPointTerminal = null;

            Channel channel = GlobalContext.getInstance().getChannelbyId(channelID);

            if (channel == null) {
                logger.error("Channel not Found with ChannelID [" + channelID + "], rejecting...");
                return null;
            }

            String institutionCode = channel.getInstitutionId();
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
            Set<SecureKey> incomingKeySet = endPointTerminal.getKeySet();
            SecureDESKey AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES_KEY, incomingKeySet);
            SecureDESKey AESData = SecureDESKey.getKeyByType(KeyType.TYPE_AES_VALUE, incomingKeySet);

            String encrypted = Base64.getEncoder().encodeToString(WSEncryptionUtil.AES256Encrypt(AESData.getKeyBytes(), AESKey.getKeyBytes()));

            logger.info("Encrypted Key generated for channel ID [{}]", channelID);
            return encrypted;

        } catch (Exception e) {
            logger.error("Failed to encrypt key, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            return null;
        }
    }

    public static Boolean ValidateUserandAppSession(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;

        //Verify Also if Customer has an Active Session. In case of 1 Customer Login from 2 Devices!
        dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
        params = new HashMap<String, Object>();
        params.put("MOBNO", wsmodel.getMobilenumber());

        MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

        if(customer == null){
            logger.error("Customer [" + wsmodel.getMobilenumber() + "] not found while validating session, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
            return false;
        }
        else if(Util.hasText(wsmodel.getSecurityparams().getDeviceid()) && wsmodel.getSecurityparams().getDeviceid() != null)
        {
            if(!wsmodel.getServicename().equals("SetLanguage"))
            {
                if(Util.hasText(customer.getLanguage()))
                {
                    wsmodel.setLanguage(customer.getLanguage());
                }
                if(Util.hasText(customer.getNotiflanguage()))
                {
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                }
            }

            if(Util.hasText(wsmodel.getReason()) && !wsmodel.getReason().equals(SMSCategory.LOGOUT_ALL_SESSIONS))
            {
                if(!VerifyDeviceBinding(wsmodel, customer,wsmodel.getSecurityparams().getDeviceid())){
                    logger.error("This Device ID ["+wsmodel.getSecurityparams().getDeviceid()+ "] is not Binding, against this mobilenumber["+wsmodel.getMobilenumber()+"], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                    return false;
                }
            }
        }

        if (wsmodel.getServicename().equals("GetCountryList") || wsmodel.getServicename().equals("CreateWallet") || (wsmodel.getServicename().equals("VerifyWalletPin") && Util.hasText(wsmodel.getReason())) && wsmodel.getReason().equals(SMSCategory.FORGOT_USERNAME)
                || (wsmodel.getServicename().equals("VerifyUserSecretQuestion") && Util.hasText(wsmodel.getReason())) && wsmodel.getReason().equals(SMSCategory.FORGOT_PASSWORD)
                || (wsmodel.getServicename().equals("GetUserKYCQuestionList"))
                || (wsmodel.getServicename().equals("DisableMobileNumberForChange")) || (wsmodel.getServicename().equals("RedeemWallet"))  // For Update Mobile Work
                || (wsmodel.getServicename().equals("UpdateMobileNumber")) || (wsmodel.getServicename().equals("VerifyMobileNumberForChange")) // For Update Mobile Work
                || (wsmodel.getServicename().equals("CancelCashOut") && Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals("MERCHANTPORTAL")) // For EnvoiCash Cancellation from Merchant Portal
                || (wsmodel.getServicename().equals("GetEnvoiCashList") && Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals("MERCHANTPORTAL")) // For EnvoiCash List from Merchant Portal
                || (wsmodel.getServicename().equals("UpdateProfile") && Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals("statusupdate"))
                || (wsmodel.getServicename().equals("UpdateProfile") && Util.hasText(wsmodel.getReason()) && wsmodel.getReason().equals("CreateWallet")) // coming from TruID webhook
        )
            //Raza TODO: PLZ Dupdate THIS
        {
            logger.info("not verifying session for API [{}]", wsmodel.getServicename());
            return true;
        }

        if (wsmodel.getSecurityparams() == null) {
            logger.info("not verifying session for because device ID null API [{}]", wsmodel.getServicename());
            return true;
        }

        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0 && sessionlist.get(0).getToken().equals(wsmodel.getToken())) {
            logger.info("Active session token found for device, verifying session w.r.t. Customer...");


//            if (customer != null) {
                //if(customer.getId() != sessionlist.get(0).getDevice().getCustomer().getId()) //TODO: Raza 1 Device Only
                if (sessionlist.get(0).getDevice().getCustomer() != null && customer.getId() != sessionlist.get(0).getDevice().getCustomer().getId()) {
                    logger.error("Device Session and Customer mismatch, updating device customer after checking session and customer...");
                    if (customer.getId() != sessionlist.get(0).getCustomer().getId()) {
                        wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
                        return false;
                    } else {
                        sessionlist.get(0).getDevice().setCustomer(customer); //Raza Update Last Customer of Device
                    }

                }
                else {
                    sessionlist.get(0).getDevice().setCustomer(customer); //Raza Update Last Customer of Device 15-04-2022
                }
//            } else {
//                logger.error("Customer [" + wsmodel.getMobilenumber() + "] not found while validating session, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
//                return false;
//            }


            //dbQuery = "from " + MWSessionConfig.class.getName() + " c where c.customertype= :CUST ";
            //params = new HashMap<String, Object>();
            //params.put("CUST", CustomerType.CUSTOMER);

            /*
            MWSessionConfig sessconfig = (MWSessionConfig) GeneralDao.Instance.findObject(dbQuery, params);
            if(sessconfig != null && Util.hasText(sessconfig.getIdletimemin()))
            {
                if(Util.hasText(customer.getLastrequesttime()))
                {
                    logger.info("Checking Idle TimeOut...");
                    //logger.info("customer.getLastrequesttime [" + customer.getLastrequesttime() + "]");
                    //logger.info("sessconfig.getIdletimemin() [" + sessconfig.getIdletimemin() + "]");
                    //logger.info("sessconfig.getIdletimemin() + 00 [" + (sessconfig.getIdletimemin() + "00") + "]");
                    //logger.info("Session Idle MAX [" + (Long.parseLong(customer.getLastrequesttime()) + Long.parseLong(sessconfig.getIdletimemin() + "00")) + "]");
                    //logger.info("This TIME [" + Long.parseLong(WebServiceUtil.TransDateTimeFormat.format(new Date())) + "]");
                    if(Long.parseLong(WebServiceUtil.TransDateTimeFormat.format(new Date())) < (Long.parseLong(customer.getLastrequesttime()) + Long.parseLong(sessconfig.getIdletimemin() + "00")))
                    {
                        logger.error("Idle session TimeOut acheived, rejecting...");
                        sessionlist.get(0).setExpired(true);
                        GeneralDao.Instance.saveOrUpdate(sessionlist.get(0));
                        wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
                        return false;
                    }
                }
                else
                {
                    customer.setLastrequesttime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
                    GeneralDao.Instance.saveOrUpdate(customer);
                }
            }
            else
            {
                logger.info("No Session TimeOut Configuration found for Customer, ignoring...");
            }*/


            wsmodel.setUserid(customer.getUserid());
            wsmodel.setCustomerid(customer.getCustomerId());
            wsmodel.setCdfacctid(customer.getCdfaccountid());
            wsmodel.setUsdacctid(customer.getUsdaccountid());
            wsmodel.setUsername(customer.getUsername());

            //For OTP Channel
            if(!Util.hasText(wsmodel.getOtpchannel())){
                wsmodel.setOtpchannel(customer.getOtpchannel());
            }

            if (!Util.hasText(wsmodel.getEmailaddress())) {
                wsmodel.setEmailaddress(customer.getEmailaddress());
            }
            if (!Util.hasText(wsmodel.getDateofbirth())) {
                wsmodel.setDateofbirth(customer.getDateofbirth());
            }

            if(!wsmodel.getServicename().equals("SetLanguage")){
                if (customer != null && Util.hasText(customer.getLanguage())) { //TODO: Raza selecting Customer's selected language over the one present in request -- MultiLanguage -- 03-09-2025
                    wsmodel.setLanguage(customer.getLanguage());
                }
                wsmodel.setNotiflanguage(customer.getNotiflanguage());
            }


            if (!Util.hasText(wsmodel.getFirstname())) {
                wsmodel.setFirstname(customer.getFirstname());
            }
            if (!Util.hasText(wsmodel.getLastname())) {
                wsmodel.setLastname(customer.getLastname());
            }
            //Nofel T: Adding for Improve Linked Accounts - 20-03-2025
            if (!Util.hasText(wsmodel.getGender())) {
                wsmodel.setGender(customer.getGender());
            }
            if (!Util.hasText(wsmodel.getEmailaddress())) {
                wsmodel.setEmailaddress(customer.getEmailaddress());
            }
            if (!Util.hasText(wsmodel.getKycstatus())) {
                wsmodel.setKycstatus(customer.getKycstatus());
            }
            //Nofel: End

            //Raza 10-03-2023 refreshing Session start
            sessionlist.get(0).setExpiredatetime(Long.parseLong(WebServiceUtil.dateFormat.format(DateUtils.addMinutes(new Date(), Integer.parseInt(GetSessionExipreTime())))));
            //Raza 10-03-2023 refreshing Session end

            return true;
        }
        else {
            if (wsmodel.getServicename().equals("GetRegisteredContacts") ||
                    wsmodel.getServicename().equals("GetUserKYCQuestionList") ||
                    wsmodel.getServicename().equals("VerifyUserSecretQuestion")) {

                logger.info("Validating only customer not session...");
//                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
//                params = new HashMap<String, Object>();
//                params.put("MOBNO", wsmodel.getMobilenumber());
//                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (customer != null && customer.getHaswallet() != null && customer.getHaswallet()) {
                    logger.info("Customer verified without session, porcessing...");
                }
                else
                {
                    logger.error("Customer [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                    return false;
                }
                wsmodel.setUserid(customer.getUserid());
                wsmodel.setCdfacctid(customer.getCdfaccountid());
                wsmodel.setUsdacctid(customer.getUsdaccountid());
                return true;
            }
            else if (wsmodel.getServicename().equals("GetKYCQuestionList") || wsmodel.getServicename().equals("GetSessionState")) {
                logger.info("Not validating user or session for GetKYCQuestionList, processing...");
                return true;
            }
            else if (wsmodel.getServicename().equals("UpdateProfile")) {
//                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
//                params = new HashMap<String, Object>();
//                params.put("MOBNO", wsmodel.getMobilenumber());
//
//                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (customer != null && customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) {
                    logger.info("Not validating session for UpdateProfile request for migrated customer, returning...");
                    wsmodel.setUserid(customer.getUserid());
                    wsmodel.setCdfacctid(customer.getCdfaccountid());
                    wsmodel.setUsdacctid(customer.getUsdaccountid());
                    wsmodel.setUsername(customer.getUsername());
                    if (!Util.hasText(wsmodel.getDateofbirth())) {
                        wsmodel.setDateofbirth(customer.getDateofbirth());
                    }
                    if (!Util.hasText(wsmodel.getLanguage())) {
                        wsmodel.setLanguage(customer.getLanguage());
                    }
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    if (!Util.hasText(wsmodel.getFirstname())) {
                        wsmodel.setFirstname(customer.getFirstname());
                    }
                    if (!Util.hasText(wsmodel.getLastname())) {
                        wsmodel.setLastname(customer.getLastname());
                    }
                    return true;
                }
            }
            else if (wsmodel.getServicename().equals("ConfirmOTP") && Util.hasText(wsmodel.getAdvanceflag()) && wsmodel.getAdvanceflag().equals("true")) {
                logger.info("Not validating session for LogIn-Migrated-SignUp...");
                return true;
            }


            logger.info("No Active session token found, rejecting...");

            //TODO: Raza update expired logic
            MarkSessionsExpire(wsmodel);
            /*
            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
            params = new HashMap<String, Object>();
            params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
            params.put("ISEXPIRED", false);

            sessionlist = GeneralDao.Instance.find(dbQuery, params);
            if(sessionlist != null && sessionlist.size() > 0)
            {
                for(MWDeviceSessionLog sess : sessionlist)
                {
                    if(sess.getExpiredatetime() <= Long.parseLong(WebServiceUtil.dateFormat.format(new Date()))) {
                        sess.setExpired(true);
                        GeneralDao.Instance.saveOrUpdate(sess);
                    }
                }
            }*/
            //TODO: Raza update expired logic

            wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED); //94 - Permission Denied
            return false;
        }
    }


    public static Boolean ValidateUserSession(AppWsEntity wsmodel, MWCustomer customer) {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0 && sessionlist.get(0).getToken().equals(wsmodel.getToken())) {
            logger.info("Active session token found for customer, checking allowed Idle Time...");

            dbQuery = "from " + MWSessionConfig.class.getName() + " c where c.customertype= :CUST ";
            params = new HashMap<String, Object>();
            params.put("CUST", CustomerType.CUSTOMER);

            MWSessionConfig sessconfig = (MWSessionConfig) GeneralDao.Instance.findObject(dbQuery, params);
            if (sessconfig != null && Util.hasText(sessconfig.getIdletimemin())) {
                if (Util.hasText(customer.getLastrequesttime())) {
                    if(Util.hasText(customer.getLanguage()))
                    {
                        wsmodel.setLanguage(customer.getLanguage());
                    }
                    if(Util.hasText(customer.getNotiflanguage()))
                    {
                        wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    }

                    if ((Long.parseLong(customer.getLastrequesttime()) + Long.parseLong(sessconfig.getIdletimemin() + "00")) < Long.parseLong(WebServiceUtil.TransDateTimeFormat.format(new Date()))) {
                        logger.info("Customer [" + customer.getMobilenumber() + "] LastRequestTime [" + customer.getLastrequesttime() + "] ExpTime [" + (Long.parseLong(customer.getLastrequesttime()) + Long.parseLong(sessconfig.getIdletimemin() + "00")) + "] Current Time [" + Long.parseLong(WebServiceUtil.TransDateTimeFormat.format(new Date())) + "], rejecting...");

                        logger.error("Idle session TimeOut acheived, rejecting...");
                        sessionlist.get(0).setExpired(true);
                        GeneralDao.Instance.saveOrUpdate(sessionlist.get(0));
                        wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
                        return false;
                    }
                } else {

                    customer.setLastrequesttime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
                    GeneralDao.Instance.saveOrUpdate(customer);
                }
            }

            return true;
        }
        else {
            logger.info("No Active session token found, rejecting...");


            //TODO: Raza update expired logic
            MarkSessionsExpire(wsmodel);
            /*
            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
            params = new HashMap<String, Object>();
            params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
            params.put("ISEXPIRED", false);

            sessionlist = GeneralDao.Instance.find(dbQuery, params);
            if(sessionlist != null && sessionlist.size() > 0)
            {
                for(MWDeviceSessionLog sess : sessionlist)
                {
                    if(sess.getExpiredatetime() <= Long.parseLong(WebServiceUtil.dateFormat.format(new Date()))) {
                        sess.setExpired(true);
                        GeneralDao.Instance.saveOrUpdate(sess);
                    }
                }
            }*/
            //TODO: Raza update expired logic

            wsmodel.setRespcode(ISOResponseCodes.MW_SESSION_EXPIRED);
            return false;
        }

    }

    public static Boolean CloseUserSessions(AppWsEntity wsmodel, MWCustomer customer) {
        try {
            //String dbQuery;
            //Map<String, Object> params;
            //TODO: Raza update expired logic
            MarkSessionsExpire(wsmodel);
            /*
            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
            params = new HashMap<String, Object>();
            params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
            params.put("ISEXPIRED", false);

            List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);
            if (sessionlist != null && sessionlist.size() > 0) {
                for (MWDeviceSessionLog sess : sessionlist) {
                    sess.setExpired(true);
                    GeneralDao.Instance.saveOrUpdate(sess);
                }
            }*/
            return true; //Raza even if no session found for Customer, still reply OK
            //TODO: Raza update expired logic
        } catch (Exception e) {
            logger.error("");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
    }

    public static Boolean ValidateRegisterdDeviceForSignUp(AppWsEntity wsmodel) {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0) {
            logger.info("Active session token found for device, cannot allow new SignUp. Rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
            return false;
        }
        else {
            logger.info("No Active session found for existing device for SignUP, checking w.r.t. firebase token...");

            if (Util.hasText(wsmodel.getSecurityparams().getFirebasetoken())) //TODO: Raza Update This adding on RunTime
            {
                dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.firebasetoken= :FIRE " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                params = new HashMap<String, Object>();

                params.put("FIRE", wsmodel.getSecurityparams().getFirebasetoken());

                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                sessionlist = GeneralDao.Instance.find(dbQuery, params);
            } else //TODO: Raza Update This adding on RunTime
            {
                sessionlist = null;
            }

            if (sessionlist != null && sessionlist.size() > 0) {
                logger.info("Active session token found for device against firebasetoken, cannot allow new SignUp. Rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
                return false;
            } else {
                logger.info("No Active session found of existing device for SignUP, allowing...");

                if (Util.hasText(wsmodel.getSecurityparams().getFirebasetoken())) {
                    //TODO: Raza update FireBase Token Update Logic
                    dbQuery = "from " + MWCustomer.class.getName() + " c where c.firebasetoken= :FIRE ";
                    params = new HashMap<String, Object>();
                    params.put("FIRE", wsmodel.getSecurityparams().getFirebasetoken());
                    List<MWCustomer> firebasecustomers = GeneralDao.Instance.find(dbQuery, params);
                    if (firebasecustomers != null && firebasecustomers.size() > 0) {
                        for (MWCustomer mwcust : firebasecustomers) {
                            mwcust.setFirebasetoken(null);
                            GeneralDao.Instance.saveOrUpdate(mwcust);
                        }
                    }
                    //TODO: Raza update FireBase Token Update Logic
                }

            }


            return true;
        }


    }

    public static boolean ValidateEncryptedKey(AppWsEntity wsmodel) //String EncryptKey)
    {
        //Implement Encrypt Key verification here
        String institutionCode = null;
        ProcessContext processContext = null;
        Terminal endPointTerminal = null;
        Set<SecureKey> incomingKeySet = null;
        SecureDESKey AESKey = null;
        SecureDESKey AESData = null;
        String DecryptedData = null;
        try {
            //Implement Encrypt Key verification here


            //if(Util.hasText(wsmodel.getVersion2()) && wsmodel.getVersion2().equals("true"))
            if (wsmodel.getVersion().equals("false"))
            {
//                //Raza adding to depricate V2 23-11-2024 start
//                if(wsmodel.getSecurityparams() != null && Util.hasText(wsmodel.getSecurityparams().getDeviceid())){
//                    logger.error("V2 Request received from Device Id [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
//                    return false;
//                }
//                //Raza adding to depricate V2 23-11-2024 end

                if (GlobalContext.getInstance().getChannelbyId(ChannelCodes.MOBILEAPP) == null) {
                    logger.error("Invalid ChannelId, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return false;
                }

                institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.MOBILEAPP)).getInstitutionId(); //ChannelCodes.MOBILEAPP)).getInstitutionId();
                //endPointTerminal = wsmodel.getProcessContext().getAcquierSwitchTerminal(institutionCode); //Raza commenting
                processContext = new ProcessContext();
                processContext.init();
                endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
                incomingKeySet = endPointTerminal.getKeySet();
                AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES_KEY, incomingKeySet);
                AESData = SecureDESKey.getKeyByType(KeyType.TYPE_AES_VALUE, incomingKeySet);

                //String Key = "8a0e35bb17f6b07104d967d0f39646ccc492f333460d7e8d942daa007daf6d6"; //Raza TODO put in DB
                //String Data = "baebd8095263eed4beb79ced664c054f32a707b5e7c31049d5a3236860c1a904"; //Raza TODO put in DB

                    DecryptedData = WSEncryptionUtil.AES256Decrypt(Base64.getDecoder().decode(wsmodel.getEncryptkey()), AESKey.getKeyBytes());

                    if (DecryptedData.equals(AESData.getKeyBytes())) {
                        logger.info("EncryptedKey Validated successfully!");

                        //wsmodel.setStan(Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));

                        if (!Util.hasText(wsmodel.getStan())) //TODO: Raza use sequence for STAN & Not Random
                        {
                            wsmodel.setStan(Util.generateStan());
                        }
                        if (!Util.hasText(wsmodel.getTransdatetime())) {
                            wsmodel.setTransdatetime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
                        }
                        if (!Util.hasText(wsmodel.getRrn())) {
                            wsmodel.setRrn(Util.generateRrn(wsmodel.getStan(), wsmodel.getTransdatetime()));
                        }
                        return true;
                    } else {
                        logger.error("EncryptedKey Validated failed for V2!");
                        wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
                        return false;
                    }

            }
            else if (wsmodel.getVersion().equals("true")) {

                if (GlobalContext.getInstance().getChannelbyId(ChannelCodes.MOBILEAPPV3) == null) {
                    logger.error("Invalid ChannelId for V3, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return false;
                }

                institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.MOBILEAPPV3)).getInstitutionId(); //ChannelCodes.MOBILEAPP)).getInstitutionId();

                processContext = new ProcessContext();
                processContext.init();
                endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
                incomingKeySet = endPointTerminal.getKeySet();
                AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES_KEY, incomingKeySet);
                AESData = SecureDESKey.getKeyByType(KeyType.TYPE_AES_VALUE, incomingKeySet);

                //String Key = "8a0e35bb17f6b07104d967d0f39646ccc492f333460d7e8d942daa007daf6d6"; //Raza TODO put in DB
                //String Data = "baebd8095263eed4beb79ced664c054f32a707b5e7c31049d5a3236860c1a904"; //Raza TODO put in DB

                try {
                    DecryptedData = WSEncryptionUtil.AES256Decrypt(Base64.getDecoder().decode(wsmodel.getEncryptkey()), AESKey.getKeyBytes());

                    if (DecryptedData.equals(AESData.getKeyBytes())) {
                        logger.info("EncryptedKey Validated successfully for V3!");

                        //wsmodel.setStan(Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));

                        if (!Util.hasText(wsmodel.getStan())) //TODO: Raza use sequence for STAN & Not Random
                        {
                            wsmodel.setStan(Util.generateStan());
                        }
                        if (!Util.hasText(wsmodel.getTransdatetime())) {
                            wsmodel.setTransdatetime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
                        }
                        if (!Util.hasText(wsmodel.getRrn())) {
                            wsmodel.setRrn(Util.generateRrn(wsmodel.getStan(), wsmodel.getTransdatetime()));
                        }
                        return true;
                    } else {
                        logger.error("EncryptedKey Validated failed for V3!");
                        wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
                        return false;
                    }
                } catch (Exception e) {
                    logger.error("Exception caught while decrypting Encryption Key for V3, rejecting...");
                    logger.error(WebServiceUtil.getStrException(e));

                    wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA);
                    return false;
                }
            }
            else {
                logger.error("Encrypt Key verification failed due to invalid version [" + wsmodel.getVersion() + "]");
                wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA); //Raza 13-07-2022 for V3
                return false;
            }

        } catch (Exception e) {
            logger.error("Exception caught while decrypting Encryption Key, checking for V3...");
            logger.error(WebServiceUtil.getStrException(e));
            //e.printStackTrace();
            //wsmodel.setRespcode(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE);
            wsmodel.setRespcode(ISOResponseCodes.ERROR_ENCRYPTDATA); //Raza 13-07-2022 for V3
            return false;
        }
    }


    public static void GenerateTxnDataForSwitch(AppWsEntity wsmodel) {
        logger.info("Generating Data for Switch...");


//        if(Util.hasText(wsmodel.getMobilenumber()))
//        {
//            wsmodel.setUserid(wsmodel.getMobilenumber());
//        }
//
//        if(Util.hasText(wsmodel.getDestmobilenumber()))
//        {
//            wsmodel.setDestuserid(wsmodel.getDestmobilenumber());
//        }

        wsmodel.setTranrefnumber(GenerateUUID());
        wsmodel.setTransdatetime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
        //wsmodel.setStan(Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));
        //wsmodel.setRrn(wsmodel.getStan() + wsmodel.getStan());
        wsmodel.setChannelid(ChannelCodes.MOBILEAPP); //TODO: Raza Update THIS
        wsmodel.setEncryptkey("wmEiiZkAxjH6lSTqDw++In9LNLVpIwCRTSplDBt1LyCrDmfmTwg61ajYEZZLgFVjc9Tm3ZcCiv9StE3Riz6uWvD8BfoMi3gqV0MGEM6ZLZqONoqiwY35CZDd8/UeWUjY");
        //TODO: Raza Update THIS

    }

    public static Boolean ValidateUserandDeviceforReset(AppWsEntity wsmodel) {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0 && sessionlist.get(0).getToken().equals(wsmodel.getToken())) {
            logger.info("Active session token found for device, cannot reset password user already LoggedIN, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
            return false;
        } else {
            logger.info("Validating customer for reset password...");
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());
            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
            if (customer != null) //&& customer.getHaswallet() != null && customer.getHaswallet()) // && customer.getStatus().equals(CustomerStatus.ACTIVE))
            {
                logger.info("Customer verified without session, validating customer session from other devices...");

                dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.customer.mobilenumber= :MOB " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                params = new HashMap<String, Object>();
                params.put("MOB", wsmodel.getMobilenumber());
                params.put("ISEXPIRED", false);
                params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                sessionlist = GeneralDao.Instance.find(dbQuery, params);
                if (sessionlist != null && sessionlist.size() > 0) {
                    logger.error("Customer already have an active session from other device, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
                    return false;
                }


                    /*if(customer.getResetcredentials() != null && customer.getResetcredentials())
                    {
                        logger.error("Customer with mobile [" + customer.getMobilenumber() + "] in Reset State, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_CHANGE_PASSWORD);
                        return false;
                    }*/

                return true;
            } else {
                if (customer != null) {
                    logger.error("Customer [" + wsmodel.getMobilenumber() + "] has no valid or has invalid status, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_CUSTOMER_STATUS);
                    return false;
                }

                logger.error("Customer [" + wsmodel.getMobilenumber() + "] not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                return false;
            }
        }
    }

    public static String GenerateUUID() {
        String value = "";
        String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        int length = 24;
        while (length > 0) {
            int character = (int) (Math.random() * 24);
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
            length--;
        }
        //Global.useridcount--;
        //value = builder.toString()+Global.useridcount;
        //value = StringUtils.rightPad(value,'x');
        return builder.toString();
    }


    public static String GetandExecuteOriginalTxn(String origtxn, AppWsEntity wsmodel) {
        logger.info("Getting Original Transction from Confirm OTP Request.....");

        if (!Util.hasText(origtxn)) //Raza checking again TODO: remove if required
        {
            logger.error("TxnRefNum not present to get Original Txn, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
            return null;
        }

        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + AppWsEntity.class.getName() + " c where c.tranrefnumber= :TXNREF " + " and c.respcode in( :RESP ) ";
        params = new HashMap<String, Object>();
        params.put("TXNREF", origtxn);
        List<String> respcodes = new ArrayList<>();
        respcodes.add(ISOResponseCodes.APPROVED);
        respcodes.add(ISOResponseCodes.MW_APPROVED);
        params.put("RESP", respcodes);

        AppWsEntity dbtxn = (AppWsEntity) GeneralDao.Instance.findObject(dbQuery, params);

        if (dbtxn == null) {
            logger.error("NO Approved or Original Txn not found against Txnrefnum [{}], rejecting...", origtxn);
            wsmodel.setRespcode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
            return null;
        }

        logger.info("Original Txn found [" + dbtxn.getServicename() + "] for OTP Confirmation, processing...");
        wsmodel.setOrigdataelement(dbtxn.getTranrefnumber());
        wsmodel.setServicename("ConfirmBankOtp"); //Raza middleware does not use service name so overwriting..
        wsmodel.setAccountnumber(dbtxn.getAccountnumber());
        wsmodel.setAccountcurrency(dbtxn.getAccountcurrency());
        wsmodel.setTrancurrency(dbtxn.getTrancurrency());
        wsmodel.setDestaccount(dbtxn.getDestaccount());
        wsmodel.setDestcurrency(dbtxn.getDestcurrency());
        wsmodel.setPoolaccountnumber(dbtxn.getPoolaccountnumber());
        wsmodel.setPoolaccountcurrency(dbtxn.getPoolaccountcurrency());
        wsmodel.setBankcode(dbtxn.getBankcode());
        wsmodel.setPaymentmethod(dbtxn.getPaymentmethod());
        wsmodel.setOriginalapi(dbtxn.getServicename()); //Raza 06052021
        wsmodel.setAcctalias(dbtxn.getAcctalias()); //Raza 07052021
        wsmodel.setDestmobilenumber(dbtxn.getDestmobilenumber()); //Raza 07052021
        //Muhammad Hamza: Adding for Mobile Banking Banking : Bank to other Bank - start
        wsmodel.setTypefilter(dbtxn.getTypefilter());
        wsmodel.setBikcode(Util.hasText(dbtxn.getBiccode()) ? dbtxn.getBiccode() : null);
        wsmodel.setDestusername(Util.hasText(dbtxn.getDestusername()) ? dbtxn.getDestusername() : null);
        wsmodel.setDestaddress(Util.hasText(dbtxn.getDestaddress()) ? dbtxn.getDestaddress() : null);
        wsmodel.setAddress(Util.hasText(dbtxn.getAddress()) ? dbtxn.getAddress() : null);
        wsmodel.setBankname(Util.hasText(dbtxn.getBankname()) ? dbtxn.getBankname() : null);
        //Muhammad Hamza: Adding for Mobile Banking Banking : Bank to other Bank - End
//            wsmodel.setBiccode(Util.hasText(dbtxn.getBikcode()) ? dbtxn.getBikcode() : dbtxn.getBiccode());
        return dbtxn.getServicename();
    }

    public static String GetSessionExipreTime() {
        try {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWSessionConfig.class.getName() + " c where c.customertype= :CUST ";
            params = new HashMap<String, Object>();
            params.put("CUST", CustomerType.CUSTOMER);

            MWSessionConfig sessconfig = (MWSessionConfig) GeneralDao.Instance.findObject(dbQuery, params);

            if (sessconfig != null && Util.hasText(sessconfig.getSessiontimemin())) {
                return sessconfig.getSessiontimemin();
            } else {
                logger.error("SessionConfig not found for Customer, returning by code...");
                return "10";
            }
        } catch (Exception e) {
            logger.error("Exception caught while getting SessionConfig for Customer, returning by code...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            return "10";
        }
    }

    public static Boolean systemDeviceBindingEnabled() {
        try {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWSessionConfig.class.getName() + " c where c.customertype= :CUST ";// + "or c.id= :ID";
            params = new HashMap<String, Object>();
            params.put("CUST", CustomerType.CUSTOMER);
            //params.put("ID",1);

            MWSessionConfig sessconfig = (MWSessionConfig) GeneralDao.Instance.findObject(dbQuery, params);

            if (sessconfig.getGlobalswitch() != null && sessconfig.getGlobalswitch()) {
                return true;
            } else {
                logger.info("System Device Binding Disable for Every Customer, ignoring...");
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception caught while getting Global Swicth for Customer, returning false...");
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
    }

    public static void CheckandSendSMS(AppWsEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.
        try {
            String servicename = wsmodel.getServicename();

            if (Util.hasText(servicename) && servicename.equals("CancelCashOut") && Util.hasText(wsmodel.getDestmobilenumber())) {
                servicename = "CancelEnvoiCash";
            } else if (Util.hasText(servicename) && servicename.equals("QRSendMoney") && Util.hasText(wsmodel.getMerchantcode())) {
                logger.error("Changing service from " + servicename + " to MerchantQRSendMoney"); // waleed changing service name on identification of message
                servicename = "MerchantQRSendMoney";
            }

            //TODO: Raza adding below ConfirmBankOtp check 06052021 -- Fix it
            SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(servicename);
            if (trancode != null && trancode.getOtprequired() != null && trancode.getOtprequired() && !wsmodel.getServicename().equals("ConfirmBankOtp")) {
                if (trancode.getSmsconfig() != null) {
                    if (wsmodel.getServicename().equals("GetBankDetails") || (Util.hasText(wsmodel.getOriginalapi()) && wsmodel.getOriginalapi().equals("LoadWallet")) && Util.hasText(wsmodel.getDestmobilenumber())) {
                        //Hamza Adding for OTP Channel to send Email OTP according to set OTP Channel =- Start

                        if(!Util.hasText(wsmodel.getOtpchannel()) || (Util.hasText(wsmodel.getOtpchannel()) && (wsmodel.getOtpchannel().equals(OTPChannel.SMS)
                                || wsmodel.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))){
                            logger.info("Recieving Otp Channel is [" + wsmodel.getOtpchannel() + "], sending SMS...");
                            if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getDestmobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }
                        //Hamza Adding for OTP Channel to send Email OTP according to set OTP Channel =- END
                    } else if ((wsmodel.getServicename().equals("LoadWallet") || wsmodel.getServicename().equals("BankToWallet") ||
                            wsmodel.getServicename().equals("BankToAlias") ||
                            wsmodel.getServicename().equals("BankToBank")) && Util.hasText(wsmodel.getDestmobilenumber()))
                    {
                        if(!Util.hasText(wsmodel.getOtpchannel()) || (Util.hasText(wsmodel.getOtpchannel()) && (wsmodel.getOtpchannel().equals(OTPChannel.SMS)
                                || wsmodel.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))){
                            logger.info("Recieving Otp Channel is [" + wsmodel.getOtpchannel() + "], sending SMS...");
                            if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getDestmobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }
                        //Hamza Adding for OTP Channel to send Email OTP according to set OTP Channel =- END

                    } else {
                        //Hamza Adding for OTP Channel to send Email OTP according to set OTP Channel =- Start

                        if(!Util.hasText(wsmodel.getOtpchannel()) || (Util.hasText(wsmodel.getOtpchannel()) && (wsmodel.getOtpchannel().equals(OTPChannel.SMS)
                                || wsmodel.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))){
                            logger.info("Recieving Otp Channel is [" + wsmodel.getOtpchannel() + "], Now sending SMS...");
                            if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }

                        //Hamza Adding for OTP Channel to send Email OTP according to set OTP Channel =- END
                    }

                }
            }
            else if (trancode != null && trancode.getSmsenabled() != null && trancode.getSmsenabled() && (!Util.hasText(wsmodel.getOriginalapi()) || (Util.hasText(wsmodel.getOriginalapi()) && !wsmodel.getOriginalapi().equals("ConfirmBankOtp")))) {
                if (trancode.getSmsconfig() != null) {
                    if (!SMSGatewayHandler.CreateandSendSMS(trancode.getSmsconfig(), wsmodel.getMobilenumber(), wsmodel, false)) {
                        logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                        //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent SMS, replying as per transaction response...");
            logger.error(WebServiceUtil.getStrException(e));
        }

        try {
            if (Util.hasText(wsmodel.getDestmobilenumber()) &&
                    !wsmodel.getServicename().equals("BankToWalletRequest")
                    && !wsmodel.getServicename().equals("BankToAliasRequest")
                    && !wsmodel.getServicename().equals("BankToBankRequest")
                    && !wsmodel.getServicename().equals("LoadWalletRequest")
                    && !wsmodel.getServicename().equals("UnloadWallet")
                    && !wsmodel.getServicename().equals("ConfirmBankOtp")) {
                String servicename = wsmodel.getServicename();

                if (Util.hasText(servicename) && servicename.equals("CancelCashOut") && Util.hasText(wsmodel.getDestmobilenumber())) {
                    servicename = "CancelEnvoiCash";
                } else if (Util.hasText(servicename) && servicename.equals("QRSendMoney") && Util.hasText(wsmodel.getMerchantcode())) {
                    logger.error("Changing service from " + servicename + " to MerchantQRSendMoney"); // waleed changing service name on identification of message
                    servicename = "MerchantQRSendMoney";
                }

                SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(servicename);
                if (trancode != null && trancode.getSmsenabled() != null && trancode.getSmsenabled()) {
                    if (trancode.getSmsconfig() != null) {

                        if (!SMSGatewayHandler.CreateandSendSMS(trancode.getSmsconfig(), wsmodel.getDestmobilenumber(), wsmodel, true)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                            //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        }

                        if (Util.hasText(wsmodel.getServicename())
                                && (wsmodel.getServicename().equals("QRSendMoney") || wsmodel.getServicename().equals("MerchantQRSendMoney"))
                                && Util.hasText(wsmodel.getSelfflag()) && wsmodel.getSelfflag().equals("true")
                                && Util.hasText(wsmodel.getAlternatenumber())) {
                            if (!SMSGatewayHandler.CreateandSendSMS(trancode.getSmsconfig(), wsmodel.getAlternatenumber(), wsmodel, true)) {
                                logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getAlternatenumber() + "], ignoring...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent SMS to DestMobile, replying as per transaction response...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }
    }

    public static void CheckandSendNotification(AppWsEntity wsmodel) {
        //Raza check if Notification is required or not
        //logger.info("Checking if Notification required...");
        try {

            String servicename = wsmodel.getServicename();

            if (Util.hasText(servicename) && servicename.equals("CancelCashOut") && Util.hasText(wsmodel.getDestmobilenumber())) {
                servicename = "CancelEnvoiCash";
            }
            SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(servicename);
            if (trancode != null && trancode.getNotificationenabled() != null && trancode.getNotificationenabled()) {
                //logger.info("Notification required...");
                if (trancode.getNotificationconfig() != null) {
                    //logger.info("Notification Config found...");
                    if (!NotificationHandler.CreateandSendNotification(trancode.getNotificationconfig(), wsmodel.getMobilenumber(), wsmodel, false)) {
                        logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                        //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent Notification, replying as per transaction response...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }

        try {
            if (Util.hasText(wsmodel.getDestmobilenumber()) && !wsmodel.getServicename().equals("GetBankDetails") && /*!wsmodel.getServicename().equals("BankToWallet") && !wsmodel.getServicename().equals("BankToAlias") &&*/ !wsmodel.getServicename().equals("UnloadWallet")) {
                String servicename = wsmodel.getServicename();

                if (Util.hasText(servicename) && servicename.equals("CancelCashOut") && Util.hasText(wsmodel.getDestmobilenumber())) {
                    servicename = "CancelEnvoiCash";
                }
                SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(servicename);
                if (trancode != null && trancode.getNotificationenabled() != null && trancode.getNotificationenabled()) {
                    if (trancode.getNotificationconfig() != null) {
                        if (!NotificationHandler.CreateandSendNotification(trancode.getNotificationconfig(), wsmodel.getDestmobilenumber(), wsmodel, true)) {
                            logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                            //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent Notification to DestMobile, replying as per transaction response...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }
    }

    public static void CheckandSendEmail(AppWsEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.
        try {
            String servicename = wsmodel.getServicename();

            if (Util.hasText(servicename) && servicename.equals("CancelCashOut") && Util.hasText(wsmodel.getDestemailaddress())) {
                servicename = "CancelEnvoiCash";
            } else if (Util.hasText(servicename) && servicename.equals("QRSendMoney") && Util.hasText(wsmodel.getMerchantcode())) {
                logger.error("Changing service from " + servicename + " to MerchantQRSendMoney"); // waleed changing service name on identification of message
                servicename = "MerchantQRSendMoney";
            }

            //TODO: Raza adding below ConfirmBankOtp check 06052021 -- Fix it
            SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(servicename);
            if (trancode != null && trancode.getOtprequired() != null && trancode.getOtprequired()
                    && !wsmodel.getServicename().equals("ConfirmBankOtp")) {
                if (trancode.getEmailconfig() != null) {
                    if (wsmodel.getServicename().equals("GetBankDetails") || (Util.hasText(wsmodel.getOriginalapi())
                            && wsmodel.getOriginalapi().equals("LoadWallet")) && Util.hasText(wsmodel.getDestmobilenumber())) {

                        if(!Util.hasText(wsmodel.getOtpchannel()) || (Util.hasText(wsmodel.getOtpchannel()) && (wsmodel.getOtpchannel().equals(OTPChannel.EMAIL)
                                || wsmodel.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))){
                            if (!EmailGatewayHandler.CreateandSendEmailOTP(trancode.getEmailconfig(), wsmodel.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }


                    } else if ((wsmodel.getServicename().equals("LoadWallet") || wsmodel.getServicename().equals("BankToWallet") ||
                            wsmodel.getServicename().equals("BankToAlias") ||
                            wsmodel.getServicename().equals("BankToBank")) && Util.hasText(wsmodel.getDestemailaddress())) {

                        if(!Util.hasText(wsmodel.getOtpchannel()) || (Util.hasText(wsmodel.getOtpchannel()) && (wsmodel.getOtpchannel().equals(OTPChannel.EMAIL)
                                || wsmodel.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))){
                            if (!EmailGatewayHandler.CreateandSendEmailOTP(trancode.getEmailconfig(), wsmodel.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }
                    } else {
                        if(!Util.hasText(wsmodel.getOtpchannel()) || (Util.hasText(wsmodel.getOtpchannel()) && (wsmodel.getOtpchannel().equals(OTPChannel.EMAIL)
                                || wsmodel.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))){
                            if (!EmailGatewayHandler.CreateandSendEmailOTP(trancode.getEmailconfig(), wsmodel.getEmailaddress(), wsmodel)) {
                                logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                                //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }
                    }

                }
            } else if (trancode != null && trancode.getEmailenabled() != null && trancode.getEmailenabled()
                    && (!Util.hasText(wsmodel.getOriginalapi()) || (Util.hasText(wsmodel.getOriginalapi())
                    && !wsmodel.getOriginalapi().equals("ConfirmBankOtp")))) {
                if (trancode.getEmailconfig() != null) {
                    if (!EmailGatewayHandler.CreateandSendEmail(trancode.getEmailconfig(), wsmodel.getEmailaddress(), wsmodel, false)) {
                        logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                        //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent Email, replying as per transaction response...");
            e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }

        try {
            if (Util.hasText(wsmodel.getDestemailaddress()) &&
                    !wsmodel.getServicename().equals("BankToWalletRequest")
                    && !wsmodel.getServicename().equals("BankToAliasRequest")
                    && !wsmodel.getServicename().equals("BankToBankRequest")
                    && !wsmodel.getServicename().equals("LoadWalletRequest")
                    && !wsmodel.getServicename().equals("UnloadWallet")
                    && !wsmodel.getServicename().equals("ConfirmBankOtp")) {
                String servicename = wsmodel.getServicename();

                if (Util.hasText(servicename) && servicename.equals("CancelCashOut") && Util.hasText(wsmodel.getDestemailaddress())) {
                    servicename = "CancelEnvoiCash";
                } else if (Util.hasText(servicename) && servicename.equals("QRSendMoney") && Util.hasText(wsmodel.getMerchantcode())) {
                    logger.error("Changing service from " + servicename + " to MerchantQRSendMoney"); // waleed changing service name on identification of message
                    servicename = "MerchantQRSendMoney";
                }

                SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(servicename);
                if (trancode != null && trancode.getEmailenabled() != null && trancode.getEmailenabled()) {
                    if (trancode.getEmailconfig() != null) {

                        if (!EmailGatewayHandler.CreateandSendEmail(trancode.getEmailconfig(), wsmodel.getDestemailaddress(), wsmodel, true)) {
                            logger.error("Failed to create & Send Email for Address [" + wsmodel.getDestemailaddress() + "], rejecting...");
                            //wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent Email to DestEmail, replying as per transaction response...");
            e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }
    }


    public static Boolean ValidateUserandAppSessionForStartUp(AppWsEntity wsmodel) {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0) {
            logger.info("Active session token found for device, verifying session w.r.t. Customer...");

            if (Util.hasText(wsmodel.getMobilenumber())) {
                //Verify Also if Customer has an Active Session. In case of 1 Customer Login from 2 Devices!
                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOBNO ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);
                if (customer != null && customer.getHaswallet() != null && customer.getHaswallet() && customer.getIsmigratedactive() != null && !customer.getIsmigratedactive()) {
                    if (sessionlist.get(0).getDevice() != null && sessionlist.get(0).getDevice().getCustomer() != null && customer.getId() != sessionlist.get(0).getDevice().getCustomer().getId()) {
                        logger.error("Device Session and Customer mismatch, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION);
                        return false;
                    } //TODO: Raza verify this below check
                    else if (sessionlist.get(0).getDevice() != null && sessionlist.get(0).getDevice().getCustomer() == null) {
                        logger.error("Session Found but Device Customer is null, LoggingOut Session and returning...");
                        sessionlist.get(0).setExpired(true);
                        GeneralDao.Instance.saveOrUpdate(sessionlist.get(0));
                        wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                        return false;
                    }
                } else if (customer != null && customer.getHaswallet() != null && customer.getHaswallet() && customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) {
                    logger.info("Active Session found for migrated customer with wallets, replying...");
                    wsmodel.setTotalcount((sessionlist.get(0).getCustomer().getUnreadnotifcount() != null) ? sessionlist.get(0).getCustomer().getUnreadnotifcount() + "" : "0");
                    wsmodel.setToken(sessionlist.get(0).getToken());
                    wsmodel.setLanguage(sessionlist.get(0).getCustomer().getLanguage());
                    wsmodel.setNotiflanguage(Util.hasText(sessionlist.get(0).getCustomer().getNotiflanguage()) ? sessionlist.get(0).getCustomer().getNotiflanguage() : Util.getDefaultMobileAppLanguage());
                    wsmodel.setPermissions(sessionlist.get(0).getCustomer().getPermissions());
                    wsmodel.setKycstatus(GlobalContext.getInstance().getKYCStatusDetails(sessionlist.get(0).getCustomer().getKycstatus()).getDescription());
                    wsmodel.setRespcode(ISOResponseCodes.MIGRATED_REKYC_REQ);
                    return false;
                } else if (customer != null && customer.getHaswallet() != null && !customer.getHaswallet() && customer.getIsmigratedactive() != null && customer.getIsmigratedactive()) {
                    logger.info("Active Session found for migrated customer with wallets, replying...");
                    wsmodel.setTotalcount((sessionlist.get(0).getCustomer().getUnreadnotifcount() != null) ? sessionlist.get(0).getCustomer().getUnreadnotifcount() + "" : "0");
                    wsmodel.setToken(sessionlist.get(0).getToken());
                    wsmodel.setLanguage(sessionlist.get(0).getCustomer().getLanguage());
                    wsmodel.setNotiflanguage(Util.hasText(sessionlist.get(0).getCustomer().getNotiflanguage()) ? sessionlist.get(0).getCustomer().getNotiflanguage() : Util.getDefaultMobileAppLanguage());
                    wsmodel.setPermissions(sessionlist.get(0).getCustomer().getPermissions());
                    wsmodel.setKycstatus(GlobalContext.getInstance().getKYCStatusDetails(sessionlist.get(0).getCustomer().getKycstatus()).getDescription());
                    wsmodel.setRespcode(ISOResponseCodes.MIGRATED_SIGNUP_REQUIRED);
                    return false;
                } else {
                    logger.error("Customer [" + wsmodel.getMobilenumber() + "] not found while validating session, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MW_CUSTOMER_NOT_FOUND);
                    return false;
                }
            }

            wsmodel.setTotalcount((sessionlist.get(0).getCustomer().getUnreadnotifcount() != null) ? sessionlist.get(0).getCustomer().getUnreadnotifcount() + "" : "0");
            wsmodel.setToken(sessionlist.get(0).getToken());
            wsmodel.setLanguage(sessionlist.get(0).getCustomer().getLanguage());
            wsmodel.setNotiflanguage(Util.hasText(sessionlist.get(0).getCustomer().getNotiflanguage()) ? sessionlist.get(0).getCustomer().getNotiflanguage() : Util.getDefaultMobileAppLanguage());
            wsmodel.setPermissions(sessionlist.get(0).getCustomer().getPermissions());
            wsmodel.setKycstatus(GlobalContext.getInstance().getKYCStatusDetails(sessionlist.get(0).getCustomer().getKycstatus()).getDescription());

            if (!Util.hasText(sessionlist.get(0).getCustomer().getTermsandcondition()) || (Util.hasText(sessionlist.get(0).getCustomer().getTermsandcondition()) && !sessionlist.get(0).getCustomer().getTermsandcondition().equals("1"))) {
                logger.error("Customer session found, but TermsConditions not accepted, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_TERMS_CONDITIONS_REQ);
                return false;
            }

            sessionlist.get(0).getCustomer().setLastrequesttime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
            GeneralDao.Instance.saveOrUpdate(sessionlist.get(0).getCustomer());

            return true;
        } else if (Util.hasText(wsmodel.getMobilenumber())) {
            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.customer.mobilenumber= :MOB " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());
            params.put("ISEXPIRED", false);
            params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

            sessionlist = GeneralDao.Instance.find(dbQuery, params);

            if (sessionlist != null && sessionlist.size() > 0) {
                logger.error("Customer already has an active session from other device, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN);
                return false;
            }

            wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED); //94 - Permission Denied
            return false;
        } else {
            logger.info("No session found for device, replying...");
            wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED); //94 - Permission Denied
            return false;
        }
    }

    public static void MarkSessionsExpire(AppWsEntity wsmodel) //For Device and For Customer both
    {
        logger.info("Marking all sessions expire...");
        String dbQuery;
        Map<String, Object> params;

        //TODO: Raza update expired logic
        //Raza expire sessions of Device
        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0) {
            for (MWDeviceSessionLog sess : sessionlist) {
                sess.setExpired(true);
                GeneralDao.Instance.saveOrUpdate(sess);
            }
        }

        //Raza expire sessions of Customer
        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.customer.mobilenumber= :MOB " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("MOB", wsmodel.getMobilenumber());
        params.put("ISEXPIRED", false);

        sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0) {
            for (MWDeviceSessionLog sess : sessionlist) {
                sess.setExpired(true);
                GeneralDao.Instance.saveOrUpdate(sess);
            }
        }
        //TODO: Raza update expired logic

        logger.info("Marked all sessions expired");

    }


    //TODO: 04-09-2025 Raza need to improve Secret Questions Logic as it should not be dependent on Text of Secret Question.. This will open a change on Mobile App & Front End interfaces...
    public static boolean AddSecurityQuestions(AppWsEntity wsmodel, MWCustomer customer) {
        try {
            logger.info("Adding Security Questions...");

            String dbQuery;
            Map<String, Object> params;
            dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
            params = new HashMap<String, Object>();
            params.put("QUEST", wsmodel.getSecretquestion1());

            MWSecurQuestions dbquestion = (MWSecurQuestions) GeneralDao.Instance.findObject(dbQuery, params);
            MWSecurQuestions question;

            if (dbquestion != null) {
                question = dbquestion;
            } else {
                dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.frquestion= :QUEST  ";
                dbquestion = (MWSecurQuestions) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbquestion != null) {
                    question = dbquestion;
                } else {
                    dbQuery = "from " + MWSecurQuestionsTranslation.class.getName() + " c where c.question= :QUEST  ";
                    MWSecurQuestionsTranslation xlatedbquestion = (MWSecurQuestionsTranslation) GeneralDao.Instance.findObject(dbQuery, params);

                    if(xlatedbquestion != null){
                        question = xlatedbquestion.getSecurquestion();
                    }
                    else{
                        logger.error("Question1 not found in DB to Update, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        return false;
                    }
                }
            }

            dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUST and c.questionnumber= :QUEST ";
            params = new HashMap<String, Object>();
            params.put("CUST", customer);
            params.put("QUEST", 1);

            MWCustSecurQuestions customerquestion = (MWCustSecurQuestions) GeneralDao.Instance.findObject(dbQuery, params);

            if (customerquestion == null) {
                customerquestion = new MWCustSecurQuestions(1, question, wsmodel.getSecretquestionanswer1().trim(), customer);
            } else {
                customerquestion.setQuestion(question);
                customerquestion.setAnswer(wsmodel.getSecretquestionanswer1().trim());
            }

            //GeneralDao.Instance.saveOrUpdate(question);
            GeneralDao.Instance.saveOrUpdate(customerquestion);

            if (Util.hasText(wsmodel.getSecretquestion2())) {
                dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.question= :QUEST  ";
                params = new HashMap<String, Object>();
                params.put("QUEST", wsmodel.getSecretquestion2());

                MWSecurQuestions dbquestion2 = (MWSecurQuestions) GeneralDao.Instance.findObject(dbQuery, params);

                MWSecurQuestions question2 = null;
                if (dbquestion2 != null) {
                    question2 = dbquestion2;
                } else {
                    dbQuery = "from " + MWSecurQuestions.class.getName() + " c where c.frquestion= :QUEST  ";
                    dbquestion2 = (MWSecurQuestions) GeneralDao.Instance.findObject(dbQuery, params);

                    if (dbquestion2 != null) {
                        question2 = dbquestion2;
                    } else {

                        dbQuery = "from " + MWSecurQuestionsTranslation.class.getName() + " c where c.question= :QUEST  ";
                        MWSecurQuestionsTranslation xlatedbquestion = (MWSecurQuestionsTranslation) GeneralDao.Instance.findObject(dbQuery, params);

                        if(xlatedbquestion != null){
                            question2 = xlatedbquestion.getSecurquestion();
                        }
                        else{
                            logger.error("Question2 not found in DB to Update, asuming migrated...");
                            //wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                            //return false;
                        }
                    }
                }

                if (question2 != null) {
                    dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUST and c.questionnumber= :QUEST ";
                    params = new HashMap<String, Object>();
                    params.put("CUST", customer);
                    params.put("QUEST", 2);

                    MWCustSecurQuestions customerquestion2 = (MWCustSecurQuestions) GeneralDao.Instance.findObject(dbQuery, params);

                    if (customerquestion2 == null) {
                        customerquestion2 = new MWCustSecurQuestions(2, question2, wsmodel.getSecretquestionanswer2().trim(), customer);
                    } else {
                        customerquestion2.setQuestion(question2);
                        customerquestion2.setAnswer(wsmodel.getSecretquestionanswer2().trim());
                    }

                    //GeneralDao.Instance.saveOrUpdate(question2);
                    GeneralDao.Instance.saveOrUpdate(customerquestion2);
                }
            }


            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while adding security questions!");
            return false;
        }
    }


    public static boolean GetAndSendSMSviaHibernateID(AppWsEntity wsmodel) {
        return SMSGatewayHandler.GetAndSendSMSviaHibernateID(wsmodel);
    }

    public static boolean processGetAndSendNotificationviaHibernateID(AppWsEntity wsmodel){
        return NotificationHandler.GetAndSendNotificationviaHibernateID(wsmodel);
    }

    public static boolean processGetAndSendOTPviaHibernateID(AppWsEntity wsmodel) {
        return SMSGatewayHandler.GetAndSendOTPviaHibernateID(wsmodel);
    }

    public static boolean processGetAndSendEmailviaHibernateID(AppWsEntity wsmodel){
        return EmailGatewayHandler.GetAndSendEmailviaHibernateID(wsmodel);
    }

    public static boolean processGetAndSendOTPEmailviaHibernateID(AppWsEntity wsmodel){
        return EmailGatewayHandler.GetAndSendOTPEmailviaHibernateID(wsmodel);
    }

    public static void APICheckandSendSMS(AppWsEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.

        logger.info("Checking and Sending SMS for ServiceName [" + wsmodel.getServicename() + "]");
        if (Util.hasText(wsmodel.getServicename()) && !wsmodel.getServicename().equals("AgentPOSCreateOTP")) {
            //logger.info("Sending OTP for AgentPOSCreateOTP...");
            if (Util.hasText(wsmodel.getMobilenumber())) {
                try {
                    if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                        if (!SMSGatewayHandler.SendSMS(wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        }
                    } else {
                        try {
                            SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                            if (trancode != null && trancode.getOtprequired()) {
                                if (trancode.getSmsconfig() != null) {
                                    if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getMobilenumber(), wsmodel)) {
                                        logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    }
                                }
                            } else if (trancode != null && trancode.getSmsenabled() != null && trancode.getSmsenabled()) {
                                if (trancode.getSmsconfig() != null) {
                                    if (!SMSGatewayHandler.CreateandSendSMS(trancode.getSmsconfig(), wsmodel.getMobilenumber(), wsmodel, false)) {
                                        logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Failed to Sent SMS, replying as per transaction response...");
                            //e.printStackTrace();
                            logger.error(WebServiceUtil.getStrException(e));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to Sent SMS, replying as per transaction response...");
                    wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                    //e.printStackTrace();
                    logger.error(WebServiceUtil.getStrException(e));
                }
            }


            if (Util.hasText(wsmodel.getDestmobilenumber())) {
                String mobile = wsmodel.getMobilenumber();
                wsmodel.setMobilenumber(wsmodel.getDestmobilenumber());
                try {
                    if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                        if (!SMSGatewayHandler.SendSMS(wsmodel)) {
                            logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        }
                        wsmodel.setMobilenumber(mobile);
                    } else {
                        try {
                            SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                            if (trancode != null && trancode.getOtprequired()) {
                                if (trancode.getSmsconfig() != null) {
                                    if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getDestmobilenumber(), wsmodel)) {
                                        logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    }
                                }
                            } else if (trancode != null && trancode.getSmsenabled() != null && trancode.getSmsenabled()) {
                                if (trancode.getSmsconfig() != null) {
                                    if (!SMSGatewayHandler.CreateandSendSMS(trancode.getSmsconfig(), wsmodel.getDestmobilenumber(), wsmodel, true)) {
                                        logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    }
                                }
                            }
                            wsmodel.setMobilenumber(mobile);
                        } catch (Exception e) {
                            logger.error("Failed to Sent SMS, replying as per transaction response...");
                            //e.printStackTrace();
                            logger.error(WebServiceUtil.getStrException(e));
                            wsmodel.setMobilenumber(mobile);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to Sent SMS, replying as per transaction response...");
                    wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                    //e.printStackTrace();
                    logger.error(WebServiceUtil.getStrException(e));
                    wsmodel.setMobilenumber(mobile);
                }
            }
        } else {
            try {
                if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                    if (!SMSGatewayHandler.SendSMS(wsmodel)) {
                        logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    }
                } else {
                    try {

                        if (Util.hasText(wsmodel.getServicename()) && wsmodel.getServicename().equals("AgentPOSCreateOTP")) {
                            String dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("MOB", wsmodel.getDestmobilenumber());

                            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                            if (customer != null) {
                                wsmodel.setNotiflanguage(customer.getNotiflanguage());
                                wsmodel.setLanguage(customer.getLanguage());
                                wsmodel.setFirstname(customer.getFirstname());
                                wsmodel.setMiddlename(customer.getMiddlename());
                                wsmodel.setLastname(customer.getLastname());
                                wsmodel.setUsername(customer.getUsername());
                            }
                        }

                        SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                        if (trancode != null && trancode.getOtprequired()) {
                            if (trancode.getSmsconfig() != null) {
                                if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getDestmobilenumber(), wsmodel)) {
                                    logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                }
                            }
                        }
                        else if (trancode != null && trancode.getSmsenabled() != null && trancode.getSmsenabled()) {
                            if (trancode.getSmsconfig() != null) {
                                if (!SMSGatewayHandler.CreateandSendSMS(trancode.getSmsconfig(), wsmodel.getMobilenumber(), wsmodel, false)) {
                                    logger.error("Failed to create & Send SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to Sent SMS, replying as per transaction response...");
                        //e.printStackTrace();
                        logger.error(WebServiceUtil.getStrException(e));
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to Sent SMS, replying as per transaction response...");
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                e.printStackTrace();
                logger.error(WebServiceUtil.getStrException(e));
            }
        }
    }

    public static void APICheckandSendNotification(AppWsEntity wsmodel) {
        //Raza check if Notification is required or not
        //logger.info("Checking if Notification required...");
        try {


            if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                if (!NotificationHandler.SendNotification(wsmodel)) {
                    logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                }
            } else {
                try {
                    SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                    if (trancode != null && trancode.getNotificationenabled() != null && trancode.getNotificationenabled()) {
                        //logger.info("Notification required...");
                        if (trancode.getNotificationconfig() != null) {
                            //logger.info("Notification Config found...");
                            if (!NotificationHandler.CreateandSendNotification(trancode.getNotificationconfig(), wsmodel.getMobilenumber(), wsmodel, false)) {
                                logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to Sent Notification, replying as per transaction response...");
                    //e.printStackTrace();
                    logger.error(WebServiceUtil.getStrException(e));
                }

                try {
                    if (Util.hasText(wsmodel.getDestmobilenumber())) {
                        SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                        if (trancode != null && trancode.getNotificationenabled() != null && trancode.getNotificationenabled()) {
                            if (trancode.getNotificationconfig() != null) {
                                if (!NotificationHandler.CreateandSendNotification(trancode.getNotificationconfig(), wsmodel.getDestmobilenumber(), wsmodel, true)) {
                                    logger.error("Failed to create & Send Notification for Mobile [" + wsmodel.getDestmobilenumber() + "], rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to Sent Notification to DestMobile, replying as per transaction response...");
                    //e.printStackTrace();
                    logger.error(WebServiceUtil.getStrException(e));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent Notification to DestMobile, replying as per transaction response...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }
    }

    public static void APICheckandSendEmail(AppWsEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.
        logger.info("Checking and Sending Email for ServiceName [" + wsmodel.getServicename() + "]");
        if (Util.hasText(wsmodel.getServicename()) && !wsmodel.getServicename().equals("AgentPOSCreateOTP")) {
            logger.info("Sending OTP for AgentPOSCreateOTP...");
            if (Util.hasText(wsmodel.getDestemailaddress())) {
                String email = wsmodel.getEmailaddress();
                wsmodel.setEmailaddress(wsmodel.getDestemailaddress());
                try {
                    if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                        if (!EmailGatewayHandler.SendEmail(wsmodel)) {
                            logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                        }
                        wsmodel.setMobilenumber(email);
                    } else {
                        try {
                            SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                            if (trancode != null && trancode.getOtprequired()) {
                                if (trancode.getEmailconfig() != null) {
                                    if (!EmailGatewayHandler.CreateandSendEmailOTP(trancode.getEmailconfig(), wsmodel.getDestemailaddress(), wsmodel)) {
                                        logger.error("Failed to create & Send Email for Address [" + wsmodel.getDestemailaddress() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    }
                                }
                            } else if (trancode != null && trancode.getEmailenabled() != null && trancode.getEmailenabled()) {
                                if (trancode.getEmailconfig() != null) {
                                    if (!EmailGatewayHandler.CreateandSendEmail(trancode.getEmailconfig(), wsmodel.getDestemailaddress(), wsmodel, true)) {
                                        logger.error("Failed to create & Send Email for Address [" + wsmodel.getDestemailaddress() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                    }
                                }
                            }
                            wsmodel.setMobilenumber(email);
                        } catch (Exception e) {
                            logger.error("Failed to Sent Email, replying as per transaction response...");
                            e.printStackTrace();
                            logger.error(WebServiceUtil.getStrException(e));
                            wsmodel.setEmailaddress(email);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to Sent Email, replying as per transaction response...");
                    wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                    e.printStackTrace();
                    logger.error(WebServiceUtil.getStrException(e));
                    wsmodel.setMobilenumber(email);
                }
            }
        } else {
            try {
                if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                    if (!EmailGatewayHandler.SendEmail(wsmodel)) {
                        logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                    }
                } else {
                    try {
                        SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getServicename());
                        if (trancode != null && trancode.getOtprequired()) {
                            if (trancode.getEmailconfig() != null) {
                                if (!EmailGatewayHandler.CreateandSendEmailOTP(trancode.getEmailconfig(), wsmodel.getDestemailaddress(), wsmodel)) {
                                    logger.error("Failed to create & Send Email for Address [" + wsmodel.getDestemailaddress() + "], rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                }
                            }
                        } else if (trancode != null && trancode.getEmailenabled() != null && trancode.getEmailenabled()) {
                            if (trancode.getEmailconfig() != null) {
                                if (!EmailGatewayHandler.CreateandSendEmail(trancode.getEmailconfig(), wsmodel.getEmailaddress(), wsmodel, false)) {
                                    logger.error("Failed to create & Send Email for Address [" + wsmodel.getEmailaddress() + "], rejecting...");
                                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to Sent Email, replying as per transaction response...");
                        e.printStackTrace();
                        logger.error(WebServiceUtil.getStrException(e));
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to Sent Email, replying as per transaction response...");
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                e.printStackTrace();
                logger.error(WebServiceUtil.getStrException(e));
            }
        }
    }

    public static void APICheckandSendOTPSMS(WebServiceEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.

        try {
            if(Util.isMultiLangEnabled() && Util.isMultiLangSMSEnabled()){
                if(wsmodel instanceof AppNotifWsEntity &&
                        (!Util.hasText(wsmodel.getTitle())
                                || !Util.hasText(wsmodel.getFrtitle())
                                || !Util.hasText(wsmodel.getBody())
                                || !Util.hasText(wsmodel.getFrbody()))){
                    if(((AppNotifWsEntity) wsmodel).getXlatenotifiobjlist() != null && ((AppNotifWsEntity) wsmodel).getXlatenotifiobjlist().size() > 0){
                        for(XlateNotifiObj o : ((AppNotifWsEntity) wsmodel).getXlatenotifiobjlist()){
                            if(Util.hasText(o.getLangcode()) && o.getLangcode().toUpperCase().equals("ENG")){
                                wsmodel.setTitle(o.getTitle());
                                wsmodel.setBody(o.getBody());
                            }
                            else if(Util.hasText(o.getLangcode()) && o.getLangcode().toUpperCase().equals("FRA")){
                                wsmodel.setTitle(o.getTitle());
                                wsmodel.setBody(o.getBody());
                            }
                        }
                    }
                }
            }

            if (Util.hasText(wsmodel.getTitle()) || Util.hasText(wsmodel.getBody())) {
                if (!SMSGatewayHandler.SendOTPSMS(wsmodel)) {
                    logger.error("Failed to create & Send OTP SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                }
            } else {
                try {
                    SwitchTransactionCodes trancode = GlobalContext.getInstance().getTransactionCodeDescbyAPIforSMSNotif(wsmodel.getOriginalapi());
                    if (trancode != null && trancode.getOtprequired()) {
                        if (trancode.getSmsconfig() != null) {
                            if (!SMSGatewayHandler.CreateandSendOTP(trancode.getSmsconfig(), wsmodel.getMobilenumber(), wsmodel)) {
                                logger.error("Failed to create & Send OTP SMS for Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                wsmodel.setRespcode(ISOResponseCodes.SMS_GATEWAY_DOWN); //TODO: Raza Update This
                            }
                        }
                    } else if (trancode != null && trancode.getSmsenabled() != null && trancode.getSmsenabled()) {
                        logger.error("OTP required disabled for tranaction [" + wsmodel.getOriginalapi() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.BAD_TRANSACTION_TYPE);
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Failed to Sent SMS, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                    //e.printStackTrace();
                    logger.error(WebServiceUtil.getStrException(e));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Sent OTP SMS, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
        }
    }

    public static Boolean GetRespListV2(AppWsEntity wsmodel) {
        try {
            String dbQuery;
            Map<String, Object> params;
            Boolean retval = false;

            if(Util.isMultiLangEnabled()){

                if(Util.isMultiLangRespCodeEnabled()){

                    if(Util.hasText(wsmodel.getMobilenumber())) { //Raza enabling conditional session Validation 01-09-2025
                        logger.info("Validating Session...");
                        if (!ValidateUserandAppSession(wsmodel)) {
                            logger.error("Failed to Validate Session, ignoring...");
                        }
                    }

                    dbQuery = "select distinct r from SwitchRespCodes r " +
                            "left join fetch r.translations t " +
                            "where r.apprequired = :REQ";
                    params = new HashMap<String, Object>();
                    params.put("REQ", true);

                    // Execute query with JOIN FETCH
                    List<SwitchRespCodes> dbcodes = GeneralDao.Instance.find(dbQuery, params);

                    if (dbcodes != null && !dbcodes.isEmpty()) {
                        List<RespList> respList = new ArrayList<>();
                        for (SwitchRespCodes r : dbcodes) {

                            // Default values from parent
                            String description = r.getDisplayname();
                            String emgmessage = r.getDescription();
                            String frmessage = r.getFradescription();
                            String engtitle = r.getTitle();
                            String frtitle = r.getFratitle();

                            // Override if translation exists for requested language
                            if (Util.hasText(wsmodel.getLanguage()) && r.getTranslations() != null) {
                                Optional<SwitchRespCodesTranslation> match = r.getTranslations()
                                        .stream()
                                        .filter(t -> t.getLangcode().equalsIgnoreCase(Util.hasText(wsmodel.getLanguage()) ? wsmodel.getLanguage() : Util.getDefaultMobileAppLanguage() ))
                                        .findFirst();
                                if (match.isPresent() && Util.hasText(match.get().getDescription())) {
                                    description = match.get().getDisplayname();
                                    emgmessage = match.get().getDescription();
                                    frmessage = match.get().getDescription();
                                    engtitle = match.get().getTitle();
                                    frtitle = match.get().getTitle();
                                }
                            }

                            RespList obj = new RespList(r.getCode(), description, emgmessage, frmessage, engtitle);
                            respList.add(obj);
                        }

                        wsmodel.setResplist(respList);

                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        retval = true;
                    } else {
                        logger.error("No IMT Reason List found in DB, falling back to old implementation...");
                        retval = GetRespListV1(wsmodel);
                    }
                }
                else{
                    logger.info("MultiLanguage ResponseCodes feature disabled, processing through old implementation...");
                    retval = GetRespListV1(wsmodel);
                }
            }
            else{
                logger.info("MultiLanguage feature disabled, processing through old implementation...");
                retval = GetRespListV1(wsmodel);
            }
            return retval;
        } catch (Exception e) {
            logger.error("Exception caught while getting RespList from DB, falling back to old implementation...");
            logger.error(WebServiceUtil.getStrException(e));
            return GetRespListV1(wsmodel);
        }
    }

    public static Boolean GetRespListV1(AppWsEntity wsmodel) {
        try {
            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + SwitchRespCodes.class.getName() + " c where c.apprequired= :REQ  ";
            params = new HashMap<String, Object>();
            params.put("REQ", true);

            List<SwitchRespCodes> dbcodes = GeneralDao.Instance.find(dbQuery, params);

            if (dbcodes != null && dbcodes.size() > 0) {
                List<RespList> respList = new ArrayList<>();
                for (SwitchRespCodes dbcode : dbcodes) {

                    String title = "";
                    if (Util.hasText(wsmodel.getLanguage())) {
                        title = (wsmodel.getLanguage().equals("ENG") ? dbcode.getTitle() : dbcode.getFratitle()); // title is set based on language
                    } else {
                        title = dbcode.getFratitle(); // Default language is french
                    }
                    RespList respcode = new RespList(dbcode.getCode(), dbcode.getDisplayname(), dbcode.getDescription(), dbcode.getFradescription(), title);
                    respList.add(respcode);
                }

                wsmodel.setResplist(respList);
                return true;
            } else {
                logger.error("No RespCode foubd in DB with APP_REQ as true for app, ignoring...");
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception caught while getting RespList from DB");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
    }

    public static AppWsEntity SendSecretQuestions(AppWsEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.

        try {

            if (Util.hasText(wsmodel.getMobilenumber())) {
                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
                params = new HashMap<String, Object>();
                params.put("MOB", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (customer != null) {
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    wsmodel.setLanguage(customer.getLanguage());
                    wsmodel.setFirstname(customer.getFirstname());
                    dbQuery = "from " + MWCustSecurQuestions.class.getName() + " c where c.customer= :CUST  ";
                    params = new HashMap<String, Object>();
                    params.put("CUST", customer);

                    List<MWCustSecurQuestions> questions = GeneralDao.Instance.find(dbQuery, params);

                    if (questions != null && questions.size() > 0) {

                        for (MWCustSecurQuestions quest : questions) {
                            if (Util.hasText(customer.getLanguage()) && customer.getLanguage().equals("FRA")) {
                                if (quest.getQuestionnumber() != null && quest.getQuestionnumber().equals(1)) {
                                    wsmodel.setSecretquestion1(quest.getQuestion().getFrquestion());
                                    wsmodel.setSecretquestionanswer1(Util.normalizeStringExcludingWhiteSpaces(quest.getAnswer().trim()).toUpperCase());
                                }

                                if (quest.getQuestionnumber() != null && quest.getQuestionnumber().equals(2)) {
                                    wsmodel.setSecretquestion2(quest.getQuestion().getFrquestion());
                                    wsmodel.setSecretquestionanswer2(Util.normalizeStringExcludingWhiteSpaces(quest.getAnswer().trim()).toUpperCase());
                                }
                            } else {
                                if (quest.getQuestionnumber() != null && quest.getQuestionnumber().equals(1)) {
                                    wsmodel.setSecretquestion1(quest.getQuestion().getQuestion());
                                    wsmodel.setSecretquestionanswer1(Util.normalizeStringExcludingWhiteSpaces(quest.getAnswer().trim()).toUpperCase());
                                }

                                if (quest.getQuestionnumber() != null && quest.getQuestionnumber().equals(2)) {
                                    wsmodel.setSecretquestion2(quest.getQuestion().getQuestion());
                                    wsmodel.setSecretquestionanswer2(Util.normalizeStringExcludingWhiteSpaces(quest.getAnswer().trim()).toUpperCase());
                                }
                            }
                        }


                        try {
                            try {
                                if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                        && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                    if (SMSGatewayHandler.CreateandSendSecretQuestOneSMS(wsmodel)) {
                                        logger.info("SecretQuestions1 SMS for Mobilenumber [" + wsmodel.getMobilenumber() + "] sent successfully!");

                                        if (questions.size() == 2) {
                                            if (SMSGatewayHandler.CreateandSendSecretQuestTwoSMS(wsmodel)) {
                                                logger.info("SecretQuestions2 SMS for Mobilenumber [" + wsmodel.getMobilenumber() + "] sent successfully!");
                                                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                            } else {
                                                logger.error("Failed to send SMS SecretQuestion2 for Customer Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                                wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                                            }
                                        } else {
                                            logger.info("Only Sending one message for Migrated Customer for Mobilenumber [" + wsmodel.getMobilenumber() + "] sent successfully!");
                                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                        }
                                    } else {
                                        logger.error("Failed to send SMS SecretQuestion1 for Customer Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Exception caght while sending SMS, ignoring...");
                                logger.error(WebServiceUtil.getStrException(e));
                                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                            }

                            try {
                                if (Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                        && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL))) {

                                    if(EmailGatewayHandler.CreateandSendSecretQuestEmail(wsmodel))
                                    {
                                        logger.info("SecretQuestions SMS for Mobilenumber [" + wsmodel.getMobilenumber() + "] sent successfully!");
                                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                                    }
                                    else
                                    {
                                        logger.error("Failed to send SMS SecretQuestion for Customer Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                                    }
                                }
                            }
                            catch (Exception e){
                                logger.error("Exception caght while sending email, ignoring...");
                                logger.error(WebServiceUtil.getStrException(e));
                                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                            }
                        }
                        catch (Exception e){
                            logger.error("Exception cauhgt while sending Secret Questions, rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                        }
                    } else {
                        logger.error("No secret question found for CustomerID [" + customer.getId() + "] Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                    }
                } else {
                    logger.error("Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                }
            } else {
                logger.error("Mobile Number not present in Request to Send Secret Questions, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            }
        } catch (Exception e) {
            logger.error("Failed to Sent SMS, replying as per transaction response...");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            logger.error(WebServiceUtil.getStrException(e));
        }
        return wsmodel;
    }

    public static AppWsEntity SendCustomerPIN(AppWsEntity wsmodel) {
        //Raza check if SMS is required or not, For calls where SMS require additional work on middleware, api should implement sms flow on its own.

        try {

            if (Util.hasText(wsmodel.getMobilenumber())) {
                String dbQuery;
                Map<String, Object> params;

                dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB  ";
                params = new HashMap<String, Object>();
                params.put("MOB", wsmodel.getMobilenumber());

                MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

                if (customer != null) {
                    wsmodel.setFirstname(customer.getFirstname());
                    wsmodel.setLastname(customer.getLastname());
                    wsmodel.setUsername(customer.getUsername());
                    wsmodel.setNotiflanguage(customer.getNotiflanguage());
                    wsmodel.setLanguage(customer.getLanguage());
                    if (customer.getIsmigrated() != null && customer.getIsmigrated()) {
                        logger.info("Migrated Customer found to Send PIN over SMS...");
                        wsmodel.setPindata(customer.getMigratedpin());
                    } else if (Util.hasText(wsmodel.getPindata())) {
                        logger.info("Customer PIN in API request found to Send PIN over SMS...");
                    } else {
                        logger.error("Customer not migrated and PinData not found, cannot send PIN over SMS, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        return wsmodel;
                    }


                    if (SMSGatewayHandler.CreateandSendPinSMS(wsmodel)) {
                        logger.info("PIN on SMS for Mobilenumber [" + wsmodel.getMobilenumber() + "] sent successfully!");
                        wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                        return wsmodel;
                    } else {
                        logger.error("Failed to send SMS SecretQuestion for Customer Mobile [" + wsmodel.getMobilenumber() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        return wsmodel;
                    }
                } else {
                    logger.error("Migrated or No Customer not found against MobileNumber [" + wsmodel.getMobilenumber() + "], cannot send PIN! rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                    return wsmodel;
                }
            } else {
                logger.error("Mobile Number not present in Request to Send Secret Questions, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }
        } catch (Exception e) {
            logger.error("Failed to Sent SMS, replying as per transaction response...");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            return wsmodel;
        }
    }

    public static Boolean GenerateAppSession(AppWsEntity wsmodel, MWCustomer customer) {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Customer found, getting session...");
        dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.deviceid= :DEVC " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
        params.put("ISEXPIRED", false);
        params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

        List<MWDeviceSessionLog> sessionlist = GeneralDao.Instance.find(dbQuery, params);

        if (sessionlist != null && sessionlist.size() > 0) {
            logger.error("User already have a active session token, cannot allow ReLogin, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.MW_USER_ALREADY_LOGGEDIN); //94 - Permission Denied
            return false;
        } else {
            dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.customer.mobilenumber= :MOB " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
            params = new HashMap<String, Object>();
            params.put("MOB", wsmodel.getMobilenumber());
            params.put("ISEXPIRED", false);
            params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

            sessionlist = GeneralDao.Instance.find(dbQuery, params);

            if (sessionlist != null && sessionlist.size() > 0) {
                logger.error("Customer already LoggedIn from another device, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION); //94 - Permission Denied
                return false;
            } else {
                String firebasetoken = wsmodel.getSecurityparams().getFirebasetoken();

                if (!Util.hasText(firebasetoken)) {
                    firebasetoken = customer.getFirebasetoken();
                }


                if (Util.hasText(firebasetoken)) {
                    dbQuery = "from " + MWDeviceSessionLog.class.getName() + " c where c.device.firebasetoken= :FIRE " + " and c.expiredatetime > :EXPIRY " + " and c.isExpired = :ISEXPIRED " + " order by expiredatetime desc ";
                    params = new HashMap<String, Object>();
                    params.put("FIRE", firebasetoken);
                    params.put("ISEXPIRED", false);
                    params.put("EXPIRY", Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));

                    sessionlist = GeneralDao.Instance.find(dbQuery, params);

                    if (sessionlist != null && sessionlist.size() > 0) {
                        logger.error("Device already LoggedIn w.r.t. Firebase token, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_INVALID_MOBILE_SESSION); //94 - Permission Denied
                        return false;
                    }
                }
            }
        }

        MarkSessionsExpire(wsmodel);

        String firebasetoken = wsmodel.getSecurityparams().getFirebasetoken();

        if (!Util.hasText(firebasetoken)) {
            firebasetoken = customer.getFirebasetoken();
        }

        if (Util.hasText(firebasetoken)) {
            //TODO: Raza update FireBase Token Update Logic ; Old customer that was using that device
            dbQuery = "from " + MWCustomer.class.getName() + " c where c.firebasetoken= :FIRE ";
            params = new HashMap<String, Object>();
            params.put("FIRE", firebasetoken);
            List<MWCustomer> firebasecustomers = GeneralDao.Instance.find(dbQuery, params);
            if (firebasecustomers != null && firebasecustomers.size() > 0) {
                for (MWCustomer mwcust : firebasecustomers) {
                    if (!mwcust.getMobilenumber().equals(customer.getMobilenumber())) {
                        mwcust.setFirebasetoken(null);
                        GeneralDao.Instance.saveOrUpdate(mwcust);
                    }
                }
            }

            dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.firebasetoken= :FIRE ";
            params = new HashMap<String, Object>();
            params.put("FIRE", firebasetoken);
            List<MWDeviceLog> firebasedevices = GeneralDao.Instance.find(dbQuery, params);
            if (firebasedevices != null && firebasedevices.size() > 0) {
                for (MWDeviceLog dev : firebasedevices) {
                    if (!dev.getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid())) {
                        dev.setFirebasetoken(null);
                        GeneralDao.Instance.saveOrUpdate(dev);
                    }
                }
            }
            //TODO: Raza update FireBase Token Update Logic ; Old customer that was using that device
        }


        customer.setFirebasetoken(firebasetoken);
        GeneralDao.Instance.saveOrUpdate(customer);

        logger.info("Updating customer for device after otp confirmed");

        dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.deviceid= :DEVC ";
        params = new HashMap<String, Object>();
        params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

        MWDeviceLog device = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

        if (device == null) {
            device = new MWDeviceLog();
            device.setDeviceid(wsmodel.getSecurityparams().getDeviceid());
            device.setFirebasetoken(wsmodel.getSecurityparams().getFirebasetoken());
            device.setDevicemodel(wsmodel.getSecurityparams().getDevicemodel());
            device.setOperatingsystem(wsmodel.getSecurityparams().getOperatingsystem());
            device.setScreenresolution(wsmodel.getSecurityparams().getScreenresolution());
            device.setBaseintegrity(false);
            device.setCtsprofile(false);
            device.setIsrooted(false);
        }

        dbQuery = "from " + MWDeviceLog.class.getName() + " c where c.customer.mobilenumber = :MOB ";
        params = new HashMap<String, Object>();
        params.put("MOB", wsmodel.getMobilenumber());

        MWDeviceLog olddevice = (MWDeviceLog) GeneralDao.Instance.findObject(dbQuery, params);

        if (olddevice != null) {
            olddevice.setCustomer(null);
            GeneralDao.Instance.saveOrUpdate(olddevice);
        }

        device.setCustomer(customer);
        GeneralDao.Instance.saveOrUpdate(device);

        MWDeviceSessionLog session = new MWDeviceSessionLog();
        session.setDevice(device);
        session.setCustomer(customer);
        session.setCreatedatetime(Long.parseLong(WebServiceUtil.dateFormat.format(new Date())));
        session.setExpiredatetime(Long.parseLong(WebServiceUtil.dateFormat.format(DateUtils.addMinutes(new Date(), Integer.parseInt(GetSessionExipreTime())))));
        session.setExpired(false);
        String tvalue = "";
        for (int i = 0; i < 12; i++) {
            Random rnd = new Random();
            int a = rnd.nextInt(10);
            tvalue += a;
        }
        session.setToken(tvalue);
        GeneralDao.Instance.saveOrUpdate(session);
        wsmodel.setToken(tvalue);

        if (wsmodel.getSecurityparams() != null) {
            customer.setLastloginlatitude(wsmodel.getSecurityparams().getGpslatitude());
            customer.setLastloginlongitude(wsmodel.getSecurityparams().getGpslongitude());
            GeneralDao.Instance.saveOrUpdate(customer);
        }

        logger.info("login");
        return true;
    }

    public static Boolean VerifyLoginPrerequisites(AppWsEntity wsmodel, MWCustomer customer)
    {
        String dbQuery;
        Map<String, Object> params;

        if (wsmodel.getSecurityparams() == null) {
            logger.error("Security param object not found in request, rejecting...");
            return false;
        }


        //Customer Config Validation start
        try {
            dbQuery = "from " + MWCustConfig.class.getName() + " c where (c.customertype= :CUSTYPE and c.category= :CAT) ";

            params = new HashMap<String, Object>();
            params.put("CUSTYPE", CustomerType.CUSTOMER);
            params.put("CAT", MWCustConfigCategory.MAX_LOGIN_ATTEMPTS);

            if(Util.hasText(wsmodel.getMobilenumber())){
                dbQuery += " or (c.mobilenumber= :MOB) ";
                params.put("MOB", wsmodel.getMobilenumber());
            }
            if(customer != null && Util.hasText(customer.getCustomerId())){
                dbQuery += " or (c.customerid= :CUST) ";
                params.put("CUST", customer.getCustomerId());
            }
            if(Util.hasText(wsmodel.getSecurityparams().getDeviceid())){
                dbQuery += " or (c.deviceid= :DEVC) ";
                params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());
            }

            List<MWCustConfig> dbconfiglist = GeneralDao.Instance.find(dbQuery, params);

            if (dbconfiglist != null && dbconfiglist.size() > 0) {

                logger.info("Validation records [" + dbconfiglist.size() + "] found, validating...");

                for(MWCustConfig o : dbconfiglist){
                        try {
                            if (o.getCategory().equals(MWCustConfigCategory.DEVICE_RESTRICTION)){ //1-Device Restriction

                                if (Util.hasText(o.getMobilenumber()) && Util.hasText(o.getDeviceid()) && Util.hasText(wsmodel.getSecurityparams().getDeviceid())) {

                                    if (wsmodel.getSecurityparams().getDeviceid().equals(o.getDeviceid())
                                            &&
                                            !o.getMobilenumber().equals(customer.getMobilenumber())) { //1.1 checking device-customer restriction start
                                        logger.error("MobileNumber [" + wsmodel.getMobilenumber() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                                        return false;
                                    }

                                    if(wsmodel.getSecurityparams().getDeviceid().equals(o.getDeviceid())
                                        &&
                                            customer != null && Util.hasText(customer.getCustomerId()) && Util.hasText(o.getCustomerid())
                                            && !o.getCustomerid().equals(customer.getCustomerId())){ //1.2 checking device-customer restriction start
                                            logger.error("Customer ID [" + customer.getCustomerId() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                                            wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                                            return false;
                                    }

                                    if(Util.hasText(o.getMobilenumber()) && Util.hasText(o.getDeviceid())
                                            && o.getMobilenumber().equals(wsmodel.getMobilenumber())
                                    && !o.getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid())){  //1.3 checking customer-device restriction start
                                        logger.error("MobileNumber [" + wsmodel.getMobilenumber() + "] or Customer ID [" + customer.getCustomerId() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                                        return false;
                                    }

                                    if(Util.hasText(o.getDeviceid()) &&
                                            customer != null && Util.hasText(customer.getCustomerId()) && Util.hasText(o.getCustomerid())
                                            && o.getCustomerid().equals(customer.getCustomerId())
                                            && !o.getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid())){ //1.4 checking customer-device restriction start
                                        logger.error("Customer ID [" + customer.getCustomerId() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                                        wsmodel.setRespcode(ISOResponseCodes.MW_PERMISSION_DENIED);
                                        return false;
                                    }
                                }
                            }
                            else if(o.getCategory().equals(MWCustConfigCategory.MAX_LOGIN_ATTEMPTS)){ //2nd setting Max Failed Login attempts for future start
                                if (Util.hasText(o.getFailloginattempts())) {
                                    //Raza parsing in Long to throw exception for invalid value...
                                    wsmodel.setMaxloginattempts(Long.parseLong(o.getFailloginattempts()) + "");
                                } else {
                                    logger.error("MaxLogIn Attempts Value not found in Config for Customer, setting hardcoded value 3...");
                                    wsmodel.setMaxloginattempts("3");
                                }
                            }
                            else if(o.getCategory().equals(MWCustConfigCategory.DEV_BIND_REST)){
                                /*if(Util.hasText(o.getMobilenumber()) && customer != null && o.getMobilenumber().equals(customer.getMobilenumber())){ //TODO: Verify this.. Raza using Mobile Number from customer object and not wsmodel
                                    wsmodel.setTrustedflag("WhiteList");
                                }
                                else*/ if(Util.hasText(o.getCustomerid()) && customer != null && Util.hasText(customer.getCustomerId()) && o.getCustomerid().equals(customer.getCustomerId())){
                                    wsmodel.setTrustedflag("WhiteList");
                                }
                            }
                        }
                        catch (Exception e){
                            logger.error("Exception caught while applying validation for CustConfig Record [" + o.getId() + "], ignoring...");
                            logger.error(WebServiceUtil.getStrException(e));
                            if(!Util.hasText(wsmodel.getMaxloginattempts())){
                                wsmodel.setMaxloginattempts("3");
                            }
                        }

                }



            }
            else{
                logger.info("No Validation found in CustomerConfig ignoring...");
                wsmodel.setMaxloginattempts("3");
            }
        }
        catch (Exception e){
            logger.error("Exception caught while applying validation for CustomerConfig, ignoring...");
            logger.error(WebServiceUtil.getStrException(e));
            if(!Util.hasText(wsmodel.getMaxloginattempts())){
                wsmodel.setMaxloginattempts("3");
            }
        }



        //Customer Config Validation end


        /*//1st checking device-customer restriction start
        try {

            dbQuery = "from " + MWCustConfig.class.getName() + " c where c.deviceid= :DEVC ";
            params = new HashMap<String, Object>();
            params.put("DEVC", wsmodel.getSecurityparams().getDeviceid());

            MWCustConfig dbconfig = (MWCustConfig) GeneralDao.Instance.findObject(dbQuery, params);

            if (dbconfig != null) {
                if (Util.hasText(dbconfig.getMobilenumber())) {
                    if (!dbconfig.getMobilenumber().equals(customer.getMobilenumber())) {
                        logger.error("MobileNumber [" + wsmodel.getMobilenumber() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                        return false;
                    }
                }

                if (Util.hasText(dbconfig.getCustomerid())) {
                    if (!dbconfig.getCustomerid().equals(customer.getCustomerId())) {
                        logger.error("Customer ID [" + customer.getCustomerId() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught while getting prerequiste for Mobile Device, ignoring...");
            logger.error(WebServiceUtil.getStrException(e));
        }
        //1st checking device-customer restriction end*/


        /*//2nd checking customer-device restriction start
        try {

            dbQuery = "from " + MWCustConfig.class.getName() + " c where c.mobilenumber= :MOB";
            params = new HashMap<String, Object>();
            params.put("MOB", customer.getMobilenumber());

            if(customer.getCustomerId() != null && Util.hasText(customer.getCustomerId())) {
                dbQuery +=  " or  c.customerid= :CUST";
                params.put("CUST", customer.getCustomerId());
            }

            logger.info("Here is dbQuery:["+ dbQuery +"] with params["+ params +"]");

            MWCustConfig dbconfig = (MWCustConfig) GeneralDao.Instance.findObject(dbQuery, params);

                if (dbconfig != null) {
                    if (Util.hasText(dbconfig.getDeviceid())) {
                        if (!dbconfig.getDeviceid().equals(wsmodel.getSecurityparams().getDeviceid())) {
                            logger.error("MobileNumber [" + wsmodel.getMobilenumber() + "] or Customer ID [" + customer.getCustomerId() + "] is not allowed on Device [" + wsmodel.getSecurityparams().getDeviceid() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.PERMISSION_DENIED);
                            return false;
                        }
                    }

            }
        } catch (Exception e) {
            logger.error("Exception caught while getting prerequiste for Mobile Number or Customer ID, ignoring...");
            logger.error(WebServiceUtil.getStrException(e));
        }
        //2nd checking customer-device restriction end*/


        /*//3rd setting Max Failed Login attempts for future start
        try {
            dbQuery = "from " + MWCustConfig.class.getName() + " c where c.customertype= :CUST and c.category= :CAT ";
            params = new HashMap<String, Object>();
            params.put("CUST", CustomerType.CUSTOMER);
            params.put("CAT", MWCustConfigCategory.MAX_LOGIN_ATTEMPTS);

            MWCustConfig dbconfig = (MWCustConfig) GeneralDao.Instance.findObject(dbQuery, params);

            if (dbconfig != null) {
                if (Util.hasText(dbconfig.getFailloginattempts())) {
                    //Raza parsing in Long to throw exception for invalid value...
                    wsmodel.setMaxloginattempts(Long.parseLong(dbconfig.getFailloginattempts()) + "");
                } else {
                    logger.error("MaxLogIn Attempts Value not found in Config for Customer, setting hardcoded value 3...");
                    wsmodel.setMaxloginattempts("3");
                }
            } else {
                logger.error("MaxLogIn Attempts Config not configured for Customer, setting hardcoded value 3...");
                wsmodel.setMaxloginattempts("3");
            }
        } catch (Exception e) {
            logger.error("Exception caught while getting Maximum retiries for Customer, setting hardcoded value 3...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setMaxloginattempts("3");
        }
        //3rd setting Max Failed Login attempts for future end*/

        //4th ICCID Binding start
        try {
            if (customer.getIccidbindingenabled() && !customer.getIccid().equals(wsmodel.getSecurityparams().getIccid()))
            {
                logger.error("Customer ICCID enabled and mismatched, rejecting...");
                if(Util.hasText(wsmodel.getTrustedflag()) && wsmodel.getTrustedflag().equals("WhiteList")){
                    logger.info("Ignoring ICCID mismatch for WhiteListed Customer [" + wsmodel.getMobilenumber() + "]");
                }
                else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught while checking for ICCID, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
        //4th ICCID Binding end

        //5th Device Binding start
        if(!VerifyDeviceBinding(wsmodel, customer, wsmodel.getSecurityparams().getDeviceid())){
            return false;
        }
        //4th Device Binding end

        return true;
    }

    public static Boolean VerifyDeviceBinding(AppWsEntity wsmodel, MWCustomer customer,String deviceid ){
        try {
            String dbQuery;
            Map<String, Object> params;

            if(systemDeviceBindingEnabled()) {
                dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.device.deviceid= :DEVC ";
                params = new HashMap<String, Object>();
                params.put("DEVC", deviceid);

                List<MWCustDeviceBindingLog> dblist = GeneralDao.Instance.find(dbQuery, params);

                if (dblist != null && dblist.size() > 0) {
                    Boolean matched = false;
                    for (MWCustDeviceBindingLog l : dblist) {
                        if (l.getCustomer().getId() == customer.getId()) {
                            matched = true;
                            logger.info("Binding Device [" + deviceid + "] Found against this Customer [" + customer.getMobilenumber() + "], Match[" + matched + "]!!");
                            break;
                        }
                    }
                    if (!matched) {
                        if(wsmodel.getServicename().equals("CreateWallet")){
                            logger.info("Device [" + deviceid + "] not matched for Customer [" + customer.getMobilenumber() + "] on SignUp, ignoring...");
                        }
                        else if(Util.hasText(wsmodel.getTrustedflag()) && wsmodel.getTrustedflag().equals("WhiteList")){
                            logger.info("Device [" + deviceid + "] not matched for a WhiteListed Customer [" + customer.getMobilenumber() + "], ignoring...");
                        }
                        else{
                            logger.error("Device [" + deviceid + "] not matched for Customer [" + customer.getMobilenumber() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.MW_DEVC_BIND_WITH_ANOTHER_CUSTOMER); //Device Binded to other Customer
                            return false;
                        }
                    }
                }
                else {
                    logger.error("No Customer Device Binding Log found for Customer [" + customer.getMobilenumber() + "] and Device [" + deviceid + "] while binding is enabled, checking with Customer...");
                    logger.info("Trusted Flag [" + wsmodel.getTrustedflag() + "]");
                    if(customer.getDevicebindingenabled()
                            && (!Util.hasText(wsmodel.getTrustedflag()) || (Util.hasText(wsmodel.getTrustedflag()) && !wsmodel.getTrustedflag().equals("WhiteList")))){
                        logger.error("Customer [" + customer.getMobilenumber() + "] device binding is enabled, but device [" + deviceid + "] not binded, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.MW_CUST_BIND_WITH_ANOTHER_DEVC); //Customer Binded to other Device
                        return false;
                    }
                }

                /*if (dblist != null && dblist.size() > 0) {

                    for (MWCustDeviceBindingLog l : dblist) {
                        MWDeviceLog deviceLog = l.getDevice();
                        MWCustomer boundCustomer = deviceLog.getCustomer();
                        logger.info("Checking Device [" + deviceid + "] is Binding against this Customer [" + customer.getMobilenumber() + "], Processing...");
                        //Muhammad Hamza added:
                        //This is only for Phase 1 and For phase 2: we just remove boundcustomer != null
                        if (l.getCustomer().getId() == customer.getId() && deviceLog.getDeviceid().equals(deviceid) && boundCustomer != null) {
                            matched = true;
                            logger.info("Binding Device [" + deviceid + "] Found against this Customer [" + customer.getMobilenumber() + "], Match[" + matched + "]!!");
                            break;
                        }
                    }
                } else {
                    logger.error("No Customer Device Binding Log found for Customer [" + customer.getMobilenumber() + "] while binding is enabled, rejecting...");
                    return false;
                }

                if (!matched) {
                    logger.error("Device [" + deviceid + "] not matched for Customer [" + customer.getMobilenumber() + "], rejecting...");
                    return false;
                }*/
            }
        }
        catch (Exception e) {
            logger.error("Exception caught while getting Maximum retiries for Customer, setting hardcoded value 3...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }
        return true;
    }


//    public static Boolean VerifyDeviceBinding(MWCustomer customer,String deviceid ){
//        try {
//            String dbQuery;
//            Map<String, Object> params;
//
//            if(customer.getDevicebindingenabled() && systemDeviceBindingEnabled()) {
//                Boolean matched = false;
//                dbQuery = "from " + MWCustDeviceBindingLog.class.getName() + " c where c.customer= :CUST ";
//                params = new HashMap<String, Object>();
//                params.put("CUST", customer);
//
//                List<MWCustDeviceBindingLog> dblist = GeneralDao.Instance.find(dbQuery, params);
//
//                if (dblist != null && dblist.size() > 0) {
////                    for (MWCustDeviceBindingLog l : dblist) {
////                        if (l.getDevice().getDeviceid().equals(deviceid)) {
////                            matched = true;
////                            break;
////                        }
////                    }
//                    //Muhammad Hamza Added on 12-May-2024
//                    for (MWCustDeviceBindingLog l : dblist) {
//                        MWDeviceLog deviceLog = l.getDevice();
//                        MWCustomer boundCustomer = deviceLog.getCustomer();
//                        logger.info("Checking Device [" + deviceid + "] is Binding against this Customer [" + boundCustomer + "], Processing...");
//
//                        if (deviceLog.getDeviceid().equals(deviceid) && boundCustomer != null) {
//                            matched = true;
//                            logger.info("Binding Device [" + deviceid + "] Found against this Customer [" + customer + "], Match[" + matched + "]!!");
//                            break;
//                        }
//                    }
//                } else {
//                    logger.error("No Customer Device Binding Log found for Customer [" + customer.getMobilenumber() + "] while binding is enabled, rejecting...");
//                    return false;
//                }
//
//                if (!matched) {
//                    logger.error("Device [" + deviceid + "] not matched for Customer [" + customer.getMobilenumber() + "], rejecting...");
//                    return false;
//                }
//            }
//        }
//        catch (Exception e) {
//            logger.error("Exception caught while getting Maximum retiries for Customer, setting hardcoded value 3...");
//            //e.printStackTrace();
//            logger.error(WebServiceUtil.getStrException(e));
//            return false;
//        }
//        return true;
//    }

    public static AppWsEntity ExecuteGetFormTemplateRequest(AppWsEntity wsmodel) {
        try {

            if(wsmodel.getServicename().equals("GetSignUpFormMeta")) {
                wsmodel.setFormcode("SIGNUP_V2");
            }
            if (!Util.hasText(wsmodel.getFormcode())) {
                logger.error("FormCode missing in request");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                wsmodel.setRespcodedesc("FormCode is required");
                return wsmodel;
            }

            logger.info("Executing GetFormTemplate Request for formCode: " + wsmodel.getFormcode());

            String dbQuery = "SELECT f FROM FormTemplates f WHERE f.formCode = :formcode AND f.status = 1";
            Map<String, Object> params = new HashMap<>();
            params.put("formcode", wsmodel.getFormcode());

            List<FormTemplate> forms = GeneralDao.Instance.find(dbQuery, params);

            if (forms != null && !forms.isEmpty()) {
                FormTemplate form = forms.get(0);
                wsmodel.setFormjson(form.getFormjson());

                wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            } else {
                logger.error("No active form found for formCode: " + wsmodel.getFormcode());
                wsmodel.setRespcode(ISOResponseCodes.FORM_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Exception caught while fetching form metadata");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            wsmodel.setRespcodedesc("Error fetching form metadata");
        }
        return wsmodel;

    }

    public static AppWsEntity ExecuteGetTrackingIdRequest(AppWsEntity wsmodel) {

        try {
            logger.info("Executing GetTrackingId Request...");

                SendToOpenAPI(wsmodel);
                if (wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {
                    logger.info("Approved Response received from OpenAPI, replying...");

                    return wsmodel;
                } else {
                    logger.error("Invalid Response [" + wsmodel.getRespcode() + "] received from OpenAPI, returning... ");
                    return wsmodel;
                }


        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    //TODO: Raza update below explicit logic, if the same SendToOpenAPI method(s) can be reused..
    public static AppWsEntity SendToFraud(AppWsEntity wsmodel) {
        try {
            //send to wallet for wallet creation
            wsmodel.setFwdchannelid(ChannelCodes.MIDDLEWARE); //Raza always set FWD Channel ID from Middleware

            Channel destchannel = GlobalContext.getInstance().getChannelbyId(ChannelCodes.FRAUD_MGMT); //Raza should be done through Routing

            if (destchannel == null) {
                logger.error("No Channel found against Channel ID [" + ChannelCodes.FRAUD_MGMT + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.HOST_LINK_DOWN);
                return wsmodel;
            }

            logger.info("Calling Fraud MGMT Sys...");

            if (!BuildMsgForFraud(wsmodel)) {
                logger.error("Unable to Build Message For Fraud MGMT, rejecting...");
                return wsmodel;
            }


            Client client = Client.create();
            client.setConnectTimeout(destchannel.getConnecttimeout() * 1000);
            client.setReadTimeout(destchannel.getReadtimeout() * 1000);
            WebResource webResource = null;
            AppWsEntity resp;
            try {

                logger.info("Calling URL [" + ((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + destchannel.getPort() + destchannel.getWebserviceURL()  + "]");
                webResource = client.resource(((destchannel.getSslEnable() != null && destchannel.getSslEnable()) ? "https://" : "http://") + destchannel.getIp() + ":" + destchannel.getPort() + destchannel.getWebserviceURL());

                //Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
                };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                resp = webResource.type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        //.header("Content-Type", "application/json")
                        .post(AppWsEntity.class, wsmodel);

                logger.info("Response Received [" + resp.getRespcode() + "], Replying to Acquirer...");
                wsmodel.setRespcode(resp.getRespcode());
                if (resp.getRespcode().equals(ISOResponseCodes.APPROVED) || resp.getRespcode().equals(ISOResponseCodes.MW_OTP_REQUIRED)) {
                    if (Util.hasText(resp.getAmounttransaction())) {
                        wsmodel.setAmounttransaction(resp.getAmounttransaction());
                    }
                    //Muhammad Hamza Added for MNO - Wallet to Wallet --START
                    if(resp.getMnoslist() != null){
                        wsmodel.setMnoslist(resp.getMnoslist());
                    }
                    //Muhammad Hamza Added for MNO - Wallet to Wallet --End

                    //Muhammad Hamza Added for E-Ticketing for RAWBank --START


                    if(resp.getQrcode() != null)
                    {
                        wsmodel.setQrcode(resp.getQrcode());
                    }

                    if(resp.getEventid() != null)
                    {
                        wsmodel.setEventid(resp.getEventid());
                    }

                    if(resp.getBookid() != null)
                    {
                        wsmodel.setBookid(resp.getBookid());
                    }
                    if(resp.getTickettypeid() != null)
                    {
                        wsmodel.setTickettypeid(resp.getTickettypeid());
                    }
                    if(Util.hasText(resp.getQuantity()))
                    {
                        wsmodel.setQuantity(resp.getQuantity());
                    }
                    //Muhammad Hamza Added for E-Ticketing for RAWBank --END


                    //Muhammad Hamza Added for Mobile banking feature for RAWBank --START
                    if(resp.getDestaccounttitle() != null)
                    {
                        wsmodel.setDestaccounttitle(resp.getDestaccounttitle());
                    }
                    if(resp.getBeneficiarylist() != null)
                    {
                        wsmodel.setBeneficiarylist(resp.getBeneficiarylist());
                    }
                    if(Util.hasText(String.valueOf(resp.getIsemailverified())))
                    {
                        wsmodel.setIsemailverified(resp.getIsemailverified());
                    }
                    if(Util.hasText(String.valueOf(resp.getIscmsaccoutlinked())))
                    {
                        wsmodel.setIscmsaccoutlinked(resp.getIscmsaccoutlinked());
                    }
                    if(Util.hasText(resp.getDestemailaddress()))
                    {
                        wsmodel.setDestemailaddress(resp.getDestemailaddress());
                    }
                    if(Util.hasText(resp.getBranchname()))
                    {
                        wsmodel.setBranchname(resp.getBranchname());
                    }
                    if(Util.hasText(resp.getBranchcode()))
                    {
                        wsmodel.setBranchcode(resp.getBranchcode());
                    }
                    if(Util.hasText(resp.getClientid())){
                        wsmodel.setClientid(resp.getClientid());
                    }
                    if(Util.hasText(resp.getAccountholdername())){
                        wsmodel.setAccountholdername(resp.getAccountholdername());
                    }
                    if(Util.hasText(resp.getAccountholderaddress())){
                        wsmodel.setAccountholderaddress(resp.getAccountholderaddress());
                    }
                    if(Util.hasText(resp.getAccountcurrency())){
                        wsmodel.setAccountcurrency(resp.getAccountcurrency());
                    }
                    if(Util.hasText(resp.getAccountkey())){
                        wsmodel.setAccountkey(resp.getAccountkey());
                    }
                    if(Util.hasText(resp.getAccountnumber())){
                        wsmodel.setAccountnumber(resp.getAccountnumber());
                    }

                    if(Util.hasText(resp.getBankcode())){
                        wsmodel.setBankcode(resp.getBankcode());
                    }
                    if(resp.getAccountopposition() != null){
                        wsmodel.setAccountopposition(resp.getAccountopposition());
                    }
                    if(resp.getCustomeropposition() != null){
                        wsmodel.setCustomeropposition(resp.getCustomeropposition());
                    }
                    if(Util.hasText(resp.getBiccode())){
                        wsmodel.setBiccode(resp.getBiccode());
                    }
                    //Muhammad Hamza Added for Mobile banking feature for RAWBank --END

                    if (Util.hasText(resp.getDestaccount())) //Raza adding for Meezan Load/Unload txns
                    {
                        wsmodel.setDestaccount(resp.getDestaccount());
                    }
                    if (Util.hasText(resp.getUserid())) {
                        wsmodel.setUserid(resp.getUserid());
                    }
                    if (Util.hasText(resp.getIdentificationno())) {
                        wsmodel.setIdentificationno(resp.getIdentificationno().trim());
                    }
                    if (Util.hasText(resp.getAccountnumber())) //Raza adding for Meezan Delink API
                    {
                        wsmodel.setAccountnumber(resp.getAccountnumber());
                    }
                    if (Util.hasText(resp.getCustomername())) {
                        wsmodel.setCustomername(resp.getCustomername());
                    }
                    if (Util.hasText(resp.getMiddlename())) {
                        wsmodel.setMiddlename(resp.getMiddlename().trim());
                    }
                    if (Util.hasText(resp.getNationality())) {
                        wsmodel.setNationality(resp.getNationality());
                    }
                    if (Util.hasText(resp.getFathername())) {
                        wsmodel.setFathername(resp.getFathername());
                    }
                    if (Util.hasText(resp.getDateofbirth())) {
                        wsmodel.setDateofbirth(resp.getDateofbirth());
                    }
                    if (Util.hasText(resp.getPlaceofbirth())) {
                        wsmodel.setPlaceofbirth(resp.getPlaceofbirth());
                    }
                    if (Util.hasText(resp.getMothername())) {
                        wsmodel.setMothername(resp.getMothername());
                    }
                    if (Util.hasText(resp.getAddress())) {
                        wsmodel.setAddress(resp.getAddress());
                    }
                    if (Util.hasText(resp.getCity())) {
                        wsmodel.setCity(resp.getCity());
                    }
                    if (Util.hasText(resp.getProvince())) {
                        wsmodel.setProvince(resp.getProvince());
                    }
                    if (Util.hasText(resp.getCountry())) {
                        wsmodel.setCountry(resp.getCountry());
                    }
                    if (Util.hasText(resp.getCreationdate())) {
                        wsmodel.setCreationdate(resp.getCreationdate());
                    }
                    if (Util.hasText(resp.getAccesstoken())) {
                        wsmodel.setAccesstoken(resp.getAccesstoken());
                    }
                    if (resp.getAccountlist() != null) {
                        wsmodel.setAccountlist(resp.getAccountlist());
                    }
                    if (Util.hasText(resp.getAcctlimit())) {
                        wsmodel.setAcctlimit(resp.getAcctlimit());
                    }
                    if (Util.hasText(resp.getActivationtime())) {
                        wsmodel.setActivationtime(resp.getActivationtime());
                    }
                    if (Util.hasText(resp.getAllowed())) {
                        wsmodel.setAllowed(resp.getAllowed());
                    }
                    if (Util.hasText(resp.getAvaillimit())) {
                        wsmodel.setAvaillimit(resp.getAvaillimit());
                    }
                    if (Util.hasText(resp.getAvaillimitfreq())) {
                        wsmodel.setAvaillimitfreq(resp.getAvaillimitfreq());
                    }
                    if (Util.hasText(resp.getAvaillimitfreq())) {
                        wsmodel.setAvaillimitfreq(resp.getAvaillimitfreq());
                    }
                    if (Util.hasText(resp.getDailylimit())) {
                        wsmodel.setDailylimit(resp.getDailylimit());
                    }
                    if (resp.getLinkedaccounts() != null) {
                        wsmodel.setLinkedaccounts(resp.getLinkedaccounts());
                    }
                    if (Util.hasText(resp.getMonthlylimit())) {
                        wsmodel.setMonthlylimit(resp.getMonthlylimit());
                    }
                    if (Util.hasText(resp.getSrcchargeamount())) {
                        wsmodel.setSrcchargeamount(resp.getSrcchargeamount());
                    }
                    if (Util.hasText(resp.getDestchargeamount())) {
                        wsmodel.setDestchargeamount(resp.getDestchargeamount());
                    }
                    if (Util.hasText(resp.getTaxamount())) {
                        wsmodel.setTaxamount(resp.getTaxamount());
                    }
                    if (resp.getAccountlimits() != null) {
                        wsmodel.setAccountlimits(resp.getAccountlimits());
                    }
                    if (resp.getProvisionalwallets() != null) {
                        wsmodel.setProvisionalwallets(resp.getProvisionalwallets());
                    }
                    if (Util.hasText(resp.getRequesttime())) {
                        wsmodel.setRequesttime(resp.getRequesttime());
                    }
                    if (Util.hasText(resp.getActivationtime())) {
                        wsmodel.setActivationtime(resp.getActivationtime());
                    }
                    if (Util.hasText(resp.getStan())) {
                        wsmodel.setStan(resp.getStan());
                    }
                    if (Util.hasText(resp.getRrn())) {
                        wsmodel.setRrn(resp.getRrn());
                    }
//                    if (Util.hasText(resp.getStatus())) {
//                        wsmodel.setStatus(resp.getStatus());
//                    }
                    if (resp.getTransactions() != null) {
                        wsmodel.setTransactions(resp.getTransactions());
                    }
                    if (resp.getUsertransactions() != null) {
                        wsmodel.setUsertransactions(resp.getUsertransactions());
                    }
                    if (resp.getTransactionDetail() != null) {
                        wsmodel.setTransactionDetail(resp.getTransactionDetail());
                    }
                    if (Util.hasText(resp.getCurrency())) {
                        wsmodel.setCurrency(resp.getCurrency());
                    }
                    if (Util.hasText(resp.getProvince())) {
                        wsmodel.setProvince(resp.getProvince());
                    }
                    if (Util.hasText(resp.getEnableflag())) {
                        wsmodel.setEnableflag(resp.getEnableflag());
                    }
                    if (Util.hasText(resp.getYearlylimit())) {
                        wsmodel.setYearlylimit(resp.getYearlylimit());
                    }
                    if (Util.hasText(resp.getAccountbalance())) {
                        wsmodel.setAccountbalance(resp.getAccountbalance());
                    }
                    if (Util.hasText(resp.getAccountbalance())) {
                        wsmodel.setAccountbalance(resp.getAccountbalance());
                    }
                    if (Util.hasText(resp.getCardnumber())) {
                        wsmodel.setCardnumber(resp.getCardnumber());
                    }
                    if (Util.hasText(resp.getCardexpiry())) {
                        wsmodel.setCardexpiry(resp.getCardexpiry());
                    }
                    if (Util.hasText(resp.getBankcode())) {
                        wsmodel.setBankcode(resp.getBankcode());
                    }
                    if (Util.hasText(resp.getNewpindata())) {
                        wsmodel.setNewpindata(resp.getNewpindata());
                    }
                    if (Util.hasText(resp.getSecretquestion1())) {
                        wsmodel.setSecretquestion1(resp.getSecretquestion1());
                    }
                    if (Util.hasText(resp.getSecretquestionanswer1())) {
                        wsmodel.setSecretquestionanswer1(resp.getSecretquestionanswer1().trim());
                    }
                    if (Util.hasText(resp.getSecretquestion2())) {
                        wsmodel.setSecretquestion2(resp.getSecretquestion2());
                    }
                    if (Util.hasText(resp.getSecretquestionanswer2())) {
                        wsmodel.setSecretquestionanswer2(resp.getSecretquestionanswer2().trim());
                    }
                    if (Util.hasText(resp.getTotalcount())) {
                        wsmodel.setTotalcount(resp.getTotalcount());
                    }
                    if (Util.hasText(resp.getBlockedflag())) {
                        wsmodel.setBlockedflag(resp.getBlockedflag());
                    }
                    if (Util.hasText(resp.getTempblockflag())) {
                        wsmodel.setTempblockflag(resp.getTempblockflag());
                    }
                    if (Util.hasText(resp.getAgentid())) {
                        wsmodel.setAgentid(resp.getAgentid());
                    }
                    if (Util.hasText(resp.getCdfaccountnumber())) {
                        wsmodel.setCdfaccountnumber(resp.getCdfaccountnumber());
                    }
                    if (Util.hasText(resp.getUsdaccountnumber())) {
                        wsmodel.setUsdaccountnumber(resp.getUsdaccountnumber());
                    }
                    if (Util.hasText(resp.getCdfacctid())) {
                        wsmodel.setCdfacctid(resp.getCdfacctid());
                    }
                    if (Util.hasText(resp.getUsdacctid())) {
                        wsmodel.setUsdacctid(resp.getUsdacctid());
                    }
                    if (resp.getWalletsAndLinkedAccounts() != null) {
                        wsmodel.setWalletsAndLinkedAccounts(resp.getWalletsAndLinkedAccounts());
                    }
                    if (Util.hasText(resp.getBusinessname())) {
                        wsmodel.setBusinessname(resp.getBusinessname());
                    }
                    if (Util.hasText(resp.getFirstname())) {
                        wsmodel.setFirstname(resp.getFirstname().trim());
                    }
                    if (Util.hasText(resp.getLastname())) {
                        wsmodel.setLastname(resp.getLastname().trim());
                    }
                    if (Util.hasText(resp.getMerchanttype())) {
                        wsmodel.setMerchanttype(resp.getMerchanttype());
                    }
                    if (Util.hasText(resp.getEmailaddress())) {
                        wsmodel.setEmailaddress(resp.getEmailaddress());
                    }
                    if (Util.hasText(resp.getAlternatenumber())) {
                        wsmodel.setAlternatenumber(resp.getAlternatenumber());
                    }
                    if (Util.hasText(resp.getAlternateemail())) {
                        wsmodel.setAlternateemail(resp.getAlternateemail());
                    }
                    if (Util.hasText(resp.getIdentificationtype())) {
                        wsmodel.setIdentificationtype(resp.getIdentificationtype());
                    }
                    if (Util.hasText(resp.getCreatoruser())) {
                        wsmodel.setCreatoruser(resp.getCreatoruser());
                    }
                    if (Util.hasText(resp.getCnicpicturefront())) {
                        wsmodel.setCnicpicturefront(resp.getCnicpicturefront());
                    }
                    if (Util.hasText(resp.getCnicpictureback())) {
                        wsmodel.setCnicpictureback(resp.getCnicpictureback());
                    }
                    if (Util.hasText(resp.getCustomerpicture())) {
                        wsmodel.setCustomerpicture(resp.getCustomerpicture());
                    }
                    if (Util.hasText(resp.getCommcdfacctid())) {
                        wsmodel.setCommcdfacctid(resp.getCommcdfacctid());
                    }
                    if (Util.hasText(resp.getCommusdacctid())) {
                        wsmodel.setCommusdacctid(resp.getCommusdacctid());
                    }
                    if (Util.hasText(resp.getDestmerchantid())) {
                        wsmodel.setAgentid(resp.getAgentid());
                    }
                    if (Util.hasText(resp.getMemo())) {
                        wsmodel.setMemo(resp.getMemo());
                    }
                    if (Util.hasText(resp.getQrid())) {
                        wsmodel.setQrid(resp.getQrid());
                    }
                    if (Util.hasText(resp.getAdvertisement1())) {
                        wsmodel.setAdvertisement1(resp.getAdvertisement1());
                    }
                    if (Util.hasText(resp.getAdvertisement2())) {
                        wsmodel.setAdvertisement1(resp.getAdvertisement2());
                    }
                    if (Util.hasText(resp.getAdvertisement3())) {
                        wsmodel.setAdvertisement3(resp.getAdvertisement3());
                    }
                    if (Util.hasText(resp.getCashoutpin())) {
                        logger.info("CashOutPin [" + resp.getCashoutpin() + "]");
                        wsmodel.setCashoutpin(resp.getCashoutpin());
                    }
                    if (Util.hasText(resp.getCurrencyrate())) {
                        wsmodel.setCurrencyrate(resp.getCurrencyrate());
                    }
                    if (Util.hasText(resp.getRequestmoneyref())) {
                        wsmodel.setRequestmoneyref(resp.getRequestmoneyref());
                    }
                    if (Util.hasText(resp.getExpiry())) {
                        wsmodel.setExpiry(resp.getExpiry());
                    }
                    if (Util.hasText(resp.getAuthorizationnumber())) {
                        wsmodel.setAuthorizationnumber(resp.getAuthorizationnumber());
                    }
                    if (Util.hasText(resp.getBillamount())) {
                        wsmodel.setBillamount(resp.getBillamount());
                    }
                    if (Util.hasText(resp.getWalletcurrency())) {
                        wsmodel.setWalletcurrency(resp.getWalletcurrency());
                    }
                    if (Util.hasText(resp.getWalletstatus())) {
                        wsmodel.setWalletstatus(resp.getWalletstatus());
                    }
                    if (Util.hasText(resp.getProduct())) {
                        wsmodel.setProduct(resp.getProduct());
                    }
                    if (Util.hasText(resp.getPoolaccountnumber())) {
                        wsmodel.setPoolaccountnumber(resp.getPoolaccountnumber());
                    }
                    if (Util.hasText(resp.getPoolaccountcurrency())) {
                        wsmodel.setPoolaccountcurrency(resp.getPoolaccountcurrency());
                    }
                    if (resp.getAliaslist() != null) {
                        wsmodel.setAliaslist(resp.getAliaslist());
                    }
                    if (resp.getWalletlist() != null) {
                        wsmodel.setWalletlist(resp.getWalletlist());
                    }
                    if (Util.hasText(resp.getAccountcurrency())) {
                        wsmodel.setAccountcurrency(resp.getAccountcurrency());
                    }
                    //if(Util.hasText(resp.getStatus()))
                    //{
                    //    wsmodel.setStatus(resp.getStatus());
                    //}
                    if (Util.hasText(resp.getState())) {
                        wsmodel.setState(resp.getState());
                    }
                    if (Util.hasText(resp.getAcctStatus())) {
                        wsmodel.setAcctStatus(resp.getAcctStatus());
                    }
                    if (Util.hasText(resp.getOriginalapi())) {
                        wsmodel.setOriginalapi(resp.getOriginalapi());
                    }
                    if (Util.hasText(resp.getDestmobilenumber())) {
                        wsmodel.setDestmobilenumber(resp.getDestmobilenumber());
                    }
                    if (Util.hasText(resp.getAmounttransaction())) {
                        wsmodel.setAmounttransaction(resp.getAmounttransaction());
                    }
                    if (Util.hasText(resp.getDestacctid())) {
                        wsmodel.setDestacctid(resp.getDestacctid());
                    }
                    if (Util.hasText(resp.getDestcurrency())) {
                        wsmodel.setDestcurrency(resp.getDestcurrency());
                    }
                    if (Util.hasText(resp.getAmountcbill())) {
                        wsmodel.setAmountcbill(resp.getAmountcbill());
                    }
                    if (Util.hasText(resp.getAmountcommissioncdf())) {
                        wsmodel.setAmountcommissioncdf(resp.getAmountcommissioncdf());
                    }
                    if (Util.hasText(resp.getAmountcommissionusd())) {
                        wsmodel.setAmountcommissionusd(resp.getAmountcommissionusd());
                    }
                    if (resp.getRequestmoneylist() != null) {
                        wsmodel.setRequestmoneylist(resp.getRequestmoneylist());
                    }
                    if (resp.getCashoutlist() != null) {
                        wsmodel.setCashoutlist(resp.getCashoutlist());
                    }
                    if (resp.getPendingremitloglist() != null) {
                        wsmodel.setPendingremitloglist(resp.getPendingremitloglist());
                    }
                    if (resp.getMerchantDashboardDataObjList() != null) {
                        wsmodel.setMerchantDashboardDataObjList(resp.getMerchantDashboardDataObjList());
                    }
                    if (resp.getMerchantDashboardDataObjListForChild() != null) {
                        wsmodel.setMerchantDashboardDataObjListForChild(resp.getMerchantDashboardDataObjListForChild());
                    }
                    if (Util.hasText(resp.getPaymentmethod())) {
                        wsmodel.setPaymentmethod(resp.getPaymentmethod());
                    }
                    if (resp.getTransactionportallist() != null) {
                        wsmodel.setTransactionportallist(resp.getTransactionportallist());
                    }
                    if (Util.hasText(resp.getMobilenumber())) {
                        wsmodel.setMobilenumber(resp.getMobilenumber());
                    }
                    if (Util.hasText(resp.getDestmobilenumber())) {
                        wsmodel.setDestmobilenumber(resp.getDestmobilenumber());
                    }
                    if (Util.hasText(resp.getFilename())) {
                        wsmodel.setFilename(resp.getFilename());
                    }
                    if (Util.hasText(resp.getNewfilename())) {
                        wsmodel.setNewfilename(resp.getNewfilename());
                    }
                    if (resp.getBillpackages() != null) {
                        wsmodel.setBillpackages(resp.getBillpackages());
                    }
                    if (resp.getAmtwithinduedate() != null) {
                        wsmodel.setAmtwithinduedate(resp.getAmtwithinduedate());
                    }
                    if (resp.getAmtafterduedate() != null) {
                        wsmodel.setAmtafterduedate(resp.getAmtafterduedate());
                    }
                    if (resp.getDuedate() != null) {
                        wsmodel.setDuedate(resp.getDuedate());
                    }
                    if (Util.hasText(resp.getBillid())) {
                        wsmodel.setBillid(resp.getBillid());
                    }
                    if (Util.hasText(resp.getTrancurrency())) {
                        wsmodel.setTrancurrency(resp.getTrancurrency());
                    }
                    if (Util.hasText(resp.getWalletcurrency())) {
                        wsmodel.setWalletcurrency(resp.getWalletcurrency());
                    }
                    if (Util.hasText(resp.getTranauthid())) {
                        wsmodel.setTranauthid(resp.getTranauthid());
                    }
                    if (resp.getAppgraphlist() != null) {
                        wsmodel.setAppgraphlist(resp.getAppgraphlist());
                    }
                    if (resp.getSecretquestionslist() != null) {
                        wsmodel.setSecretquestionslist(resp.getSecretquestionslist());
                    }
                    if (Util.hasText(resp.getApiname())) {
                        wsmodel.setApiname(resp.getApiname());
                    }
                    if (Util.hasText(resp.getFrapiname())) {
                        wsmodel.setFrapiname(resp.getFrapiname());
                    }
                    if (resp.getPayerslist() != null) {
                        wsmodel.setPayerslist(resp.getPayerslist());
                    }
                    if (Util.hasText(resp.getPayerid())) {
                        wsmodel.setPayerid(resp.getPayerid());
                    }
                    if (Util.hasText(resp.getPayertype())) {
                        wsmodel.setPayertype(resp.getPayertype());
                    }
                    if (Util.hasText(resp.getQuotationextid())) {
                        wsmodel.setQuotationextid(resp.getQuotationextid());
                    }
                    if (Util.hasText(resp.getQuotationid())) {
                        wsmodel.setQuotationid(resp.getQuotationid());
                    }
                    if (Util.hasText(resp.getTransactionid())) {
                        wsmodel.setTransactionid(resp.getTransactionid());
                    }
                    if (Util.hasText(resp.getTransactionextid())) {
                        wsmodel.setTransactionextid(resp.getTransactionextid());
                    }
                    if (Util.hasText(resp.getIban())) {
                        wsmodel.setIban(resp.getIban());
                    }
                    if (Util.hasText(resp.getKycstatus())) {
                        wsmodel.setKycstatus(resp.getKycstatus());
                    }
                    if (Util.hasText(resp.getDuration())) {
                        wsmodel.setDuration(resp.getDuration());
                    }
                    if (Util.hasText(resp.getSubscriberno())) {
                        wsmodel.setSubscriberno(resp.getSubscriberno());
                    }
                    if (resp.getChargedetails() != null) {
                        wsmodel.setChargedetails(resp.getChargedetails());
                    }
                    if (Util.hasText(resp.getPermissions())) {
                        wsmodel.setPermissions(resp.getPermissions());
                    }
                    if (Util.hasText(resp.getGender())) {
                        wsmodel.setGender(resp.getGender());
                    }
                    if (Util.hasText(resp.getMonthlyincome())) {
                        wsmodel.setMonthlyincome(resp.getMonthlyincome());
                    }
                    if (Util.hasText(resp.getOccupation())) {
                        wsmodel.setOccupation(resp.getOccupation());
                    }
                    if (Util.hasText(resp.getMonthlyexpenditure())) {
                        wsmodel.setMonthlyexpenditure(resp.getMonthlyexpenditure());
                    }
                    if (Util.hasText(resp.getCustcomments())) {
                        wsmodel.setCustcomments(resp.getCustcomments());
                    }
                    if (Util.hasText(resp.getBillerid()) && (wsmodel.getServicename().equals("InitSendMoneyInternational") || wsmodel.getServicename().equals("SendMoneyInternational"))) {
                        logger.info("Setting BillerID [" + resp.getBillerid() + "] ...");
                        wsmodel.setBillerid(resp.getBillerid());
                    }
                    if (Util.hasText(resp.getBillername())) {
                        logger.info("Setting BillerName [" + resp.getBillername() + "] ...");
                        wsmodel.setBillername(resp.getBillername());
                    }
                    if (Util.hasText(resp.getAvailablebalance())) {
                        wsmodel.setAvailablebalance(resp.getAvailablebalance());
                    }
                    if (Util.hasText(resp.getMunicipality())) {
                        wsmodel.setMunicipality(resp.getMunicipality());
                    }
                    if (Util.hasText(resp.getDestfirstname())) {
                        wsmodel.setDestfirstname(resp.getDestfirstname());
                    }
                    if (Util.hasText(resp.getDestlastname())) {
                        wsmodel.setDestlastname(resp.getDestlastname());
                    }
                    if (Util.hasText(resp.getBillerrespcode())) {
                        wsmodel.setBillerrespcode(resp.getBillerrespcode());
                    }
                    if (Util.hasText(resp.getBillerrespcodedesc())) {
                        wsmodel.setBillerrespcodedesc(resp.getBillerrespcodedesc());
                    }
                    if (Util.hasText(resp.getTrantype())) {
                        wsmodel.setTrantype(resp.getTrantype());
                    }
                    if (resp.getUnreadapprovalcount() != null) {
                        wsmodel.setUnreadapprovalcount(resp.getUnreadapprovalcount());
                    }
                    if (Util.hasText(resp.getMerchantname())) {
                        wsmodel.setMerchantname(resp.getMerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantname())) {
                        wsmodel.setDestmerchantname(resp.getDestmerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantcode())) {
                        wsmodel.setDestmerchantcode(resp.getDestmerchantcode());
                    }
                    if (Util.hasText(resp.getMerchantname())) {
                        wsmodel.setMerchantname(resp.getMerchantname());
                    }
                    if (Util.hasText(resp.getDestmerchantname())) {
                        wsmodel.setDestmerchantname(resp.getDestmerchantname());
                    }
                    if (Util.hasText(resp.getMerchantcode())) {
                        wsmodel.setMerchantcode(resp.getMerchantcode());
                    }
                    if (Util.hasText(resp.getDestmerchantcode())) {
                        wsmodel.setDestmerchantcode(resp.getDestmerchantcode());
                    }
                    if (wsmodel.getIs_merchant() != null) {
                        wsmodel.setIs_merchant(resp.getIs_merchant());
                    }
                    if (resp.getCountrylist() != null) {
                        wsmodel.setCountrylist(resp.getCountrylist());
                    }
                    if (Util.hasText(resp.getClabe())) {
                        wsmodel.setClabe(resp.getClabe());
                    }
                    if (Util.hasText(resp.getCbu())) {
                        wsmodel.setCbu(resp.getCbu());
                    }
                    if (Util.hasText(resp.getCbualias())) {
                        wsmodel.setCbualias(resp.getCbualias());
                    }
                    if (Util.hasText(resp.getBikcode())) {
                        wsmodel.setBikcode(resp.getBikcode());
                    }
                    if (Util.hasText(resp.getAbaroutingnumber())) {
                        wsmodel.setAbaroutingnumber(resp.getAbaroutingnumber());
                    }
                    if (Util.hasText(resp.getBsbnumber())) {
                        wsmodel.setBsbnumber(resp.getBsbnumber());
                    }
                    if (Util.hasText(resp.getRoutingcode())) {
                        wsmodel.setRoutingcode(resp.getRoutingcode());
                    }
                    if (Util.hasText(resp.getEntityttid())) {
                        wsmodel.setEntityttid(resp.getEntityttid());
                    }
                    if (Util.hasText(resp.getAccounttype())) {
                        wsmodel.setAccounttype(resp.getAccounttype());
                    }
                    if (resp.getCreditaccountnumber() != null) {
                        wsmodel.setCreditaccountnumber(resp.getCreditaccountnumber());
                    }
                    if (resp.getBankMnemonic() != null) {
                        wsmodel.setBankMnemonic(resp.getBankMnemonic());
                    }
                    if (Util.hasText(resp.getRelationshipcode())) {
                        wsmodel.setRelationshipcode(resp.getRelationshipcode());
                    }
                    if (Util.hasText(resp.getMig_cdfbalance())) {
                        wsmodel.setMig_cdfbalance(resp.getMig_cdfbalance());
                    }
                    if (Util.hasText(resp.getMig_usdbalance())) {
                        wsmodel.setMig_usdbalance(resp.getMig_usdbalance());
                    }
                    if (Util.hasText(resp.getOrigdataelement())) {
                        wsmodel.setOrigdataelement(resp.getOrigdataelement());
                    }
                    if (Util.hasText(resp.getCustomerid())) {
                        wsmodel.setCustomerid(resp.getCustomerid());
                    }
                    if (Util.hasText(resp.getNewmobilenumber())) {
                        wsmodel.setNewmobilenumber(resp.getNewmobilenumber());
                    }
                    if (resp.getS2mcardslist() != null) {
                        wsmodel.setS2mcardslist(resp.getS2mcardslist());
                    }
                    if (resp.getTranObjList() != null) {
                        wsmodel.setTranObjList(resp.getTranObjList());
                    }
                    if (resp.getS2mBalance() != null) {
                        wsmodel.setS2mBalance(resp.getS2mBalance());
                    }
                    if (Util.hasText(resp.getOrderid())) {
                        wsmodel.setOrderid(resp.getOrderid());
                    }
                    if (Util.hasText(resp.getRedirecthtml())) {
                        wsmodel.setRedirecthtml(resp.getRedirecthtml());
                    }
                    if (Util.hasText(resp.getCardsecuritycode())) {
                        wsmodel.setCardsecuritycode(resp.getCardsecuritycode());
                    }
                    if (Util.hasText(resp.getThreedsenabled())) {
                        wsmodel.setThreedsenabled(resp.getThreedsenabled());
                    }
                    if (Util.hasText(resp.getThreedsversion())) {
                        wsmodel.setThreedsversion(resp.getThreedsversion());
                    }
                    if (Util.hasText(resp.getThreedsacceptedversion())) {
                        wsmodel.setThreedsacceptedversion(resp.getThreedsacceptedversion());
                    }
                    if (Util.hasText(resp.getScheme())) {
                        wsmodel.setScheme(resp.getScheme());
                    }
                    if (Util.hasText(resp.getBrand())) {
                        wsmodel.setBrand(resp.getBrand());
                    }
                    if (Util.hasText(resp.getSelfflag())) //Raza adding for Till Notification 01-12-2022
                    {
                        wsmodel.setSelfflag(resp.getSelfflag());
                    }
                    if (Util.hasText(resp.getExchangerate())) {
                        wsmodel.setExchangerate(resp.getExchangerate());
                    }
                    if (resp.getCard() != null) {
                        wsmodel.setCard(resp.getCard());
                    }
                    if (resp.getCardlist() != null) {
                        wsmodel.setCardlist(resp.getCardlist());
                    }
                    // Added by Affan on 26-July-23 Start
                    if (resp.getCscBalanceResp() != null) {
                        wsmodel.setCscBalanceResp(resp.getCscBalanceResp());
                    }
                    if (resp.getCscgetamountdueresp() != null) {
                        wsmodel.setCscgetamountdueresp(resp.getCscgetamountdueresp());
                    }
                    if (resp.getCscTransactionList() != null) {
                        wsmodel.setCscTransactionList(resp.getCscTransactionList());
                    }
                    if (resp.getCscGetClientDetailsResp() != null) {
                        wsmodel.setCscGetClientDetailsResp(resp.getCscGetClientDetailsResp());
                    }
                    if (resp.getCscCard() != null) {
                        wsmodel.setCscCard(resp.getCscCard());
                    }
                    if (resp.getCscGetCardsRespList() != null) {
                        wsmodel.setCscGetCardsRespList(resp.getCscGetCardsRespList());
                    }
                    if (resp.getCscLoadCard() != null) {
                        wsmodel.setCscLoadCard(resp.getCscLoadCard());
                    }
                    // Added by Affan on 26-July-23 End
                    if (resp.getForexrates() != null) {
                        wsmodel.setForexrates(resp.getForexrates());
                    }
                    if (Util.hasText(resp.getDestdateofbirth())) {
                        wsmodel.setDestdateofbirth(resp.getDestdateofbirth());
                    }
                    if (Util.hasText(resp.getBillertxnid())) {
                        wsmodel.setBillertxnid(resp.getBillertxnid());
                    }
                    if (resp.getSecondaryMobileNumbers()!=null){
                        wsmodel.setSecondaryMobileNumbers(resp.getSecondaryMobileNumbers());
                    }
                    if (resp.getSecondaryEmails()!=null){
                        wsmodel.setSecondaryEmails(resp.getSecondaryEmails());
                    }
                }

                wsmodel.setStatus(resp.getStatus());
                wsmodel.setIcccarddata(resp.getIcccarddata());

                return wsmodel;
            } catch (Exception e) {
                logger.error("Exception caught while sending [" + wsmodel.getServicename() + "] to Switch System");
                logger.error(WebServiceUtil.getStrException(e));
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            } finally {
                logger.info("Destroying client for API call, deleting request...");
                if (client != null) {
                    client.destroy();
                }
            }
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing ServiceRequest [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static Boolean BuildMsgForFraud(AppWsEntity wsmodel) {
        try {
            if (Util.hasText(wsmodel.getAmounttransaction())) {
                if (wsmodel.getAmounttransaction().contains(".")) {
                    wsmodel.setAmounttransaction(wsmodel.getAmounttransaction().replace(".", ""));
                }
            }

            //Raza only generating & sending TxnRefNum & DateTime before sending to OpenAPI, middleware failed calls should be verified through logs.
            if (!Util.hasText(wsmodel.getTranrefnumber())) {
                wsmodel.setTranrefnumber(Util.generateTxnRefNumber(60));
            }
            if (!Util.hasText(wsmodel.getTransdatetime())) {
                wsmodel.setTransdatetime(WebServiceUtil.TransDateTimeFormat.format(new Date()));
            }

            Terminal endPointTerminal = null;

            if (GlobalContext.getInstance().getChannelbyId(ChannelCodes.FRAUD_MGMT) == null) {
                logger.error("Open API Channel not Found with ChannelID [" + ChannelCodes.FRAUD_MGMT + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.HOST_LINK_DOWN);
                return false;
            }

            String institutionCode = (GlobalContext.getInstance().getChannelbyId(ChannelCodes.FRAUD_MGMT)).getInstitutionId();
            ProcessContext processContext = new ProcessContext();
            processContext.init();
            endPointTerminal = processContext.getAcquierSwitchTerminal(institutionCode);
            Set<SecureKey> incomingKeySet = endPointTerminal.getKeySet();
            SecureDESKey AESKey = SecureDESKey.getKeyByType(KeyType.TYPE_AES_KEY, incomingKeySet);
            SecureDESKey AESData = SecureDESKey.getKeyByType(KeyType.TYPE_AES_VALUE, incomingKeySet);

            String EncryptedData = Base64.getEncoder().encodeToString(WSEncryptionUtil.AES256Encrypt(AESData.getKeyBytes(), AESKey.getKeyBytes()));

            logger.info("Encrypted Key generated for ChannelOPI!");
            wsmodel.setEncryptkey(EncryptedData);
            logger.info("Encrypted Key Updated for ChannelOPI!");
            return true;

        } catch (Exception e) {
            logger.error("Exception caught while updating encrypt key for Channel OpenAPI, rejecting...");
            wsmodel.setRespcode(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE); //75 Unknown Txn Sorce for OpenAPI
            logger.error(WebServiceUtil.getStrException(e));
            return false;
        }


    }

    //Nofel Adding for Device Unbinding - Start - 17092025
    public static AppWsEntity executeDeviceUnbinding(AppWsEntity wsmodel) {
        //First


        return wsmodel;
    }

    public static AppWsEntity executeConfirmDeviceUnbinding(AppWsEntity wsmodel, boolean otpConfirmed) {
        try {

            String serviceName = wsmodel.getServicename();

            logger.info("Executing ConfirmDeviceUnbinding Request...");

            //Validating User and Device Sessions
            if (!ValidateUserandAppSession(wsmodel)) {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " c where c.mobilenumber= :MOB ";
            params = new HashMap<>();
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if (customer == null) {
                wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                return wsmodel;
            }


            if (!otpConfirmed) {

                try {

                    if (wsmodel.getPindata() == null) {
                        logger.error("PIN Data is missing in the request, rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.ERROR_PINDATA);
                        return wsmodel;
                    }
                    wsmodel.setServicename("VerifyPin");
                    MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);

                }
                catch (Exception e)
                {

                    logger.error("Exception caught while sending VerifyPin for ConfirmDeviceUnbinding Request to OpenAPI, rejecting...");
                    logger.error(WebServiceUtil.getStrException(e));
                    wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
                    return wsmodel;

                }
                finally {

                    wsmodel.setServicename(serviceName);

                }

                if (Util.hasText(wsmodel.getRespcode()) && !wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)) {

                    logger.error("Negative response recieved from OpenAPI while verifying PIN, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.BAD_PIN);
                    return wsmodel;

                } else {

                    logger.info("Positive response received from OpenAPI while verifying PIN, proceeding...");

                    //Verify unbinding limits

                    //Fetching Customers unbinding count
                    dbQuery = "from " + MWCustomerDeviceUnbindingCount.class.getName() + " c where c.customerID = :CUST_ID ";
                    params = new HashMap<String, Object>();
                    params.put("CUST_ID", customer.getId().toString());

                    MWCustomerDeviceUnbindingCount unbindingCount = (MWCustomerDeviceUnbindingCount) GeneralDao.Instance.findObject(dbQuery, params);

                    logger.info("Executed Query to fetch Customer's unbinding count: {}", dbQuery);
                    logger.info(" with params: Customer ID[{}]", params.get("CUST_ID"));

                    //Fetch Daily and Global unbinding limits
                    dbQuery = "from " + MWDeviceUnbindLimitConfig.class.getName();

                    MWDeviceUnbindLimitConfig unbindLimitConfig = (MWDeviceUnbindLimitConfig) GeneralDao.Instance.findUnique(dbQuery);

                    logger.info("Executed Query to fetch Device Unbinding Limit Config: {}", dbQuery);
                    logger.info("No params for this query.");

                    if (unbindingCount != null) {
                        logger.info(unbindingCount.toString());
                    } else {
                        logger.warn("unbindingCount is NULL");

                        logger.info("initializing customer [{}] unbinding counts to 0", customer.getId());
                        unbindingCount = new MWCustomerDeviceUnbindingCount();
                        unbindingCount.setCustomerID(customer.getId().toString());
                        unbindingCount.setDailyUnbindingCount(0L);
                        unbindingCount.setMonthlyUnbindingCount(0L);
                        GeneralDao.Instance.saveOrUpdate(unbindingCount);

                    }

                    if (unbindLimitConfig != null) {
                        logger.info(unbindLimitConfig.toString());
                    } else {
                        logger.warn("unbindLimitConfig is NULL");
                    }

                    if (unbindingCount != null && unbindLimitConfig != null
                            && unbindingCount.getDailyUnbindingCount() != null
                            && unbindingCount.getMonthlyUnbindingCount() != null
                            && unbindLimitConfig.getDailyLimit() != null
                            && unbindLimitConfig.getMonthlyLimit() != null) {

                        if ((unbindingCount.getDailyUnbindingCount() < unbindLimitConfig.getDailyLimit())
                                && (unbindingCount.getMonthlyUnbindingCount() < unbindLimitConfig.getMonthlyLimit())) {


                            logger.info("Customer's unbinding count does not cross the daily/monthly limit, proceeding to roll out OTP...");

                            //roll out OTP
                            try {

                                logger.info("Attempting to roll out OTP for confirmation for Device unbinding...");

                                if (customer.getOtpchannel() == null || (Util.hasText(customer.getOtpchannel())
                                        && (customer.getOtpchannel().equals(OTPChannel.SMS) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))) {
                                    if (!SMSGatewayHandler.CreateandSendOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_UNBIND_DEVICE, customer.getMobilenumber(), wsmodel)) {

                                        logger.error("Failed to create & Send SMS for Mobile [" + customer.getMobilenumber() + "], ignoring...");

                                    }
                                }

                            } catch (Exception e) {

                                logger.error("Exception caght while sending SMS, ignoring...");
                                logger.error(WebServiceUtil.getStrException(e));

                            }

                            try {

                                if ((Util.hasText(customer.getEmailaddress()) && Util.hasText(customer.getOtpchannel())
                                        && (customer.getOtpchannel().equals(OTPChannel.EMAIL) || customer.getOtpchannel().equals(OTPChannel.SMS_EMAIL)))
                                        && !EmailGatewayHandler.CreateandSendEmailOTP(CustomerType.CUSTOMER, SMSCategory.VERIFY_UNBIND_DEVICE, customer.getEmailaddress(), wsmodel)) {

                                    logger.error("Failed to create & Send Email for EmailID [" + customer.getEmailaddress() + "], ignoring...");

                                }

                            } catch (Exception e) {

                                logger.error("Exception caght while sending email, ignoring...");
                                logger.error(WebServiceUtil.getStrException(e));

                            }

                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                            return wsmodel;

                        } else {

                            logger.info("Customer's unbinding count crosses the daily/monthly, rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.LIMIT_EXCEEDED);
                            return wsmodel;

                        }
                    } else {
                        logger.warn("Null detected in unbindingCount or unbindLimitConfig, cannot proceed with comparison.");
                        wsmodel.setRespcode(ISOResponseCodes.ERROR_AVAILLIMIT);
                        return wsmodel;
                    }

                }
            } else {

                MWCustDeviceBindHandler.processConfirmRemoveDeviceFromBindingRequest(wsmodel);
                MarkSessionsExpire(wsmodel);
                return wsmodel;

            }


        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }

    }

    public static AppWsEntity executeGenerateTruIDToken(AppWsEntity wsModel) {
        return wsModel;
    }

    public static AppWsEntity executeFetchUnbindingReasons(AppWsEntity wsmodel)
    {
        try {
            logger.info("Executing FetchUnbindingReasons Request...");
            String dbQuery = "from " + MWDeviceUnbindingReasons.class.getName();

            logger.info("Executing Query to fetch Device Unbinding Reasons: {}", dbQuery);

            @SuppressWarnings("unchecked")
            List<MWDeviceUnbindingReasons> reasonList =
                            (List<MWDeviceUnbindingReasons>)GeneralDao.Instance.find(dbQuery);


            if (reasonList == null || reasonList.isEmpty()) {
                reasonList = new ArrayList<>();
            }

            List<MWDeviceUnbindingReasonsDTO> mwDeviceUnbindingReasonsDTOS = new ArrayList<>();

            for (MWDeviceUnbindingReasons reason : reasonList) {
                MWDeviceUnbindingReasonsDTO dto = new MWDeviceUnbindingReasonsDTO();
                dto.setId(reason.getId());
                dto.setReasonCode(reason.getReasonCode());
                dto.setReasonDescription(reason.getReasonDescription());
                mwDeviceUnbindingReasonsDTOS.add(dto);
            }

            wsmodel.setUnbindDeviceReasons(mwDeviceUnbindingReasonsDTOS);
            wsmodel.setTotalcount(String.valueOf(reasonList.size()));
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity executeSoftDeleteUser(AppWsEntity wsmodel)
    {
        try {
            logger.info("Executing Soft delete user request...");

            if(!Util.hasText(wsmodel.getUserid())) {
                logger.error("Please provide user ID, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " mc where mc.userid = :USER_ID";
            params = new HashMap<>();
            params.put("USER_ID", wsmodel.getUserid());

            MWCustomer customer = (MWCustomer) GeneralDao.Instance.findObject(dbQuery, params);

            if(customer == null) {
                logger.error("Customer not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
                return wsmodel;
            }

            String allowedStatus = SystemConfig.getConfigByIdentifier("ALLOWED_SOFT_DELETE_STATUS");

            List<String> allowedStatusList = Optional.ofNullable(allowedStatus)
                    .map(s -> Arrays.stream(s.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

            if(!allowedStatus.contains(customer.getStatus()))
            {
                logger.error("User not allowed to be soft deleted, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_TRANSACTION_ALLOWED);
                return wsmodel;
            }

            customer.setUsername(wsmodel.getUsername());
            customer.setEmailaddress(wsmodel.getEmailaddress());
            customer.setCnic(wsmodel.getIdentificationno());
            customer.setMobilenumber(wsmodel.getMobilenumber());
            customer.setStatus(wsmodel.getStatus());

            GeneralDao.Instance.saveOrUpdate(customer);

            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            return wsmodel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity executeGetAllUsers(AppWsEntity wsmodel)
    {
        try {
            logger.info("Executing Soft delete user request...");

            if(wsmodel.getStatuslist() == null || wsmodel.getStatuslist().isEmpty())
            {
                logger.error("Status list not found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            dbQuery = "from " + MWCustomer.class.getName() + " mc where mc.status in (:STATUS_LIST)";
            params = new HashMap<>();
            params.put("STATUS_LIST", wsmodel.getStatuslist());

            List<MWCustomer> customerlist = (List<MWCustomer>) GeneralDao.Instance.find(dbQuery, params);

            if(customerlist == null || customerlist.isEmpty())
            {
                logger.error("No customers found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.DATA_NOT_FOUND);
                return wsmodel;
            }

            List<String> useridlist = customerlist
                    .stream()
                    .map(customer-> customer.getUserid())
                    .collect(Collectors.toList());

            wsmodel.setUseridlist(useridlist);

            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            return wsmodel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity executeEnvVars(AppWsEntity wsmodel)
    {
        try {
            logger.info("Executing Envronment variables request...");

            String dbQuery;
            Map<String, Object> params;

            Channel channel = GlobalContext.getInstance().getChannelbyId(wsmodel.getChannelid());

            dbQuery = "from " + SecureKey.class.getName() + " sk where sk.terminal.code = :TERMINAL";
            params = new HashMap<>();
            params.put("TERMINAL", Long.parseLong(channel.getInstitutionId()));

            List<SecureKey> secureKeys = (List<SecureKey>) GeneralDao.Instance.find(dbQuery, params);

            if(secureKeys == null || secureKeys.isEmpty()) {
                logger.error("No secure keys found, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.DATA_NOT_FOUND);
                return wsmodel;
            }

            logger.info("Secure keys found, processing...");

            Map<String, String> configMap = new HashMap<>();

            String loginName = channel.getCredusername();
            String loginPass = channel.getCredpassword();
            String authHeaderUsername = channel.getCredchanneltype();
            String authHeaderPassword = channel.getCredchannelsubtype();

            // API Headers
            configMap.put("loginName", loginName); //c33d5a5d673e2ba9abb4f4ae8b47b3ecf0a61b92
            configMap.put("loginPass", loginPass); //578561a6806447c83b9ad95c9e7c5bc42d89b1e1
            configMap.put("authUserName", authHeaderUsername);
            configMap.put("authUserPass", authHeaderPassword);

            // Encryption Keys
            String encryptKey = generateEncryptKey(channel.getChannelId());

            configMap.put("encryptKey", encryptKey);
            configMap.put("tpk", findSecureKey(secureKeys, KeyType.TYPE_TPK).getKeyBytes());
            configMap.put("passwordEncryptionKey", findSecureKey(secureKeys, KeyType.TYPE_APP_PASS).getKeyBytes());
            configMap.put("otpEncryptionKey", findSecureKey(secureKeys, KeyType.TYPE_APP_OTP).getKeyBytes()); //3aae908b20ce459ac85a45e48bf7d9c85d8ee80f718e1e2f6a38ef6014f25dbe
            configMap.put("qrEncrytionKey", findSecureKey(secureKeys, KeyType.TYPE_QR).getKeyBytes()); //d7a1c345c46a77644fba6e438a98c2369871bda07f409039e5c11ed2b49d489c

            wsmodel.setVariables(configMap);
            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            return wsmodel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity executeBanksListRequest(AppWsEntity wsmodel)
    {
        try {
            logger.info("Executing [{}] request...", wsmodel.getServicename());

            ExecuteMWServiceRequest(wsmodel, false);

            logger.info("Response code [{}] recieved", wsmodel.getRespcode());

            return wsmodel;
        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Executing Request [" + wsmodel.getServicename() + "]");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    private static SecureKey findSecureKey(List<SecureKey> keys, String keyType) {
        return keys.stream()
                .filter(sk -> keyType.equals(sk.getKeyType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing key: " + keyType));
    }
}
