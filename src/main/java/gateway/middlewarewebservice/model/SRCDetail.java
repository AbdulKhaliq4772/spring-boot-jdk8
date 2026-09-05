package gateway.middlewarewebservice.model;

public class SRCDetail {

    private String fee;

    private String tax;

    private String sendamount;


    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getSendamount() {
        return sendamount;
    }

    public void setSendamount(String sendamount) {
        this.sendamount = sendamount;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }
}
