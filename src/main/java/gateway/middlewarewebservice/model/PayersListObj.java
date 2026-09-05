package gateway.middlewarewebservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayersListObj {

    private String id;

    private String name;

    private String payertype;

    private String currency;

    private List<PayersCreditPartyIDAccpt> beneficiaryoptions;

    private List<PayersAddressDetails> addressrequiredfields;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPayertype() {
        return payertype;
    }

    public void setPayertype(String payertype) {
        this.payertype = payertype;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<PayersCreditPartyIDAccpt> getBeneficiaryoptions() {
        return beneficiaryoptions;
    }

    public void setBeneficiaryoptions(List<PayersCreditPartyIDAccpt> beneficiaryoptions) {
        this.beneficiaryoptions = beneficiaryoptions;
        //option.setRequiredfields(cleanRequiredFields(option.getRequiredfields()));
    }

    public List<PayersAddressDetails> getAddressrequiredfields() {
        return addressrequiredfields;
    }

    public void setAddressrequiredfields(List<PayersAddressDetails> addressrequiredfields) {
        this.addressrequiredfields = addressrequiredfields;
    }
}
