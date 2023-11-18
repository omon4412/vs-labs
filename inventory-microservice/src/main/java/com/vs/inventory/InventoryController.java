package com.vs.inventory;

import com.vs.inventory.dto.InventoryDto;
import com.vs.inventory.mapper.InventoryMapper;
import com.vs.inventory.model.Inventory;
import com.vs.inventory.model.OrderAndInventoryWrapper;
import com.vs.inventory.service.InventoryService;
import com.vs.inventory.validation.Create;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @CachePut(value = "inventory", key = "#productDto.productCode")
    @PostMapping
    public InventoryDto addProduct(@Validated(Create.class) @RequestBody InventoryDto productDto) {

        Inventory inventory = InventoryMapper.INSTANCE.toInventory(productDto);
        return InventoryMapper.INSTANCE.toInventoryDto(inventoryService.save(inventory));
    }

    @Cacheable(value = "inventory", key = "#productCode")
    @GetMapping("/{productCode}")
    public InventoryDto getProduct(@PathVariable String productCode) {
        return InventoryMapper.INSTANCE.toInventoryDto(inventoryService.getProductById(productCode));
    }

    @CacheEvict(value = "inventory", key = "#productId")
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        inventoryService.deleteProductById(productId);
    }

    @Cacheable(value = "inventory")
    @GetMapping
    public Collection<InventoryDto> findAll() {
        return inventoryService.findAll().stream()
                .map(InventoryMapper.INSTANCE::toInventoryDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/check/{orderId}")
    public Collection<InventoryDto> checkPositions(@PathVariable String orderId) {
        return inventoryService.checkPositions(orderId).stream().map(InventoryMapper.INSTANCE::toInventoryDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/reserve")
    public void reservePositions(@RequestBody OrderAndInventoryWrapper wrapper) {
        inventoryService.reservePositions(wrapper);
    }
}
