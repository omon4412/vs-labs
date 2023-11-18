package com.vs.product.controller;


import com.vs.product.dto.ProductDto;
import com.vs.product.mapper.ProductMapper;
import com.vs.product.model.Product;
import com.vs.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductResolver {

    private final ProductService productService;

    @MutationMapping
    public ProductDto addProduct(@Argument ProductDto productDto) {
        Product product = ProductMapper.INSTANCE.toProduct(productDto);
        return ProductMapper.INSTANCE.toProductDto(productService.save(product));
    }

    @MutationMapping
    public ProductDto updateProduct(@Argument ProductDto productDto) {
        Product product = ProductMapper.INSTANCE.toProduct(productDto);
        return ProductMapper.INSTANCE.toProductDto(productService.update(product));
    }

    @QueryMapping
    public ProductDto getProduct(@Argument String productId) {
        return ProductMapper.INSTANCE.toProductDto(productService.getProductById(productId));
    }

    @QueryMapping
    public Collection<ProductDto> findAll() {
        return productService.findAll().stream()
                .map(ProductMapper.INSTANCE::toProductDto)
                .collect(Collectors.toList());
    }

    @MutationMapping
    public void deleteProduct(@Argument String productId) {
        productService.deleteProductById(productId);
    }
}
