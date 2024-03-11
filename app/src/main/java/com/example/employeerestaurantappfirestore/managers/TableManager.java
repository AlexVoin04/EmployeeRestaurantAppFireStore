package com.example.employeerestaurantappfirestore.managers;

import android.util.Log;

import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class TableManager {
    private final FirebaseFirestore firestore;
    public TableManager() {
        firestore = FirebaseFirestore.getInstance();
    }
    public void updateTableStatus(String tableId, String newIdTableStatus) {
        DocumentReference tableReference = firestore
                .collection("Tables")
                .document(tableId);

        DocumentReference tableStatusReference = firestore
                .collection("TableStatus")
                .document(newIdTableStatus);

        tableReference.update("idTableStatus", tableStatusReference)
                .addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d("FireStore", "Статус стола "+tableId+" обновлен: " + tableStatusReference.getId());
                    } else {
                        Exception e = updateTask.getException();
                        if (e != null) {
                            Log.e("FireStore", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                });
    }

    public void changCallStatus(ModelTableList table){
        // Изменение значения idTableStatus в зависимости от текущего значения
        String currentIdCallStatus = table.getIdCallStatus().getId();
        String newIdCallStatus = "1";
        if (currentIdCallStatus.equals("1")) {
            newIdCallStatus = "2";
        }

        // Получение ссылки на документ Firestore по его id
        DocumentReference tableReference = firestore
                .collection("Tables")
                .document(table.getTableId());

        DocumentReference tableStatusReference = firestore
                .collection("CallStatus")
                .document(newIdCallStatus);

        // Обновление значения idTableStatus в Firestore
        tableReference.update("idCallStatus", tableStatusReference)
                .addOnCompleteListener(updateTask ->{
                    if (updateTask.isSuccessful()) {
                        Log.d("FireStore", "Статус вызова для стола "+table.getTableId()+" обновлен: " + tableStatusReference.getId());
                    } else {
                        Exception e = updateTask.getException();
                        if (e != null) {
                            Log.e("FireStore", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                });
    }
}
