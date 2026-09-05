package gateway.middlewarewebservice.entity;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NPMWResponseData {

    private String nayapayId;

    private String userId;

    public String getNayapayId() {
        return nayapayId;
    }

    public void setNayapayId(String nayapayId) {
        this.nayapayId = nayapayId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
