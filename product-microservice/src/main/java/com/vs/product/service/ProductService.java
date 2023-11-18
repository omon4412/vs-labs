package com.vs.product.service;


import com.vs.product.model.Product;

import java.util.Collection;

public interface ProductService {

    Product save(Product item);

    Product update(Product product);

    Product getProductById(String id);

    Collection<Product> findAll();

    void deleteProductById(String productId);
}
