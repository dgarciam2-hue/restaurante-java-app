package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.CategoryNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCategoryException;
import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.model.Category;

import java.util.List;

public interface CategoryRepository {
    public Category addCategory(String categoryName) throws InvalidCategoryException, InvalidTextException;
    public Category getCategoryByName(String categoryName) throws InvalidCategoryException, CategoryNotFoundException, InvalidTextException;
    public void updateCategory (String oldName, String newName) throws InvalidCategoryException, CategoryNotFoundException, InvalidTextException;
    public void deleteCategoryName(String categoryName) throws InvalidCategoryException, CategoryNotFoundException, InvalidTextException;
    public List<Category> getCategories();
}
