package gateway.middlewarewebservice.entity;


import com.owlike.genson.annotation.JsonIgnore;
import pk.vaulsys.apigateway.persistence.IEntity;

@Entity
@Table(name = "BULK_PAY_TRANSACTIONS")
public class BulkPayTransaction implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator = "BULK_PAY_TRAN_ID_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "BULK_PAY_TRAN_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "BULK_PAY_TRAN_ID_SEQ")
            })
    private Long id;

    @Transient
    private String bulkpaymentsummaryid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BULK_PAY_SUMMARY", referencedColumnName = "ID")
    private BulkPaySummary bulkPaySummary;

    @Column(name = "STAN")
    private String stan;

    @Column(name = "RRN")
    private String rrn;

    @Column(name = "TRANSDATETIME")
    private String transdatetime;

    @Column(name = "RESPCODE")
    private String respcode;

    @Column(name = "AMOUNT_TRAN")
    private String amounttransaction;

    @Column(name = "TRAN_CURRENCY")
    private String trancurrency;

    @Column(name = "SERVICENAME")
    private String servicename = null;

    @Column(name = "TXN_REF_NUM")
    private String txnrefnum = null;

    @Column(name = "ORIG_DATA_ELEMENT")
    private String origdataelement = null;

    @Column(name = "REASON")
    private String reason = null;

    @Column(name = "MOBILE_NUMBER")
    private String mobilenumber = null;

    @Column(name = "AGENT_ID")
    private String agentid = null;

    @Column(name = "MERCHANT_ID")
    private String merchantid = null;

    @Column(name = "SDC_ID")
    private String sdcid = null;

    @Column(name = "CUSTOMER_TYPE")
    private String customertype = null;

    @Column(name = "TYPE_FILTER")
    private String typefilter = null;

    @Column(name = "ADVANCE_FLAG")
    private String advanceflag = null;

    @Column(name = "DEST_MOBILENUMBER")
    private String destmobilenumber = null;

    @Column(name = "DEST_MERCHANTID")
    private String destmerchantid = null;

    @Column(name = "DEST_AGENTID")
    private String destagentid = null;

    @Column(name = "DEST_SDCID")
    private String destsdcid = null;

    @Column(name = "ACCOUNT_NUMBER")
    private String accountnumber = null;

    @Column(name = "DEST_ACCOUNT_NUMBER")
    private String destaccountnumber = null;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public BulkPaySummary getBulkPaySummary() {
        return bulkPaySummary;
    }

    public void setBulkPaySummary(BulkPaySummary bulkPaySummary) {
        this.bulkPaySummary = bulkPaySummary;
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

    public String getAmounttransaction() {
        return amounttransaction;
    }

    public void setAmounttransaction(String amounttransaction) {
        this.amounttransaction = amounttransaction;
    }

    public String getTrancurrency() {
        return trancurrency;
    }

    public void setTrancurrency(String trancurrency) {
        this.trancurrency = trancurrency;
    }

    public String getBulkpaymentsummaryid() {
        return bulkpaymentsummaryid;
    }

    public void setBulkpaymentsummaryid(String bulkpaymentsummaryid) {
        this.bulkpaymentsummaryid = bulkpaymentsummaryid;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getTxnrefnum() {
        return txnrefnum;
    }

    public void setTxnrefnum(String txnrefnum) {
        this.txnrefnum = txnrefnum;
    }

    public String getOrigdataelement() {
        return origdataelement;
    }

    public void setOrigdataelement(String origdataelement) {
        this.origdataelement = origdataelement;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
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

    public String getSdcid() {
        return sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getCustomertype() {
        return customertype;
    }

    public void setCustomertype(String customertype) {
        this.customertype = customertype;
    }

    public String getTypefilter() {
        return typefilter;
    }

    public void setTypefilter(String typefilter) {
        this.typefilter = typefilter;
    }

    public String getAdvanceflag() {
        return advanceflag;
    }

    public void setAdvanceflag(String advanceflag) {
        this.advanceflag = advanceflag;
    }

    public String getDestmobilenumber() {
        return destmobilenumber;
    }

    public void setDestmobilenumber(String destmobilenumber) {
        this.destmobilenumber = destmobilenumber;
    }

    public String getDestmerchantid() {
        return destmerchantid;
    }

    public void setDestmerchantid(String destmerchantid) {
        this.destmerchantid = destmerchantid;
    }

    public String getDestagentid() {
        return destagentid;
    }

    public void setDestagentid(String destagentid) {
        this.destagentid = destagentid;
    }

    public String getDestsdcid() {
        return destsdcid;
    }

    public void setDestsdcid(String destsdcid) {
        this.destsdcid = destsdcid;
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
}
