package com.vs.order.service;

import com.vs.order.model.Order;
import com.vs.order.model.OrderResponse;
import com.vs.order.model.ProductItem;

import java.util.Collection;

public interface OrderService {

    Order save(Order order);

    Order getOrderById(String id);

    Order addProductItemToOrder(ProductItem productItem, String orderID);

    Collection<Order> findAll();

    Collection<ProductItem> getProductItemsToOrder(String orderId);

    void deleteOrderById(String orderId);

    void deleteProductItemsToOrder(String orderId);

    ProductItem getProductItemToOrder(String orderId, String itemId);

    void deleteProductItemToOrder(String orderId, String itemId);

    OrderResponse makeOrder(String orderId);
}
