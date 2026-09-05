package gateway.middlewarewebservice.model;

// Added by Affan on 26-July-23

public class CSCBalanceResp
{
    private String accountNumber, availableAmount, accountStatus, currencyCode, currentAmount, limitAmount, pendingAuthsAmount, billingLevel, clientLevel;

    public CSCBalanceResp(){}

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(String availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(String currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(String limitAmount) {
        this.limitAmount = limitAmount;
    }

    public String getPendingAuthsAmount() {
        return pendingAuthsAmount;
    }

    public void setPendingAuthsAmount(String pendingAuthsAmount) {
        this.pendingAuthsAmount = pendingAuthsAmount;
    }

    public String getBillingLevel() {
        return billingLevel;
    }

    public void setBillingLevel(String billingLevel) {
        this.billingLevel = billingLevel;
    }

    public String getClientLevel() {
        return clientLevel;
    }

    public void setClientLevel(String clientLevel) {
        this.clientLevel = clientLevel;
    }
}
