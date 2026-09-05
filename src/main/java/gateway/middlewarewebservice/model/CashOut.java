package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class CashOut {

    private String authorizationnumber;
    private String cashoutpin;
    private String amount;
    private String currency;
    private String expiry;
    private String reason;
    private String destmobilenumber;
    private String approvalcharges;
    private String cancelcharges;


    //Below Fields are added for EnvoiCash of Merchant Portal
    private String requestdatetime;
    private String canceldatetime;
    private String completedatetime;
    private String iscompleted;
    private String isexpired;
    private String iscanceled;
    private String rrn;


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


    public String getAuthorizationnumber() {
        return authorizationnumber;
    }

    public void setAuthorizationnumber(String authorizationnumber) {
        this.authorizationnumber = authorizationnumber;
    }

    public String getCashoutpin() {
        return cashoutpin;
    }

    public void setCashoutpin(String cashoutpin) {
        this.cashoutpin = cashoutpin;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDestmobilenumber() {
        return destmobilenumber;
    }

    public void setDestmobilenumber(String destmobilenumber) {
        this.destmobilenumber = destmobilenumber;
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

    public String getRequestdatetime() {
        return requestdatetime;
    }

    public void setRequestdatetime(String requestdatetime) {
        this.requestdatetime = requestdatetime;
    }

    public String getCanceldatetime() {
        return canceldatetime;
    }

    public void setCanceldatetime(String canceldatetime) {
        this.canceldatetime = canceldatetime;
    }

    public String getCompletedatetime() {
        return completedatetime;
    }

    public void setCompletedatetime(String completedatetime) {
        this.completedatetime = completedatetime;
    }

    public String getIscompleted() {
        return iscompleted;
    }

    public void setIscompleted(String iscompleted) {
        this.iscompleted = iscompleted;
    }

    public String getIsexpired() {
        return isexpired;
    }

    public void setIsexpired(String isexpired) {
        this.isexpired = isexpired;
    }

    public String getIscanceled() {
        return iscanceled;
    }

    public void setIscanceled(String iscanceled) {
        this.iscanceled = iscanceled;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }
}
