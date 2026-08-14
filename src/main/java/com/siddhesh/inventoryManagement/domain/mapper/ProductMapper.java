package com.siddhesh.inventoryManagement.domain.mapper;

import com.siddhesh.inventoryManagement.domain.dtos.ProductCreationRequest;
import com.siddhesh.inventoryManagement.domain.dtos.ProductResponse;
import com.siddhesh.inventoryManagement.domain.entities.Product;
import com.siddhesh.inventoryManagement.domain.entities.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .quantity(product.getQuantity())
                .active(product.getActive())
                .build();
    }

    public Product toEntity(
            ProductCreationRequest request,
            ProductCategory category
    ) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .build();
    }
}