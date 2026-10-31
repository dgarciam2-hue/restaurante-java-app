package com.devtalles.proyecto.utils;

import com.devtalles.proyecto.exceptions.*;

public class Validates {

    public static void validateText(String txt, String message) throws InvalidTextException {
        if(txt == null || txt.trim().isEmpty()) {
            throw new InvalidTextException(message);
        }
    }

    public static <T> void validateObject(T obj, String message) throws InvalidObjectException {
        if(obj == null) {
            throw new InvalidObjectException(message);
        }
    }

    public static <T extends Number> void validateNumber(T value, String message) throws InvalidNumberException {
        if(value == null) {
            throw new InvalidNumberException(message);
        }
    }
}
