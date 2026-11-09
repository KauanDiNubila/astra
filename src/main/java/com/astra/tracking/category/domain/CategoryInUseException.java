package com.astra.tracking.category.domain;

import com.astra.shared.exception.ConflictException;

public class CategoryInUseException extends ConflictException {

    public CategoryInUseException() {
        super("Category has sessions and cannot be deleted");
    }
}
