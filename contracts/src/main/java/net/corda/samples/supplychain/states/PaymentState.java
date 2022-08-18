package net.corda.samples.supplychain.states;

import net.corda.samples.supplychain.contracts.PaymentStateContract;
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
@BelongsToContract(PaymentStateContract.class)
public class PaymentState implements ContractState {
    private UUID paymentId;
    private UUID invoiceId;
    private UUID orderId;
    private Double amount;
    private AnonymousParty sender;
    private AnonymousParty recipient;
    private List<AbstractParty> participants;

    private String paymentState;

    private Double invoiceValue;

    public PaymentState(UUID paymentId, UUID invoiceId, UUID orderId, Double amount, AnonymousParty sender, AnonymousParty recipient, String paymentState, Double invoiceValue) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.amount = amount;
        this.sender = sender;
        this.recipient = recipient;
        this.paymentState = paymentState;
        this.invoiceValue = invoiceValue;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(sender);
        participants.add(recipient);
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

    @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Double getInvoiceValue() {
        return invoiceValue;
    }

    public void setInvoiceValue(Double invoiceValue) {
        this.invoiceValue = invoiceValue;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }
}