package gateway.middlewarewebservice.model;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
public class MerchantTransaction {

    private String transactionid;
    private String type;
    private String acctid;
    private String acctalias;
    private String srceid;
    private String srcename;
    private String srcenayapayid;
    private String destid;
    private String destname;
    private String destnayapayid;
    private String agentid;
    private String parentid;
    private String refnum;
    private String invoiceid;
    private String amount;
    private String srccharge;
    private String destcharge;
    private String timestamp;
    private String banktransactionid;

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAcctid() {
        return acctid;
    }

    public void setAcctid(String acctid) {
        this.acctid = acctid;
    }

    public String getAcctalias() {
        return acctalias;
    }

    public void setAcctalias(String acctalias) {
        this.acctalias = acctalias;
    }

    public String getSrceid() {
        return srceid;
    }

    public void setSrceid(String srceid) {
        this.srceid = srceid;
    }

    public String getSrcename() {
        return srcename;
    }

    public void setSrcename(String srcename) {
        this.srcename = srcename;
    }

    public String getSrcenayapayid() {
        return srcenayapayid;
    }

    public void setSrcenayapayid(String srcenayapayid) {
        this.srcenayapayid = srcenayapayid;
    }

    public String getDestid() {
        return destid;
    }

    public void setDestid(String destid) {
        this.destid = destid;
    }

    public String getDestname() {
        return destname;
    }

    public void setDestname(String destname) {
        this.destname = destname;
    }

    public String getDestnayapayid() {
        return destnayapayid;
    }

    public void setDestnayapayid(String destnayapayid) {
        this.destnayapayid = destnayapayid;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getRefnum() {
        return refnum;
    }

    public void setRefnum(String refnum) {
        this.refnum = refnum;
    }

    public String getInvoiceid() {
        return invoiceid;
    }

    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSrccharge() {
        return srccharge;
    }

    public void setSrccharge(String srccharge) {
        this.srccharge = srccharge;
    }

    public String getDestcharge() {
        return destcharge;
    }

    public void setDestcharge(String destcharge) {
        this.destcharge = destcharge;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBanktransactionid() {
        return banktransactionid;
    }

    public void setBanktransactionid(String banktransactionid) {
        this.banktransactionid = banktransactionid;
    }
}
