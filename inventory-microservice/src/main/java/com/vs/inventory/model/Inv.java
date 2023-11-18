package com.vs.inventory.model;

import lombok.Data;

@Data
public class Inv {
    private String productCode;
    private int count;
    private int price;
}
