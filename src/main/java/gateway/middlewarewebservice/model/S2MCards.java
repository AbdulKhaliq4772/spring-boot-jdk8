package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Raza on 14-Sep-20.
 */
@XmlRootElement
public class S2MCards {

    private String accountnumber;
    private String card;
    private String pan;
    private String expiry;
    private String name_on_card;
    private String cardstatus;
    private String cardtype;
    private String cardcurrency;

    private List<TranObj> tranObjList; //Added By Waleed

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getName_on_card() {
        return name_on_card;
    }

    public void setName_on_card(String name_on_card) {
        this.name_on_card = name_on_card;
    }

    public String getCardstatus() {
        return cardstatus;
    }

    public void setCardstatus(String cardstatus) {
        this.cardstatus = cardstatus;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getCardcurrency() {
        return cardcurrency;
    }

    public void setCardcurrency(String cardcurrency) {
        this.cardcurrency = cardcurrency;
    }

    public List<TranObj> getTranObjList() {
        return tranObjList;
    }

    public void setTranObjList(List<TranObj> tranObjList) {
        this.tranObjList = tranObjList;
    }
}
