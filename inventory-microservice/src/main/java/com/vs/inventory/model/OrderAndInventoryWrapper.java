package com.vs.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class OrderAndInventoryWrapper {
    private String orderId;
    private Collection<Inv> inventoryList;
}

