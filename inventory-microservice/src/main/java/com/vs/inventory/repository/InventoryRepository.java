package com.vs.inventory.repository;

import com.vs.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
    //    Inventory findFirstByProduct(Product product);
//
//    @Query("select i from Inventory i " +
//            "where i.product.productCode = :productCode")
    Inventory findByProductCode(String productCode);

    //
//    @Query("delete from Inventory i " +
//            "where i.product.productCode = :productCode")
    void deleteByProductCode(String productCode);

    //
//    @Query("select i from Inventory i " +
//            "where i.product.productCode in :productCodes")
    Collection<Inventory> findAllByProductCodeIn(List<String> productCodes);
}
