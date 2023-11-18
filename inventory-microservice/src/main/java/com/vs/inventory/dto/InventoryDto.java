package com.vs.inventory.dto;

import com.vs.inventory.validation.Create;
import com.vs.inventory.validation.Update;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Data
public class InventoryDto implements Serializable {
    @NotNull(groups = {Create.class})
    private String productCode;
    @NotNull(groups = {Create.class, Update.class})
    @Positive(groups = {Create.class, Update.class})
    private int count;
    @NotNull(groups = {Create.class, Update.class})
    @Positive(groups = {Create.class, Update.class})
    private double price;
}
