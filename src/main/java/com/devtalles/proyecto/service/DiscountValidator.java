package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidDiscountException;

public class DiscountValidator {

    public static void validate (int idDiscount, double percentage) throws InvalidDiscountException {
        if(percentage <= 0 || percentage>100) {
            throw new InvalidDiscountException("El porcentaje de descuento se debe encontrar en el intervalo (1-100)%");
        }

        if(idDiscount < 0) {
            throw new InvalidDiscountException("El ID del descuento debe ser mayor a cero");
        }
    }
}
