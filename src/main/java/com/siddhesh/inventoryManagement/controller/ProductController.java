package com.siddhesh.inventoryManagement.controller;

import com.siddhesh.inventoryManagement.domain.dtos.CreatedResponse;
import com.siddhesh.inventoryManagement.domain.dtos.ProductCreationRequest;
import com.siddhesh.inventoryManagement.domain.dtos.ProductResponse;

import com.siddhesh.inventoryManagement.domain.dtos.ProductUpdateRequest;
import com.siddhesh.inventoryManagement.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/products")
public class ProductController {
//
//    GET    /api/v1/products
//
//    POST   /api/v1/products
//
//    PATCH  /api/v1/products/{id}
//
//    DELETE /api/v1/products/{id}



//    GET /products?page=1
//
//    GET /products?category=electronics
//
//    GET /products?search=laptop
//
//    GET /products?sort=price
//
//    GET /products?minPrice=1000&maxPrice=5000p
//
//    GET /products?available=true
    //    GET    /api/v1/products

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(
                productService.getProducts()
        );
    }

    //    GET    /api/v1/products/{id}

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

//    POST   /api/v1/products
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CreatedResponse> createProduct(
            @Valid @RequestBody ProductCreationRequest payload
            ){

        return ResponseEntity.ok(productService.createProduct(payload));
    }

    //    PATCH  /api/v1/products/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value="/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
           @Valid @RequestBody ProductUpdateRequest payload
    ){


        return ResponseEntity.ok(productService.updateProduct(id,payload));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value="/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id
    ){
        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }


//
//    DELETE /api/v1/products/{id}





}
