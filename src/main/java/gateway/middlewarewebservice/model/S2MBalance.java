package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 14-Sep-20.
 */
@XmlRootElement
public class S2MBalance {

    private String available;
    private String currencycode;


    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getCurrencycode() {
        return currencycode;
    }

    public void setCurrencycode(String currencycode) {
        this.currencycode = currencycode;
    }
}
