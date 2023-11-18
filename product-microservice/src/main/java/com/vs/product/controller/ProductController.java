package com.vs.product.controller;

import com.vs.product.dto.ProductDto;
import com.vs.product.mapper.ProductMapper;
import com.vs.product.model.Product;
import com.vs.product.service.ProductService;
import com.vs.product.validation.Create;
import com.vs.product.validation.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @CachePut(value = "products", key = "#productDto.productCode")
    @PostMapping
    public ProductDto addProduct(@Validated(Create.class) @RequestBody ProductDto productDto) {
        Product product = ProductMapper.INSTANCE.toProduct(productDto);
        return ProductMapper.INSTANCE.toProductDto(productService.save(product));
    }

    @CachePut(value = "products", key = "#productDto.productCode")
    @PutMapping
    public ProductDto updateProduct(@Validated(Update.class) @RequestBody ProductDto productDto) {
        Product product = ProductMapper.INSTANCE.toProduct(productDto);
        return ProductMapper.INSTANCE.toProductDto(productService.update(product));
    }

    @CacheEvict(value = "products", key = "#productId")
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        productService.deleteProductById(productId);
    }

    @Cacheable(value = "products", key = "#productId")
    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable String productId) {
        return ProductMapper.INSTANCE.toProductDto(productService.getProductById(productId));
    }

    @Cacheable(value = "products")
    @GetMapping
    public Collection<ProductDto> findAll() {
        return productService.findAll().stream()
                .map(ProductMapper.INSTANCE::toProductDto)
                .collect(Collectors.toList());
    }
}
