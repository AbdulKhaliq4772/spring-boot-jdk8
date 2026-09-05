package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class RequestMoney {

    private String referenceid;
    private String requestername;
    private String requestermobilenumber;
    private String amount;
    private String currency;
    private String expiry;
    private String reason;
    private String ismine;
    private String approvalmobilenumber;
    private String approvalname;
    private String approvalcharges;
    private String cancelcharges;

    public String getReferenceid() {
        return referenceid;
    }

    public void setReferenceid(String referenceid) {
        this.referenceid = referenceid;
    }

    public String getRequestername() {
        return requestername;
    }

    public void setRequestername(String requestername) {
        this.requestername = requestername;
    }

    public String getRequestermobilenumber() {
        return requestermobilenumber;
    }

    public void setRequestermobilenumber(String requestermobilenumber) {
        this.requestermobilenumber = requestermobilenumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIsmine() {
        return ismine;
    }

    public void setIsmine(String ismine) {
        this.ismine = ismine;
    }


    public String getApprovalcharges() {
        return approvalcharges;
    }

    public void setApprovalcharges(String approvalcharges) {
        this.approvalcharges = approvalcharges;
    }

    public String getCancelcharges() {
        return cancelcharges;
    }

    public void setCancelcharges(String cancelcharges) {
        this.cancelcharges = cancelcharges;
    }

    public String getApprovalmobilenumber() {
        return approvalmobilenumber;
    }

    public void setApprovalmobilenumber(String approvalmobilenumber) {
        this.approvalmobilenumber = approvalmobilenumber;
    }

    public String getApprovalname() {
        return approvalname;
    }

    public void setApprovalname(String approvalname) {
        this.approvalname = approvalname;
    }
}
