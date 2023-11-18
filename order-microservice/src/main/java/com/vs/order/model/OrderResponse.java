package com.vs.order.model;

import lombok.Data;

@Data
public class OrderResponse {
    private boolean result;
    private String message;
    private Order order;
}
