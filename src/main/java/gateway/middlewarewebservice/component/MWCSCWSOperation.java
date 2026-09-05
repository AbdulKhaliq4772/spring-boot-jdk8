package gateway.middlewarewebservice.component;

// Added by Affan on 26-July-23

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.CSCGetCardsResp;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WSEncryptionUtil;
import pk.vaulsys.apigateway.util.WebServiceUtil;

import static pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWWSOperation.SendToOpenAPI;

public class MWCSCWSOperation {

    private static final Logger logger = LogManager.getLogger(MWCSCWSOperation.class);

    public static AppWsEntity ExecuteGenerateCSCToken(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            SendToOpenAPI(wsmodel);


            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing GenerateCSCToken, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetBalanceDetails(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String encryptedCardNumber = wsmodel.getCard().getNumber();
            String decryptedCardNumber = WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getNumber());
            if(decryptedCardNumber == null)
            {
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel.getCard().setNumber(decryptedCardNumber); // Decrypting Card Number

            SendToOpenAPI(wsmodel);

            wsmodel.getCard().setNumber(encryptedCardNumber); // Sending encryptedCardNumber

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing GenerateCSCToken, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteGetAmountDue(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String encryptedCardNumber = wsmodel.getCard().getNumber();
            String decryptedCardNumber = WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getNumber());
            if(decryptedCardNumber == null)
            {
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel.getCard().setNumber(decryptedCardNumber); // Decrypting Card Number
            SendToOpenAPI(wsmodel);

            wsmodel.getCard().setNumber(encryptedCardNumber); // Sending encryptedCardNumber

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing GetAmount, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCSCGetCards(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            SendToOpenAPI(wsmodel);

            // Encryption
            if(wsmodel.getCscGetCardsRespList() != null && wsmodel.getCscGetCardsRespList().size() > 0)
            {
                for(CSCGetCardsResp card : wsmodel.getCscGetCardsRespList())
                {
                    if(Util.hasText(card.getCardNumber()))
                    {
                        card.setCardNumber(WSEncryptionUtil.EncryptAppCardDetails(card.getCardNumber()));
                    }
                }
            }

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing CSCGetCardList, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCSCGetTransactionHistory(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String encryptedCardNumber = wsmodel.getCard().getNumber();
            String decryptedCardNumber = WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getNumber());
            if(decryptedCardNumber == null)
            {
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel.getCard().setNumber(decryptedCardNumber); // Decrypting Card Number
            SendToOpenAPI(wsmodel);

            wsmodel.getCard().setNumber(encryptedCardNumber); // Sending encryptedCardNumber

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing CSCGetTransactionHistory, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCSCChangeCardStatus(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String encryptedCardNumber = wsmodel.getCard().getNumber();
            String decryptedCardNumber = WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getNumber());
            if(decryptedCardNumber == null)
            {
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel.getCard().setNumber(decryptedCardNumber); // Decrypting Card Number
            SendToOpenAPI(wsmodel);

            wsmodel.getCard().setNumber(encryptedCardNumber); // Sending encryptedCardNumber

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing CSCChangeCardStatus, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCSCGetClientDetails(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            SendToOpenAPI(wsmodel);

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing CSCGetClientDetails, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

    public static AppWsEntity ExecuteCSCLoadCard(AppWsEntity wsmodel)
    {
        try
        {
            logger.info("Validating Session...");
            if(!MWWSOperation.ValidateUserandAppSession(wsmodel))
            {
                logger.error("Failed to Validate Session, rejecting...");
                return wsmodel;
            }

            String encryptedCardNumber = wsmodel.getCard().getNumber();
            logger.info("Account Number[" + wsmodel.getCard().getAccountnumber() + "]");
            String decryptedCardNumber = WSEncryptionUtil.DecryptAppCardDetails(wsmodel.getCard().getNumber());
            if(decryptedCardNumber == null)
            {
                wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
                return wsmodel;
            }

            wsmodel.getCard().setNumber(decryptedCardNumber); // Decrypting Card Number

            SendToOpenAPI(wsmodel);
            wsmodel.getCard().setNumber(encryptedCardNumber); // Sending encryptedCardNumber

            return wsmodel;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Executing CSCLoadCard, rejecting...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS);
            return wsmodel;
        }
    }

}
