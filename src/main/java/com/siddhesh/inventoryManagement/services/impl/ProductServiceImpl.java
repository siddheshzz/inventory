package com.siddhesh.inventoryManagement.services.impl;


import com.siddhesh.inventoryManagement.config.Exception.CategoryNotFoundException;
import com.siddhesh.inventoryManagement.config.Exception.ProductAlreadyExistsException;
import com.siddhesh.inventoryManagement.config.Exception.ProductNotFoundException;
import com.siddhesh.inventoryManagement.domain.dtos.CreatedResponse;
import com.siddhesh.inventoryManagement.domain.dtos.ProductCreationRequest;
import com.siddhesh.inventoryManagement.domain.dtos.ProductResponse;
import com.siddhesh.inventoryManagement.domain.dtos.ProductUpdateRequest;
import com.siddhesh.inventoryManagement.domain.entities.Product;
import com.siddhesh.inventoryManagement.domain.entities.ProductCategory;

import com.siddhesh.inventoryManagement.domain.mapper.ProductMapper;
import com.siddhesh.inventoryManagement.repositories.ProductCategoryRepository;
import com.siddhesh.inventoryManagement.repositories.ProductRepository;
import com.siddhesh.inventoryManagement.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponse> getProducts() {

        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductById(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toResponse(product);
    }

    @Transactional
    @Override
    public CreatedResponse createProduct(
            ProductCreationRequest request
    ) {

        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ProductAlreadyExistsException(request.getName());
        }

        ProductCategory category = categoryRepository.findById(
                request.getCategoryId()
        ).orElseThrow(() ->
                new CategoryNotFoundException(request.getCategoryId())
        );

        Product product = productMapper.toEntity(request, category);

        Product savedProduct = productRepository.save(product);

        return new CreatedResponse(savedProduct.getId().toString());
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(
            UUID id,
            ProductUpdateRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getCategoryId() != null) {
            ProductCategory category =
                    categoryRepository.findById(request.getCategoryId())
                            .orElseThrow(() ->
                                    new CategoryNotFoundException(
                                            request.getCategoryId()
                                    )
                            );

            product.setCategory(category);
        }

        return productMapper.toResponse(product);
    }

    @Transactional
    @Override
    public void deleteProduct(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);
    }
}



//
//@Service
//@RequiredArgsConstructor
//public class ProductServiceImpl implements ProductService {
//
//    private final ProductRepository productRepository;
//
//    @Override
//    public List<ProductResponse> getProducts() {
//
//        List<Product> products = productRepository.findAll();
//        List<ProductResponse> res = new ArrayList<>();
//
//        for(Product p:products){
//            res.add(new ProductResponse(
//                    p.getName(),
//                    p.getDescription(),
//                    p.getPrice(),
//                    p.getCategory(),
//                    p.getQuantity()
//            ));
//        }
//        return res;
//
////        return productRepository.findAll()
////                .stream()
////                .map(p -> new ProductResponse(
////                        p.getName(),
////                        p.getDescription(),
////                        p.getPrice(),
////                        p.getCategory(),
////                        p.getQuantity()
////                ))
////                .toList();
//    }
//
//    @Override
//    public ProductResponse getProductById(UUID id) {
//
//        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
//
//        return new ProductResponse(
//                p.getName(),
//                p.getDescription(),
//                p.getPrice(),
//                p.getCategory(),
//                p.getQuantity()
//        );
//
//    }
//
//    @Override
//    public CreatedResponse createProduct(ProductCreationRequest payload) {
//
//        //check if similar product is present or not
//
//        Optional<Product> p = productRepository.findByName(payload.getName());
//        if(p.isEmpty()){
//            Product new_product = Product.builder()
//                    .name(payload.getName())
//                    .description(payload.getDescription())
//                    .price(payload.getPrice())
//                    .category(payload.getCategory())
//                    .build();
//
//            Product savedProduct = productRepository.save(new_product);
//
//
//        return new CreatedResponse(
//                savedProduct.getId().toString()
//        );
//        }
//
//        return new CreatedResponse("done");
//    }
//
//
//    @Override
//    public ProductResponse updateProduct(UUID id, ProductUpdateRequest payload) {
//
//        Product p = productRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//
//        p.setName(payload.getName());
//        p.setDescription(payload.getDescription());
//        p.setPrice(payload.getPrice());
//        p.setCategory(payload.getCategoryId());
//
//
//        productRepository.save(p);
//
//        return new ProductResponse(
//                p.getName(),
//                p.getDescription(),
//                p.getPrice(),
//                p.getCategory(),
//                p.getQuantity()
//        );
//
//
//    }
//
//    @Override
//    public void deleteProduct(UUID id){
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new ProductNotFoundException(id));
//
//        product.setActive(false);
//
//        productRepository.save(product);
//
//
//
//    }
//
//}

