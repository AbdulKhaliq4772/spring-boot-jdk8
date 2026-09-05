package gateway.middlewarewebservice.model;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AmplDepositTransaction {


    private String branch;

    private String operationcode;

    private String customername;

    private String currencycode;

    private String amount;

    private String mobilenumber;

    private String referencenumber;

    private String debircreditflag;

    private String stan;

    private String rrn;

    private String transdatetime;

    private String respcode;

    private String respcodedesc;



    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getOperationcode() {
        return operationcode;
    }

    public void setOperationcode(String operationcode) {
        this.operationcode = operationcode;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public String getCurrencycode() {
        return currencycode;
    }

    public void setCurrencycode(String currencycode) {
        this.currencycode = currencycode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public String getDebircreditflag() {
        return debircreditflag;
    }

    public void setDebircreditflag(String debircreditflag) {
        this.debircreditflag = debircreditflag;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
    }

    public String getRespcodedesc() {
        return respcodedesc;
    }

    public void setRespcodedesc(String respcodedesc) {
        this.respcodedesc = respcodedesc;
    }

}
