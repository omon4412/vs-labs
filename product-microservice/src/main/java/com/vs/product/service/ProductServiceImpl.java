package com.vs.product.service;

import com.vs.product.exception.NotFoundException;
import com.vs.product.model.Product;
import com.vs.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    @Override
    public Product update(Product product) {
        Product findedProduct = productRepository.findById(
                        product.getProductCode())
                .orElseThrow(() -> new NotFoundException("Продукт с ID:" + product.getProductCode() + " не найден"));
        if (product.getName() != null) {
            findedProduct.setName(product.getName());
        }
        if (product.getDescription() != null) {
            findedProduct.setDescription(product.getDescription());
        }
        if (product.getWeight() != 0) {
            findedProduct.setWeight(product.getWeight());
        }
        if (product.getPrice() != 0) {
            findedProduct.setPrice(product.getPrice());
        }
        return findedProduct;
    }

    @Transactional(readOnly = true)
    @Override
    public Product getProductById(String productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            return product.get();
        } else {
            throw new NotFoundException("Продукт с ID:" + productId + " не найден");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteProductById(String productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            productRepository.deleteById(productId);
        } else {
            throw new NotFoundException("Продукт с ID:" + productId + " не найден");
        }
    }
}
