package com.example.employeerestaurantappfirestore.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.example.employeerestaurantappfirestore.model.ModelTablesData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

public class TablesDialog {
    public static void initTablesSelectBuilder(Context context, String[] tableArray,
                                               boolean[] selectedTables, ArrayList<Integer> tableList,
                                               DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Выберите столы");
        builder.setCancelable(false);
        builder.setMultiChoiceItems(tableArray, selectedTables, (dialogInterface, i, b) -> {
            if (b) {
                tableList.add(i);
                Collections.sort(tableList);
            } else {
                tableList.remove(Integer.valueOf(i));
            }
        });
        builder.setPositiveButton("OK", positiveClickListener);
        builder.setNegativeButton("Отмена", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setNeutralButton("Все столы", (dialogInterface, i) -> {
            for (int j = 0; j < selectedTables.length; j++) {
                selectedTables[j] = false;
                tableList.clear();
            }
        });
        builder.show();
    }

//    public static ModelTablesData getTables(FirebaseFirestore fireStore){
//        CollectionReference collection = fireStore.collection("Tables");
//        AtomicReference<ModelTablesData> tablesData = new AtomicReference<>(new ModelTablesData(new String[0], new boolean[0]));
//        collection.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                ArrayList<String> documentIds = new ArrayList<>();
////                documentIds.add("Все столы");
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    documentIds.add(document.getId());
//                }
//                String[] langArray = documentIds.toArray(new String[0]);
//                boolean[] selectedLanguage = new boolean[langArray.length];
//                tablesData.set(new ModelTablesData(langArray, selectedLanguage));
//            } else {
//                Log.d("Firestore", "Error getting documents: ", task.getException());
//            }
//        });
//        return tablesData.get();
//    }

    public static String[] sortTablesDialog(String[] tableArray){
        Comparator<String> numericStringComparator = Comparator.comparingInt(Integer::parseInt);

        // Сортировка массива строк с использованием компаратора
        Arrays.sort(tableArray, numericStringComparator);

        return tableArray;
    }
}
