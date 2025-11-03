package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.ProductRequestDto;
import com.example.stockgestion.Dto.response.ProductResponseDto;
import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        if (productRepository.existsBySku(productRequestDto.getSku())) {
            throw new ConflictException("Le produit avec le SKU '" + productRequestDto.getSku() + "' existe déjà.");
        }
        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setSku(productRequestDto.getSku());
        product.setCategory(productRequestDto.getCategory());
        product.setActive(productRequestDto.isActive());
        product.setPrice(productRequestDto.getPrice());
        Product savedProduct = productRepository.save(product);
        return new ProductResponseDto(savedProduct);
    }
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id){
    Product product = productRepository.findById(id).orElseThrow(() ->
        new ResourceNotFoundException("Produit", "id", id));
        return new ProductResponseDto(product);
    }
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts(){
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductResponseDto::new).toList();
    }
    @Transactional
    public ProductResponseDto updateProduct(UUID id, com.example.stockgestion.models.Product product) {
        if (id != null) {
            Product existingProduct = productRepository.findById(id).orElse(null);
            if (existingProduct != null) {
                if (product.getSku() != null) {
                    if (!existingProduct.getSku().equals(product.getSku()) &&
                        productRepository.existsBySku(product.getSku())) {
                        throw new com.example.stockgestion.exception.ConflictException("Le produit avec le SKU '" + product.getSku() + "' existe déjà.");
                    }
                    existingProduct.setSku(product.getSku());
                }
                if (product.getName() != null) existingProduct.setName(product.getName());
                if (product.getCategory() != null) existingProduct.setCategory(product.getCategory());
                if(product.getActive() != null) existingProduct.setActive(product.getActive());
                if(product.getPrice() != null) existingProduct.setPrice(product.getPrice());
                Product savedProduct = productRepository.save(existingProduct);
                return new com.example.stockgestion.Dto.response.ProductResponseDto(savedProduct);
            }
        }
        throw new com.example.stockgestion.exception.ResourceNotFoundException("Produit", "id", id);
    }
    @Transactional
    public  void deleteProduct(UUID id){
    Product existingProduct = productRepository.findById(id).orElseThrow(() ->
        new ResourceNotFoundException("Produit", "id", id));
        productRepository.delete(existingProduct);
    }
}