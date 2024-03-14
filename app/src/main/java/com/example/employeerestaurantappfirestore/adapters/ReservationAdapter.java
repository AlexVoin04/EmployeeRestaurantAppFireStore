package com.example.employeerestaurantappfirestore.adapters;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.TableFragment;
import com.example.employeerestaurantappfirestore.model.ModelReservations;
import com.example.employeerestaurantappfirestore.model.ModelReservationsList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder>{
    private List<ModelReservationsList> reservationsList;
    private TableFragment tableFragment;

    public ReservationAdapter(List<ModelReservationsList> reservationsList, TableFragment tableFragment) {
        this.reservationsList = reservationsList;
        this.tableFragment = tableFragment;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Configuration config = tableFragment.getResources().getConfiguration();
        View v;
        if (config.smallestScreenWidthDp >= 600) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reservation, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reservation, viewGroup, false);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ModelReservationsList reservation = reservationsList.get(position);
        Date dateTime = reservation.getDateTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        viewHolder.tv_time.setText(sdf.format(dateTime));
        viewHolder.tv_name.setText(reservation.getGuestsName());
        viewHolder.tv_telephone.setText(reservation.getGuestsTelephone());
        viewHolder.tv_persons.setText(String.valueOf(reservation.getNumberOfPersons()));
        viewHolder.tv_time_of_stay.setText(String.valueOf(reservation.getTimeOfStay()));
    }

    @Override
    public int getItemCount() {
        return reservationsList == null ? 0 : reservationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_time, tv_name, tv_telephone, tv_persons, tv_time_of_stay;
        LinearLayout ll_btn_delete_reservation;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_telephone = itemView.findViewById(R.id.tv_telephone);
            tv_persons = itemView.findViewById(R.id.tv_persons);
            tv_time_of_stay = itemView.findViewById(R.id.tv_time_of_stay);
            ll_btn_delete_reservation = itemView.findViewById(R.id.ll_btn_delete_reservation);
        }
    }
}
