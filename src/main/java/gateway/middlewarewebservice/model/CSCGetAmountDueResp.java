package gateway.middlewarewebservice.model;

// Added by Affan on 26-July-23

public class CSCGetAmountDueResp
{
    private String currency;
    private String paymentDueDate;
    private String amountDue;

    public CSCGetAmountDueResp(){}

    public CSCGetAmountDueResp(String amountDue, String currency, String paymentDueDate) {
        this.amountDue = amountDue;
        this.currency = currency;
        this.paymentDueDate = paymentDueDate;
    }

    public String getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(String amountDue) {
        this.amountDue = amountDue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(String paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }
}
