package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.model.Category;
import com.devtalles.proyecto.model.Discount;
import com.devtalles.proyecto.model.DiscountScope;
import com.devtalles.proyecto.model.Product;

import java.time.DayOfWeek;
import java.util.List;

public interface DiscountRepository {
    public void addDiscount(int idDiscount, String description, DayOfWeek dayWeek, double percentage, DiscountScope scope, String targetName, boolean active) throws InvalidCategoryException, InvalidDiscountException, InvalidTextException, InvalidObjectException, CategoryNotFoundException, ProductNotFoundException;
    public void deleteDiscount(int idDiscount) throws InvalidDiscountException, DiscountNotFoundException;
    public void updateDiscount(int idDiscount, Discount updatedDiscount) throws InvalidDiscountException, DiscountNotFoundException, InvalidTextException, InvalidObjectException, CategoryNotFoundException, ProductNotFoundException;
    public void activateDiscount(int idDiscount) throws InvalidDiscountException, DiscountNotFoundException;
    public void deActivateDiscount(int idDiscount) throws InvalidDiscountException, DiscountNotFoundException;
    public List<Discount> getApplicableDiscounts(Product product, Category category, DayOfWeek currentDay);
    public double getBestDiscountPercentage(Product product, Category category, DayOfWeek currentDay);
}
