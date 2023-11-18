package com.vs.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class OrderAndInventoryWrapper {
    private String orderId;
    private Collection<Inv> inventoryList;
}

