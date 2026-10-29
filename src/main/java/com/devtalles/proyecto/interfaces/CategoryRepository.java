package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.CategoryNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCategoryException;
import com.devtalles.proyecto.model.Category;

public interface CategoryRepository {
    public void addCategory(String categoryName) throws InvalidCategoryException;
    public Category getCategoryByName(String categoryName) throws InvalidCategoryException, CategoryNotFoundException;

}
