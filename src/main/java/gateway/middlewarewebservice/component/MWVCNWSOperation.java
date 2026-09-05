package gateway.middlewarewebservice.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.customer.MWCustVCNCards;
import pk.vaulsys.apigateway.notification.handler.NotificationHandler;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.handler.MiddlewareVCNWebServiceHandler;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.CardObj;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;

import java.util.*;

//import pk.vaulsys.apigateway.customer.MWCustGiftCards;

public class MWVCNWSOperation {

    private static final Logger logger = LogManager.getLogger(MWVCNWSOperation.class);

    public static AppWsEntity ExecuteGetVCNCardsRequest(AppWsEntity wsmodel, String type)
    {
        String dbQuery = null;
        Map<String, Object> params = null;
        List<MWCustVCNCards> dbcards = null;
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            //Verify MobileNumber
            dbQuery = "from " + MWCustVCNCards.class.getName() + " c where (c.customer= :CUST or c.giftedbycustomer = :GCUST) ";
            params = new HashMap<String, Object>();
            params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
            params.put("GCUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));

            if(Util.hasText(type)){
                dbQuery += " and c.type = :TYPE ";
                params.put("TYPE", type);
            }
            else{
                dbQuery += " and c.type in('Virtual Card','Gift Card') ";
            }

            logger.info("Query [" + dbQuery + "] type [" + type + "]");

            dbcards = GeneralDao.Instance.find(dbQuery, params);

            if(dbcards != null && dbcards.size() > 0)
            {
                logger.info("DB Card Count [" + dbcards.size() + "]");
                List<CardObj> cards = new ArrayList<>();

                for(MWCustVCNCards c : dbcards)
                {
                    CardObj card = new CardObj();
                    card.setId(c.getId().toString()); //Added By Waleed
                    card.setNumber(MiddlewareVCNWebServiceHandler.TranslateToApp(c.getCardpackid(), true));
                    card.setCreationdate(WebServiceUtil.TransDateTimeFormat.format(c.getCreatedate()));
                    card.setStatus(c.getStatus());
                    card.setType(c.getType());
                    card.setColor(c.getColor());
                    if(Util.hasText(c.getEmbossname())){
                        card.setCardholdername(MiddlewareVCNWebServiceHandler.TranslateToApp(c.getEmbossname(), false));
                    }
                    if(c.getGiftedbycustomer() != null){
                        card.setGiftedbymobile(c.getGiftedbycustomer().getMobilenumber());
                        card.setGiftedbyname(c.getGiftedbycustomer().getFirstname() + " " + c.getGiftedbycustomer().getLastname());
                    }
                    cards.add(card);
                }


                try{

                    //Verify MobileNumber
                    dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.registeredmobnumber= :CUST ";
                    params = new HashMap<String, Object>();
                    params.put("CUST", "+"+wsmodel.getMobilenumber().substring(2));

                    if(Util.hasText(type)){
                        dbQuery += " and c.type = :TYPE ";
                        params.put("TYPE", type);
                    }
                    else{
                        dbQuery += " and c.type = 'Gift Card' ";
                    }

                    logger.info("Additional Query [" + dbQuery + "] type [" + type + "]");

                    dbcards = GeneralDao.Instance.find(dbQuery, params);

                    if(dbcards != null){
                        logger.info("Additional Gift Cards found, updating and returning...");

                        for(MWCustVCNCards c : dbcards){
                            if(c.getCustomer() == null){
                                c.setCustomer(NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                                GeneralDao.Instance.saveOrUpdate(c);

                                CardObj card = new CardObj();
                                card.setId(c.getId().toString()); //Added By Waleed
                                card.setNumber(MiddlewareVCNWebServiceHandler.TranslateToApp(c.getCardpackid(), true));
                                card.setCreationdate(WebServiceUtil.TransDateTimeFormat.format(c.getCreatedate()));
                                card.setStatus(c.getStatus());
                                card.setType(c.getType());
                                card.setColor(c.getColor());
                                if(Util.hasText(c.getEmbossname())){
                                    card.setCardholdername(MiddlewareVCNWebServiceHandler.TranslateToApp(c.getEmbossname(), false));
                                }
                                if(c.getGiftedbycustomer() != null){
                                    card.setGiftedbymobile(c.getGiftedbycustomer().getMobilenumber());
                                    card.setGiftedbyname(c.getGiftedbycustomer().getFirstname() + " " + c.getGiftedbycustomer().getLastname());
                                }
                                cards.add(card);
                            }
                        }
                    }

                }
                catch (Exception e){
                    logger.error("Exception caught while getting additional cards, ignoring...");
                    logger.error(WebServiceUtil.getStrException(e));
                }

                wsmodel.setCardlist(cards);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
            else
            {
                logger.error("No VCN Card found in DB for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing GetVCNCards, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            dbQuery = null;
            params = null;
            dbcards = null;
        }
    }

    public static AppWsEntity ExecuteBuyVCNCardRequest(AppWsEntity wsmodel, String type)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;
        try{
            MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);

            if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                logger.info("Approved Response received for [" + wsmodel.getServicename() + "], saving card...");

                if(wsmodel.getCard() != null){

                    if(Util.hasText(wsmodel.getCard().getApplicationnumber())
                            && Util.hasText(wsmodel.getCard().getProgramcode())
                            && Util.hasText(wsmodel.getCard().getDeviceindicator())
                            && Util.hasText(wsmodel.getCard().getCardpackid())
                            && Util.hasText(wsmodel.getCard().getDevicenumber())
                            && Util.hasText(wsmodel.getCard().getRegisteredmobileno())
                            && Util.hasText(wsmodel.getCard().getEmbossname())){

                        wsmodel.getCard().setNumber(WSEncryptionUtil.encryptDBVirtualCardDetails(wsmodel.getCard().getCardpackid()));
                        wsmodel.getCard().setEmbossname(WSEncryptionUtil.encryptDBVirtualCardDetails(wsmodel.getCard().getEmbossname()));
                        wsmodel.getCard().setCardpackid(wsmodel.getCard().getNumber());
                        wsmodel.getCard().setDevicenumber(WSEncryptionUtil.encryptDBVirtualCardDetails(wsmodel.getCard().getDevicenumber()));


                        //Raza check for Duplicate...

                        logger.info("Getting and verifying destination Mobile Number....");
                        dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.cardpackid= :CRD ";
                        params = new HashMap<String, Object>();
                        params.put("CRD", wsmodel.getCard().getCardpackid());

                        dbcard = (MWCustVCNCards) GeneralDao.Instance.findObject(dbQuery, params);

                        if(dbcard == null){

                            dbcard = new MWCustVCNCards();
                            dbcard.setApplicationnumber(wsmodel.getCard().getApplicationnumber());
                            dbcard.setProgramcode(wsmodel.getCard().getProgramcode());
                            dbcard.setDeviceindicator(wsmodel.getCard().getDeviceindicator());
                            dbcard.setCardpackid(wsmodel.getCard().getCardpackid());
                            dbcard.setDevicenumber(wsmodel.getCard().getDevicenumber());
                            dbcard.setRegisteredmobnumber(wsmodel.getCard().getRegisteredmobileno());
                            dbcard.setEmbossname(wsmodel.getCard().getEmbossname());
                            dbcard.setType(type);
                            dbcard.setStatus("00");
                            // M.Umer adding for Card Color schemes for NEW cards
                            logger.info("Setting up NEW --- CARD Color to: " + wsmodel.getCardcolor() );
                            if(Util.hasText(wsmodel.getCardcolor()))
                                dbcard.setColor(wsmodel.getCardcolor());
                            else
                                dbcard.setColor("#000000"); // Default Card color.



                            if(Util.hasText(type) && type.equals("Gift Card")){
                                dbcard.setGiftedbycustomer(NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                                dbcard.setCustomer(NotificationHandler.GetCustomer(wsmodel.getDestmobilenumber()));
                            }
                            else{
                                dbcard.setCustomer(NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                            }

                            dbcard.setCreatedate(new Date());

                            GeneralDao.Instance.save(dbcard);

                            GeneralDao.Instance.endTransaction();
                            logger.info("****Testing [" + dbcard.getId() + "] Cust [" + dbcard.getCustomer() + "]");
                            GeneralDao.Instance.beginTransaction();

                            GeneralDao.Instance.refresh(dbcard);
                            wsmodel.getCard().setId(dbcard.getId()+"");

                            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                            logger.info("Setting card color in response: " + dbcard.getColor());
                            wsmodel.setCardcolor(dbcard.getColor());
                            return wsmodel;
                        }
                        else{
                            logger.error("Duplicate Card found in DB against CardPackId [" + wsmodel.getCard().getCardpackid() + "], rejecting...");
                            wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_CARD_NUMBER);
                            return wsmodel;
                        }
                    }
                    else{
                        logger.error("Missing neccessary fields to store Card Object for [" + wsmodel.getServicename() + "], rejecting....");
                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                        return wsmodel;
                    }

                }
                else{
                    logger.error("Card Object not found in [" + wsmodel.getServicename() + "] response, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
                }
            }

            return wsmodel;
        }
        finally {
            dbQuery = null;
            params = null;
        }

    }

    public static AppWsEntity ExecuteUpdateVCNProfileRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteGetVCNCardDetailsRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteLoadVCNCardRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteUnLoadVCNCardRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteEnableVCNCardRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteDisableVCNCardRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteGetVCNCardStatementRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteResetVCNCardPinRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteGetVCNCardBalanceRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteActivateVCNCardRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

    public static AppWsEntity ExecuteGetVCNCardLimitsRequest(AppWsEntity wsmodel)
    {
        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
    }

//    public static AppWsEntity ExecuteGetVCNGiftCardsRequest(AppWsEntity wsmodel)
//    {
//        String dbQuery = null;
//        Map<String, Object> params = null;
//        List<MWCustGiftCards> dbcards = null;
//        try
//        {
//            logger.info("Validating Session...");
//            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
//            {
//                logger.error("Failed to Validate Session, rejecting...");
//                return wsmodel;
//            }
//
//            //Verify MobileNumber
//            dbQuery = "from " + MWCustGiftCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//
//            dbcards = GeneralDao.Instance.find(dbQuery, params);
//
//            if(dbcards != null && dbcards.size() > 0)
//            {
//                List<CardObj> cards = new ArrayList<>();
//
//                for(MWCustGiftCards c : dbcards)
//                {
//                    CardObj card = new CardObj();
//                    card.setId(c.getId().toString()); //Added By Waleed
//                    card.setNumber(MiddlewareVCNWebServiceHandler.TranslateToApp(c.getCardpackid(), true));
//                    card.setCreationdate(WebServiceUtil.TransDateTimeFormat.format(c.getCreatedate()));
//                    card.setStatus(c.getStatus());
//                    card.setType(c.getType());
//                    if(Util.hasText(c.getEmbossname())){
//                        card.setCardholdername(MiddlewareVCNWebServiceHandler.TranslateToApp(c.getEmbossname(), false));
//                    }
//
//                    cards.add(card);
//                }
//
//                wsmodel.setCardlist(cards);
//                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
//                return wsmodel;
//            }
//            else
//            {
//                logger.error("No VCN Card found in DB for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
//                return wsmodel;
//            }
//        }
//        catch (Exception e)
//        {
//            logger.error("Exception caught while Executing GetVCNGiftCards, rejecting...");
//            logger.error(WebServiceUtil.getStrException(e));
//            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
//            return wsmodel;
//        }
//        finally {
//            dbQuery = null;
//            params = null;
//            dbcards = null;
//        }
//    }

//    public static AppWsEntity ExecuteBuyVCNGiftCardRequest(AppWsEntity wsmodel)
//    {
//        String dbQuery;
//        Map<String, Object> params;
//        MWCustVCNCards dbcard;
//        MWCustomer destcust = null;
//        try{
//            MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//
//            if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
//                logger.info("Approved Response received for [" + wsmodel.getServicename() + "], savinf card...");
//
//                if(wsmodel.getCard() != null){
//
//                    if(Util.hasText(wsmodel.getCard().getApplicationnumber())
//                            && Util.hasText(wsmodel.getCard().getProgramcode())
//                            && Util.hasText(wsmodel.getCard().getDeviceindicator())
//                            && Util.hasText(wsmodel.getCard().getCardpackid())
//                            && Util.hasText(wsmodel.getCard().getDevicenumber())
//                            && Util.hasText(wsmodel.getCard().getRegisteredmobileno())
//                            && Util.hasText(wsmodel.getCard().getEmbossname())){
//
//                        wsmodel.getCard().setNumber(WSEncryptionUtil.encryptDBVirtualCardDetails(wsmodel.getCard().getCardpackid()));
//                        wsmodel.getCard().setEmbossname(WSEncryptionUtil.encryptDBVirtualCardDetails(wsmodel.getCard().getEmbossname()));
//                        wsmodel.getCard().setCardpackid(wsmodel.getCard().getNumber());
//                        wsmodel.getCard().setDevicenumber(WSEncryptionUtil.encryptDBVirtualCardDetails(wsmodel.getCard().getDevicenumber()));
//
//
//                        //Raza check for Duplicate...
//
//                        logger.info("Getting and verifying destination Mobile Number....");
//                        dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.cardpackid= :CRD ";
//                        params = new HashMap<String, Object>();
//                        params.put("CRD", wsmodel.getCard().getCardpackid());
//
//                        dbcard = (MWCustVCNCards) GeneralDao.Instance.findObject(dbQuery, params);
//
//                        if(dbcard == null){
//
//                            dbcard = new MWCustVCNCards();
//                            dbcard.setApplicationnumber(wsmodel.getCard().getApplicationnumber());
//                            dbcard.setProgramcode(wsmodel.getCard().getProgramcode());
//                            dbcard.setDeviceindicator(wsmodel.getCard().getDeviceindicator());
//                            dbcard.setCardpackid(wsmodel.getCard().getCardpackid());
//                            dbcard.setDevicenumber(wsmodel.getCard().getDevicenumber());
//                            dbcard.setRegisteredmobnumber(wsmodel.getCard().getRegisteredmobileno());
//                            dbcard.setEmbossname(wsmodel.getCard().getEmbossname());
//                            dbcard.setType("Gift Card");
//                            dbcard.setStatus("00");
//                            dbcard.setCustomer(NotificationHandler.GetCustomer(wsmodel.getDestmobilenumber()));
//                            dbcard.setGiftedbycustomer(NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
//                            dbcard.setCreatedate(new Date());
//
//                            GeneralDao.Instance.save(dbcard);
//                        }
//                        else{
//                            logger.error("Duplicate Card found in DB against CardPackId [" + wsmodel.getCard().getCardpackid() + "], rejecting...");
//                            wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_CARD_NUMBER);
//                            return wsmodel;
//                        }
//                    }
//                    else{
//                        logger.error("Missing neccessary fields to store Card Object for [" + wsmodel.getServicename() + "], rejecting....");
//                        wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                        return wsmodel;
//                    }
//
//                }
//                else{
//                    logger.error("Card Object not found in [" + wsmodel.getServicename() + "] response, rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.TRANSACTION_REJECTED);
//                }
//            }
//
//            return wsmodel;
//        }
//        finally {
//            dbQuery = null;
//            params = null;
//        }
//
//    }

//    public static AppWsEntity ExecuteGetVCNGiftCardDetailsRequest(AppWsEntity wsmodel)
//    {
//        return MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
//    }

}
