package gateway.middlewarewebservice.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Raza on 14-Sep-20.
 */
@XmlRootElement
public class ContactRMSubjectObj {

    private String subject;

    private String description;

    public static final String CUSTOMER_PROFILE = "Customer Profile";
    public static final String BRANCH_TABLE = "Branch Table";
    public static final String GENERICS = "Generics";
    public static final String SUPPORT_EMAIL = "support@illicocash.com";


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
