package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MPGSCardObj {

    private String type;

    private String lastsessionid;

    private String cardnumber;

    private String token;

    private String status;

    private String acceptedversions;

    private String version;

    private String merchant;

    private String mcc;

    private String orderid;

    private String brand;

    private String scheme;

    private String fundingmethod;

    private String expirymonth;

    private String expiryyear;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastsessionid() {
        return lastsessionid;
    }

    public void setLastsessionid(String lastsessionid) {
        this.lastsessionid = lastsessionid;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAcceptedversions() {
        return acceptedversions;
    }

    public void setAcceptedversions(String acceptedversions) {
        this.acceptedversions = acceptedversions;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getFundingmethod() {
        return fundingmethod;
    }

    public void setFundingmethod(String fundingmethod) {
        this.fundingmethod = fundingmethod;
    }

    public String getExpirymonth() {
        return expirymonth;
    }

    public void setExpirymonth(String expirymonth) {
        this.expirymonth = expirymonth;
    }

    public String getExpiryyear() {
        return expiryyear;
    }

    public void setExpiryyear(String expiryyear) {
        this.expiryyear = expiryyear;
    }
}
