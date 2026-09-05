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
@Table(name = "SUBJECT_LIST")
public class SubjectList implements IEntity<Long>, Cloneable {
    private static final Logger logger = LogManager.getLogger(SubjectList.class);

    @Id
    private Long id;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "FRA_DESCRIPTION")
    private String fradescription;

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


    public Boolean getReasonenabled() {
        return reasonenabled;
    }

    public void setReasonenabled(Boolean reasonenabled) {
        this.reasonenabled = reasonenabled;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
