package com.vs.order.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document("orders")
@Data
public class Order implements Serializable {
    @Id
    private String id;

    private LocalDateTime orderDate;

    private List<ProductItem> productItemList = new ArrayList<>();
}
