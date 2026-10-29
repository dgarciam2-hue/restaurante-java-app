package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.CategoryNotFoundException;
import com.devtalles.proyecto.exceptions.InvalidCategoryException;
import com.devtalles.proyecto.interfaces.CategoryRepository;
import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

public class CategoryService implements CategoryRepository {
    private final RestaurantRepositoryService restaurantRepositoryService;

    public CategoryService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addCategory(String categoryName) throws InvalidCategoryException {
        Validates.validateText(categoryName, "El nombre de la categoría no puede estar vacío");

        String nameFormatted = categoryName.trim();

        boolean exist = restaurantRepositoryService.getCategories()
                .stream()
                .anyMatch(n -> n.getCategoryName().equalsIgnoreCase(categoryName));

        if(exist) {
            throw new InvalidCategoryException("La categoría " + nameFormatted + "ya existe");
        }

        restaurantRepositoryService.getCategories().add(new Category(nameFormatted));
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public Category getCategoryByName(String categoryName) throws InvalidCategoryException, CategoryNotFoundException {
        Validates.validateText(categoryName, "El nombre de la categoría no puede estar vacío");

        String nameFormatted = categoryName.trim();

        return restaurantRepositoryService.getCategories()
                .stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(nameFormatted))
                .findFirst()
                .orElseThrow( () -> new CategoryNotFoundException("La categoría " + nameFormatted + "no existe"));
    }

    public void updateCategory (String oldName, String newName) throws InvalidCategoryException, CategoryNotFoundException {
        Validates.validateText(newName, "El nuevo nombre de la categoría no puede estar vacío");

        String formattedOldName = oldName.trim();
        String formattedNewName = newName.trim();

        Category categoryToUpdate = restaurantRepositoryService.getCategories()
                .stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(formattedOldName))
                .findFirst()
                .orElseThrow( () -> new CategoryNotFoundException("La categoría '" + formattedOldName + "' no existe"));

        if(!formattedOldName.equalsIgnoreCase(formattedNewName)) {
            boolean nameExists = restaurantRepositoryService.getCategories()
                    .stream()
                    .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(formattedNewName));

            if(nameExists) {
                throw new InvalidCategoryException("Ya existe una categoría llamada " + formattedNewName);
            }
        }

        categoryToUpdate.setCategoryName(formattedNewName);
        restaurantRepositoryService.saveChanges();
    }

    public void deleteCategoryName(String categoryName) throws InvalidCategoryException, CategoryNotFoundException {
        Validates.validateText(categoryName, "El nombre de la categoría no puede estar vacío");

        String nameFormatted = categoryName.trim();

        boolean removed = restaurantRepositoryService.getCategories()
                .removeIf(c -> c.getCategoryName().equalsIgnoreCase(nameFormatted));

        if(!removed) {
            throw new CategoryNotFoundException("La categoría " + nameFormatted + "no existe");
        }

        restaurantRepositoryService.saveChanges();
    }
}
