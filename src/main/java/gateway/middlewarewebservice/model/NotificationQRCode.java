package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class NotificationQRCode {

    private String eventname;

    private List<String> eventqr;

    private String date;

    public String getEventname() {
        return eventname;
    }

    public void setEventname(String eventname) {
        this.eventname = eventname;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getEventqr() {
        return eventqr;
    }

    public void setEventqr(List<String> eventqr) {
        this.eventqr = eventqr;
    }
}
