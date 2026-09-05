package gateway.middlewarewebservice.model;

// Added by Affan on 26-July-23

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ClearedTransactionList
{
    @JsonProperty("clearedTransactions")
    private List<ClearedTransaction> clearedTransactions;

    @JsonProperty("pendingTransactions")
    private List<PendingTransactions> pendingTransactions;

    public ClearedTransactionList(){}

    public List<ClearedTransaction> getClearedTransactions() {
        return clearedTransactions;
    }

    public void setClearedTransactions(List<ClearedTransaction> clearedTransactions) {
        this.clearedTransactions = clearedTransactions;
    }

    public List<PendingTransactions> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(List<PendingTransactions> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }
}
