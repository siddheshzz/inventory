package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.categorie.CategoryResponse;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.CreateCategoryRequest;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.UpdateCategoryRequest;
import org.hibernate.sql.Update;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse getCategory(UUID id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse createCategory(CreateCategoryRequest payload);

    CategoryResponse updateCategory(UpdateCategoryRequest payload);

    void deleteCategory(UUID id);
}
