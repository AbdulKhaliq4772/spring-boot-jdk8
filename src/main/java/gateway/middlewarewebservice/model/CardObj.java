package gateway.middlewarewebservice.model;

import pk.vaulsys.apigateway.util.Util;

import java.util.List;

public class CardObj {

    private String id;

    private String brand;

    private String color;

    private String expiry;

    private String expirymonth;

    private String expiryyear;

    private String fundingmethod;

    private String number;

    private String scheme;

    private String type;

    private String creationdate;

    private String status;

    private String securitycode;

    private String statusdescription;

    private String cardholdername;




    //For S2M Cards
    private String accountnumber;
    private Long card;
    private String pan;
    //  private String expiry;
    private String name_on_card;
    private String cardstatus;
    private String cardtype;
    private String cardtypenetwork; // Added by Affan on 13-April-23
    private String cardcurrency;
    private String cardcode;

    // Naveed Adding 15-09-2025
    private String clearpan;
    private String programid;
    private String programlabe;
    private String virtual;
    // Naveed Adding 15-09-2025

    //Adding for Filter of Mini Statement
    private String startdate;
    private String enddate;

    private List<TranObj> tranObjList; //Added By Waleed

    //MS-VCN card start
    private String applicationnumber;
    private String programcode;
    private String deviceindicator;
    private String cardpackid;
    private String devicenumber;
    private String registeredmobileno;
    private String embossname;
    private String cvv;
    private String extstatusdesc;
    private String giftedbyname;
    private String giftedbymobile;
    //MS-VCN card end

    // Added by Affan on 26-July-23
    // For CSC Cards Start
    private String currency;
    private String paymentDueDate;
    private String amountDue;
    // For CSC Cards End
    // Added by Affan on 26-July-23

    // Added by Affan on 13-April-23
    public String getCardtypenetwork() {
        return cardtypenetwork;
    }

    public void setCardtypenetwork(String cardtypenetwork) {
        this.cardtypenetwork = cardtypenetwork;
    }
    // Added by Affan on 13-April-23

    // Added by Affan on 26-July-23 Start
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(String paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    public String getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(String amountDue) {
        this.amountDue = amountDue;
    }
    // Added by Affan on 26-July-23 End

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
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

    public String getFundingmethod() {
        return fundingmethod;
    }

    public void setFundingmethod(String fundingmethod) {
        this.fundingmethod = fundingmethod;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if(Util.hasText(status))
        {
            if(status.equals("00")) {
                this.statusdescription = "Enabled";
            }
            else {
                this.statusdescription = "Disabled";
            }
        }
    }

    public String getSecuritycode() {
        return securitycode;
    }

    public void setSecuritycode(String securitycode) {
        this.securitycode = securitycode;
    }

    public String getStatusdescription() {
        return statusdescription;
    }

    public void setStatusdescription(String statusdescription) {
        this.statusdescription = statusdescription;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public Long getCard() {
        return card;
    }

    public void setCard(Long card) {
        this.card = card;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getName_on_card() {
        return name_on_card;
    }

    public void setName_on_card(String name_on_card) {
        this.name_on_card = name_on_card;
    }

    public String getCardstatus() {
        return cardstatus;
    }

    public void setCardstatus(String cardstatus) {
        this.cardstatus = cardstatus;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getCardcurrency() {
        return cardcurrency;
    }

    public void setCardcurrency(String cardcurrency) {
        this.cardcurrency = cardcurrency;
    }

    public List<TranObj> getTranObjList() {
        return tranObjList;
    }

    public void setTranObjList(List<TranObj> tranObjList) {
        this.tranObjList = tranObjList;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getCardcode() {
        return cardcode;
    }

    public void setCardcode(String cardcode) {
        this.cardcode = cardcode;
    }

    public String getCardholdername() {
        return cardholdername;
    }

    public void setCardholdername(String cardholdername) {
        this.cardholdername = cardholdername;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationnumber() {
        return applicationnumber;
    }

    public void setApplicationnumber(String applicationnumber) {
        this.applicationnumber = applicationnumber;
    }

    public String getProgramcode() {
        return programcode;
    }

    public void setProgramcode(String programcode) {
        this.programcode = programcode;
    }

    public String getDeviceindicator() {
        return deviceindicator;
    }

    public void setDeviceindicator(String deviceindicator) {
        this.deviceindicator = deviceindicator;
    }

    public String getCardpackid() {
        return cardpackid;
    }

    public void setCardpackid(String cardpackid) {
        this.cardpackid = cardpackid;
    }

    public String getDevicenumber() {
        return devicenumber;
    }

    public void setDevicenumber(String devicenumber) {
        this.devicenumber = devicenumber;
    }

    public String getRegisteredmobileno() {
        return registeredmobileno;
    }

    public void setRegisteredmobileno(String registeredmobileno) {
        this.registeredmobileno = registeredmobileno;
    }

    public String getEmbossname() {
        return embossname;
    }

    public void setEmbossname(String embossname) {
        this.embossname = embossname;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExtstatusdesc() {
        return extstatusdesc;
    }

    public void setExtstatusdesc(String extstatusdesc) {
        this.extstatusdesc = extstatusdesc;
    }

    public String getGiftedbyname() {
        return giftedbyname;
    }

    public void setGiftedbyname(String giftedbyname) {
        this.giftedbyname = giftedbyname;
    }

    public String getGiftedbymobile() {
        return giftedbymobile;
    }

    public void setGiftedbymobile(String giftedbymobile) {
        this.giftedbymobile = giftedbymobile;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getClearpan() {
        return clearpan;
    }

    public void setClearpan(String clearpan) {

        //Naveed Encrypting
        //WSEncryptionUtil.EncryptAppCardDetails(clearpan);
        this.clearpan = clearpan;
    }

    public String getProgramid() {
        return programid;
    }

    public void setProgramid(String programid) {
        this.programid = programid;
    }

    public String getProgramlabe() {
        return programlabe;
    }

    public void setProgramlabe(String programlabe) {
        this.programlabe = programlabe;
    }

    public String getVirtual() {
        return virtual;
    }

    public void setVirtual(String virtual) {
        this.virtual = virtual;
    }
}
