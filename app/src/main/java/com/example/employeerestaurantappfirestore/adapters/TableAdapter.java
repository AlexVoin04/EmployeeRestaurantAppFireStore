package com.example.employeerestaurantappfirestore.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.TablesFragment;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_table, viewGroup, false);
        return new ViewHolder(v);
    }
    @SuppressLint("SetTextI18n")
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
