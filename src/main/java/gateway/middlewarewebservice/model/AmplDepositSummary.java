package gateway.middlewarewebservice.model;



import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AmplDepositSummary {

    private String summaryid;

    private String username;

    private String totalcount;

    private String successfulcount;

    private String filename;

    private String uploaddatetime;


    public String getSummaryid() {
        return summaryid;
    }

    public void setSummaryid(String summaryid) {
        this.summaryid = summaryid;
    }

    public String getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(String totalcount) {
        this.totalcount = totalcount;
    }

    public String getSuccessfulcount() {
        return successfulcount;
    }

    public void setSuccessfulcount(String successfulcount) {
        this.successfulcount = successfulcount;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUploaddatetime() {
        return uploaddatetime;
    }

    public void setUploaddatetime(String uploaddatetime) {
        this.uploaddatetime = uploaddatetime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
