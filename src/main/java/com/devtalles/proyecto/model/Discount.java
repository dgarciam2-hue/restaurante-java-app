package com.devtalles.proyecto.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.DayOfWeek;

@Data
@AllArgsConstructor
public class Discount {
    private int idDiscount;
    private String description;
    private DayOfWeek dayWeek;
    private double percentage;
    private DiscountScope scope;
    private String targetName;
    private boolean active;
}
