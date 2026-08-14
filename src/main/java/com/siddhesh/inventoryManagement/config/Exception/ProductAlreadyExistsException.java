package com.siddhesh.inventoryManagement.config.Exception;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String name) {
        super("Product not found: " + name);

    }
}
