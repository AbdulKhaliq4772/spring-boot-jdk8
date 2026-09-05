package gateway.middlewarewebservice.model;


import java.util.List;

public class PayersAddressDetails {

    private List<AddressRequiredFieldsObj> addressdetails;

    public List<AddressRequiredFieldsObj> getAddressdetails() {
        return addressdetails;
    }

    public void setAddressdetails(List<AddressRequiredFieldsObj> addressdetails) {
        this.addressdetails = addressdetails;
    }
}

