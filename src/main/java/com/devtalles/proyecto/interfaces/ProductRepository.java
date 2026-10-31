package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.*;
import com.devtalles.proyecto.model.Product;

public interface ProductRepository {
    public void addProduct(String categoryName, Product product) throws CategoryNotFoundException, InvalidProductException, InvalidObjectException, InvalidTextException;
    public void updateProduct(int idProduct, Product updatedProduct) throws InvalidProductException, InvalidObjectException, ProductNotFoundException, InvalidTextException;
    public void availableProduct(int idProduct) throws ProductNotFoundException, InvalidProductException;
    public void NoAvailableProduct(int idProduct) throws ProductNotFoundException, InvalidProductException;
    public void deleteProduct(int idProduct) throws ProductNotFoundException, InvalidProductException;
}
