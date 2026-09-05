package gateway.middlewarewebservice.model;

import pk.vaulsys.apigateway.customer.MWCustomer;

public class ExtendedMWCustomer extends MWCustomer {
    private String pindata;
    private String province;
    private String city;
    private String municipality;
    private String secretquestion1;
    private String secretquestion2;
    private String secretquestionanswer1;
    private String secretquestionanswer2;
    private String idexpiry;
    private String identificationno;

    public String getIdexpiry() {
        return idexpiry;
    }

    public void setIdexpiry(String idexpiry) {
        this.idexpiry = idexpiry;
    }

    public String getIdentificationno() {
        return identificationno;
    }

    public void setIdentificationno(String identificationno) {
        this.identificationno = identificationno;
    }

    // Getters and Setters
    public String getPindata() {
        return pindata;
    }

    public void setPindata(String pindata) {
        this.pindata = pindata;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getSecretquestion1() {
        return secretquestion1;
    }

    public void setSecretquestion1(String secretquestion1) {
        this.secretquestion1 = secretquestion1;
    }

    public String getSecretquestion2() {
        return secretquestion2;
    }

    public void setSecretquestion2(String secretquestion2) {
        this.secretquestion2 = secretquestion2;
    }

    public String getSecretquestionanswer1() {
        return secretquestionanswer1;
    }

    public void setSecretquestionanswer1(String secretquestionanswer1) {
        this.secretquestionanswer1 = secretquestionanswer1;
    }

    public String getSecretquestionanswer2() {
        return secretquestionanswer2;
    }

    public void setSecretquestionanswer2(String secretquestionanswer2) {
        this.secretquestionanswer2 = secretquestionanswer2;
    }
}

