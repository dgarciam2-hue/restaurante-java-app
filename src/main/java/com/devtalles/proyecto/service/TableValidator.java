package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidTableException;

public class TableValidator {
    public static void validate(int numTable) throws InvalidTableException {
        if(numTable <= 0){
            throw new InvalidTableException("El numero de la mesa no puede ser menor o igual a cero");
        }
    }
}
