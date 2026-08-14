package com.siddhesh.inventoryManagement.controller;


import com.siddhesh.inventoryManagement.domain.dtos.categorie.CategoryResponse;
import com.siddhesh.inventoryManagement.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
