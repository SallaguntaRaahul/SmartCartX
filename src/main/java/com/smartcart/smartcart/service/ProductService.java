package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.ProductDTO;
import com.smartcart.smartcart.dto.ProductEvent;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.kafka.ProductEventProducer;
import com.smartcart.smartcart.model.Product;
import com.smartcart.smartcart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    public Page<ProductDTO> getAllProducts(
            int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(sortBy));
        return productRepository.findAll(pageable)
                .map(this::toDTO);
    }

    public Page<ProductDTO> getProductsByCategory(
            String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findByCategory(category, pageable)
                .map(this::toDTO);
    }

    public Page<ProductDTO> searchProducts(
            String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .map(this::toDTO);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProductById(Long id) {
        log.info(">>> CACHE MISS - Fetching product {} from DB", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return toDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = toEntity(dto);
        Product saved = productRepository.save(product);
        ProductDTO result = toDTO(saved);

        // Publish PRODUCT_CREATED Kafka event
        productEventProducer.publishProductCreated(
                ProductEvent.builder()
                        .productId(saved.getId())
                        .productName(saved.getName())
                        .category(saved.getCategory())
                        .price(saved.getPrice())
                        .stockQuantity(saved.getStockQuantity())
                        .eventType("PRODUCT_CREATED")
                        .timestamp(LocalDateTime.now())
                        .build());

        return result;
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info(">>> Updating product {} and refreshing cache", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(dto.getCategory());
        Product saved = productRepository.save(product);
        ProductDTO result = toDTO(saved);

        // Publish PRODUCT_UPDATED Kafka event
        productEventProducer.publishProductUpdated(
                ProductEvent.builder()
                        .productId(saved.getId())
                        .productName(saved.getName())
                        .category(saved.getCategory())
                        .price(saved.getPrice())
                        .stockQuantity(saved.getStockQuantity())
                        .eventType("PRODUCT_UPDATED")
                        .timestamp(LocalDateTime.now())
                        .build());

        return result;
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info(">>> Deleting product {} and evicting cache", id);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .build();
    }

    private Product toEntity(ProductDTO dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity() != null ?
                        dto.getStockQuantity() : 0)
                .category(dto.getCategory())
                .build();
    }
}