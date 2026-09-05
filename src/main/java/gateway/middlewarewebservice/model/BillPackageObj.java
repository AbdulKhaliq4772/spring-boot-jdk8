package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class BillPackageObj {

    private String packagename;

    private String packageid;

    private String duration;

    private String durationtype;

    private String amount;

    private String currency;

    private String enddate;

    private String consumerno;

    private String contract;

    private String idbase;

    private String acctref;

    private String tokenid;

    private String durationcode;

    private List<BillPackageOptionsObj> options;

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getPackageid() {
        return packageid;
    }

    public void setPackageid(String packageid) {
        this.packageid = packageid;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDurationtype() {
        return durationtype;
    }

    public void setDurationtype(String durationtype) {
        this.durationtype = durationtype;
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

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }


    public String getConsumerno() {
        return consumerno;
    }

    public void setConsumerno(String consumerno) {
        this.consumerno = consumerno;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getIdbase() {
        return idbase;
    }

    public void setIdbase(String idbase) {
        this.idbase = idbase;
    }

    public String getAcctref() {
        return acctref;
    }

    public void setAcctref(String acctref) {
        this.acctref = acctref;
    }

    public String getTokenid() {
        return tokenid;
    }

    public void setTokenid(String tokenid) {
        this.tokenid = tokenid;
    }

    public List<BillPackageOptionsObj> getOptions() {
        return options;
    }

    public void setOptions(List<BillPackageOptionsObj> options) {
        this.options = options;
    }

    public String getDurationcode() {
        return durationcode;
    }

    public void setDurationcode(String durationcode) {
        this.durationcode = durationcode;
    }
}
