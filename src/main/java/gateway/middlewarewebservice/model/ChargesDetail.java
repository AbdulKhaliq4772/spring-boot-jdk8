package gateway.middlewarewebservice.model;



public class ChargesDetail {

    private String srccharge;

    private String destcharge;

    //private String billertaxfeecharge;
    private String extestimateexchangerate;

    private String extvalidexchangerate;
    private String extvalidcurrencyindicator;
    private String extestimatedreceivecurrency;
    private String extestimatedreceiveamount;
    private String extpayoutcurrency;

    private EXTCharge extcharge;

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

//    public String getBillertaxfeecharge() {
//        return billertaxfeecharge;
//    }
//
//    public void setBillertaxfeecharge(String billertaxfeecharge) {
//        this.billertaxfeecharge = billertaxfeecharge;
//    }

    public EXTCharge getExtcharge() {
        return extcharge;
    }

    public void setExtcharge(EXTCharge extcharge) {
        this.extcharge = extcharge;
    }

    public String getExtestimateexchangerate() {
        return extestimateexchangerate;
    }

    public void setExtestimateexchangerate(String extestimateexchangerate) {
        this.extestimateexchangerate = extestimateexchangerate;
    }

    public String getExtvalidexchangerate() {
        return extvalidexchangerate;
    }

    public void setExtvalidexchangerate(String extvalidexchangerate) {
        this.extvalidexchangerate = extvalidexchangerate;
    }

    public String getExtvalidcurrencyindicator() {
        return extvalidcurrencyindicator;
    }

    public void setExtvalidcurrencyindicator(String extvalidcurrencyindicator) {
        this.extvalidcurrencyindicator = extvalidcurrencyindicator;
    }

    public String getExtestimatedreceivecurrency() {
        return extestimatedreceivecurrency;
    }

    public void setExtestimatedreceivecurrency(String extestimatedreceivecurrency) {
        this.extestimatedreceivecurrency = extestimatedreceivecurrency;
    }

    public String getExtestimatedreceiveamount() {
        return extestimatedreceiveamount;
    }

    public void setExtestimatedreceiveamount(String extestimatedreceiveamount) {
        this.extestimatedreceiveamount = extestimatedreceiveamount;
    }

    public String getExtpayoutcurrency() {
        return extpayoutcurrency;
    }

    public void setExtpayoutcurrency(String extpayoutcurrency) {
        this.extpayoutcurrency = extpayoutcurrency;
    }
}
