package com.example.employeerestaurantappfirestore.interfaces;

import com.example.employeerestaurantappfirestore.model.ModelDishesQuantity;

public interface DishesListener {
    void onAddButtonClick(ModelDishesQuantity dishesQuantity);
    void onPlusButtonClick(String idDishes);
    void onMinusButtonClick(String idDishes);
    void onChangeTheQuantity(String idDishes, int quantity);
}