package com.example.employeerestaurantappfirestore.managers;

import com.example.employeerestaurantappfirestore.model.ModelDishes;
import com.example.employeerestaurantappfirestore.model.ModelMenu;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuManager {
    private FirebaseFirestore firestore;
    private ListenerRegistration menuListener;

    public MenuManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getMenuWithStatus(String menuStatusId, final OnMenuLoadedListener listener) {
        CollectionReference menuCollection = firestore.collection("Menu");
        menuListener = menuCollection.whereEqualTo("idMenuStatus", firestore.collection("MenuStatus").document(menuStatusId))
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        listener.onError(e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot menuDocument = queryDocumentSnapshots.getDocuments().get(0);
                        ModelMenu menu = menuDocument.toObject(ModelMenu.class);
                        assert menu != null;
                        List<DocumentReference> dishesReferences = menu.getDishes();
                        List<ModelDishes> dishesList = new ArrayList<>();
                        for (DocumentReference dishesReference : dishesReferences) {
                            dishesReference.addSnapshotListener((dishesDocument, dishesError) -> {
                                if (dishesError != null) {
                                    listener.onError(dishesError.getMessage());
                                    return;
                                }

                                if (dishesDocument != null && dishesDocument.exists()) {
                                    ModelDishes dishes = dishesDocument.toObject(ModelDishes.class);
                                    dishesList.add(dishes);
                                    listener.onMenuLoaded(dishesList);
                                } else {
                                    listener.onError("Dishes document doesn't exist");
                                }
                            });
                        }
                    } else {
                        listener.onError("Menu document not found");
                    }
                });
    }

    public void removeMenuListener() {
        if (menuListener != null) {
            menuListener.remove();
        }
    }

    public interface OnMenuLoadedListener {
        void onMenuLoaded(List<ModelDishes> dishes);

        void onError(String errorMessage);
    }
}
