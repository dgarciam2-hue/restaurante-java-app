package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.interfaces.DiscountRepository;
import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.model.Discount;
import com.devtalles.proyecto.model.DiscountScope;
import com.devtalles.proyecto.model.Product;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

import java.time.DayOfWeek;
import java.util.List;

public class DiscountService implements DiscountRepository {

    private final RestaurantRepositoryService restaurantRepositoryService;

    public DiscountService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addDiscount(int idDiscount, String description, DayOfWeek dayWeek, double percentage, DiscountScope scope, String targetName, boolean active) throws
            InvalidDiscountException, InvalidTextException, InvalidObjectException, CategoryNotFoundException, ProductNotFoundException {
        if (idDiscount <= 0) {
            throw new InvalidDiscountException("El ID del descuento debe ser mayor a cero.");
        }

        Validates.validateText(description, "La descripción no puede ser nula o debe tener al menos un caracter");
        Validates.validateObject(dayWeek, "El día de la semana no puede ser nulo");
        Validates.validateObject(scope,"El alcance del descuento no puede ser nulo");
        DiscountValidator.validate(idDiscount, percentage);

        String formattedDescription = description.trim();

        boolean existDiscount = restaurantRepositoryService.getDiscounts()
                .stream()
                .anyMatch(d -> d.getIdDiscount() == idDiscount);

        if(existDiscount) {
            throw new InvalidDiscountException("El descuento con id " + idDiscount + " ya existe en el sistema");
        }

        String formattedTarget = null;

        if (scope == DiscountScope.CATEGORIA) {
            Validates.validateText(targetName, "Debe especificar el nombre de la categoría para este descuento");
            formattedTarget = targetName.trim();

            // Verificar que la categoría realmente exista en el restaurante
            String categorySearch = formattedTarget;
            boolean categoryExists = restaurantRepositoryService.getCategories()
                    .stream()
                    .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(categorySearch));

            if (!categoryExists) {
                throw new CategoryNotFoundException("La categoría '" + formattedTarget + "' no existe.");
            }

        } else if (scope == DiscountScope.PRODUCTO) {
            Validates.validateText(targetName, "Debe especificar el nombre del producto para este descuento");
            formattedTarget = targetName.trim();

            // Verificar que el producto realmente exista en alguna categoría
            String productSearch = formattedTarget;
            boolean productExists = restaurantRepositoryService.getCategories()
                    .stream()
                    .flatMap(c -> c.getProducts().stream())
                    .anyMatch(p -> p.getNameProduct().equalsIgnoreCase(productSearch));

            if (!productExists) {
                throw new ProductNotFoundException("El producto '" + formattedTarget + "' no existe en el sistema.");
            }
        }

        Discount newDiscount = new Discount(idDiscount, formattedDescription, dayWeek, percentage, scope, formattedTarget, active);
        restaurantRepositoryService.getDiscounts().add(newDiscount);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void deleteDiscount(int idDiscount) throws DiscountNotFoundException, InvalidDiscountException {
        if (idDiscount <= 0) {
            throw new InvalidDiscountException("El ID del descuento debe ser mayor a cero.");
        }

        boolean removed = restaurantRepositoryService.getDiscounts()
                .removeIf(d -> d.getIdDiscount() == idDiscount);

        if(!removed) {
            throw new DiscountNotFoundException("No se encontró ningún descuento registrado con el ID " + idDiscount);
        }

        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void updateDiscount(int idDiscount, Discount updatedDiscount) throws
            InvalidDiscountException, InvalidTextException, InvalidObjectException,
            DiscountNotFoundException, CategoryNotFoundException, ProductNotFoundException {
        if (idDiscount <= 0) {
            throw new InvalidDiscountException("El ID debe ser mayor a cero.");
        }

        Validates.validateObject(updatedDiscount, "El descuento actualizado no puede ser nulo.");
        Validates.validateText(updatedDiscount.getDescription(), "La descripción no puede ser nula o vacía.");
        Validates.validateObject(updatedDiscount.getDayWeek(), "El día de la semana no puede ser nulo.");
        Validates.validateObject(updatedDiscount.getScope(), "El alcance del descuento no puede ser nulo.");
        DiscountValidator.validate(updatedDiscount.getIdDiscount(), updatedDiscount.getPercentage());

        Discount discount = getDiscountById(idDiscount);

        // Validar y formatear el objetivo del descuento actualizado
        String formattedTarget = validateAndFormatTarget(updatedDiscount.getScope(), updatedDiscount.getTargetName());

        discount.setDayWeek(updatedDiscount.getDayWeek());
        discount.setDescription(updatedDiscount.getDescription().trim());
        discount.setPercentage(updatedDiscount.getPercentage());
        discount.setScope(updatedDiscount.getScope());
        discount.setTargetName(formattedTarget); // Se actualiza también el targetName

        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void activateDiscount(int idDiscount) throws InvalidDiscountException, DiscountNotFoundException {
        if (idDiscount <= 0) {
            throw new InvalidDiscountException("El ID del descuento debe ser mayor a cero.");
        }

        Discount discount = getDiscountById(idDiscount);

        discount.setActive(true);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public void deActivateDiscount(int idDiscount) throws InvalidDiscountException, DiscountNotFoundException {
        if (idDiscount <= 0) {
            throw new InvalidDiscountException("El ID del descuento debe ser mayor a cero.");
        }

        Discount discount = getDiscountById(idDiscount);

        discount.setActive(false);
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public List<Discount> getApplicableDiscounts(Product product, Category category, DayOfWeek currentDay) {
        if (product == null || category == null || currentDay == null) {
            return List.of();
        }

        return restaurantRepositoryService.getDiscounts()
                .stream()
                .filter(Discount::isActive)
                .filter(d -> d.getDayWeek() == currentDay)
                .filter(d -> switch (d.getScope()) {
                    case TODOS -> true;
                    case CATEGORIA -> d.getTargetName() != null && d.getTargetName().equalsIgnoreCase(category.getCategoryName());
                    case PRODUCTO -> d.getTargetName() != null && d.getTargetName().equalsIgnoreCase(product.getNameProduct());
                })
                .toList();
    }

    @Override
    public double getBestDiscountPercentage(Product product, Category category, DayOfWeek currentDay) {
        List<Discount> applicableDiscounts = getApplicableDiscounts(product, category, currentDay);

        return applicableDiscounts
                .stream()
                .mapToDouble(Discount::getPercentage)
                .max()
                .orElse(0.0);
    }

    private Discount getDiscountById (int idDiscount) throws DiscountNotFoundException {
        return restaurantRepositoryService.getDiscounts()
                .stream()
                .filter(d -> d.getIdDiscount() == idDiscount)
                .findFirst()
                .orElseThrow( () -> new DiscountNotFoundException("No se encontró ningún descuento registrado con el ID " + idDiscount));
    }

    private String validateAndFormatTarget(DiscountScope scope, String targetName)
            throws InvalidTextException, CategoryNotFoundException, ProductNotFoundException {

        if (scope == DiscountScope.TODOS) {
            return null;
        }

        Validates.validateText(targetName, "Debe especificar el objetivo para un descuento de alcance " + scope);
        String formattedTarget = targetName.trim();

        if (scope == DiscountScope.CATEGORIA) {
            boolean categoryExists = restaurantRepositoryService.getCategories()
                    .stream()
                    .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(formattedTarget));

            if (!categoryExists) {
                throw new CategoryNotFoundException("La categoría " + formattedTarget + " no existe");
            }
        } else if (scope == DiscountScope.PRODUCTO) {
            boolean productExists = restaurantRepositoryService.getCategories()
                    .stream()
                    .flatMap(c -> c.getProducts().stream())
                    .anyMatch(p -> p.getNameProduct().equalsIgnoreCase(formattedTarget));

            if (!productExists) {
                throw new ProductNotFoundException("El producto " + formattedTarget + " no existe en el sistema");
            }
        }

        return formattedTarget;
    }

    public List<Discount> getDiscounts() {
        return restaurantRepositoryService.getDiscounts();
    }
}
