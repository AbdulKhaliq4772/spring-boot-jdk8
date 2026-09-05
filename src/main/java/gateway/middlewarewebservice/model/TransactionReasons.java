package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 14-Sep-20.
 */
@XmlRootElement
public class TransactionReasons {

    private String reason;

    private String reasoncode;

    private String description;

    private String fradescription;

//    private String transactiontype;


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFradescription() {
        return fradescription;
    }

    public void setFradescription(String fradescription) {
        this.fradescription = fradescription;
    }

//    public String getTransactiontype() {
//        return transactiontype;
//    }
//
//    public void setTransactiontype(String transactiontype) {
//        this.transactiontype = transactiontype;
//    }

    public String getReasoncode() {
        return reasoncode;
    }

    public void setReasoncode(String reasoncode) {
        this.reasoncode = reasoncode;
    }
}
