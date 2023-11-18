package com.vs.order.controller;

import com.vs.order.model.Order;
import com.vs.order.model.OrderResponse;
import com.vs.order.model.ProductItem;
import com.vs.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;

@Controller
@RequiredArgsConstructor
public class OrderResolver {

    private final OrderService orderService;

    @MutationMapping
    //@CachePut(value = "order", key="order")
    public Order addOrder() {
        Order order = new Order();
        return orderService.save(order);
    }

    @MutationMapping
    @CacheEvict(value = "order", key = "#orderId")
    public void deleteOrder(@Argument String orderId) {
        orderService.deleteOrderById(orderId);
    }

    @QueryMapping
    @Cacheable(value = "order", key = "#orderId")
    public Order getOrder(@Argument String orderId) {
        return orderService.getOrderById(orderId);
    }

    @QueryMapping
    @Cacheable(value = "order")
    public Collection<Order> findAll() {
        return orderService.findAll();
    }

    @MutationMapping
    public Order addProductItemToOrder(@Argument String orderId, @Argument ProductItem productItem) {
        return orderService.addProductItemToOrder(productItem, orderId);
    }

    //
//    @GetMapping("/{orderId}/items")
//    @Cacheable(value = "productItems", key = "#orderId")
//    public Collection<ProductItem> getProductItemsToOrder(@PathVariable String orderId) {
//        return orderService.getProductItemsToOrder(orderId);
//    }
//
    @QueryMapping
    public ProductItem getProductItemToOrder(@Argument String orderId,
                                             @Argument String itemId) {
        return orderService.getProductItemToOrder(orderId, itemId);
    }

    @MutationMapping
    public void deleteProductItemsToOrder(@Argument String orderId) {
        orderService.deleteProductItemsToOrder(orderId);
    }

    @MutationMapping
    public void deleteProductItemToOrder(@Argument String orderId,
                                         @Argument String itemId) {
        orderService.deleteProductItemToOrder(orderId, itemId);
    }

    @MutationMapping
    public OrderResponse makeOrder(@Argument String orderId) {
        return orderService.makeOrder(orderId);
    }
}
