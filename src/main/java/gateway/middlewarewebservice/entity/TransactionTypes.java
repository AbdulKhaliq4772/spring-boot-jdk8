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
@Table(name = "TRANSACTION_TYPES")
public class TransactionTypes implements IEntity<Long>, Cloneable {
    private static final Logger logger = LogManager.getLogger(TransactionTypes.class);

    @Id
    private Long id;

    @Column(name = "TRANSACTION_TYPE")
    private String transactiontype;

    @Column(name = "FRA_TRANSACTION_TYPE")
    private String fratransactiontype;

    @Column(name = "CREATED_DATE")
    private String createddate;

    @Column(name = "LAST_UPDATE_DATE")
    private String lastupdatedate;

    @Column(name = "ENABLED")
    private Boolean enabled;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


    public String getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(String transactiontype) {
        this.transactiontype = transactiontype;
    }

    public String getCreateddate() {
        return createddate;
    }

    public void setCreateddate(String createddate) {
        this.createddate = createddate;
    }

    public String getLastupdatedate() {
        return lastupdatedate;
    }

    public void setLastupdatedate(String lastupdatedate) {
        this.lastupdatedate = lastupdatedate;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getFratransactiontype() {
        return fratransactiontype;
    }

    public void setFratransactiontype(String fratransactiontype) {
        this.fratransactiontype = fratransactiontype;
    }
}
