package com.devtalles.proyecto.service;

import com.devtalles.proyecto.exceptions.InvalidNumberException;
import com.devtalles.proyecto.exceptions.InvalidTableException;
import com.devtalles.proyecto.exceptions.TableNotFoundException;
import com.devtalles.proyecto.interfaces.TableRepository;
import com.devtalles.proyecto.model.Table;
import com.devtalles.proyecto.model.TableStatus;
import com.devtalles.proyecto.repository.RestaurantRepositoryService;
import com.devtalles.proyecto.utils.Validates;

import java.util.List;

public class TableService implements TableRepository {

    private final RestaurantRepositoryService restaurantRepositoryService;

    public TableService(RestaurantRepositoryService restaurantRepositoryService) {
        this.restaurantRepositoryService = restaurantRepositoryService;
    }

    @Override
    public void addTable(int numberTable) throws InvalidTableException {
        TableValidator.validate(numberTable);

        boolean existsTable = restaurantRepositoryService.getTables()
                .stream()
                .anyMatch(t -> t.getNumberTable() == numberTable);

        if(existsTable) {
            throw new InvalidTableException("Ya existe una mesa con este número: " + numberTable);
        }

        restaurantRepositoryService.getTables().add(new Table(numberTable));
        restaurantRepositoryService.saveChanges();
    }

    @Override
    public List<Table> getAvailablesTables() {
        return restaurantRepositoryService.getTables()
                .stream()
                .filter(t -> t.getTableStatus() == TableStatus.LIBRE)
                .toList();
    }

    @Override
    public TableStatus getStatusTable(int numberTable) throws InvalidTableException, TableNotFoundException {
        TableValidator.validate(numberTable);

        Table existingTable = restaurantRepositoryService.getTables()
                .stream()
                .filter(t -> t.getNumberTable() == numberTable)
                .findFirst()
                .orElseThrow( () -> new TableNotFoundException("La mesa " + numberTable + " no existe"));

        return existingTable.getTableStatus();
    }

    @Override
    public void deleteTable(int numberTable) throws InvalidTableException, TableNotFoundException {
        TableValidator.validate(numberTable);

        boolean removed = restaurantRepositoryService.getTables()
                .removeIf(t -> t.getNumberTable() == numberTable);

        if(!removed) {
            throw new TableNotFoundException("La mesa " + numberTable + " no existe");
        }

        restaurantRepositoryService.saveChanges();
    }
}
