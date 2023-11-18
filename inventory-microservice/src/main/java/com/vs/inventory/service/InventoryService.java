package com.vs.inventory.service;


import com.vs.inventory.model.Inventory;
import com.vs.inventory.model.OrderAndInventoryWrapper;

import java.util.Collection;

public interface InventoryService {
    Collection<Inventory> checkPositions(String order);

    void reservePositions(OrderAndInventoryWrapper wrapper);

    void deleteProductById(String productId);

    Collection<Inventory> findAll();

    Inventory save(Inventory product);

    Inventory getProductById(String productCode);
}
