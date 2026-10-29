package com.devtalles.proyecto.utils;

import com.devtalles.proyecto.exceptions.InvalidCategoryException;

public class Validates {

    public static void validateText(String txt, String message) throws InvalidCategoryException {
        if(txt == null || txt.isEmpty()) {
            throw new InvalidCategoryException(message);
        }
    }
}
