package net.corda.samples.supplychain.states;

import net.corda.samples.supplychain.contracts.ShippingRequestStateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(ShippingRequestStateContract.class)
public class ShippingRequestState implements ContractState {
    private UUID shippingId;
    private AnonymousParty pickUpFrom;
    private String deliverTo;
    private AnonymousParty shippper;
    private UUID orderId;

    private String paymentState;
    private List<AbstractParty> participants;

    public ShippingRequestState(UUID shippingId, AnonymousParty pickUpFrom, String deliverTo, AnonymousParty shippper, UUID orderId, String paymentState) {
        this.shippingId = shippingId;
        this.pickUpFrom = pickUpFrom;
        this.deliverTo = deliverTo;
        this.shippper = shippper;
        this.orderId = orderId;
        this.paymentState = paymentState;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(pickUpFrom);
    }

    public AnonymousParty getPickUpFrom() {
        return pickUpFrom;
    }

    public void setPickUpFrom(AnonymousParty pickUpFrom) {
        this.pickUpFrom = pickUpFrom;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(String deliverTo) {
        this.deliverTo = deliverTo;
    }

    public AnonymousParty getShippper() {
        return shippper;
    }

    public void setShippper(AnonymousParty shippper) {
        this.shippper = shippper;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public UUID getShippingId() {
        return shippingId;
    }

    public void setShippingId(UUID shippingId) {
        this.shippingId = shippingId;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }
}