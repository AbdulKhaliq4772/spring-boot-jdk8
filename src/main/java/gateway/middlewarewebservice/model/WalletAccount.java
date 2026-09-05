package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class WalletAccount {

    private String accountid;

    private String accountnumber;

    private String bankname;

    private String state;

    private String alias1;

    private String alias2;

    private String alias3;

    private String alias4;

    private String creationdate;

    private String accountbalance;

    private String currency;

    private String status;

    private List<AccountLimit> accountLimitList;


    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }


    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    public String getAccountbalance() {
        return accountbalance;
    }

    public void setAccountbalance(String accountbalance) {
        this.accountbalance = accountbalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<AccountLimit> getAccountLimitList() {
        return accountLimitList;
    }

    public void setAccountLimitList(List<AccountLimit> accountLimitList) {
        this.accountLimitList = accountLimitList;
    }

    public String getAlias1() {
        return alias1;
    }

    public void setAlias1(String alias1) {
        this.alias1 = alias1;
    }

    public String getAlias2() {
        return alias2;
    }

    public void setAlias2(String alias2) {
        this.alias2 = alias2;
    }

    public String getAlias3() {
        return alias3;
    }

    public void setAlias3(String alias3) {
        this.alias3 = alias3;
    }

    public String getAlias4() {
        return alias4;
    }

    public void setAlias4(String alias4) {
        this.alias4 = alias4;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
