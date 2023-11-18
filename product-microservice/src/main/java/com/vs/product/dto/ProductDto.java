package com.vs.product.dto;

import com.vs.product.validation.Create;
import com.vs.product.validation.Update;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Data
@Builder
public class ProductDto implements Serializable {

    @NotNull(groups = {Update.class})
    private String productCode;

    @NotNull(groups = {Create.class})
    private String name;

    @NotNull(groups = {Create.class})
    @Positive
    private Double weight;

    @NotNull(groups = {Create.class})
    private String description;

    @NotNull(groups = {Create.class})
    @Positive
    private Double price;
}

