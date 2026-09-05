package gateway.middlewarewebservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cascade;
import pk.vaulsys.apigateway.persistence.IEntity;

import java.util.List;


@Entity
@Table(name = "BULK_ONBOARD_CUST_SUMMARY")
public class BulkOnBoardingCustomerSummary implements IEntity<Long>, Cloneable {

    @JsonIgnore
    @Id
    @GeneratedValue(generator = "BULKONBOARDCUST_SUMMRY_SEQ-gen")
    @org.hibernate.annotations.GenericGenerator(name = "BULKONBOARDCUST_SUMMRY_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "BULKONBOARDCUST_SUMMRY_ID_SEQ")
            })
    private Long id;

    @Transient
    private String summaryid;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "UPLOADDATETIME")
    private String uploaddatetime;

    @Column(name = "TOTALCOUNT")
    private String totalcount;

    @Column(name = "SUCCESSFULCOUNT")
    private String successfulcount;

    @Column(name = "SUMMARY")
    private String summary;

    @Column(name = "FILENAME")
    private String filename;

    @Column(name = "ORIG_FILENAME")
    private String origfilename;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bulkonboardcustsummary")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL})
    private List<BulkOnBoardingCustomerTransaction> bulkonboardcustlist;

    @JsonIgnore
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


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

    public String getOrigfilename() {
        return origfilename;
    }

    public void setOrigfilename(String origfilename) {
        this.origfilename = origfilename;
    }

    public List<BulkOnBoardingCustomerTransaction> getBulkonboardcustlist() {
        return bulkonboardcustlist;
    }

    public void setBulkonboardcustlist(List<BulkOnBoardingCustomerTransaction> bulkonboardcustlist) {
        this.bulkonboardcustlist = bulkonboardcustlist;
    }
}
