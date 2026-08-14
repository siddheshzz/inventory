package com.siddhesh.inventoryManagement.domain.mapper;

import com.siddhesh.inventoryManagement.domain.dtos.categorie.CategoryResponse;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.CreateCategoryRequest;
import com.siddhesh.inventoryManagement.domain.entities.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public ProductCategory toEntity(CreateCategoryRequest request) {

        ProductCategory category = new ProductCategory();
        category.setName(request.getName());

        return category;
    }

    public CategoryResponse toResponse(ProductCategory category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }
}
