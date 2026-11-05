package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private int id;
    private String userName;
    private String email;
    private RoleUser roleUser;
}
