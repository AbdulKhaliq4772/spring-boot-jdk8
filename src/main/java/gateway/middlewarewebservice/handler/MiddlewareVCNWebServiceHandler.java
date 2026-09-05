package gateway.middlewarewebservice.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.base.config.MWVirtualCardConfig;
import pk.vaulsys.apigateway.customer.MWCustVCNCards;
import pk.vaulsys.apigateway.notification.handler.NotificationHandler;
import pk.vaulsys.apigateway.persistence.GeneralDao;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ChannelCodes;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.base.msgspecs.component.MessageValidator;
import pk.vaulsys.apigateway.protocols.base.msgspecs.entity.SwitchMsgSpec;
import pk.vaulsys.apigateway.protocols.webservice.base.CustomerType;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWVCNWSOperation;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.CardObj;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;
import pk.vaulsys.apigateway.wfe.GlobalContext;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Raza on 14-Nov-2022.
 */

public class MiddlewareVCNWebServiceHandler {

    private static final Logger logger = LogManager.getLogger(MiddlewareVCNWebServiceHandler.class);

    public static AppWsEntity processGetVCNCardsRequest(AppWsEntity wsmodel, String type)
    {
            return MWVCNWSOperation.ExecuteGetVCNCardsRequest(wsmodel, type);
    }

    public static AppWsEntity processBuyVCNCardRequest(AppWsEntity wsmodel, String type)
    {
        wsmodel.setBillerid("Master Card");
        if(Util.hasText(type) && type.equals("Gift Card")){
            if(!Util.hasText(wsmodel.getDestmobilenumber()) || (Util.hasText(wsmodel.getDestmobilenumber()) && wsmodel.getDestmobilenumber().equals(wsmodel.getMobilenumber()))){
                logger.error("Invalid or no Dest Mobile Number [" + wsmodel.getDestmobilenumber() + "] or Dest Customer Name [" + wsmodel.getDestcustomername() + "] or Dest Email [" + wsmodel.getDestemailaddress() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.MOBILE_NUM_MISMATCH);
                return wsmodel;
            }
            else if(!Util.hasText(wsmodel.getDestcustomername())){
                logger.error("Invalid or no Dest Customer Name [" + wsmodel.getDestcustomername() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_DEST_CUSTOMER_NAME);
                return wsmodel;
            }
            else if(!Util.hasText(wsmodel.getDestemailaddress())){
                logger.error("Invalid or no Dest Email [" + wsmodel.getDestemailaddress() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_DEST_EMAIL);
                return wsmodel;
            }
            else if(!Util.hasText(wsmodel.getDestdateofbirth())){
                logger.error("Invalid or no Dest DateOfBirth [" + wsmodel.getDestdateofbirth() + "], ignoring...");
                //wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_DEST_DOB);
                //return wsmodel;
            }
            else if(Util.hasText(wsmodel.getDestdateofbirth())){

                try {
                    Date date = new SimpleDateFormat("dd-MM-yyyy").parse(wsmodel.getDestdateofbirth());
                    Calendar calendar = GregorianCalendar.getInstance();
                    Integer age = 18;
                    try{
                        SwitchMsgSpec dbspec = MessageValidator.getFieldSpecsOfService("BuyVCNGiftCard","567", GlobalContext.getInstance().getChannelbyId(ChannelCodes.MOBILEAPP));

                        if(dbspec == null){
                            logger.error("Spec not found in DB, using 18 years from code...");
                        }
                        else{
                            age = Integer.parseInt(dbspec.getThreashold());
                        }
                    }
                    catch (Exception e){
                        logger.error("Exception caught while getting Db Spec for DestDob ThreashHold, ignoring...");
                        logger.error(WebServiceUtil.getStrException(e));
                    }


                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - age);

                    if(!calendar.getTime().after(date)){
                        logger.error("Invalid DestDateOfBirth [" + wsmodel.getDestdateofbirth() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_DEST_DOB);
                        return wsmodel;
                    }
                    logger.info("DestDateOfBirth [" + wsmodel.getDestdateofbirth() + "] verified!");

                } catch (Exception e) {
                    logger.error("Exception caught while parsing Dest DateOfBirth [" + wsmodel.getDestdateofbirth() + "], rejecting...");
                    logger.error(WebServiceUtil.getStrException(e));
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_OR_NO_DEST_DOB);
                    return wsmodel;
                }

            }
        }

        //Raza check for allowed Max virtual cards...
        String dbQuery;
        Map<String, Object> params;
        MWVirtualCardConfig dbrecord;
        List<MWCustVCNCards> dbcards;
        try{

            dbQuery = "from " + MWVirtualCardConfig.class.getName() + " c where c.customertype= :CUST and c.cardprovider = :PROV and c.enabled= :ENBL and c.type= :TYPE ";
            params = new HashMap<String, Object>();
            params.put("CUST", CustomerType.CUSTOMER);
            params.put("ENBL", true);
            params.put("PROV", "MasterCard"); //TODO: Raza update later....
            params.put("TYPE", type);


            dbrecord = (MWVirtualCardConfig) GeneralDao.Instance.findObject(dbQuery, params);

            if(dbrecord != null && Util.hasText(dbrecord.getMaxallowedcards())){

                //logger.info("Config Record found in DB! Max [" + dbrecord.getMaxallowedcards() + "]");

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.customer= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));

                dbcards = GeneralDao.Instance.find(dbQuery, params);

                /*if(dbcards != null && dbcards.size() > 0){
                    logger.info("DB Cards found Count [" + dbcards.size() + "]");
                }*/

                if(dbcards != null && dbcards.size() >= Integer.parseInt(dbrecord.getMaxallowedcards())){

                    logger.error("Customer [" + wsmodel.getMobilenumber() + "] max virtual card limit reached, cannot allow [" + wsmodel.getServicename() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.MAX_NUMBEROFREQUEST_ACHEIVED);
                    return wsmodel;
                }

            }

            MWVCNWSOperation.ExecuteBuyVCNCardRequest(wsmodel, type);

            if(wsmodel.getCard() != null){
                if(Util.hasText(wsmodel.getCard().getNumber())){
                    wsmodel.getCard().setNumber(TranslateToApp(wsmodel.getCard().getNumber(), true));
                }
                if(Util.hasText(wsmodel.getCard().getCardholdername())){
                    wsmodel.getCard().setCardholdername(TranslateToApp(wsmodel.getCard().getCardholdername(), false));
                }
                if(Util.hasText(wsmodel.getCard().getDevicenumber())){
                    wsmodel.getCard().setDevicenumber(TranslateToApp(wsmodel.getCard().getDevicenumber(), false));
                }
                if(Util.hasText(wsmodel.getCard().getEmbossname())){
                    wsmodel.getCard().setEmbossname(TranslateToApp(wsmodel.getCard().getEmbossname(), false));
                }
                if(Util.hasText(wsmodel.getCard().getCardpackid())){
                    wsmodel.getCard().setCardpackid(TranslateToApp(wsmodel.getCard().getCardpackid(), true));
                }
                if(Util.hasText(wsmodel.getCard().getCvv())){
                    wsmodel.setDecryptedotp(wsmodel.getCard().getCvv());
                    wsmodel.getCard().setCvv(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCvv()));
                }
            }

            return wsmodel;
        }
        finally {
            dbQuery = null;
            params = null;
            dbrecord = null;
            dbcards = null;
        }
    }

    public static AppWsEntity processUpdateVCNProfileRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;
        List<MWCustVCNCards> listdbcards;
        List<CardObj> cardlist;
        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.customer= :CUST "; //and c.status = :STS "; //Raza updating 16-06-2023
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                //params.put("STS", "00"); //Raza commenting 16-06-2023
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                    MWVCNWSOperation.ExecuteUpdateVCNProfileRequest(wsmodel);

                    if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED))
                    {
                        if(!dbcard.getRegisteredmobnumber().equals(wsmodel.getMobilenumber())){
                            dbcard.setRegisteredmobnumber(wsmodel.getMobilenumber());
                            dbcard.setLastupdatedate(new Date());
                            GeneralDao.Instance.saveOrUpdate(dbcard);
                        }
                    }

                    if(wsmodel.getCard() != null){
                        if(Util.hasText(wsmodel.getCard().getNumber())){
                            wsmodel.getCard().setNumber(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getNumber()));
                        }
                        if(Util.hasText(wsmodel.getCard().getCvv())){
                            wsmodel.getCard().setCvv(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCvv()));
                        }
                        if(Util.hasText(wsmodel.getCard().getCardpackid())){
                            wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCardpackid()));
                        }
                    }

                    return wsmodel;
                }
                else{
                    logger.error("Card not found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.customer= :CUST "; //and c.status = :STS "; //Raza updating 16-06-2023
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));

                listdbcards = GeneralDao.Instance.find(dbQuery, params);

                if(listdbcards != null && listdbcards.size() > 0){
                    cardlist = new ArrayList<>();

                    for(int i=0 ; i < listdbcards.size() ; i++){
                        CardObj co  = new CardObj();
                        co.setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(listdbcards.get(i).getCardpackid())));
                        cardlist.add(co);
                    }
                    wsmodel.setCardlist(cardlist);
                    MWVCNWSOperation.ExecuteUpdateVCNProfileRequest(wsmodel);

                    if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                        for (int i =0 ; i < listdbcards.size() ; i++){

                            if(!wsmodel.getMobilenumber().equals(listdbcards.get(i).getRegisteredmobnumber())){
                                listdbcards.get(i).setRegisteredmobnumber(wsmodel.getMobilenumber());
                                listdbcards.get(i).setLastupdatedate(new Date());
                                GeneralDao.Instance.saveOrUpdate(listdbcards.get(i));
                            }
                        }
                    }

                    if(wsmodel.getCard() != null){
                        if(Util.hasText(wsmodel.getCard().getCardpackid())){
                            wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCardpackid()));
                        }
                    }
                    if(wsmodel.getCardlist().size() > 0){
                        for(int i=0 ; i < wsmodel.getCardlist().size() ; i++){
                            if(Util.hasText(wsmodel.getCardlist().get(i).getCardpackid())) {
                                wsmodel.getCardlist().get(i).setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCardlist().get(i).getCardpackid()));
                            }
                        }
                    }

                    return wsmodel;
                }
                else{
                    logger.error("Card not found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }

            }
        }
        finally {
            dbQuery = null;
            params = null;
        }
    }

    public static AppWsEntity processGetVCNCardDetailsRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;
        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.customer= :CUST "; //and c.status = :STS "; //Raza updating 16-06-2023
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                //params.put("STS", "00"); //Raza commenting 16-06-2023
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.getCard().setCardholdername(TranslateToApp(dbcard.getEmbossname(), false));
                    wsmodel.getCard().setType(dbcard.getType()); // Added by Affan on 20-June-23

                    if(dbcard.getGiftedbycustomer() != null){
                        wsmodel.getCard().setGiftedbyname(dbcard.getGiftedbycustomer().getFirstname() + " " + dbcard.getGiftedbycustomer().getLastname());
                        wsmodel.getCard().setGiftedbymobile(dbcard.getGiftedbycustomer().getMobilenumber());    
                    }

                    MWVCNWSOperation.ExecuteGetVCNCardDetailsRequest(wsmodel);

                    logger.info("Setting card color to: " + dbcard.getColor());
                    wsmodel.getCard().setColor(dbcard.getColor()); // Added by M.UMER on 05/09/2024 FOR COLOR CODES

                    try{
                        logger.info("WSMODEL After going to OPENAPI card color to: " + wsmodel.getCard().getColor());
                    }
                    catch(Exception e){
                        logger.info("Setting card color to: " + e);
                    }

                    if(wsmodel.getCard() != null){
                        if(Util.hasText(wsmodel.getCard().getNumber())){
                            wsmodel.getCard().setNumber(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getNumber()));
                        }
                        if(Util.hasText(wsmodel.getCard().getCvv())){
                            wsmodel.getCard().setCvv(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCvv()));
                        }
                        if(Util.hasText(wsmodel.getCard().getCardpackid())){
                            wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCardpackid()));
                        }
                    }

                    return wsmodel;
                }
                else{

                    logger.info("Getting Card Gifted By....");

                    dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.giftedbycustomer= :CUST "; //and c.status = :STS "; //Raza updating 16-06-2023
                    params = new HashMap<String, Object>();
                    params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                    params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                    dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                    if(dbcard != null){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                        wsmodel.getCard().setCardholdername(TranslateToApp(dbcard.getEmbossname(), false));
                        wsmodel.getCard().setType(dbcard.getType()); // Added by Affan on 20-June-23
                        logger.info("Setting card color to: " + dbcard.getColor());
                        wsmodel.getCard().setColor(dbcard.getColor()); // Added by M.UMER on 05/09/2024 FOR COLOR CODES
                        if(dbcard.getGiftedbycustomer() != null){
                            wsmodel.getCard().setGiftedbyname(dbcard.getGiftedbycustomer().getFirstname() + " " + dbcard.getGiftedbycustomer().getLastname());
                            wsmodel.getCard().setGiftedbymobile(dbcard.getGiftedbycustomer().getMobilenumber());
                        }
                        MWVCNWSOperation.ExecuteGetVCNCardDetailsRequest(wsmodel);

                        if(wsmodel.getCard() != null){
                            if(Util.hasText(wsmodel.getCard().getNumber())){
                                //wsmodel.getCard().setNumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getNumber())));
                                wsmodel.getCard().setNumber(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getNumber()));
                            }
                            /*if(Util.hasText(wsmodel.getCard().getCvv())){
                                wsmodel.getCard().setCvv(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCvv()));
                            }*/
                            if(Util.hasText(wsmodel.getCard().getCardpackid())){
                                wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCardpackid()));
                            }
                        }

                        return wsmodel;
                    }
                    else{
                        logger.error("Card not found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                        return wsmodel;
                    }
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

    public static AppWsEntity processLoadVCNCardRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;

        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.status = :STS and c.customer= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                params.put("STS", "00");
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                    wsmodel.setCardnumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    if(dbcard.getGiftedbycustomer() != null && dbcard.getGiftedbycustomer().getMobilenumber().equals(wsmodel.getMobilenumber())){
                        wsmodel.setDestcustomername(dbcard.getEmbossname());
                        wsmodel.setDestmobilenumber("00" + dbcard.getRegisteredmobnumber().replace("+",""));
                    }
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteLoadVCNCardRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }
                    if(Util.hasText(wsmodel.getCardnumber())){
                        wsmodel.setCardnumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCardnumber())));
                    }

                    return wsmodel;
                }
                else if (dbcard == null) {
                    logger.info("Card not found in DB against Card Id [" + wsmodel.getCard().getId() + "], checking for Gift Card...");

                    dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.status = :STS and c.giftedbycustomer= :CUST ";
                    params = new HashMap<String, Object>();
                    params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                    params.put("STS", "00");
                    params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                    dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                    if(dbcard != null){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                        wsmodel.setCardnumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                        wsmodel.setTypefilter(dbcard.getType());
                        MWVCNWSOperation.ExecuteLoadVCNCardRequest(wsmodel);

                        if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                            wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                        }
                        if(Util.hasText(wsmodel.getCardnumber())){
                            wsmodel.setCardnumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCardnumber())));
                        }

                        return wsmodel;
                    }
                    else{
                        logger.error("InActive or No Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                        wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                        return wsmodel;
                    }
                }
                else{
                    logger.error("InActive Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

    public static AppWsEntity processUnLoadVCNCardRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;

        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.status = :STS and c.customer= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                params.put("STS", "00");
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                    wsmodel.setCardnumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteUnLoadVCNCardRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }
                    if(Util.hasText(wsmodel.getCardnumber())){
                        wsmodel.setCardnumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCardnumber())));
                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Inactive or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

    public static AppWsEntity processEnableVCNCardRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;
        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.status = :STS and c.customer= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                params.put("STS", "01");
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteEnableVCNCardRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }

                    if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                        logger.info("Approved response received, enabling card...");

                        dbcard.setStatus("00");
                        GeneralDao.Instance.saveOrUpdate(dbcard);
                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Inactive or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
        }
    }

    public static AppWsEntity processUpdateVCNCardColor(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;
        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.customer= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
//                params.put("STS", "01");
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());

//                    MWVCNWSOperation.ExecuteEnableVCNCardRequest(wsmodel); M.UMER commenting to not send to OPENAPI

                    dbcard.setColor(wsmodel.getCardcolor()); //M.Umer adding it for Updating Color codes
                    logger.info("Updating Card Color to: " + dbcard.getColor());
                    GeneralDao.Instance.saveOrUpdate(dbcard);
                    wsmodel.setCardcolor(dbcard.getColor());
                    wsmodel.setRespcode(ISOResponseCodes.APPROVED);

                    // M.Umer commenting it for COLOR CODES CR.

//                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
//                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
//                    }
//
//                    if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
//                        logger.info("Approved response received, Changing Card Color to: " + wsmodel.getCardcolor());
//
////                        dbcard.setStatus("00");
//                        dbcard.setColor(wsmodel.getCardcolor()); //M.Umer adding it for Updating Color codes
//                        GeneralDao.Instance.saveOrUpdate(dbcard);
//                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Inactive or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        catch (Exception e){
            logger.info("No Card/s found against mobile number: " + wsmodel.getMobilenumber() + " And Card id: " + wsmodel.getCard().getId() + ", rejecting ....");
            if (NotificationHandler.GetCustomer(wsmodel.getMobilenumber()) == null)
                wsmodel.setRespcode(ISOResponseCodes.CUSTOMER_NOT_FOUND);
            else
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
        }
        finally {
            dbQuery = null;
            params = null;
            return wsmodel;
        }
    }

    public static AppWsEntity processDisableVCNCardRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;
        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.status = :STS and c.customer= :CUST ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                params.put("STS", "00");
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteDisableVCNCardRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }

                    if(wsmodel.getRespcode().equals(ISOResponseCodes.APPROVED)){
                        logger.info("Approved response received, disabling card...");

                        dbcard.setStatus("01");
                        GeneralDao.Instance.saveOrUpdate(dbcard);
                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Active or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
        }
    }

    public static AppWsEntity processGetVCNCardStatementRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;

        if(Util.hasText(wsmodel.getStartdate()) && Util.hasText(wsmodel.getEnddate())){
            if(!Util.isDateValid(wsmodel.getStartdate(), "MMddyyyy") || !Util.isDateValid(wsmodel.getEnddate(), "MMddyyyy")){
                wsmodel.setRespcode(ISOResponseCodes.INVALID_TRANSATION_DATE);
                return wsmodel;
            }
        }

        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.customer= :CUST "; //and c.status = :STS "; //Raza commenting 16-06-2023
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                //params.put("STS", "00"); //Raza commenting 16-06-2023
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                    wsmodel.setCardnumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteGetVCNCardStatementRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }
                    if(Util.hasText(wsmodel.getCardnumber())){
                        wsmodel.setCardnumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCardnumber())));
                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Inactive or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

    public static AppWsEntity processResetVCNCardPinRequest(AppWsEntity wsmodel)
    {
        return MWVCNWSOperation.ExecuteResetVCNCardPinRequest(wsmodel);
    }

    public static AppWsEntity processGetVCNCardBalanceRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;

        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.customer= :CUST "; //and c.status = :STS ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                //params.put("STS", "00");
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                    wsmodel.setCardnumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteGetVCNCardBalanceRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }
                    if(Util.hasText(wsmodel.getCardnumber())){
                        wsmodel.setCardnumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCardnumber())));
                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Inactive or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

    public static AppWsEntity processActivateVCNCardRequest(AppWsEntity wsmodel)
    {
        return MWVCNWSOperation.ExecuteActivateVCNCardRequest(wsmodel);
    }

    public static AppWsEntity processGetVCNCardLimitsRequest(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;

        try{
            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){

                dbQuery = "from " + MWCustVCNCards.class.getName() + " c where c.id= :CRD and c.customer= :CUST "; //and c.status = :STS ";
                params = new HashMap<String, Object>();
                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
                //params.put("STS", "00"); //Raza updating 16-06-2023
                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));

                dbcard = (MWCustVCNCards)GeneralDao.Instance.findObject(dbQuery, params);

                if(dbcard != null){
                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));

                    wsmodel.setCardnumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
                    wsmodel.setTypefilter(dbcard.getType());
                    MWVCNWSOperation.ExecuteGetVCNCardLimitsRequest(wsmodel);

                    if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getCardpackid())){
                        wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCard().getCardpackid())));
                    }
                    if(Util.hasText(wsmodel.getCardnumber())){
                        wsmodel.setCardnumber(WSEncryptionUtil.encryptAppVirtualCardDetails(WSEncryptionUtil.maskCardNumber(wsmodel.getCardnumber())));
                    }

                    return wsmodel;
                }
                else{
                    logger.error("No Inactive or Card found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                    return wsmodel;
                }
            }
            else{
                logger.error("Card Object or Number not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

    public static AppWsEntity processTestVCN(AppWsEntity wsmodel)
    {
        String dbQuery;
        Map<String, Object> params;
        MWCustVCNCards dbcard;

        try{

            dbQuery = "from " + MWCustVCNCards.class.getName() + " ";
            List<MWCustVCNCards> dbcards = GeneralDao.Instance.find(dbQuery);

            if(dbcards != null && dbcards.size() > 0)
            {
                for(MWCustVCNCards card : dbcards){

//                    TempMWCustVCNCards tcard = new TempMWCustVCNCards();
//                    tcard.setId(card.getId());
//                    tcard.setType(card.getType());
//                    tcard.setCustomer(card.getCustomer());
//                    tcard.setApplicationnumber(card.getApplicationnumber());
//                    tcard.setBranchcode(card.getBranchcode());
//                    tcard.setProgramcode(card.getProgramcode());
//                    tcard.setDeviceindicator(card.getDeviceindicator());
//                    tcard.setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(card.getCardpackid())));
//                    tcard.setDevicenumber(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(card.getDevicenumber())));
//                    tcard.setRegisteredmobnumber(card.getRegisteredmobnumber());
//                    tcard.setEmbossname(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(card.getEmbossname())));
//                    tcard.setStatus(card.getStatus());
//                    tcard.setCreatedate(card.getCreatedate());
//                    tcard.setLastupdatedate(card.getLastupdatedate());
//                    tcard.setGiftedbycustomer(card.getGiftedbycustomer());
//                    GeneralDao.Instance.save(tcard);

                }

            }
            else{
                logger.error("No Cards found in DB, ignoring...");
            }

            wsmodel.setRespcode(ISOResponseCodes.APPROVED);
            return wsmodel;
        }
        finally {
            dbQuery = null;
            params = null;
            dbcard = null;
        }
    }

//    public static AppWsEntity processGetVCNGiftCardsRequest(AppWsEntity wsmodel)
//    {
//        return MWVCNWSOperation.ExecuteGetVCNCardsRequest(wsmodel, "Gift Card");
//    }

//    public static AppWsEntity processGetVCNGiftCardDetailsRequest(AppWsEntity wsmodel)
//    {
//        String dbQuery;
//        Map<String, Object> params;
//        MWCustGiftCards dbcard;
//        try{
//            if(wsmodel.getCard() != null && Util.hasText(wsmodel.getCard().getId())){
//
//                dbQuery = "from " + MWCustGiftCards.class.getName() + " c where c.id= :CRD and c.status = :STS and c.customer= :CUST ";
//                params = new HashMap<String, Object>();
//                params.put("CUST", NotificationHandler.GetCustomer(wsmodel.getMobilenumber()));
//                params.put("STS", "00");
//                params.put("CRD", Long.parseLong(wsmodel.getCard().getId()));
//
//                dbcard = (MWCustGiftCards)GeneralDao.Instance.findObject(dbQuery, params);
//
//                if(dbcard != null){
//                    wsmodel.getCard().setCardpackid(WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(dbcard.getCardpackid())));
//                    wsmodel.getCard().setCardholdername(TranslateToApp(dbcard.getEmbossname(), false));
//                    MWVCNWSOperation.ExecuteGetVCNCardDetailsRequest(wsmodel);
//
//                    if(wsmodel.getCard() != null){
//                        if(Util.hasText(wsmodel.getCard().getNumber())){
//                            wsmodel.getCard().setNumber(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getNumber()));
//                        }
//                        if(Util.hasText(wsmodel.getCard().getCvv())){
//                            wsmodel.getCard().setCvv(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCvv()));
//                        }
//                        if(Util.hasText(wsmodel.getCard().getCardpackid())){
//                            wsmodel.getCard().setCardpackid(WSEncryptionUtil.encryptAppVirtualCardDetails(wsmodel.getCard().getCardpackid()));
//                        }
//                    }
//
//                    return wsmodel;
//                }
//                else{
//                    logger.error("Card not found in DB against Card Id [" + wsmodel.getCard().getId() + "], rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
//                    return wsmodel;
//                }
//            }
//            else{
//                logger.error("Card Object or Number not found in request, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
//                return wsmodel;
//            }
//        }
//        finally {
//            dbQuery = null;
//            params = null;
//            dbcard = null;
//        }
//    }






















    public  static String TranslateToApp(String cardfield, Boolean ismask){

        //TRANSLATE TO APP  (SERVER DECRYPT -- APP ENCRYPT)
        String SeverDecrypt = WSEncryptionUtil.decryptDBVirtualCardDetails(Base64.getDecoder().decode(cardfield));

        if(Util.hasText(SeverDecrypt)){
            if(ismask.equals(true)){
                SeverDecrypt = WSEncryptionUtil.maskCardNumber(SeverDecrypt);
            }
            String AppEncrypt = WSEncryptionUtil.encryptAppVirtualCardDetails(SeverDecrypt);
            if(Util.hasText(AppEncrypt)){
                return AppEncrypt;
            }
            else{
                logger.error("Card Translation to App failed, rejecting...");
                return null;
            }
        }
        else{
            logger.error("Card Translation to App failed, rejecting...");
            return null;
        }
    }

    public  static Boolean TranslateToServer(AppWsEntity wsmodel){

        String AppDecryptCardNumber = null;
        String AppDecryptCardHolderName = null;
        String AppDecryptExpiry = null;
        String AppDecryptSecurityCode = null;
        String ServerEncryptForCardNumber = null;
        String ServerEncryptForCardHolderName = null;
        String ServerEncryptForExpiryMonth = null;
        String ServerEncryptForExpiryYear = null;



        try{
            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            AppDecryptCardNumber = (Util.hasText(wsmodel.getCard().getNumber()) ? WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getNumber()) : "");
            AppDecryptCardHolderName = (Util.hasText(wsmodel.getCard().getCardholdername()) ? WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getCardholdername()) : "");
            AppDecryptExpiry = (Util.hasText(wsmodel.getCard().getExpiry()) ? WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getExpiry()) : "");
            AppDecryptSecurityCode = (Util.hasText(wsmodel.getCard().getSecuritycode()) ? WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getSecuritycode()) : "");

            //Clear Security Code
            if(Util.hasText(AppDecryptCardNumber) || Util.hasText(AppDecryptCardHolderName) || Util.hasText(AppDecryptExpiry)){
                ServerEncryptForCardNumber = (Util.hasText(AppDecryptCardNumber) ? WSEncryptionUtil.encryptDBCardDetails(AppDecryptCardNumber) : "");
                ServerEncryptForCardHolderName = (Util.hasText(AppDecryptCardHolderName) ? WSEncryptionUtil.encryptDBCardDetails(AppDecryptCardHolderName) : "");
                ServerEncryptForExpiryMonth = (Util.hasText(AppDecryptExpiry) ? WSEncryptionUtil.encryptDBCardDetails(AppDecryptExpiry.substring(0,2)) : "");
                ServerEncryptForExpiryYear = (Util.hasText(AppDecryptExpiry) ? WSEncryptionUtil.encryptDBCardDetails(AppDecryptExpiry.substring(2)) : "");
                if(Util.hasText(ServerEncryptForCardNumber) || Util.hasText(ServerEncryptForCardHolderName) || Util.hasText(ServerEncryptForExpiryMonth) || Util.hasText(ServerEncryptForExpiryYear)){
                    wsmodel.getCard().setNumber(ServerEncryptForCardNumber);
                    wsmodel.getCard().setCardholdername(ServerEncryptForCardHolderName);
                    wsmodel.getCard().setExpirymonth(ServerEncryptForExpiryMonth);
                    wsmodel.getCard().setExpiryyear(ServerEncryptForExpiryYear);
//              wsmodel.getCard().setExpiry(ServerEncryptForExpiryMonth + ServerEncryptForExpiryYear);
                    if(wsmodel.getServicename().equals("MPGSLoadWallet") || wsmodel.getServicename().equals("InitMPGSLoadWallet")){ //for Init and Load Illico
                        if(!Util.hasText(wsmodel.getCard().getId())){
                            wsmodel.getCard().setNumber(AppDecryptCardNumber);
                            wsmodel.getCard().setExpiry(AppDecryptExpiry);
                            wsmodel.getCard().setExpirymonth(AppDecryptExpiry.substring(0,2));
                            wsmodel.getCard().setExpiryyear(ServerEncryptForExpiryYear.substring(2));
                            wsmodel.getCard().setSecuritycode(AppDecryptSecurityCode);
                        }else{
                            wsmodel.getCard().setSecuritycode(AppDecryptSecurityCode); // Clear Security Code also set for Saved Card
                        }
                    }
                    else if(wsmodel.getServicename().equals("SaveMPGSCard"))
                    {
                        if(!Util.hasText(ServerEncryptForCardNumber) || !Util.hasText(ServerEncryptForExpiryMonth) || !Util.hasText(ServerEncryptForExpiryYear)){
                            logger.error("Card Translation to Server failed i.e. CardNumber["+ServerEncryptForCardNumber+"], ExpiryMonth["+ServerEncryptForExpiryMonth+"], ExpiryYear["+ServerEncryptForExpiryYear+"], rejecting...");
                            return false;
                        }

                    }


                    return true;
                }
                else{
                    logger.error("Card Translation to Server failed, rejecting...");
                    return false;
                }

            }
            else{
                logger.error("Card Translation to Server failed, rejecting...");
                return false;
            }
        }
        finally {
            AppDecryptCardNumber = null;
            AppDecryptCardHolderName = null;
            AppDecryptExpiry = null;
            AppDecryptSecurityCode = null;
            ServerEncryptForCardNumber = null;
            ServerEncryptForCardHolderName = null;
            ServerEncryptForExpiryMonth = null;
            ServerEncryptForExpiryYear = null;
        }



    }
}
