package gateway.middlewarewebservice.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pk.vaulsys.apigateway.persistence.IEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Raza on 20-Dec-18.
 */
@Entity
@Table(name = "MW_TRANSACTION_REASONS")
public class MWTransactionReasons implements IEntity<Long>, Cloneable {
    private static final Logger logger = LogManager.getLogger(MWTransactionReasons.class);

    @Id
    private Long id;

    @Column(name = "REASON_CODE")
    private String reasoncode;

    @Column(name = "REASON_NAME")
    private String reasonname;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "FRA_DESCRIPTION")
    private String fradescription;

    @Column(name = "TRANSACTION_TYPE")
    private String transactiontype;

    @Column(name = "TRANSACTION_CODE")
    private String transactioncode;

    @Column(name = "REASON_ENABLED")
    private Boolean reasonenabled =false;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


    public String getReasoncode() {
        return reasoncode;
    }

    public void setReasoncode(String reasoncode) {
        this.reasoncode = reasoncode;
    }

    public String getReasonname() {
        return reasonname;
    }

    public void setReasonname(String reasonname) {
        this.reasonname = reasonname;
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

    public String getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(String transactiontype) {
        this.transactiontype = transactiontype;
    }

    public String getTransactioncode() {
        return transactioncode;
    }

    public void setTransactioncode(String transactioncode) {
        this.transactioncode = transactioncode;
    }

    public Boolean getReasonenabled() {
        return reasonenabled;
    }

    public void setReasonenabled(Boolean reasonenabled) {
        this.reasonenabled = reasonenabled;
    }
}
