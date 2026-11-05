package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidTextException;
import com.devtalles.proyecto.exceptions.UserAlreadyExistsException;
import com.devtalles.proyecto.interfaces.UserRepository;
import com.devtalles.proyecto.model.RoleUser;
import com.devtalles.proyecto.model.User;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

import java.util.Locale;
import java.util.Optional;

public class UserService implements UserRepository {

    private final RestaurantRepositoryService restaurantRepositoryService;

    public UserService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public RoleUser determineRoleByEmail(String email) {
        if(email.contains("@admin.")) {
            return RoleUser.ADMINISTRADOR;
        }
        return RoleUser.MESERO;
    }

    @Override
    public User registerUser(String userName, String email) throws InvalidTextException, UserAlreadyExistsException {
        Validates.validateText(userName, "El nombre de usuario no puede ser nulo o vacío");
        Validates.validateText(email, "El email no puede ser nulo o vacío");

        if(userName.contains(" ") || userName.length() > 10) {
            throw new IllegalArgumentException("El nombre de usuario no puede contener espacios ni superar los 10 caracteres");
        }

        if (!email.contains("@") || !email.contains(".") || email.contains(" ") || email.length() > 20) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }

        boolean existsName = restaurantRepositoryService.getUsers()
                .stream()
                .anyMatch(u -> u.getUserName().equalsIgnoreCase(userName));

        if(existsName) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el nombre de usuario: " + userName);
        }

        boolean existsEmail = restaurantRepositoryService.getUsers()
                .stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        if(existsEmail) {
            throw new UserAlreadyExistsException("Ya existe un usuario registrado con el correo: " + email);
        }

        RoleUser assignedRole = determineRoleByEmail(email);
        int newId = restaurantRepositoryService.getUsers()
                .size() + 1;

        User newUser = new User(newId, userName, email, assignedRole);
        restaurantRepositoryService.getUsers().add(newUser);
        restaurantRepositoryService.saveChanges();

        return newUser;
    }

    @Override
    public Optional<User> login(String userName, String email) {
        if (email == null || email.isBlank() || userName == null || userName.isBlank()) {
            return Optional.empty();
        }
        return restaurantRepositoryService.getUsers()
                .stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email)
                && u.getUserName().equalsIgnoreCase(userName))
                .findFirst();
    }
}
