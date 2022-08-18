package net.corda.samples.example.webserver.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.corda.core.serialization.CordaSerializable;
import net.corda.samples.supplychain.states.models.ProductDetails;

import java.util.List;
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
     "buyer",
     "seller",
     "details",
     "orderValue"
})


public class OrderDetails {
    @JsonProperty("buyer")
   private String buyer ;
    @JsonProperty("seller")
    private  String seller ;
    @JsonProperty("details")
    private  List<ProductDetails> details;


//    public OrderDetails(String buyer, String seller, List<ProductDetails> details, Double orderValue) {
//        this.buyer = buyer;
//        this.seller = seller;
//        this.details = details;
//        this.orderValue = orderValue;
//    }

    public OrderDetails() {
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public List<ProductDetails> getDetails() {
        return details;
    }

    public void setDetails(List<ProductDetails> details) {
        this.details = details;
    }


}
