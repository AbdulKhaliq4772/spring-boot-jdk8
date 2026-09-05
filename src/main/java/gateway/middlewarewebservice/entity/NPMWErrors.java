package gateway.middlewarewebservice.entity;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Mati on 01/03/2019.
 */
@XmlRootElement
public class NPMWErrors {

    private String httpStatusCode;

    private String nayapayStatusCode;

    private String error;


    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getNayapayStatusCode() {
        return nayapayStatusCode;
    }

    public void setNayapayStatusCode(String nayapayStatusCode) {
        this.nayapayStatusCode = nayapayStatusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
