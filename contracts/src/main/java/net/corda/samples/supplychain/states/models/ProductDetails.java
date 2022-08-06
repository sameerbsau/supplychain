package net.corda.samples.supplychain.states.models;


import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class ProductDetails {
    private final String productId;
    private final String productDescription;
    private final String UOM;
    private final Double weight;
    private final Double quantity;
    private final Double price;


    public ProductDetails(String productId, String productDescription, String uom, Double weight, Double quantity, Double price) {
        this.productId = productId;
        this.productDescription = productDescription;
        UOM = uom;
        this.weight = weight;
        this.quantity = quantity;
        this.price = price;
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
}
