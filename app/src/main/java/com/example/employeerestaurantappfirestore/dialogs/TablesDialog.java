package com.example.employeerestaurantappfirestore.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class TablesDialog {
    public static void initTablesSelectBuilder(Context context, String[] tableArray,
                                               boolean[] selectedTables, ArrayList<Integer> tableList,
                                               TextView tv_tables_select,
                                               Runnable onSelectionCompleted) {
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
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < tableList.size(); j++) {
                stringBuilder.append(tableArray[tableList.get(j)]);
                if (j != tableList.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            tv_tables_select.setText(stringBuilder.toString());
            if (onSelectionCompleted != null) {
                onSelectionCompleted.run();
            }
        });
        builder.setNegativeButton("Отмена", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setNeutralButton("Все столы", (dialogInterface, i) -> {
            for (int j = 0; j < selectedTables.length; j++) {
                selectedTables[j] = false;
                tableList.clear();
                tv_tables_select.setText("");
                if (onSelectionCompleted != null) {
                    onSelectionCompleted.run();
                }
            }
        });
        builder.show();
    }

    public static void getTables(FirebaseFirestore fireStore, OnTablesLoadedListener listener) {
        CollectionReference collection = fireStore.collection("Tables");
        collection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> documentIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    documentIds.add(document.getId());
                }
                String[] tablesArray = documentIds.toArray(new String[0]);
                sortTablesDialog(tablesArray);
                listener.onTablesLoaded(tablesArray);
            } else {
                Log.d("Firestore", "Error getting documents: ", task.getException());
                listener.onTablesLoadFailed(task.getException());
            }
        });
    }

    public interface OnTablesLoadedListener {
        void onTablesLoaded(String[] tables);
        void onTablesLoadFailed(Exception e);
    }

    public static String[] sortTablesDialog(String[] tableArray){
        Comparator<String> numericStringComparator = Comparator.comparingInt(Integer::parseInt);

        // Сортировка массива строк с использованием компаратора
        Arrays.sort(tableArray, numericStringComparator);

        return tableArray;
    }
}
