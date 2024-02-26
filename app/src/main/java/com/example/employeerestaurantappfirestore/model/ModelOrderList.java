package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ModelOrderList extends ModelOrder implements Serializable {
    private String orderId;
    private Date DateTimeMax;
    private boolean isCompleted;

    public ModelOrderList() {
    }

    public ModelOrderList(double cost, DocumentReference idTable, List<OrderDishes> dishes, String comment) {
        super(cost, idTable, dishes, comment);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getDateTimeMax() {
        return DateTimeMax;
    }

    public void setDateTimeMax(Date dateTimeMax) {
        DateTimeMax = dateTimeMax;
    }

    public boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
