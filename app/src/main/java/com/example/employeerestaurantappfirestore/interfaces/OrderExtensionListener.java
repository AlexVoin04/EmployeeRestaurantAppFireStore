package com.example.employeerestaurantappfirestore.interfaces;

import com.example.employeerestaurantappfirestore.model.ModelDishesQuantity;

import java.util.List;

public interface OrderExtensionListener {
    void onOrderExtension(List<ModelDishesQuantity> dishesQuantityList, String comment);
}
