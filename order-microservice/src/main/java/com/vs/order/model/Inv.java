package com.vs.order.model;

import lombok.Data;

@Data
public class Inv {
    private String productCode;
    private int count;
    private double price;
}
