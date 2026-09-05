package gateway.middlewarewebservice.model;


import java.time.LocalDateTime;

public class BulkPaymentSummary {
    private String summaryid;
    private String username;
    private String uploaddatetime;
//    private String respcode;
    private String totalcount;
    private String successfulcount;

//    private String failedcount;
    private String summary;

    private String filename;
    private Boolean is_merchant;
    private String origfilename;


    public BulkPaymentSummary() {
    }

    public LocalDateTime getUploaddatetimeDT() {
        if(this.uploaddatetime == null){return null;}
        if(this.uploaddatetime.length()!=0 && this.uploaddatetime.length()==14){
            return LocalDateTime.of(
                    Integer.parseInt(this.uploaddatetime.substring(0,4))
                    , Integer.parseInt(this.uploaddatetime.substring(4,6))
                    , Integer.parseInt(this.uploaddatetime.substring(6,8))
                    , Integer.parseInt(this.uploaddatetime.substring(8,10))
                    , Integer.parseInt(this.uploaddatetime.substring(10,12))
                    , Integer.parseInt(this.uploaddatetime.substring(12,14))
            );
        }

        return null;
    }

//    public EnumCaptions.TransactionResponseCodeEnum getTransactionResponseCodeEnumName() {
//        return EnumCaptions.TransactionResponseCodeEnum.valueOfKey(this.respcode);
////        return transactionResponseCode;
//    }

    public String getSummaryid() {
        return summaryid;
    }

    public void setSummaryid(String summaryid) {
        this.summaryid = summaryid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUploaddatetime() {
        return uploaddatetime;
    }

    public void setUploaddatetime(String uploaddatetime) {
        this.uploaddatetime = uploaddatetime;
    }

//    public String getRespcode() {
//        return respcode;
//    }
//
//    public void setRespcode(String respcode) {
//        this.respcode = respcode;
//    }

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Boolean getIs_merchant() {
        return is_merchant;
    }

    public void setIs_merchant(Boolean is_merchant) {
        this.is_merchant = is_merchant;
    }

    public String getOrigfilename() {
        return origfilename;
    }

    public void setOrigfilename(String origfilename) {
        this.origfilename = origfilename;
    }
}
