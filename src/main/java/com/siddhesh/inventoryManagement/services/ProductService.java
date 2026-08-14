package com.siddhesh.inventoryManagement.services;


import com.siddhesh.inventoryManagement.domain.dtos.CreatedResponse;
import com.siddhesh.inventoryManagement.domain.dtos.ProductCreationRequest;
import com.siddhesh.inventoryManagement.domain.dtos.ProductResponse;
import com.siddhesh.inventoryManagement.domain.dtos.ProductUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<ProductResponse> getProducts();

    ProductResponse getProductById(UUID id);

    CreatedResponse createProduct(ProductCreationRequest payload);

    ProductResponse updateProduct(UUID id , ProductUpdateRequest payload);

    void deleteProduct(UUID id);




}
