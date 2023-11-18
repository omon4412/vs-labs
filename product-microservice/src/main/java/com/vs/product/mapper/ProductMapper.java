package com.vs.product.mapper;

import com.vs.product.dto.ProductDto;
import com.vs.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "productCode", source = "productCode")
    Product toProduct(ProductDto productDto);

    @Mapping(target = "productCode", source = "productCode")
    ProductDto toProductDto(Product product);
}
