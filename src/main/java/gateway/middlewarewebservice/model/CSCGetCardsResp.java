package gateway.middlewarewebservice.model;

// Added by Affan on 26-July-23

public class CSCGetCardsResp
{
    private String cardNumber, cardAlias, effectiveDate, expiryDate, cardStatus, cardLevel, cardType, embossLine1, groupNumber, serviceContractId, serviceId, accountNumber;

    public CSCGetCardsResp(){}

    public CSCGetCardsResp(String cardNumber, String cardAlias, String effectiveDate, String expiryDate, String cardStatus, String cardLevel, String cardType, String embossLine1, String groupNumber, String serviceContractId, String serviceId) {
        this.cardNumber = cardNumber;
        this.cardAlias = cardAlias;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.cardStatus = cardStatus;
        this.cardLevel = cardLevel;
        this.cardType = cardType;
        this.embossLine1 = embossLine1;
        this.groupNumber = groupNumber;
        this.serviceContractId = serviceContractId;
        this.serviceId = serviceId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardAlias() {
        return cardAlias;
    }

    public void setCardAlias(String cardAlias) {
        this.cardAlias = cardAlias;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus;
    }

    public String getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(String cardLevel) {
        this.cardLevel = cardLevel;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getEmbossLine1() {
        return embossLine1;
    }

    public void setEmbossLine1(String embossLine1) {
        this.embossLine1 = embossLine1;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getServiceContractId() {
        return serviceContractId;
    }

    public void setServiceContractId(String serviceContractId) {
        this.serviceContractId = serviceContractId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
