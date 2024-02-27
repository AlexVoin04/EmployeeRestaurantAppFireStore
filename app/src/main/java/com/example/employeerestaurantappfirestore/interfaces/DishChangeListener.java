package com.example.employeerestaurantappfirestore.interfaces;

import com.example.employeerestaurantappfirestore.model.ModelOrder;

import java.util.List;

public interface DishChangeListener {
    void onChangeFields(List<ModelOrder.OrderDishes> dishes);
}
