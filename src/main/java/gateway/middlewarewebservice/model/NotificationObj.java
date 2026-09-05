package gateway.middlewarewebservice.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import pk.vaulsys.apigateway.util.Util;
import pk.vaulsys.apigateway.util.WebServiceUtil;

import java.util.ArrayList;
import java.util.List;

public class NotificationObj {
    private static final Logger logger = LogManager.getLogger(NotificationObj.class);

    private String category;
    private String title;
    private String notification;
    private String id;
    private String creationdatetime;

    private NotificationQRCode ticketObject;

//    private Map<String, Object> ticketObject;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = Util.hasText(notification) ? notification.replaceAll("<SKIP_FIREBASE>", "").replaceAll("</SKIP_FIREBASE>", "") : notification;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreationdatetime() {
        return creationdatetime;
    }

    public void setCreationdatetime(String creationdatetime) {
        this.creationdatetime = creationdatetime;
    }

    public NotificationQRCode getTicketObject() {
        return ticketObject;
    }

    public void setTicketObject(NotificationQRCode ticketObject) {
        this.ticketObject = ticketObject;
    }

    public void setTicketObjectFromString(String ticketObject) {
        try {
            if (Util.hasText(ticketObject)) {
                this.ticketObject = new NotificationQRCode();
                JSONObject jo = new JSONObject(ticketObject);

                if (jo.has("eventname")) {
                    this.ticketObject.setEventname(jo.getString("eventname"));
                }
                if (jo.has("eventqr")) {
                    Object eventqrValue = jo.get("eventqr");

                    if (eventqrValue instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) eventqrValue;
                        List<String> eventqrList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String qrCode = jsonArray.getString(i);
                            eventqrList.add(qrCode);
                        }
                        this.ticketObject.setEventqr(eventqrList);

                    } else if (eventqrValue instanceof String) {

                        List<String> eventqrList = new ArrayList<>();
                        eventqrList.add((String) eventqrValue);
                        this.ticketObject.setEventqr(eventqrList);

                    } else {
                        this.ticketObject.setEventqr(new ArrayList<>());
                    }
                }
                if (jo.has("date")) {
                    this.ticketObject.setDate(jo.getString("date"));
                }

            }
        } catch (Exception e) {
            logger.error("Exception caught while setting external josn in notification, ignoring...");
            logger.error(WebServiceUtil.getStrException(e));
        }

    }

    public static void main(String[] args){
        String firstname = "Georges";
        String lastname = "Sandja";
        String destfirstname = "Sandja";
        String destlastname = "Georges";
        if(
                (firstname.trim().toLowerCase().equals(destfirstname.trim().toLowerCase())
                        &&
                        lastname.trim().toLowerCase().equals(destlastname.trim().toLowerCase()))
                        ||
                        (firstname.trim().toLowerCase().equals(destlastname.trim().toLowerCase())
                                &&
                                lastname.trim().toLowerCase().equals(destfirstname.trim().toLowerCase()))
        ){
            logger.info("Self Remittance identified, returning static Purpose of Remittance");

            System.out.println("true");

        }
        else{
            System.out.println("false");
        }

    }
}
