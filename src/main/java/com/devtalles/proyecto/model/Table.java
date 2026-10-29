package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Table {
    private int numberTable;
    private TableStatus tableStatus;
}
