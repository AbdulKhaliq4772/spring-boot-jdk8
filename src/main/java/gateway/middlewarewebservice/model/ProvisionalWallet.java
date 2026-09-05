package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 23-Nov-18.
 */
@XmlRootElement
public class ProvisionalWallet {

    private String userid;

    private String cnic;

    private String fullname;

    private String customerpic;

    private String creationdate;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getCustomerpic() {
        return customerpic;
    }

    public void setCustomerpic(String customerpic) {
        this.customerpic = customerpic;
    }

    public String getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }
}
