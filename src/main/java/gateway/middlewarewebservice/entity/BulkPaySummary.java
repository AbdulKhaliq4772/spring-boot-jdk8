package gateway.middlewarewebservice.entity;


import com.owlike.genson.annotation.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import pk.vaulsys.apigateway.persistence.IEntity;
import pk.vaulsys.apigateway.util.Util;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "BULK_PAY_SUMMARY")
public class BulkPaySummary implements IEntity<Long>, Cloneable {

    @JsonIgnore
    @Id
    @GeneratedValue(generator = "BULK_PAY_SUMMRY_ID_SEQ-gen")
    @GenericGenerator(name = "BULK_PAY_SUMMRY_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "increment_size", value = "1"),
                    @Parameter(name = "sequence_name", value = "BULK_PAY_SUMMRY_ID_SEQ")
            })
    private Long id;

    @Transient
    private String summaryId;

    @Column(name = "FILENAME")
    private String fileName;

    @Column(name = "OPERATION")
    private String operation;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "UPLOADDATETIME")
    private Date uploadDatetime;

    @Column(name = "TOTALCOUNT")
    private String totalCount;

    @Column(name = "SUCCESSFULCOUNT")
    private String successfulCount;

    @Column(name = "SUMMARY")
    private String summary;

    @Column(name = "ORIG_FILENAME")
    private String origFileName;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bulkPaySummary")
    @Cascade(value = {CascadeType.ALL})
    private List<BulkPayTransaction> bulktranlist;

    @JsonIgnore
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getUploadDatetime() {
        return uploadDatetime;
    }

    public void setUploadDatetime(Date uploadDatetime) {
        this.uploadDatetime = uploadDatetime;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getSuccessfulCount() {
        return successfulCount;
    }

    public void setSuccessfulCount(String successfulCount) {
        this.successfulCount = successfulCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @JsonIgnore
    public List<BulkPayTransaction> getBulkTranList() {
        return bulktranlist;
    }

    public void setBulkTranList(List<BulkPayTransaction> bulktranlist) {
        this.bulktranlist = bulktranlist;
    }

    public String getSummaryId() {
        if (!Util.hasText(summaryId)) {
            this.summaryId = this.getId() + "";
        }
        return this.summaryId;
    }

    public void setSummaryId(String summaryId) {
        this.summaryId = summaryId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOrigFileName() {
        return origFileName;
    }

    public void setOrigFileName(String origFilename) {
        this.origFileName = origFilename;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
