package com.example.employeerestaurantappfirestore.model;

public class ModelDishesQuantity {
    private ModelDishes dish;
    private int Quantity;

    public ModelDishesQuantity(ModelDishes dish, int quantity) {
        this.dish = dish;
        Quantity = quantity;
    }

    public ModelDishes getDish() {
        return dish;
    }

    public void setDish(ModelDishes dish) {
        this.dish = dish;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }
}
