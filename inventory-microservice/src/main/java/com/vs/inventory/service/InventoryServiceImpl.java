package com.vs.inventory.service;

import com.vs.inventory.exception.BadRequestException;
import com.vs.inventory.exception.NotFoundException;
import com.vs.inventory.exception.ServiceNotAvailableException;
import com.vs.inventory.model.*;
import com.vs.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.rmi.server.ServerNotActiveException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final WebClient webClient;

    private final DiscoveryClient discoveryClient;

    @Transactional(readOnly = true)
    @Override
    public Collection<Inventory> checkPositions(String orderId) {
        List<ServiceInstance> serviceInstances =
                discoveryClient.getInstances("order-service");
        if (serviceInstances.size() == 0) {
            throw new ServiceNotAvailableException("Сервис заказов недоступен");
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        URI serviceInstanceUri = serviceInstance.getUri();
        Order orderFromServer = webClient
                .get()
                .uri(serviceInstanceUri + "/api/v1/orders/" + orderId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        error -> Mono.error(
                                new NotFoundException(
                                        "Заказ с ID:" + orderId + " не найден")))
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new ServerNotActiveException("Сервис заказов недоступен")))
                .bodyToMono(Order.class)
                .doOnError(error -> {
                    log.error(error.getMessage());
                    throw new NotFoundException(error.getMessage());
                })
                .block();

        Collection<Inventory> inventoryList = inventoryRepository.findAllByProductCodeIn(
                orderFromServer.getProductItemList().stream()
                        .map(ProductItem::getProductCode)
                        .collect(Collectors.toList()));

        return inventoryList.stream().filter(
                        inventory -> {
                            Optional<ProductItem> productItem = orderFromServer.getProductItemList().stream()
                                    .filter(item -> item.getProductCode().equals(inventory.getProductCode()))
                                    .findFirst();

                            return productItem.isPresent() && inventory.getCount() >= productItem.get().getQuantity();
                        })
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void reservePositions(OrderAndInventoryWrapper wrapper) {
        List<ServiceInstance> serviceInstances =
                discoveryClient.getInstances("order-service");
        if (serviceInstances.size() == 0) {
            throw new ServiceNotAvailableException("Сервис заказов недоступен");
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        URI serviceInstanceUri = serviceInstance.getUri();
        Order orderFromServer = webClient
                .get()
                .uri(serviceInstanceUri + "/api/v1/orders/" + wrapper.getOrderId())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        error -> Mono.error(
                                new NotFoundException(
                                        "Заказ с ID:" + wrapper.getOrderId() + " не найден")))
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new ServerNotActiveException("Сервис заказов недоступен")))
                .bodyToMono(Order.class)
                .doOnError(error -> {
                    log.error(error.getMessage());
                    throw new NotFoundException(error.getMessage());
                })
                .block();

        wrapper.getInventoryList().forEach(
                inventory -> {
                    Inventory inventory2 = inventoryRepository.findByProductCode(inventory.getProductCode());
                    if (inventory2 != null) {
                        int count = inventory2.getCount() - inventory.getCount();
                        if (count >= 0) {
                            inventory2.setCount(count);
                            inventory2.setPrice(inventory.getPrice() * inventory2.getCount());
                        } else {
                            throw new BadRequestException("Нет такого количества");
                        }
                        inventoryRepository.save(inventory2);
                    }
                }
        );
    }

    @Transactional
    @Override
    public Inventory save(Inventory product) {
        List<ServiceInstance> serviceInstances =
                discoveryClient.getInstances("product-service");
        if (serviceInstances.size() == 0) {
            throw new ServiceNotAvailableException("Сервис предметов недоступен");
        }
        ServiceInstance serviceInstance = serviceInstances.get(0);
        URI serviceInstanceUri = serviceInstance.getUri();
        Product productFromServer = webClient
                .get()
                .uri(serviceInstanceUri + "/api/v1/products/" + product.getProductCode())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        error -> Mono.error(
                                new NotFoundException(
                                        "Продукт с ID:" + product.getProductCode() + " не найден")))
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new ServerNotActiveException("Сервис предметов недоступен")))
                .bodyToMono(Product.class)
                .doOnError(error -> {
                    log.error(error.getMessage());
                    throw new NotFoundException(error.getMessage());
                })
                .block();

        Inventory inventory = inventoryRepository.findByProductCode(product.getProductCode());
        if (inventory != null) {
            int count = inventory.getCount() + product.getCount();
            if (count >= 0) {
                product.setCount(count);
            } else {
                throw new BadRequestException("Нет такого количества");
            }
            product.setId(inventory.getId());
        }

        product.setProduct(productFromServer);
        product.setPrice(productFromServer.getPrice() * product.getCount());

        return inventoryRepository.save(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Inventory getProductById(String productCode) {
        Inventory inventory = inventoryRepository.findByProductCode(productCode);
        if (inventory != null) {
            return inventory;
        } else {
            throw new NotFoundException("Продукт с ID:" + productCode + " на складе не найден");
        }
    }

    @Transactional
    @Override
    public void deleteProductById(String productId) {
        Inventory inventory = inventoryRepository.findByProductCode(productId);
        if (inventory != null) {
            inventoryRepository.deleteByProductCode(productId);
        } else {
            throw new NotFoundException("Продукт с ID:" + productId + " на складе не найден");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<Inventory> findAll() {
        return inventoryRepository.findAll();
    }
}
