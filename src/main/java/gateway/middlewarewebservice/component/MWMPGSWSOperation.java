package gateway.middlewarewebservice.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.customer.MWCustMPGSCards;
import pk.vaulsys.apigateway.notification.handler.NotificationHandler;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.handler.MiddlewareMPGSWebServiceHandler;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.CardObj;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;

import java.util.*;

/**
 * Created by Raza on 14-Sep-2022.
 */

public class MWMPGSWSOperation {

    private static final Logger logger = LogManager.getLogger(MWMPGSWSOperation.class);
    //private static MWMPGSWSOperation //Instance = null;

//    public static MWMPGSWSOperation getInstance() {
//        if(Instance == null) {
//            Instance= new MWMPGSWSOperation();
//        }
//        return Instance;
//    }

    public MWMPGSWSOperation()
    {}

    public static AppWsEntity ExecuteGetMPGSCardsRequest(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }


            String dbQuery;
            Map<String, Object> params;

            //Verify MobileNumber
            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());

            List<MWCustMPGSCards> dbcards = GeneralDao.Instance.find(dbQuery, params);

            if(dbcards != null && dbcards.size() > 0)
            {
                List<CardObj> cards = new ArrayList<>();

                for(MWCustMPGSCards c : dbcards)
                {
                    CardObj card = new CardObj();
                    card.setId(c.getId().toString()); //Added By Waleed
                    card.setNumber(MiddlewareMPGSWebServiceHandler.MPGSTranslateToApp(c.getCardnumber(),null,true));
                    card.setExpiry(MiddlewareMPGSWebServiceHandler.MPGSTranslateToApp(c.getExpirymonth(),c.getExpiryyear(),false) );
                    card.setCreationdate(WebServiceUtil.TransDateTimeFormat.format(c.getCreatedate()));
                    card.setFundingmethod(c.getFundingmethod());
                    card.setBrand(c.getBrand());
                    card.setScheme(c.getScheme());
                    card.setStatus(c.getStatus());
                    card.setType(c.getType());
                    card.setExpirymonth(MiddlewareMPGSWebServiceHandler.MPGSTranslateToApp(c.getExpirymonth(),null,false));
                    card.setExpiryyear(MiddlewareMPGSWebServiceHandler.MPGSTranslateToApp(c.getExpiryyear(),null,false));
                    if(Util.hasText(c.getCardholdername())){
                        card.setCardholdername(MiddlewareMPGSWebServiceHandler.MPGSTranslateToApp(c.getCardholdername(),null,false));
                    }


                    cards.add(card);
                }

                wsmodel.setCardlist(cards);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
            else
            {
                logger.error("No Active MPGS Card found in DB for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NO_PROFILE_AVAILABLE);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing GetMPGSCardsRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity ExecuteInitMPGSLoadIllicoRequest(AppWsEntity wsmodel)
    {
        try
        {
            if(Util.hasText(wsmodel.getCard().getId())){
                logger.info("id found in card obj for saved card");
                String dbQuery;
                Map<String, Object> params;
                //Verify MobileNumber
                dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID and c.expirymonth= :MON and c.expiryyear= :YER"; //and c.status= :STATUS ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("ID", Long.parseLong(wsmodel.getCard().getId()));
                params.put("MON", wsmodel.getCard().getExpirymonth());
                params.put("YER", wsmodel.getCard().getExpiryyear());
                //params.put("STATUS", "00"); //00 - Enable
                MWCustMPGSCards dbcard = (MWCustMPGSCards) GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null && dbcard.getStatus().equals("00"))
                {
                    wsmodel.getCard().setNumber(WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(dbcard.getCardnumber())));  //Clear Card Number
                    wsmodel.setCardnumber(wsmodel.getCard().getNumber());
                    wsmodel.getCard().setExpirymonth(WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(dbcard.getExpirymonth())));  //Clear Expiry Month
                    wsmodel.getCard().setExpiryyear(WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(dbcard.getExpiryyear())));   //Clear Expiry Year
                    wsmodel.getCard().setExpiry(wsmodel.getCard().getExpirymonth() + wsmodel.getCard().getExpiryyear()); //Clear Expiry
                    wsmodel.setCardexpiry(wsmodel.getCard().getExpirymonth() + wsmodel.getExpiry());
                }else{
                    logger.error("Active Card not found against Id ["+ wsmodel.getCard().getId() +"], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return  wsmodel;
                }
            }
            else{
                logger.info("request forwarding to openapi through non saved card,ignoring...");
            }

            //Verify App Session, Send to OpenAPI..
            MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);

//            if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
//            {
//                //saving Card Data in temporary table..
//                TempMWCustMPGSCards tempcard = new TempMWCustMPGSCards();
//
//
//            }
            //wsmodel.setCardnumber(null);
            //wsmodel.setCardexpiry(null);
            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing InitMPGSLoadIllicoRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity ExecuteMPGSLoadWalletRequest(AppWsEntity wsmodel)
    {
        try
        {
            if(Util.hasText(wsmodel.getCard().getId())){
                logger.info("id found in card obj for saved card");
                String dbQuery;
                Map<String, Object> params;
                //Verify MobileNumber
                dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID and c.expirymonth= :MON and c.expiryyear= :YER"; //and c.status= :STATUS ";
                params = new HashMap<String, Object>();
                params.put("MOBNO", wsmodel.getMobilenumber());
                params.put("ID", Long.parseLong(wsmodel.getCard().getId()));
                params.put("MON", wsmodel.getCard().getExpirymonth());
                params.put("YER", wsmodel.getCard().getExpiryyear());
                //params.put("STATUS", "00"); //00 - Enable
                MWCustMPGSCards dbcard = (MWCustMPGSCards) GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null && dbcard.getStatus().equals("00"))
                {
                    wsmodel.getCard().setNumber(WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(dbcard.getCardnumber())));  //Clear Card Number
                    wsmodel.setCardnumber(wsmodel.getCard().getNumber());
                    wsmodel.getCard().setExpirymonth(WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(dbcard.getExpirymonth())));  //Clear Expiry Month
                    wsmodel.getCard().setExpiryyear(WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(dbcard.getExpiryyear())));   //Clear Expiry Year
                    wsmodel.getCard().setExpiry(wsmodel.getCard().getExpirymonth() + wsmodel.getCard().getExpiryyear()); //Clear Expiry
                    wsmodel.setCardexpiry(wsmodel.getCard().getExpirymonth() + wsmodel.getExpiry());
                }else{
                    logger.error("Active Card not found against Id ["+ wsmodel.getCard().getId() +"], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return  wsmodel;
                }
            }
            else{
                logger.info("request forwarding to openapi through non saved card,ignoring...");
            }

            //Verify App Session, Send to OpenAPI..
            MWWSOperation.ExecuteMWServiceRequest(wsmodel, false);
            //wsmodel.setCardnumber(null);
            //wsmodel.setCardexpiry(null);
            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing MPGSLoadWalletRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity ExecuteEnableMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }


            String dbQuery;
            Map<String, Object> params;
//
//            //Verify MobileNumber
//            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.cardnumber= :CARD and c.expirymonth= :MON and c.expiryyear= :YER "; //and c.status= :STATUS ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//            params.put("CARD", wsmodel.getCard().getNumber());
//            params.put("MON", wsmodel.getCard().getExpiry().substring(0,2));
//            params.put("YER", wsmodel.getCard().getExpiry().substring(2));
//            //params.put("STATUS", "01"); //01 - Disabled


            //Verify MobileNumber
//            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID and c.expirymonth= :MON and c.expiryyear= :YER "; //and c.status= :STATUS ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//            params.put("ID", wsmodel.getCard().getId());
//            params.put("MON", wsmodel.getCard().getExpiry().substring(0,2));
//            params.put("YER", wsmodel.getCard().getExpiry().substring(2));
//            //params.put("STATUS", "01"); //01 - Disabled


            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID and c.expirymonth= :MON and c.expiryyear= :YER "; //and c.status= :STATUS ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());
            params.put("ID", Long.parseLong(wsmodel.getCard().getId()));
            params.put("MON", wsmodel.getCard().getExpirymonth());
            params.put("YER", wsmodel.getCard().getExpiryyear());
            //params.put("STATUS", "01"); //01 - Disabled

//            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID "; //and c.status= :STATUS ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//            params.put("ID", wsmodel.getCard().getId());
            MWCustMPGSCards dbcard = (MWCustMPGSCards) GeneralDao.Instance.findObject(dbQuery, params);

            if(dbcard != null && dbcard.getStatus().equals("01"))
            {
                dbcard.setStatus("00");
                GeneralDao.Instance.saveOrUpdate(dbcard);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
            else
            {
                if(dbcard != null){
                    logger.error("Card not Disabled, cannot enable already enabled card, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.BAD_CARD_STATUS);
                }
                else{
                    logger.error("No Disabled MPGS Card found in DB for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD);
                }

                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing EnableMPGSCardRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity ExecuteDisbaleMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

//            //Verify MobileNumber
//            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.cardnumber= :CARD and c.expirymonth= :MON and c.expiryyear= :YER"; //and c.status= :STATUS ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//            params.put("CARD", wsmodel.getCard().getNumber());
//            params.put("MON", wsmodel.getCard().getExpiry().substring(0,2));
//            params.put("YER", wsmodel.getCard().getExpiry().substring(2));
//            //params.put("STATUS", "00"); //00 - Enable

            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID and c.expirymonth= :MON and c.expiryyear= :YER"; //and c.status= :STATUS ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());
            params.put("ID", Long.parseLong(wsmodel.getCard().getId()));
            params.put("MON", wsmodel.getCard().getExpirymonth());
            params.put("YER", wsmodel.getCard().getExpiryyear());
            //params.put("STATUS", "00"); //00 - Enable
            MWCustMPGSCards dbcard = (MWCustMPGSCards) GeneralDao.Instance.findObject(dbQuery, params);

            if(dbcard != null && dbcard.getStatus().equals("00"))
            {
                dbcard.setStatus("01");
                GeneralDao.Instance.saveOrUpdate(dbcard);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
            else
            {
                if(dbcard != null) {
                    logger.error("Cannot Disbale already disabled card, rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.BAD_CARD_STATUS);
                }
                    else{
                    logger.error("No Active MPGS Card found in DB for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD);
                }

                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing DisbaleMPGSCardRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity ExecuteDeleteMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

//            //Verify MobileNumber
//            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.cardnumber= :CARD and c.expirymonth= :MON and c.expiryyear= :YER ";
//            params = new HashMap<String, Object>();
//            params.put("MOBNO", wsmodel.getMobilenumber());
//            params.put("CARD", wsmodel.getCard().getNumber());
//            params.put("MON", wsmodel.getCard().getExpiry().substring(0,2));
//            params.put("YER", wsmodel.getCard().getExpiry().substring(2));

            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.customer.mobilenumber= :MOBNO and c.id= :ID and c.expirymonth= :MON and c.expiryyear= :YER ";
            params = new HashMap<String, Object>();
            params.put("MOBNO", wsmodel.getMobilenumber());
            params.put("ID", Long.parseLong(wsmodel.getCard().getId()));
            params.put("MON", wsmodel.getCard().getExpirymonth());
            params.put("YER", wsmodel.getCard().getExpiryyear());


            MWCustMPGSCards dbcard = (MWCustMPGSCards) GeneralDao.Instance.findObject(dbQuery, params);

            if(dbcard != null)
            {
                GeneralDao.Instance.delete(dbcard);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
                return wsmodel;
            }
            else
            {
                logger.error("No MPGS Card found in DB for Customer [" + wsmodel.getMobilenumber() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD);
                return wsmodel;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing DeleteMPGSCardRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity ExecuteSaveMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String dbQuery;
            Map<String, Object> params;

            //Verify MobileNumber
            dbQuery = "from " + MWCustMPGSCards.class.getName() + " c where c.cardnumber= :CARD and c.customer.mobilenumber= :MOB ";
            params = new HashMap<String, Object>();
            params.put("CARD", wsmodel.getCard().getNumber());
            params.put("MOB", wsmodel.getMobilenumber());

            MWCustMPGSCards dbcard = (MWCustMPGSCards)GeneralDao.Instance.findObject(dbQuery, params);

            if(dbcard == null)
            {
                dbcard = new MWCustMPGSCards();

                dbcard.setCardnumber(wsmodel.getCard().getNumber());
//              dbcard.setExpirymonth(wsmodel.getCard().getExpiry().substring(0,2));
//              dbcard.setExpiryyear(wsmodel.getCard().getExpiry().substring(2));
                dbcard.setExpirymonth(wsmodel.getCard().getExpirymonth()); //AddedByWaleed
                dbcard.setExpiryyear(wsmodel.getCard().getExpiryyear()); //AddedByWaleed
                dbcard.setCustomer(NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                dbcard.setFundingmethod(wsmodel.getCard().getFundingmethod());
                dbcard.setBrand(wsmodel.getCard().getBrand());
                dbcard.setScheme(wsmodel.getCard().getScheme());
                dbcard.setCreatedate(new Date());
                dbcard.setType(Util.hasText(wsmodel.getCard().getType()) ? wsmodel.getCard().getType() : "CARD");
                dbcard.setStatus("00");
                dbcard.setCardholdername(wsmodel.getCard().getCardholdername());

                GeneralDao.Instance.save(dbcard);
                wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            }
            else
            {
                logger.error("Duplicate Card found in DB, cannot save card, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.DUPLICATE_CARD_NUMBER);
            }
            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing SaveMPGSCardRequest, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }



}
