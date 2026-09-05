package gateway.middlewarewebservice.model;



import java.time.LocalDateTime;

public class BulkPaymentTransaction {
    private String id;
    private String bulkpaymentsummaryid;
    private String stan;
    private String rrn;
    private String transdatetime;
    private String respcode;

    private String towallet;
    private String amounttransaction;
    private String currency;
    private String cashoutpin;
    private String referencenumber;
    private String servicename;


    public BulkPaymentTransaction() {
    }


    public LocalDateTime getTransactiondatetimeDT() {
        if(this.transdatetime == null){return null;}
        if(this.transdatetime.length()!=0 && this.transdatetime.length()==14){
            return LocalDateTime.of(
                    Integer.parseInt(this.transdatetime.substring(0,4))
                    , Integer.parseInt(this.transdatetime.substring(4,6))
                    , Integer.parseInt(this.transdatetime.substring(6,8))
                    , Integer.parseInt(this.transdatetime.substring(8,10))
                    , Integer.parseInt(this.transdatetime.substring(10,12))
                    , Integer.parseInt(this.transdatetime.substring(12,14))
            );
        }

        return null;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBulkpaymentsummaryid() {
        return bulkpaymentsummaryid;
    }

    public void setBulkpaymentsummaryid(String bulkpaymentsummaryid) {
        this.bulkpaymentsummaryid = bulkpaymentsummaryid;
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

    public String getTowallet() {
        return towallet;
    }

    public void setTowallet(String towallet) {
        this.towallet = towallet;
    }

    public String getAmounttransaction() {
        return amounttransaction;
    }

    public void setAmounttransaction(String amounttransaction) {
        this.amounttransaction = amounttransaction;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCashoutpin() {
        return cashoutpin;
    }

    public void setCashoutpin(String cashoutpin) {
        this.cashoutpin = cashoutpin;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }
}
