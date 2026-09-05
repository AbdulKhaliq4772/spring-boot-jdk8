package gateway.middlewarewebservice.model;

// Added by Affan on 26-July-23

public class CSCLoadCardResp
{
    private String responseCode, responseDescription, authCode, balanceAmount, currencyCode, expiryDate, retrievalReference;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(String balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getRetrievalReference() {
        return retrievalReference;
    }

    public void setRetrievalReference(String retrievalReference) {
        this.retrievalReference = retrievalReference;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CSCLoadCardResp{");
        sb.append("responseCode='").append(responseCode).append('\'');
        sb.append(", responseDescription='").append(responseDescription).append('\'');
        sb.append(", authCode='").append(authCode).append('\'');
        sb.append(", balanceAmount='").append(balanceAmount).append('\'');
        sb.append(", currencyCode='").append(currencyCode).append('\'');
        sb.append(", expiryDate='").append(expiryDate).append('\'');
        sb.append(", retrievalReference='").append(retrievalReference).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
