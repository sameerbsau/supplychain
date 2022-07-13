package net.corda.samples.supplychain.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.samples.supplychain.contracts.CargoStateContract;
import net.corda.samples.supplychain.contracts.OrderStateContract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@BelongsToContract(OrderStateContract.class)
public class OrderState implements ContractState {

    private UUID orderID;

    private AnonymousParty buyer;

    private AnonymousParty seller;
    private String orderDetails;

    private Double orderValue;
    private Instant createdTime;
    private String orderStatus;

    private List<AbstractParty> participants;


    public OrderState(UUID orderID, AnonymousParty buyer, AnonymousParty seller, String orderDetails, Double orderValue, Instant createdTime, String orderStatus) {
        this.orderID = orderID;
        this.buyer = buyer;
        this.seller = seller;
        this.orderDetails = orderDetails;
        this.orderValue = orderValue;
        this.createdTime = createdTime;
        this.orderStatus = orderStatus;
        this.participants = new ArrayList<AbstractParty>();
        participants.add(buyer);
        participants.add(seller);
    }

    public UUID getOrderID() {
        return orderID;
    }

    public void setOrderID(UUID orderID) {
        this.orderID = orderID;
    }

    public AnonymousParty getBuyer() {
        return buyer;
    }

    public void setBuyer(AnonymousParty buyer) {
        this.buyer = buyer;
    }

    public AnonymousParty getSeller() {
        return seller;
    }

    public void setSeller(AnonymousParty seller) {
        this.seller = seller;
    }

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(Double orderValue) {
        this.orderValue = orderValue;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return this.participants;
    }
}
