package com.vs.order.controller;

import com.vs.order.model.Order;
import com.vs.order.model.OrderResponse;
import com.vs.order.model.ProductItem;
import com.vs.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @CachePut(value = "order", key = "#order.id")
    public Order addOrder(@Valid @RequestBody Order order) {
        return orderService.save(order);
    }

    @DeleteMapping("/{orderId}")
    @CacheEvict(value = "order", key = "#orderId")
    public void deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrderById(orderId);
    }

    @GetMapping("/{orderId}")
    @Cacheable(value = "order", key = "#orderId")
    public Order getOrder(@PathVariable String orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping
    @Cacheable(value = "order")
    public Collection<Order> findAll() {
        return orderService.findAll();
    }

    @PostMapping("/{orderId}/items")
    public Order addProductItemToOrder(@PathVariable String orderId, @Valid @RequestBody ProductItem productItem) {
        return orderService.addProductItemToOrder(productItem, orderId);
    }

    @GetMapping("/{orderId}/items")
    @Cacheable(value = "productItems", key = "#orderId")
    public Collection<ProductItem> getProductItemsToOrder(@PathVariable String orderId) {
        return orderService.getProductItemsToOrder(orderId);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    public ProductItem getProductItemToOrder(@PathVariable String orderId,
                                             @PathVariable String itemId) {
        return orderService.getProductItemToOrder(orderId, itemId);
    }

    @DeleteMapping("/{orderId}/items")
    public void deleteProductItemsToOrder(@PathVariable String orderId) {
        orderService.deleteProductItemsToOrder(orderId);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public void deleteProductItemToOrder(@PathVariable String orderId,
                                         @PathVariable String itemId) {
        orderService.deleteProductItemToOrder(orderId, itemId);
    }

    @PostMapping("/{orderId}/make")
    public OrderResponse makeOrder(@PathVariable String orderId) {
        return orderService.makeOrder(orderId);
    }
}
