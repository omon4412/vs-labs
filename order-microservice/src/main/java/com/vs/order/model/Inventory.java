package com.vs.order.model;

import lombok.Data;

@Data
public class Inventory {

    private int id;

    private String productCode;

    private int count;

    private double price;

    private Product product;
}
