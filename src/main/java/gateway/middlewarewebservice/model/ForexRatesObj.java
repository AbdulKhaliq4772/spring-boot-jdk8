package gateway.middlewarewebservice.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ForexRatesObj {

    private String currency;

    private String buyingrate;

    private String sellingrate;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBuyingrate() {
        return buyingrate;
    }

    public void setBuyingrate(String buyingrate) {
        this.buyingrate = buyingrate;
    }

    public String getSellingrate() {
        return sellingrate;
    }

    public void setSellingrate(String sellingrate) {
        this.sellingrate = sellingrate;
    }
}
