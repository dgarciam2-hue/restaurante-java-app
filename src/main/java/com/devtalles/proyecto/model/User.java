package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class User {
    private int id;
    private String userName;
    private String email;
    private RoleUser roleUser;
    private List<Order> orders;

    public User(int id, String userName, String email, RoleUser roleUser) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.roleUser = roleUser;

        if(roleUser == RoleUser.MESERO) {
            this.orders = new ArrayList<>();
        }
    }
}
