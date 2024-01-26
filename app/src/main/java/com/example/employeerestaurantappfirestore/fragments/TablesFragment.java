package com.example.employeerestaurantappfirestore.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.employeerestaurantappfirestore.R;

public class TablesFragment extends Fragment {
    private Spinner spin_filter_status, spin_filter_number_of_seats;
    private Integer filterStatusNumber, filterSeatsNumber;
    private View view;
    private Context context;

    public static TablesFragment newInstance() {
        return new TablesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_tables, container, false);
        initViews();
        initAdapterForSpinners();
        initListeners();
        return view;
    }

    private void initViews(){
        context = getContext();
        spin_filter_status = view.findViewById(R.id.spin_filter_status);
        spin_filter_number_of_seats = view.findViewById(R.id.spin_filter_number_of_seats);
    }

    private void initListeners(){
        spin_filter_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterStatusNumber = pos;
//                getTheLatestOrdersForToday();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spin_filter_number_of_seats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterSeatsNumber = pos;
//                getTheLatestOrdersForToday();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initAdapterForSpinners(){
        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(
                context,
                R.array.tables_status_filter_array,
                android.R.layout.simple_spinner_item
        );
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_filter_status.setAdapter(adapterStatus);
        ArrayAdapter<CharSequence> adapterSeats = ArrayAdapter.createFromResource(
                context,
                R.array.tables_number_of_seats_filter_array,
                android.R.layout.simple_spinner_item
        );
        adapterSeats.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_filter_number_of_seats.setAdapter(adapterSeats);
    }

}