package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class ModelReservations {
    private Date DateTime;
    private String GuestsName;
    private String GuestsTelephone;
    private int NumberOfPersons;
    private int TimeOfStay;
    private DocumentReference IdTable;

    public ModelReservations() {
    }

    public ModelReservations(Date dateTime, String guestsName, String guestsTelephone, int numberOfPersons, int timeOfStay, DocumentReference idTable) {
        DateTime = dateTime;
        GuestsName = guestsName;
        GuestsTelephone = guestsTelephone;
        NumberOfPersons = numberOfPersons;
        TimeOfStay = timeOfStay;
        IdTable = idTable;
    }

    public Date getDateTime() {
        return DateTime;
    }

    public void setDateTime(Date dateTime) {
        DateTime = dateTime;
    }

    public String getGuestsName() {
        return GuestsName;
    }

    public void setGuestsName(String guestsName) {
        GuestsName = guestsName;
    }

    public String getGuestsTelephone() {
        return GuestsTelephone;
    }

    public void setGuestsTelephone(String guestsTelephone) {
        GuestsTelephone = guestsTelephone;
    }

    public int getNumberOfPersons() {
        return NumberOfPersons;
    }

    public void setNumberOfPersons(int numberOfPersons) {
        NumberOfPersons = numberOfPersons;
    }

    public int getTimeOfStay() {
        return TimeOfStay;
    }

    public void setTimeOfStay(int timeOfStay) {
        TimeOfStay = timeOfStay;
    }

    public DocumentReference getIdTable() {
        return IdTable;
    }

    public void setIdTable(DocumentReference idTable) {
        IdTable = idTable;
    }
}
