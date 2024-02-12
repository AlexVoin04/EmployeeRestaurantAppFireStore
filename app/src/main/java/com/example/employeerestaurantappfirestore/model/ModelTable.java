package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;

public class ModelTable {
    private int Number;
    private int NumberOfSeats;
    private DocumentReference idTableStatus;
    private DocumentReference idCallStatus;
    public ModelTable() {
    }
    public ModelTable(int number, int numberOfSeats, DocumentReference idTableStatus, DocumentReference idCallStatus) {
        Number = number;
        NumberOfSeats = numberOfSeats;
        this.idTableStatus = idTableStatus;
        this.idCallStatus = idCallStatus;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public int getNumberOfSeats() {
        return NumberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        NumberOfSeats = numberOfSeats;
    }

    public DocumentReference getIdTableStatus() {
        return idTableStatus;
    }

    public void setIdTableStatus(DocumentReference idTableStatus) {
        this.idTableStatus = idTableStatus;
    }

    public DocumentReference getIdCallStatus() {
        return idCallStatus;
    }

    public void setIdCallStatus(DocumentReference idCallStatus) {
        this.idCallStatus = idCallStatus;
    }
}
