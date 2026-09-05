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
@Table(name = "MW_BANKCODES")
public class MWBanksDetails implements IEntity<Long>, Cloneable {
    private static final Logger logger = LogManager.getLogger(MWBanksDetails.class);

    @Id
    private Long id;

    @Column(name = "BANK_CODE")
    private String bankcode;

    @Column(name = "NAME")
    private String name;

    @Column(name = "FRA_NAME")
    private String franame;

    @Column(name = "BRANCH_CODE")
    private String branchcode;

    @Column(name = "SWIFT_CODE")
    private String swiftcode;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "STATE")
    private String state;

    @Column(name = "CITY")
    private String city;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATED_DATE")
    private String createddate;

    @Column(name = "LAST_UPDATED_DATE")
    private String lastupdatedate;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "FETCH_ACCOUNT_SUPPORTED")
    private String fetchaccountsupported;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFraname() {
        return franame;
    }

    public void setFraname(String franame) {
        this.franame = franame;
    }

    public String getBranchcode() {
        return branchcode;
    }

    public void setBranchcode(String branchcode) {
        this.branchcode = branchcode;
    }

    public String getSwiftcode() {
        return swiftcode;
    }

    public void setSwiftcode(String swiftcode) {
        this.swiftcode = swiftcode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getFetchaccountsupported() {
        return fetchaccountsupported;
    }

    public void setFetchaccountsupported(String fetchaccountsupported) {
        this.fetchaccountsupported = fetchaccountsupported;
    }
}
