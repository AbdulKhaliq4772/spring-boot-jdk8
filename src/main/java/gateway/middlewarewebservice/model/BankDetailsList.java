package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 14-Sep-20.
 */
@XmlRootElement
public class BankDetailsList {

    private Long id;

    private String bankcode;

    private String name;

    private String franame;

    private String branchcode;

    private String swiftcode;

    private String currency;

    private String state;

    private String city;

    private String country;

    private String description;

    private Boolean enabled;

    private String fetchaccountsupported;

    public Long getId() {
        return id;
    }

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
