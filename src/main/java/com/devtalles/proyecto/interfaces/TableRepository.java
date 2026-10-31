package com.devtalles.proyecto.interfaces;

import com.devtalles.proyecto.exceptions.InvalidNumberException;
import com.devtalles.proyecto.exceptions.InvalidTableException;
import com.devtalles.proyecto.exceptions.TableNotFoundException;
import com.devtalles.proyecto.model.Table;
import com.devtalles.proyecto.model.TableStatus;

import java.util.List;

public interface TableRepository {
    public void addTable(int numberTable) throws InvalidNumberException, InvalidTableException;
    public List<Table> getAvailablesTables();
    public TableStatus getStatusTable(int numberTable) throws InvalidNumberException, InvalidTableException, TableNotFoundException;
    public void deleteTable(int numberTable) throws InvalidTableException, TableNotFoundException;
}
