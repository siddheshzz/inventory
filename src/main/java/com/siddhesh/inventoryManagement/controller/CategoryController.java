package com.siddhesh.inventoryManagement.controller;


import com.siddhesh.inventoryManagement.domain.dtos.categorie.CategoryResponse;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.CreateCategoryRequest;
import com.siddhesh.inventoryManagement.domain.dtos.categorie.UpdateCategoryRequest;
import com.siddhesh.inventoryManagement.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/categorie")
public class CategoryController {

    private final CategoryService categoryService;

    //GET /CATEGORIE/{id}
    //GET /categories
    //POST

    //PATCH /categorie/{id}
    //DELETE

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}")
    ResponseEntity<CategoryResponse> getCategory(
            @PathVariable UUID id
    ){

        return ResponseEntity.ok(categoryService.getCategory(id));

    }
    // GET /api/v1/categories
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    // POST /api/v1/categories
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest payload
    ) {
        return ResponseEntity.ok(
                categoryService.createCategory(payload)
        );
    }

    // PATCH /api/v1/categories/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest payload
    ) {
        return ResponseEntity.ok(
                categoryService.updateCategory( payload)
        );
    }

    // DELETE /api/v1/categories/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id
    ) {
        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }

}
