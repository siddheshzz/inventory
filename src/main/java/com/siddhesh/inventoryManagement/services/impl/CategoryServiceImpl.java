package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.config.Exception.CategoryAlreadyExistException;
import com.siddhesh.inventoryManagement.config.Exception.CategoryNotFoundException;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.CategoryResponse;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.CreateCategoryRequest;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.UpdateCategoryRequest;
import com.siddhesh.inventoryManagement.domain.entities.ProductCategory;
import com.siddhesh.inventoryManagement.domain.mapper.CategoryMapper;
import com.siddhesh.inventoryManagement.repositories.ProductCategoryRepository;
import com.siddhesh.inventoryManagement.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse getCategory(UUID id) {

        ProductCategory cat = productCategoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        return categoryMapper.toResponse(cat);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<CategoryResponse> categories = new ArrayList<>();

        List<ProductCategory> prods = productCategoryRepository.findAll();

        for(ProductCategory c: prods){
            categories.add(categoryMapper.toResponse(c));
        }
        return categories;
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest payload) {
        if(productCategoryRepository.existsByNameIgnoreCase(payload.getName())){
            throw new CategoryAlreadyExistException("Alrady there re baba");
        }

        ProductCategory cat = categoryMapper.toEntity(payload);

        ProductCategory savedCategory = productCategoryRepository.save(cat);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(UpdateCategoryRequest payload) {
        ProductCategory category = productCategoryRepository.findByName(payload.getName())
                .orElseThrow(() -> new CategoryNotFoundException(UUID.randomUUID()));

        // Check whether another category already has this name
        if (productCategoryRepository.existsByNameIgnoreCase(
                payload.getName())) {
            throw new CategoryAlreadyExistException(
                    "Category with name '" + payload.getName() + "' already exists"
            );
        }

        category.setName(payload.getName());

        ProductCategory updatedCategory = productCategoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }


    @Override
    public void deleteCategory(UUID id) {
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(UUID.randomUUID()));

        productCategoryRepository.delete(category);


    }
}
