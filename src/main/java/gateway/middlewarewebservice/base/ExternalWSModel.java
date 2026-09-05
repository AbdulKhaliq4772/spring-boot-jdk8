package gateway.middlewarewebservice.base;

import pk.vaulsys.apigateway.protocols.webservice.middlewarewebservice.model.NotificationQRCode;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExternalWSModel {

    private String msisdn;

    private String deliverymode;

    private String category;

    private NotificationQRCode ticket;

    private String referencenumber;

    private String datetime;

    private String respcode;

    private String respcodedesc;

    private String incomingip;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getDeliverymode() {
        return deliverymode;
    }

    public void setDeliverymode(String deliverymode) {
        this.deliverymode = deliverymode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public NotificationQRCode getTicket() {
        return ticket;
    }

    public void setTicket(NotificationQRCode ticket) {
        this.ticket = ticket;
    }


    public String getReferencenumber() {
        return referencenumber;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getRespcode() {
        return respcode;
    }

    public void setRespcode(String respcode) {
        this.respcode = respcode;
    }

    public String getRespcodedesc() {
        return respcodedesc;
    }

    public void setRespcodedesc(String respcodedesc) {
        this.respcodedesc = respcodedesc;
    }

    public String getIncomingip() {
        return incomingip;
    }

    public void setIncomingip(String incomingip) {
        this.incomingip = incomingip;
    }
}
