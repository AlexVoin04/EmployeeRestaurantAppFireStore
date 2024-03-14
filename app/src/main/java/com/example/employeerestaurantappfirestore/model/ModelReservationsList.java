package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class ModelReservationsList extends ModelReservations{
    private String reservationId;

    public ModelReservationsList() {
    }

    public ModelReservationsList(Date dateTime, String guestsName, String guestsTelephone, int numberOfPersons, int timeOfStay, DocumentReference idTable) {
        super(dateTime, guestsName, guestsTelephone, numberOfPersons, timeOfStay, idTable);
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
}
