package com.vs.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductItem implements Serializable {
    //private String id;
    @NotBlank
    private String productCode;
    @NotBlank
    private String productName;
    @Min(1)
    private int quantity;
    @Min(0)
    private double price;
    //@DBRef
    //@JsonBackReference
    //@DocumentReference(lazy = true, lookup = "{ 'productItemList' : ?#{#self.id} }")
    //@ReadOnlyProperty
    //private Order order;
//    @Field("orderId") // Ссылка на ID заказа
//    private String orderId;
}
