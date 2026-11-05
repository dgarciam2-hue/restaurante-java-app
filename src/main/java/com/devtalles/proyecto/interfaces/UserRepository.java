package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.exceptions.UserAlreadyExistsException;
import com.devtalles.proyecto.model.RoleUser;
import com.devtalles.proyecto.model.User;

import java.util.Optional;

public interface UserRepository {
    public RoleUser determineRoleByEmail(String email);
    public User registerUser(String userName, String email) throws InvalidTextException, UserAlreadyExistsException;
    public Optional<User> login(String userName, String email);
}
