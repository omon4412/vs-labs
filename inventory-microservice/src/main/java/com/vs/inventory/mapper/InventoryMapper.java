package com.vs.inventory.mapper;

import com.vs.inventory.dto.InventoryDto;
import com.vs.inventory.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InventoryMapper {
    InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);

    @Mapping(target = "productCode", source = "productCode")
    @Mapping(target = "id", ignore = true)
    Inventory toInventory(InventoryDto inventoryDto);

    @Mapping(target = "productCode", source = "productCode")
    com.vs.grpc.Inventory toGRPCInventory(Inventory inventory);

    @Mapping(target = "productCode", source = "productCode")
    InventoryDto toInventoryDto(Inventory inventory);
}
