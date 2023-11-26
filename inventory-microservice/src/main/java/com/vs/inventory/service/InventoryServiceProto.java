package com.vs.inventory.service;

import com.vs.grpc.OrderAndInventoryWrapper;
import com.vs.grpc.OrderIdRequest;
import com.vs.grpc.InventoryServiceGrpc;
import com.vs.grpc.ProductsResponse;
import com.vs.inventory.exception.BadRequestException;
import com.vs.inventory.exception.NotFoundException;
import com.vs.inventory.exception.ServiceNotAvailableException;
import com.vs.inventory.model.Inventory;
import com.vs.inventory.model.Order;
import com.vs.inventory.model.Product;
import com.vs.inventory.model.ProductItem;
import com.vs.inventory.repository.InventoryRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.rmi.server.ServerNotActiveException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceProto extends InventoryServiceGrpc.InventoryServiceImplBase {
    private final InventoryRepository inventoryRepository;

    private final WebClient webClient;

    private final DiscoveryClient discoveryClient;

    @Override
    public void checkInventory(OrderIdRequest request, StreamObserver<ProductsResponse> responseObserver) {
        String orderId = request.getOrderId();
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

        List<com.vs.grpc.Inventory> collect = inventoryList.stream().filter(
                        inventory -> {
                            Optional<ProductItem> productItem = orderFromServer.getProductItemList().stream()
                                    .filter(item -> item.getProductCode().equals(inventory.getProductCode()))
                                    .findFirst();
                            inventory.setProduct(new Product());
                            return productItem.isPresent() && inventory.getCount() >= productItem.get().getQuantity();
                        })
                .map(i -> com.vs.grpc.Inventory.newBuilder()
                        .setCount(i.getCount())
                        .setId(i.getId())
                        .setPrice(i.getPrice())
                        .setProductCode(i.getProductCode())
                        .build())
                .collect(Collectors.toList());


        ProductsResponse response = ProductsResponse.newBuilder()
                .addAllTest(collect)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reserveInventory(OrderAndInventoryWrapper request, StreamObserver<ProductsResponse> responseObserver) {
        request.getInventoryListList().forEach(
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
        ProductsResponse response = ProductsResponse.newBuilder()
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
