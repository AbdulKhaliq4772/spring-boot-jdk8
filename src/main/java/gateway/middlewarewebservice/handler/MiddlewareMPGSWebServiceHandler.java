package gateway.middlewarewebservice.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWMPGSWSOperation;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;

import java.util.Base64;

/**
 * Created by Raza on 14-Sep-2022.
 */

public class MiddlewareMPGSWebServiceHandler {
    private static final Logger logger = LogManager.getLogger(MiddlewareMPGSWebServiceHandler.class);

    //private static MiddlewareMPGSWebServiceHandler Instance = null;

//    public static MiddlewareMPGSWebServiceHandler getInstance()
//    {
//        if(Instance == null) {
//            return Instance = new MiddlewareMPGSWebServiceHandler();
//        }
//        return Instance;
//    }

    public MiddlewareMPGSWebServiceHandler()
    {}


    public static AppWsEntity processGetMPGSCardsRequest(AppWsEntity wsmodel)
    {
        try
        {
            return MWMPGSWSOperation.ExecuteGetMPGSCardsRequest(wsmodel);
        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing GetMPGSCardsRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity processInitMPGSLoadWalletRequest(AppWsEntity wsmodel)
    {
        try
        {
            if(wsmodel.getCard() == null ||
                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry())))) { // || !Util.hasText(wsmodel.getCard().getSecuritycode()))) ){
                logger.error("Card data not found or missing mandatory fields from Card Data Object, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            if(!Util.hasText(wsmodel.getTrancurrency()) || (Util.hasText(wsmodel.getTrancurrency()) && !wsmodel.getTrancurrency().equals("USD")))
            {
                logger.error("Invalid TranCurrency [" + wsmodel.getTrancurrency() + "], rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.CURRENCY_NOT_ALLOWED);
                return wsmodel;
            }


//          CardObj cardObj = wsmodel.getCard();
            String cn =   wsmodel.getCard().getNumber();
            String ch =   wsmodel.getCard().getCardholdername();
            String exp =   wsmodel.getCard().getExpiry();
            String mon =   wsmodel.getCard().getExpirymonth();
            String yer =   wsmodel.getCard().getExpiryyear();

            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            if(!MPGSTranslateToServer(wsmodel)){
                logger.error("Card Translation to server failed, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel =  MWMPGSWSOperation.ExecuteInitMPGSLoadIllicoRequest(wsmodel);
            //Setting BackUp of CardObj
            logger.info("Setting up App encrypted card obj");
            wsmodel.getCard().setNumber(cn);
            wsmodel.getCard().setCardholdername(ch);
            wsmodel.getCard().setExpirymonth(mon);
            wsmodel.getCard().setExpiryyear(yer);
            wsmodel.getCard().setExpiry(exp);

            wsmodel.setCardnumber(cn);
            wsmodel.setCardexpiry(exp);



            return wsmodel;
//            return MWMPGSWSOperation.ExecuteInitMPGSLoadIllicoRequest(wsmodel);
        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing InitMPGSLoadWalletRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity processMPGSLoadWalletRequest(AppWsEntity wsmodel)
    {
        try
        {
            if(wsmodel.getCard() == null ||
                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()) || !Util.hasText(wsmodel.getCard().getSecuritycode())))) {
                logger.error("Card data not found or missing mandatory fields from Card Data Object, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            if(!Util.hasText(wsmodel.getOrderid()))
            {
                logger.error("Order Id not found in request, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
                return wsmodel;
            }



//          CardObj cardObj = wsmodel.getCard();
            String cn =   wsmodel.getCard().getNumber();
            String ch =   wsmodel.getCard().getCardholdername();
            String exp =   wsmodel.getCard().getExpiry();
            String mon =   wsmodel.getCard().getExpirymonth();
            String yer =   wsmodel.getCard().getExpiryyear();
            String sec =   wsmodel.getCard().getSecuritycode();

            logger.info("Card Number = " + cn);
            logger.info("Card Holder Name = " + ch);
            logger.info("Expiry = " + exp);
            logger.info("Expiry Month = " + mon);
            logger.info("Expiry Year = " + yer);
            logger.info("Security Code = " + sec);

            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            if(!MPGSTranslateToServer(wsmodel)){
                logger.error("Card Translation to server failed, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel =  MWMPGSWSOperation.ExecuteMPGSLoadWalletRequest(wsmodel);
            //Setting BackUp of CardObj
            logger.info("Setting up App encrypted card obj");
            wsmodel.getCard().setNumber(cn);
            wsmodel.getCard().setCardholdername(ch);
            wsmodel.getCard().setExpirymonth(mon);
            wsmodel.getCard().setExpiryyear(yer);
            wsmodel.getCard().setExpiry(exp);
            wsmodel.getCard().setSecuritycode(sec);

            wsmodel.setCardnumber(cn);
            wsmodel.setCardexpiry(exp);

            return wsmodel;

//          return MWMPGSWSOperation.ExecuteMPGSLoadWalletRequest(wsmodel);
        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing MPGSLoadWalletRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity processEnableMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
              //CommentedByWaleed
//            if(wsmodel.getCard() == null ||
//                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getId()) || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()) || wsmodel.getCard().getExpiry().length() < 4)))
            if(wsmodel.getCard() == null ||
                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getId()) || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()))))
            {
                logger.error("Card Number or Expiry missing or is invalid, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

//          CardObj cardObj = wsmodel.getCard();
            String cn =   wsmodel.getCard().getNumber();
            String ch =   wsmodel.getCard().getCardholdername();
            String exp =   wsmodel.getCard().getExpiry();
            String mon =   wsmodel.getCard().getExpirymonth();
            String yer =   wsmodel.getCard().getExpiryyear();

            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            if(!MPGSTranslateToServer(wsmodel)){
                logger.error("Card Translation to server failed, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel =  MWMPGSWSOperation.ExecuteEnableMPGSCardRequest(wsmodel);
            //Setting BackUp of CardObj
            logger.info("Setting up App encrypted card obj");
            wsmodel.getCard().setNumber(cn);
            wsmodel.getCard().setCardholdername(ch);
            wsmodel.getCard().setExpirymonth(mon);
            wsmodel.getCard().setExpiryyear(yer);
            wsmodel.getCard().setExpiry(exp);

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing EnableMPGSCardRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity processDisbaleMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            //CommentedByWaleed
//           if(wsmodel.getCard() == null ||
//                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getId()) || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()) || wsmodel.getCard().getExpiry().length() < 4)))
            if(wsmodel.getCard() == null ||
                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getId()) || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()))))
            {
                logger.error("Card Number or Expiry missing or is invalid, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }


//          CardObj cardObj = wsmodel.getCard();
            String cn =   wsmodel.getCard().getNumber();
            String ch =   wsmodel.getCard().getCardholdername();
            String exp =   wsmodel.getCard().getExpiry();
            String mon =   wsmodel.getCard().getExpirymonth();
            String yer =   wsmodel.getCard().getExpiryyear();

            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            if(!MPGSTranslateToServer(wsmodel)){
                logger.error("Card Translation to server failed, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel =  MWMPGSWSOperation.ExecuteDisbaleMPGSCardRequest(wsmodel);
            //Setting BackUp of CardObj
            logger.info("Setting up App encrypted card obj");
            wsmodel.getCard().setNumber(cn);
            wsmodel.getCard().setCardholdername(ch);
            wsmodel.getCard().setExpirymonth(mon);
            wsmodel.getCard().setExpiryyear(yer);
            wsmodel.getCard().setExpiry(exp);

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing DisbaleMPGSCardRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity processDeleteMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            //CommentedByWaleed
//           if(wsmodel.getCard() == null ||
//                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getId()) || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()) || wsmodel.getCard().getExpiry().length() < 4)))
            if(wsmodel.getCard() == null ||
                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getId()) || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()))))
            {
                logger.error("Card Number or Expiry missing or is invalid, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

//          CardObj cardObj = wsmodel.getCard();
            String cn =   wsmodel.getCard().getNumber();
            String ch =   wsmodel.getCard().getCardholdername();
            String exp =   wsmodel.getCard().getExpiry();
            String mon =   wsmodel.getCard().getExpirymonth();
            String yer =   wsmodel.getCard().getExpiryyear();

            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            if(!MPGSTranslateToServer(wsmodel)){
                logger.error("Card Translation to server failed, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel =  MWMPGSWSOperation.ExecuteDeleteMPGSCardRequest(wsmodel);
            //Setting BackUp of CardObj
            logger.info("Setting up App encrypted card obj");
            wsmodel.getCard().setNumber(cn);
            wsmodel.getCard().setCardholdername(ch);
            wsmodel.getCard().setExpirymonth(mon);
            wsmodel.getCard().setExpiryyear(yer);
            wsmodel.getCard().setExpiry(exp);

            return wsmodel;

        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing DeleteMPGSCardRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }

    public static AppWsEntity processSaveMPGSCardRequest(AppWsEntity wsmodel)
    {
        try
        {
            //CommentedByWaleed
//          if(wsmodel.getCard() == null ||
//                    (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry()) || wsmodel.getCard().getExpiry().length() < 4) || !Util.hasText(wsmodel.getCard().getBrand()) || !Util.hasText(wsmodel.getCard().getScheme())))
            if(wsmodel.getCard() == null || (wsmodel.getCard() != null && (!Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getExpiry())  || !Util.hasText(wsmodel.getCard().getBrand()) || !Util.hasText(wsmodel.getCard().getScheme()))))
            {
                logger.error("Card Number or Expiry missing or is invalid, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

//            CardObj cardObj = wsmodel.getCard();
              String cn =   wsmodel.getCard().getNumber();
              String ch =   wsmodel.getCard().getCardholdername();
              String exp =   wsmodel.getCard().getExpiry();
              String mon =   wsmodel.getCard().getExpirymonth();
              String yer =   wsmodel.getCard().getExpiryyear();

            //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
            if(!MPGSTranslateToServer(wsmodel)){
                logger.error("Card Translation to server failed, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel = MWMPGSWSOperation.ExecuteSaveMPGSCardRequest(wsmodel);
            //Setting BackUp of CardObj
            logger.info("Setting up App encrypted card obj");
            wsmodel.getCard().setNumber(cn);
            wsmodel.getCard().setCardholdername(ch);
            wsmodel.getCard().setExpirymonth(mon);
            wsmodel.getCard().setExpiryyear(yer);
            wsmodel.getCard().setExpiry(exp);

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while processing SaveMPGSCardRequest");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
        finally {
            //Instance = null;
        }
    }



    public  static Boolean MPGSTranslateToServer(AppWsEntity wsmodel){

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

//   public  static String MPGSTranslateToServer(String cardnumber){
//
//        //TRANSLATE TO SERVER (APP DECRYPT -- SERVER ENCRYPT)
//       String AppDecrypt = WSEncryptionUtil.DecryptAppCardDetails(cardnumber);
//       if(Util.hasText(AppDecrypt)){
//        String ServerEncrypt = WSEncryptionUtil.encryptDBCardDetails(AppDecrypt);
//        if(Util.hasText(ServerEncrypt)){
//            return ServerEncrypt;
//        }
//        else{
//            logger.error("Card Translation to Server failed, rejecting...");
//            return null;
//        }
//
//       }
//       else{
//           logger.error("Card Translation to Server failed, rejecting...");
//           return null;
//       }
//    }

    public  static String MPGSTranslateToApp(String cardfield,String cardfieldconcat, Boolean ismask){

        //TRANSLATE TO APP  (SERVER DECRYPT -- APP ENCRYPT)
        String SeverDecrypt = WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(cardfield));
        if(Util.hasText(cardfieldconcat)){
            SeverDecrypt = SeverDecrypt + WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(cardfieldconcat));
        }
        if(Util.hasText(SeverDecrypt)){
            if(ismask.equals(true)){
                SeverDecrypt = WSEncryptionUtil.maskCardNumber(SeverDecrypt);
            }
            String AppEncrypt = WSEncryptionUtil.EncryptAppCardDetails(SeverDecrypt);
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

    public  static String MPGSTranslateForSmsNotif(String cardfield,String cardfieldconcat, Boolean ismask){

        //TRANSLATE TO APP  (SERVER DECRYPT -- APP ENCRYPT)
        String SeverDecrypt = WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(cardfield));
        if(Util.hasText(cardfieldconcat)){
            SeverDecrypt = SeverDecrypt + WSEncryptionUtil.decryptDBCardDetails(Base64.getDecoder().decode(cardfieldconcat));
        }
        if(Util.hasText(SeverDecrypt)){
            if(ismask.equals(true)){
                SeverDecrypt = WSEncryptionUtil.maskCardNumber(SeverDecrypt);
            }

            return SeverDecrypt;
        }
        else{
            logger.error("Card Translation to App failed, returning null...");
            return null;
        }
    }

}
