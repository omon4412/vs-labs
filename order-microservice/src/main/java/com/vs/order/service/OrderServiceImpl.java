package com.vs.order.service;

import com.vs.order.Producer;
import com.vs.order.exception.NotFoundException;
import com.vs.order.exception.ServiceNotAvailableException;
import com.vs.order.model.*;
import com.vs.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.rmi.server.ServerNotActiveException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    private final DiscoveryClient discoveryClient;

    private final Producer kafkaProducer;

    @Transactional
    @Override
    public Order save(Order order) {
        order.setOrderDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    @Override
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));
    }

    @Transactional
    @Override
    public Order addProductItemToOrder(ProductItem productItem, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));

        List<ServiceInstance> serviceInstances =
                discoveryClient.getInstances("product-service");
        if (serviceInstances.size() == 0) {
            throw new ServiceNotAvailableException("Сервис предметов недоступен");
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        URI serviceInstanceUri = serviceInstance.getUri();
        Product productFromServer = webClient
                .get()
                .uri(serviceInstanceUri + "/api/v1/products/" + productItem.getProductCode())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        error -> Mono.error(
                                new NotFoundException(
                                        "Продукт с ID:" + productItem.getProductCode() + " не найден")))
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new ServerNotActiveException("Сервис предметов недоступен")))
                .bodyToMono(Product.class)
                .doOnError(error -> {
                    log.error(error.getMessage());
                    throw new NotFoundException(error.getMessage());
                })
                .block();
        productItem.setProductName(productFromServer.getName());
        Optional<ProductItem> productItem1 = order.getProductItemList().stream()
                .filter(i -> i.getProductCode().equals(productItem.getProductCode()))
                .findFirst();
        if (productItem1.isPresent()) {
            productItem1.get().setQuantity(productItem1.get().getQuantity() + productItem.getQuantity());
            productItem1.get().setPrice(productItem1.get().getPrice() + productItem.getPrice());
        } else {
            order.getProductItemList().add(productItem);
        }
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ProductItem> getProductItemsToOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));

        Collection<ProductItem> productItemList = order.getProductItemList();
        if (productItemList.size() == 0) {
            return Collections.emptyList();
        }
        return productItemList;
    }

    @Transactional
    @Override
    public void deleteOrderById(String orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));

        orderRepository.deleteById(orderId);
    }

    @Transactional
    @Override
    public void deleteProductItemsToOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));

        order.getProductItemList().clear();
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductItem getProductItemToOrder(String orderId, String itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));
        return order.getProductItemList().stream()
                .filter(i -> i.getProductCode().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Позиция с ID:" + itemId + " не найдена"));
    }

    @Transactional
    @Override
    public void deleteProductItemToOrder(String orderId, String itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));
        ProductItem product = order.getProductItemList().stream()
                .filter(i -> i.getProductCode().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Позиция с ID:" + itemId + " не найдена"));

        order.getProductItemList().remove(product);
        orderRepository.save(order);
    }

    @Transactional
    @Override
    public OrderResponse makeOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с ID:" + orderId + " не найден"));

        OrderResponse orderResponse = new OrderResponse();
        if (order.getProductItemList().size() == 0) {
            orderResponse.setResult(false);
            orderResponse.setMessage("Нет элементов в заказе");
            ProducerRecord<String, OrderResponse> record = new ProducerRecord<>("notification", orderResponse);
            kafkaProducer.sendMessage(record.toString());
            return orderResponse;
        }

        List<ServiceInstance> serviceInstances =
                discoveryClient.getInstances("inventory-service");
        if (serviceInstances.size() == 0) {
            throw new ServiceNotAvailableException("Сервис склад недоступен");
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        URI serviceInstanceUri = serviceInstance.getUri();
        Collection<Inventory> productFromServer = webClient
                .post()
                .uri(serviceInstanceUri + "/api/v1/inventory/check/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                //.body(BodyInserters.fromValue(order))
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new ServerNotActiveException("Сервис склад недоступен")))
                .bodyToFlux(Inventory.class)
                .doOnError(error -> {
                    log.error(error.getMessage());
                    throw new NotFoundException(error.getMessage());
                }).collectList()
                .block();

        if (productFromServer.size() == 0) {
            orderResponse.setResult(false);
            orderResponse.setMessage("Все товары закончились");
            ProducerRecord<String, OrderResponse> record = new ProducerRecord<>("notification", orderResponse);
            kafkaProducer.sendMessage(record.toString());
            return orderResponse;
        }

        OrderAndInventoryWrapper wrapper = new OrderAndInventoryWrapper(order.getId(), productFromServer.stream()
                .map(
                        i -> {
                            Inv inv = new Inv();
                            inv.setCount(order.getProductItemList().stream()
                                    .filter(ii -> i.getProductCode().equals(ii.getProductCode())).findFirst().get().getQuantity());
                            inv.setProductCode(order.getProductItemList().stream()
                                    .filter(ii -> i.getProductCode().equals(ii.getProductCode())).findFirst().get().getProductCode());
                            inv.setPrice(order.getProductItemList().stream()
                                    .filter(ii -> i.getProductCode().equals(ii.getProductCode())).findFirst().get().getPrice());
                            return inv;
                        }
                ).collect(Collectors.toList()));
        Void t = webClient
                .post()
                .uri(serviceInstanceUri + "/api/v1/inventory/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(wrapper))
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new ServerNotActiveException("Сервис склад недоступен")))
                .bodyToMono(Void.class)
                .block();

        List<ProductItem> result = order.getProductItemList().stream()
                .filter(product -> productFromServer.stream()
                        .anyMatch(position -> position.getProductCode().equals(product.getProductCode())))
                .collect(Collectors.toList());
        order.setProductItemList(result);

        orderResponse.setResult(true);
        orderResponse.setOrder(order);
        orderResponse.setMessage("Заказ оформлен");

        ProducerRecord<String, OrderResponse> record = new ProducerRecord<>("notification", orderResponse);
        kafkaProducer.sendMessage(record.toString());

        return orderResponse;
    }
}
