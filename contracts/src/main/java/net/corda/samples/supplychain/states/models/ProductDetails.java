package net.corda.samples.supplychain.states.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "productId",
        "productDescription",
        "UOM",
        "weight",
        "quantity",
        "price"
})
public class ProductDetails {
    @JsonProperty("productId")
    private  String productId;
    @JsonProperty("productDescription")
    private  String productDescription;
    @JsonProperty("UOM")
    private  String UOM;
    @JsonProperty("weight")
    private  Double weight;
    @JsonProperty("quantity")
    private  Double quantity;
    @JsonProperty("price")
    private  Double price;



//    public ProductDetails(String productId, String productDescription, String uom, Double weight, Double quantity, Double price) {
//        this.productId = productId;
//        this.productDescription = productDescription;
//        UOM = uom;
//        this.weight = weight;
//        this.quantity = quantity;
//        this.price = price;
//    }

    public ProductDetails() {
    }

    public String getProductId() {
        return productId;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getUOM() {
        return UOM;
    }

    public Double getWeight() {
        return weight;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public void setUOM(String UOM) {
        this.UOM = UOM;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
