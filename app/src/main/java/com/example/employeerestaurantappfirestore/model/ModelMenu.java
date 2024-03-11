package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.List;

public class ModelMenu {
    private Date DateOfCreation;
    private DocumentReference IdMenuStatus;
    private List<DocumentReference> dishes;

    public ModelMenu() {
    }

    public ModelMenu(Date dateOfCreation, DocumentReference idMenuStatus, List<DocumentReference> dishes) {
        DateOfCreation = dateOfCreation;
        IdMenuStatus = idMenuStatus;
        this.dishes = dishes;
    }

    public Date getDateOfCreation() {
        return DateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        DateOfCreation = dateOfCreation;
    }

    public DocumentReference getIdMenuStatus() {
        return IdMenuStatus;
    }

    public void setIdMenuStatus(DocumentReference idMenuStatus) {
        IdMenuStatus = idMenuStatus;
    }

    public List<DocumentReference> getDishes() {
        return dishes;
    }

    public void setDishes(List<DocumentReference> dishes) {
        this.dishes = dishes;
    }
}
