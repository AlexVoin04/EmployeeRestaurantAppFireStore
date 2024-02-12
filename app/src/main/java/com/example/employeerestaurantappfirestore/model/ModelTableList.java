package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;

public class ModelTableList extends ModelTable{
    private String tableId;

    public ModelTableList() {
    }

    public ModelTableList(int number, int numberOfSeats, DocumentReference idTableStatus, DocumentReference idCallStatus) {
        super(number, numberOfSeats, idTableStatus, idCallStatus);
    }

    public String getTableId() {
        return tableId;
    }
    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
}
