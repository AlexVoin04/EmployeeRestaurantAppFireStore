package com.example.employeerestaurantappfirestore.interfaces;

import com.example.employeerestaurantappfirestore.model.ModelOrderList;

public interface OnOrderItemClickListener {
    void onOrderItemClicked(String orderId);
    void onNewItemClicked();
}
