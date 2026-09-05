package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class MerchantDashboardDataObj {
    private String merchantId;
    private String timeperiod;
    private String successfulseries;
    private String successfulamountusd;
    private String successfulamountcdf;
    private String failedseries;
    private String failedamountusd;
    private String failedamountcdf;

    private String successfulcountusd;
    private String successfulcountcdf;

    public MerchantDashboardDataObj() {
    }


    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTimeperiod() {
        return timeperiod;
    }

    public void setTimeperiod(String timeperiod) {
        this.timeperiod = timeperiod;
    }

    public String getSuccessfulseries() {
        return successfulseries;
    }

    public void setSuccessfulseries(String successfulseries) {
        this.successfulseries = successfulseries;
    }

    public String getSuccessfulamountusd() {
        return successfulamountusd;
    }

    public void setSuccessfulamountusd(String successfulamountusd) {
        this.successfulamountusd = successfulamountusd;
    }

    public String getSuccessfulamountcdf() {
        return successfulamountcdf;
    }

    public void setSuccessfulamountcdf(String successfulamountcdf) {
        this.successfulamountcdf = successfulamountcdf;
    }

    public String getFailedseries() {
        return failedseries;
    }

    public void setFailedseries(String failedseries) {
        this.failedseries = failedseries;
    }

    public String getFailedamountusd() {
        return failedamountusd;
    }

    public void setFailedamountusd(String failedamountusd) {
        this.failedamountusd = failedamountusd;
    }

    public String getFailedamountcdf() {
        return failedamountcdf;
    }

    public void setFailedamountcdf(String failedamountcdf) {
        this.failedamountcdf = failedamountcdf;
    }

    public String getSuccessfulcountcdf() {
        return successfulcountcdf;
    }

    public void setSuccessfulcountcdf(String successfulcountcdf) {
        this.successfulcountcdf = successfulcountcdf;
    }

    public String getSuccessfulcountusd() {
        return successfulcountusd;
    }

    public void setSuccessfulcountusd(String successfulcountusd) {
        this.successfulcountusd = successfulcountusd;
    }
}
