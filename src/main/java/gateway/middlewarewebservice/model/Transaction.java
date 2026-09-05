package gateway.middlewarewebservice.model;

/**
 * Created by RAZA MURTAZA BAIG on 1/27/2018.
 */
public class Transaction {

    private String username;
    private String type;
    private String amount;
    private String transdatetime;
    private String transrefnum;
    private String bankid;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransdatetime() {
        return transdatetime;
    }

    public void setTransdatetime(String transdatetime) {
        this.transdatetime = transdatetime;
    }

    public String getTransrefnum() {
        return transrefnum;
    }

    public void setTransrefnum(String transrefnum) {
        this.transrefnum = transrefnum;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBankid() {
        return bankid;
    }

    public void setBankid(String bankid) {
        this.bankid = bankid;
    }
}
