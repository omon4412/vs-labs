package com.vs.inventory.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Order {

    private String id;

    private Date orderDate;

    private List<ProductItem> productItemList = new ArrayList<>();
}
