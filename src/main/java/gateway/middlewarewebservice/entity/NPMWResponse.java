package gateway.middlewarewebservice.entity;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NPMWResponse {

        private String mobileNumber;

        private Boolean success;

        //private NPMWErrors errors;

        private NPMWResponseData data;


    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }



    public NPMWResponseData getData() {
        return data;
    }

    public void setData(NPMWResponseData data) {
        this.data = data;
    }

//    public NPMWErrors getErrors() {
//        return errors;
//    }
//
//    public void setErrors(NPMWErrors errors) {
//        this.errors = errors;
//    }
}
