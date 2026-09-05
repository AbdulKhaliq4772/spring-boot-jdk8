package gateway.middlewarewebservice.model;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
public class UserTransaction {

    private String trantype;
    private String txnRefNumber;
    private String tranDateTime;
    private String amountTran;
    private String respCode;
    private String paymentMethod;
    private String tranCurrency;
    private String agentid;
    private String merchantid;
    private String mobilenumber;
    private String destmobilenumber;
    private String accountnumber;
    private String destaccountnumber;
    private String alias;
    private String username;
    private String consumernumber;
    private String billername;
    private String billid;


    public String getTrantype() {
        return trantype;
    }

    public void setTrantype(String trantype) {
        this.trantype = trantype;
    }

    public String getTxnRefNumber() {
        return txnRefNumber;
    }

    public void setTxnRefNumber(String txnRefNumber) {
        this.txnRefNumber = txnRefNumber;
    }

    public String getTranDateTime() {
        return tranDateTime;
    }

    public void setTranDateTime(String tranDateTime) {
        this.tranDateTime = tranDateTime;
    }

    public String getAmountTran() {
        return amountTran;
    }

    public void setAmountTran(String amountTran) {
        this.amountTran = amountTran;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTranCurrency() {
        return tranCurrency;
    }

    public void setTranCurrency(String tranCurrency) {
        this.tranCurrency = tranCurrency;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getMerchantid() {
        return merchantid;
    }

    public void setMerchantid(String merchantid) {
        this.merchantid = merchantid;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getDestmobilenumber() {
        return destmobilenumber;
    }

    public void setDestmobilenumber(String destmobilenumber) {
        this.destmobilenumber = destmobilenumber;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getDestaccountnumber() {
        return destaccountnumber;
    }

    public void setDestaccountnumber(String destaccountnumber) {
        this.destaccountnumber = destaccountnumber;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConsumernumber() {
        return consumernumber;
    }

    public void setConsumernumber(String consumernumber) {
        this.consumernumber = consumernumber;
    }

    public String getBillername() {
        return billername;
    }

    public void setBillername(String billername) {
        this.billername = billername;
    }

    public String getBillid() {
        return billid;
    }

    public void setBillid(String billid) {
        this.billid = billid;
    }
}
