package net.corda.samples.supplychain.states;

import net.corda.samples.supplychain.contracts.InvoiceStateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(InvoiceStateContract.class)
public class InvoiceState implements ContractState {

    private Double amount;
    private AnonymousParty sender;
    private AnonymousParty recipient;
    private UUID invoiceID;
    private List<AbstractParty> participants;

    private Double orderValue;

    private UUID orderId;

    public InvoiceState(Double amount, AnonymousParty sender, AnonymousParty recipient, UUID invoiceID, Double orderValue, UUID orderId) {
        this.amount = amount;
        this.sender = sender;
        this.recipient = recipient;
        this.invoiceID = invoiceID;
        this.orderValue = orderValue;
        this.orderId = orderId;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(recipient);
        participants.add(sender);
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public AnonymousParty getSender() {
        return sender;
    }

    public void setSender(AnonymousParty sender) {
        this.sender = sender;
    }

    public AnonymousParty getRecipient() {
        return recipient;
    }

    public void setRecipient(AnonymousParty recipient) {
        this.recipient = recipient;
    }

    public UUID getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(UUID invoiceID) {
        this.invoiceID = invoiceID;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}