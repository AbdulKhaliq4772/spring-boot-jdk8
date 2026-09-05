package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class AccountLimit {

    private String transaction;

    private String amount;

    private String availlimit;

    private String availlimitfreq;

    private String minamount;

    private String maxamount;

    private String source;

    private String channel;

    private String origin;

    private String dailyamount;

    private String dailytxncount;

    private String yearlyamount;

    private String yearlytxncount;

    private String periodicamount;

    private String periodictxncount;

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAvaillimit() {
        return availlimit;
    }

    public void setAvaillimit(String availlimit) {
        this.availlimit = availlimit;
    }

    public String getAvaillimitfreq() {
        return availlimitfreq;
    }

    public void setAvaillimitfreq(String availlimitfreq) {
        this.availlimitfreq = availlimitfreq;
    }

    public String getMinamount() {
        return minamount;
    }

    public void setMinamount(String minamount) {
        this.minamount = minamount;
    }

    public String getMaxamount() {
        return maxamount;
    }

    public void setMaxamount(String maxamount) {
        this.maxamount = maxamount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDailyamount() {
        return dailyamount;
    }

    public void setDailyamount(String dailyamount) {
        this.dailyamount = dailyamount;
    }

    public String getDailytxncount() {
        return dailytxncount;
    }

    public void setDailytxncount(String dailytxncount) {
        this.dailytxncount = dailytxncount;
    }

    public String getYearlyamount() {
        return yearlyamount;
    }

    public void setYearlyamount(String yearlyamount) {
        this.yearlyamount = yearlyamount;
    }

    public String getYearlytxncount() {
        return yearlytxncount;
    }

    public void setYearlytxncount(String yearlytxncount) {
        this.yearlytxncount = yearlytxncount;
    }

    public String getPeriodicamount() {
        return periodicamount;
    }

    public void setPeriodicamount(String periodicamount) {
        this.periodicamount = periodicamount;
    }

    public String getPeriodictxncount() {
        return periodictxncount;
    }

    public void setPeriodictxncount(String periodictxncount) {
        this.periodictxncount = periodictxncount;
    }
}
