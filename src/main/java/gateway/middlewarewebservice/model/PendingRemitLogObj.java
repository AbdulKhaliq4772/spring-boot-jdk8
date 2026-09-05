package gateway.middlewarewebservice.model;

import com.owlike.genson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class PendingRemitLogObj {

    @JsonIgnore
    private Long id;

    private String referencenumber;
    private String amount;
    private String currency;
    private String beneficiarymobile;
    private String sendermobile;
    private String reason;
    private Long createdatetime;
    private Long expiredatetime;
    private Long approvedatetime;
    private Long canceldatetime;
    private Boolean isapproved;
    private Boolean isexpired;
    private Boolean iscanceled;
    private String billerid;
    private String rrn;
    private String exttxnid;
    private String respcode;
    private String respcodedesc;
    private String firstname;
    private String lastname;
    private String payertype;
    private String destcountrycode;
    private String billername;
    private String CustomerId;
    private String expirypolicy;
    private String expiryunit;

    public PendingRemitLogObj()
    {}

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
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

    public String getBeneficiarymobile() {
        return beneficiarymobile;
    }

    public void setBeneficiarymobile(String beneficiarymobile) {
        this.beneficiarymobile = beneficiarymobile;
    }

    public String getSendermobile() {
        return sendermobile;
    }

    public void setSendermobile(String sendermobile) {
        this.sendermobile = sendermobile;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getCreatedatetime() {
        return createdatetime;
    }

    public void setCreatedatetime(Long createdatetime) {
        this.createdatetime = createdatetime;
    }

    public Long getExpiredatetime() {
        return expiredatetime;
    }

    public void setExpiredatetime(Long expiredatetime) {
        this.expiredatetime = expiredatetime;
    }

    public Long getApprovedatetime() {
        return approvedatetime;
    }

    public void setApprovedatetime(Long approvedatetime) {
        this.approvedatetime = approvedatetime;
    }

    public Long getCanceldatetime() {
        return canceldatetime;
    }

    public void setCanceldatetime(Long canceldatetime) {
        this.canceldatetime = canceldatetime;
    }

    public Boolean getIsapproved() {
        return isapproved;
    }

    public void setIsapproved(Boolean isapproved) {
        this.isapproved = isapproved;
    }

    public Boolean getIsexpired() {
        return isexpired;
    }

    public void setIsexpired(Boolean isexpired) {
        this.isexpired = isexpired;
    }

    public Boolean getIscanceled() {
        return iscanceled;
    }

    public void setIscanceled(Boolean iscanceled) {
        this.iscanceled = iscanceled;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }


    public String getBillerid() {
        return billerid;
    }

    public void setBillerid(String billerid) {
        this.billerid = billerid;
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

    public String getPayertype() {
        return payertype;
    }

    public void setPayertype(String payertype) {
        this.payertype = payertype;
    }

    public String getDestcountrycode() {
        return destcountrycode;
    }

    public void setDestcountrycode(String destcountrycode) {
        this.destcountrycode = destcountrycode;
    }

    public String getExpirypolicy() {
        return expirypolicy;
    }

    public void setExpirypolicy(String expirypolicy) {
        this.expirypolicy = expirypolicy;
    }

    public String getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(String customerId) {
        CustomerId = customerId;
    }

    public String getExpiryunit() {
        return expiryunit;
    }

    public void setExpiryunit(String expiryunit) {
        this.expiryunit = expiryunit;
    }

    public String getExttxnid() {
        return exttxnid;
    }

    public void setExttxnid(String exttxnid) {
        this.exttxnid = exttxnid;
    }

    public String getBillername() {
        return billername;
    }

    public void setBillername(String billername) {
        this.billername = billername;
    }
}
