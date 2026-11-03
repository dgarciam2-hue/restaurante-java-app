package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.interfaces.ProductRepository;
import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.model.Product;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

public class ProductService implements ProductRepository {

    private final RestaurantRepositoryService restaurantRepositoryService;

    public ProductService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addProduct(String categoryName, Product newProduct) throws
            CategoryNotFoundException, InvalidProductException, InvalidObjectException, InvalidTextException {

        Validates.validateText(categoryName, "El nombre de la categoría no puede estar vacío.");
        Validates.validateObject(newProduct, "El producto no puede ser nulo.");
        Validates.validateText(newProduct.getDescriptionProduct(), "La descripción del producto no puede estar vacía.");
        Validates.validateText(newProduct.getNameProduct(), "El nombre del producto no puede estar vacío.");

        if (newProduct.getIdProduct() <= 0) {
            throw new InvalidProductException("El ID del producto debe ser un entero positivo.");
        }
        ProductValidator.validate(newProduct);

        String formattedCategoryName = categoryName.trim();
        String formattedProductName = newProduct.getNameProduct().trim();

        Category category = restaurantRepositoryService.getCategories()
                .stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(formattedCategoryName))
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException("La categoría '" + formattedCategoryName + "' no existe."));

        boolean idExistsInSystem = restaurantRepositoryService.getCategories()
                .stream()
                .flatMap(c -> c.getProducts().stream())
                .anyMatch(p -> p.getIdProduct() == newProduct.getIdProduct());

        if (idExistsInSystem) {
            throw new InvalidProductException("Ya existe un producto con el ID " + newProduct.getIdProduct() + " en el sistema.");
        }

        boolean nameExistsInSystem = restaurantRepositoryService.getCategories()
                .stream()
                .flatMap(c -> c.getProducts().stream())
                .anyMatch(p -> p.getNameProduct().equalsIgnoreCase(formattedProductName));

        if (nameExistsInSystem) {
            throw new InvalidProductException("Ya existe el producto '" + formattedProductName + "' en el sistema.");
        }

        category.getProducts().add(newProduct);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void updateProduct(int idProduct, Product updatedProduct) throws
            InvalidProductException, InvalidObjectException, ProductNotFoundException, InvalidTextException {

        if (idProduct <= 0) {
            throw new InvalidProductException("El ID del producto debe ser mayor a cero.");
        }

        Validates.validateObject(updatedProduct, "El producto actualizado no puede ser nulo.");
        Validates.validateText(updatedProduct.getDescriptionProduct(), "La descripción del producto no puede estar vacía.");
        Validates.validateText(updatedProduct.getNameProduct(), "El nuevo nombre del producto no puede estar vacío.");
        ProductValidator.validate(updatedProduct);

        String formattedNewName = updatedProduct.getNameProduct().trim();

        Product existingProduct = getProductById(idProduct);

        if (!existingProduct.getNameProduct().equalsIgnoreCase(formattedNewName)) {
            boolean nameExists = restaurantRepositoryService.getCategories()
                    .stream()
                    .flatMap(c -> c.getProducts().stream())
                    .anyMatch(p -> p.getNameProduct().equalsIgnoreCase(formattedNewName) && p.getIdProduct() != idProduct);

            if (nameExists) {
                throw new InvalidProductException("Ya existe otro producto con el nombre '" + formattedNewName + "'.");
            }
        }

        existingProduct.setNameProduct(formattedNewName);
        existingProduct.setPriceProduct(updatedProduct.getPriceProduct());
        existingProduct.setDescriptionProduct(updatedProduct.getDescriptionProduct().trim());
        existingProduct.setStockProduct(updatedProduct.getStockProduct());

        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void availableProduct(int idProduct) throws ProductNotFoundException, InvalidProductException {
        if (idProduct <= 0) {
            throw new InvalidProductException("El ID del producto debe ser mayor a cero.");
        }

        Product product = getProductById(idProduct);
        product.setAvailabilityProduct(true);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void NoAvailableProduct(int idProduct) throws ProductNotFoundException, InvalidProductException {
        if (idProduct <= 0) {
            throw new InvalidProductException("El ID del producto debe ser mayor a cero.");
        }

        Product product = getProductById(idProduct);
        product.setAvailabilityProduct(false);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void deleteProduct(int idProduct) throws ProductNotFoundException, InvalidProductException {
        if (idProduct <= 0) {
            throw new InvalidProductException("El ID del producto debe ser mayor a cero.");
        }

        Category category = restaurantRepositoryService.getCategories()
                .stream()
                .filter(c -> c.getProducts().stream().anyMatch(p -> p.getIdProduct() == idProduct))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("El producto con ID " + idProduct + " no existe."));

        category.getProducts().removeIf(p -> p.getIdProduct() == idProduct);
        restaurantRepositoryService.saveChanges();
    }

    public Product getProductById(int idProduct) throws ProductNotFoundException {
        return restaurantRepositoryService.getCategories()
                .stream()
                .flatMap(c -> c.getProducts().stream())
                .filter(p -> p.getIdProduct() == idProduct)
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("El producto con ID " + idProduct + " no existe."));
    }
}
