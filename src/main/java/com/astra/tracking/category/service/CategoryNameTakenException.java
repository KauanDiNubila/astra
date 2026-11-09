package com.astra.tracking.category.service;

import com.astra.shared.exception.ConflictException;

public class CategoryNameTakenException extends ConflictException {

    public CategoryNameTakenException(String name) {
        super("Category name already in use: " + name);
    }
}
