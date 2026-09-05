package gateway.middlewarewebservice.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.protocols.PaymentSchemes.base.ISOResponseCodes;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.component.MWCSCWSOperation;
import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.entity.AppWsEntity;
import pk.vaulsys.apigateway.util.Util;

// Added by Affan on 26-July-23

public class MiddlewareCSCWebServiceHandler {

    private static final Logger logger = LogManager.getLogger(MiddlewareCSCWebServiceHandler.class);

    public static AppWsEntity processGenerateToken(AppWsEntity wsmodel)
    {
            return MWCSCWSOperation.ExecuteGenerateCSCToken(wsmodel);
    }

    public static AppWsEntity processGetBalanceDetails(AppWsEntity wsmodel)
    {
        if(wsmodel.getCard() == null || !Util.hasText(wsmodel.getCard().getNumber()))
        {
            logger.error("Card Number Required, Rejecting....");
            wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
            return wsmodel;
        }
        return MWCSCWSOperation.ExecuteGetBalanceDetails(wsmodel);
    }

    public static AppWsEntity processGetAmountDue(AppWsEntity wsmodel)
    {
        if(wsmodel.getCard() == null || !Util.hasText(wsmodel.getCard().getNumber()))
        {
            logger.error("Card Number Required, Rejecting....");
            wsmodel.setRespcode(ISOResponseCodes.INVALID_CARD_DATA);
            return wsmodel;
        }

        return MWCSCWSOperation.ExecuteGetAmountDue(wsmodel);
    }

    public static AppWsEntity processCSCGetCard(AppWsEntity wsmodel)
    {
        return MWCSCWSOperation.ExecuteCSCGetCards(wsmodel);
    }

    public static AppWsEntity processCSCGetTransactionHistory(AppWsEntity wsmodel)
    {
        if(wsmodel.getCard() == null || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getStartdate()) || !Util.hasText(wsmodel.getCard().getEnddate()))
        {
            logger.error("CardNumber, StartDate, EndDate is Required, Rejecting....");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        return MWCSCWSOperation.ExecuteCSCGetTransactionHistory(wsmodel);
    }

    public static AppWsEntity processCSCChangeCardStatus(AppWsEntity wsmodel)
    {
        if(wsmodel.getCard() == null || !Util.hasText(wsmodel.getCard().getStatus()))
        {
            logger.error("Card Number, CardStatus is Required, Rejecting....");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        return MWCSCWSOperation.ExecuteCSCChangeCardStatus(wsmodel);
    }

    public static AppWsEntity processCSCGetClientDetails(AppWsEntity wsmodel)
    {
        return MWCSCWSOperation.ExecuteCSCGetClientDetails(wsmodel);
    }

    public static AppWsEntity processCSCLoaCard(AppWsEntity wsmodel)
    {
        if(wsmodel.getCard() == null || !Util.hasText(wsmodel.getCard().getNumber()) || !Util.hasText(wsmodel.getCard().getAccountnumber()) || !Util.hasText(wsmodel.getCard().getExpiry()))
        {
            logger.error("Card Number, CardExpiry, AccountNumber is Required, Rejecting....");
            wsmodel.setRespcode(ISOResponseCodes.FIELD_ERROR);
            return wsmodel;
        }

        return MWCSCWSOperation.ExecuteCSCLoadCard(wsmodel);
    }
}
