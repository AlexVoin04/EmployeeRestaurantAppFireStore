package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class ModelOrder {
    private String orderId;
    private double Cost;
    private DocumentReference IdTable;
    private List<OrderDishes> Dishes;
    private String Comment;

    public static class OrderDishes{
        @ServerTimestamp
        private Date DateTime;
        private DocumentReference IdDishStatus;
        private DocumentReference IdDishes;
        private int Quantity;
        private double Cost;

        public OrderDishes() {
        }

        public OrderDishes(Date dateTime, DocumentReference idDishStatus, DocumentReference idDishes, int quantity, double cost) {
            DateTime = dateTime;
            IdDishStatus = idDishStatus;
            IdDishes = idDishes;
            Quantity = quantity;
            Cost = cost;
        }

        public Date getDateTime() {
            return DateTime;
        }

        public void setDateTime(Date dateTime) {
            DateTime = dateTime;
        }

        public DocumentReference getIdDishStatus() {
            return IdDishStatus;
        }

        public void setIdDishStatus(DocumentReference idDishStatus) {
            IdDishStatus = idDishStatus;
        }

        public DocumentReference getIdDishes() {
            return IdDishes;
        }

        public void setIdDishes(DocumentReference idDishes) {
            IdDishes = idDishes;
        }

        public int getQuantity() {
            return Quantity;
        }

        public void setQuantity(int quantity) {
            Quantity = quantity;
        }

        public double getCost() {
            return Cost;
        }

        public void setCost(double cost) {
            Cost = cost;
        }
    }

    public ModelOrder() {
    }

    public ModelOrder(double cost, DocumentReference idTable, List<OrderDishes> dishes, String comment) {
        Cost = cost;
        IdTable = idTable;
        Dishes = dishes;
        Comment = comment;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getCost() {
        return Cost;
    }

    public void setCost(double cost) {
        Cost = cost;
    }

    public DocumentReference getIdTable() {
        return IdTable;
    }

    public void setIdTable(DocumentReference idTable) {
        IdTable = idTable;
    }

    public List<OrderDishes> getDishes() {
        return Dishes;
    }

    public void setDishes(List<OrderDishes> dishes) {
        Dishes = dishes;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }
}
