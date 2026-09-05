package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BenefciaryObj {

    private String id;

    private String consumerno;

    private String firstname;

    private String lastname;

    private String utilcompany;

    private String country;

    private String account;

    private String iban;

    private String mobile;

    private String swiftcode;

    private String ifscode;

    private String packagecode;

    private String idtype;

    private String idcode;

    private String countryname;

    private String sortcode;

    private String cardnumber;

    private String creditaccount;

    private String clabe;

    private String cbu;

    private String cbualias;

    private String bikcode;

    private String abaroutingnumber;

    private String bsbnumber;

    private String routingcode;

    private String entityttid;

    private String accounttype;

    private String destaddress;

    private String payerid;

    private String payertype;

    private String branchnumber;

    private String email;

    //Mobile Banking Features  --Start
    private String sourceaccount;

    private String sourcetitle;

    private String beneficiarytype;

    private String beneficiaryname;

    private String destaccounttitle;

    private String destaccountnumber;

    private String transactiontype;

    private String currency;

    private String bankcode;

    private String bankname;

    private String city;

    //Mobile banking Features  --End

    public String getConsumerno() {
        return consumerno;
    }

    public void setConsumerno(String consumerno) {
        this.consumerno = consumerno;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUtilcompany() {
        return utilcompany;
    }

    public void setUtilcompany(String utilcompany) {
        this.utilcompany = utilcompany;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSwiftcode() {
        return swiftcode;
    }

    public void setSwiftcode(String swiftcode) {
        this.swiftcode = swiftcode;
    }

    public String getIfscode() {
        return ifscode;
    }

    public void setIfscode(String ifscode) {
        this.ifscode = ifscode;
    }

    public String getPackagecode() {
        return packagecode;
    }

    public void setPackagecode(String packagecode) {
        this.packagecode = packagecode;
    }

    public String getIdtype() {
        return idtype;
    }

    public void setIdtype(String idtype) {
        this.idtype = idtype;
    }

    public String getIdcode() {
        return idcode;
    }

    public void setIdcode(String idcode) {
        this.idcode = idcode;
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }

    public String getSortcode() {
        return sortcode;
    }

    public void setSortcode(String sortcode) {
        this.sortcode = sortcode;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCreditaccount() {
        return creditaccount;
    }

    public void setCreditaccount(String creditaccount) {
        this.creditaccount = creditaccount;
    }

    public String getClabe() {
        return clabe;
    }

    public void setClabe(String clabe) {
        this.clabe = clabe;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public String getCbualias() {
        return cbualias;
    }

    public void setCbualias(String cbualias) {
        this.cbualias = cbualias;
    }

    public String getBikcode() {
        return bikcode;
    }

    public void setBikcode(String bikcode) {
        this.bikcode = bikcode;
    }

    public String getAbaroutingnumber() {
        return abaroutingnumber;
    }

    public void setAbaroutingnumber(String abaroutingnumber) {
        this.abaroutingnumber = abaroutingnumber;
    }

    public String getBsbnumber() {
        return bsbnumber;
    }

    public void setBsbnumber(String bsbnumber) {
        this.bsbnumber = bsbnumber;
    }

    public String getRoutingcode() {
        return routingcode;
    }

    public void setRoutingcode(String routingcode) {
        this.routingcode = routingcode;
    }

    public String getEntityttid() {
        return entityttid;
    }

    public void setEntityttid(String entityttid) {
        this.entityttid = entityttid;
    }

    public String getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(String accounttype) {
        this.accounttype = accounttype;
    }

    public String getDestaddress() {
        return destaddress;
    }

    public void setDestaddress(String destaddress) {
        this.destaddress = destaddress;
    }

    public String getPayerid() {
        return payerid;
    }

    public void setPayerid(String payerid) {
        this.payerid = payerid;
    }

    public String getPayertype() {
        return payertype;
    }

    public void setPayertype(String payertype) {
        this.payertype = payertype;
    }

    public String getBranchnumber() {
        return branchnumber;
    }

    public void setBranchnumber(String branchnumber) {
        this.branchnumber = branchnumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSourceaccount() {
        return sourceaccount;
    }

    public void setSourceaccount(String sourceaccount) {
        this.sourceaccount = sourceaccount;
    }

    public String getSourcetitle() {
        return sourcetitle;
    }

    public void setSourcetitle(String sourcetitle) {
        this.sourcetitle = sourcetitle;
    }

    public String getBeneficiarytype() {
        return beneficiarytype;
    }

    public void setBeneficiarytype(String beneficiarytype) {
        this.beneficiarytype = beneficiarytype;
    }


    public String getDestaccounttitle() {
        return destaccounttitle;
    }

    public void setDestaccounttitle(String destaccounttitle) {
        this.destaccounttitle = destaccounttitle;
    }

    public String getDestaccountnumber() {
        return destaccountnumber;
    }

    public void setDestaccountnumber(String destaccountnumber) {
        this.destaccountnumber = destaccountnumber;
    }

    public String getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(String transactiontype) {
        this.transactiontype = transactiontype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBeneficiaryname() {
        return beneficiaryname;
    }

    public void setBeneficiaryname(String beneficiaryname) {
        this.beneficiaryname = beneficiaryname;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
