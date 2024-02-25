package com.example.employeerestaurantappfirestore.adapters;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.TablesFragment;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder>{
    List<ModelTableList> modelTableList;
    TablesFragment tablesFragment;

    public TableAdapter(List<ModelTableList> modelTableList, TablesFragment tablesFragment) {
        this.modelTableList = modelTableList;
        this.tablesFragment = tablesFragment;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Configuration config = tablesFragment.getResources().getConfiguration();
        View v;
        if (config.smallestScreenWidthDp >= 600) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_table, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_table_smart, viewGroup, false);
        }
//        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_table, viewGroup, false);
        return new ViewHolder(v);
    }
    @SuppressLint({"SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position){
        ModelTableList table = modelTableList.get(position);
        final DocumentReference idTableStatusRef = table.getIdTableStatus();
        final DocumentReference idCallStatusRef = table.getIdCallStatus();
        viewHolder.tv_table.setText(table.getTableId());
        viewHolder.tv_number_of_seats.setText(String.valueOf(table.getNumberOfSeats()));

        // Установка состояния кнопок RadioButton в зависимости от значения idTableStatusRef.getId()
        switch (idTableStatusRef.getId()) {
            case "1":
                viewHolder.rg_table_status.check(R.id.rBtnFree);
                break;
            case "2":
                viewHolder.rg_table_status.check(R.id.rBtnOccupied);
                break;
            case "3":
                viewHolder.rg_table_status.check(R.id.rBtnReserved);
                break;
            default:
                // Если значение не соответствует ни одному из ожидаемых, оставляем состояние по умолчанию
                viewHolder.rg_table_status.clearCheck();
                break;
        }

        Drawable circleBackground;
        if(idCallStatusRef.getId().equals("1")){
            circleBackground = ContextCompat.getDrawable(tablesFragment.requireContext(), R.drawable.circle_background_active);
        }
        else {
            circleBackground = ContextCompat.getDrawable(tablesFragment.requireContext(), R.drawable.circle_background);
        }

        viewHolder.ll_call_status.setBackground(circleBackground);
        viewHolder.ll_call_status.setOnClickListener(view -> changCallStatus(table));
        viewHolder.rg_table_status.setOnCheckedChangeListener((group, checkedId) -> {
            String newIdTableStatus = "1";
            if (checkedId == R.id.rBtnFree) {
                newIdTableStatus = "1";
            } else if (checkedId == R.id.rBtnOccupied) {
                newIdTableStatus = "2";
            } else if (checkedId == R.id.rBtnReserved) {
                newIdTableStatus = "3";
            }
            // Обновление значения idTableStatus в объекте table
            updateTableStatus(table, newIdTableStatus);
        });
    }

    private void updateTableStatus(ModelTableList table, String newIdTableStatus) {
        DocumentReference tableReference = FirebaseFirestore.getInstance()
                .collection("Tables")
                .document(table.getTableId());

        DocumentReference tableStatusReference = FirebaseFirestore.getInstance()
                .collection("TableStatus")
                .document(newIdTableStatus);

        tableReference.update("idTableStatus", tableStatusReference)
                .addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d("FireStore", "Статус стола "+table.getTableId()+" обновлен: " + tableStatusReference.getId());
                    } else {
                        Exception e = updateTask.getException();
                        if (e != null) {
                            Log.e("FireStore", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                });
    }

    private void changCallStatus(ModelTableList table){
        // Изменение значения idTableStatus в зависимости от текущего значения
        String currentIdCallStatus = table.getIdCallStatus().getId();
        String newIdCallStatus = "1";
        if (currentIdCallStatus.equals("1")) {
            newIdCallStatus = "2";
        }

        // Получение ссылки на документ Firestore по его id
        DocumentReference tableReference = FirebaseFirestore.getInstance()
                .collection("Tables")
                .document(table.getTableId());

        DocumentReference tableStatusReference = FirebaseFirestore.getInstance()
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
    @Override
    public int getItemCount() {
        return modelTableList == null ? 0 : modelTableList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_number_of_seats, tv_table;
        LinearLayout ll_call_status;
        RadioGroup rg_table_status;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_number_of_seats = itemView.findViewById(R.id.tv_number_of_seats);
            tv_table = itemView.findViewById(R.id.tv_table);
            ll_call_status = itemView.findViewById(R.id.ll_call_status);
            rg_table_status = itemView.findViewById(R.id.rg_table_status);
        }
    }
}
