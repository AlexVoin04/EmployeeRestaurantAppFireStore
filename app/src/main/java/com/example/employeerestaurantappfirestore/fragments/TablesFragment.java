package com.example.employeerestaurantappfirestore.fragments;

import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.OrderAdapter;
import com.example.employeerestaurantappfirestore.adapters.TableAdapter;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TablesFragment extends Fragment {
    private Spinner spin_filter_status, spin_filter_number_of_seats;
    private RecyclerView rv_tables;
    private FirebaseFirestore fireStore;
    private Integer filterStatusNumber, filterSeatsNumber;
    private View view;
    private Context context;
    private RelativeLayout rl_tables_not_found;
    private List<ModelTableList> tableLists;
    private TableAdapter tableAdapter;
    private NestedScrollView nsv_table;
    private TextView tv_tables_select;

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
        getTables();
        return view;
    }

    private void initViews(){
        context = getContext();
        fireStore = FirebaseFirestore.getInstance();
        tableLists = new ArrayList<>();
        filterSeatsNumber = 0;
        filterStatusNumber = 0;
        tv_tables_select = view.findViewById(R.id.tv_tables_select);
        nsv_table = view.findViewById(R.id.nsv_table);
        spin_filter_status = view.findViewById(R.id.spin_filter_status);
        spin_filter_number_of_seats = view.findViewById(R.id.spin_filter_number_of_seats);
        rl_tables_not_found = view.findViewById(R.id.rl_tables_not_found);
        rv_tables = view.findViewById(R.id.rv_tables);
        rv_tables.setLayoutManager(new LinearLayoutManager(context));
        initAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getTables(){
        int scrollY = rv_tables.getScrollY();
        setStatusVisible(View.GONE, View.VISIBLE);
        CollectionReference tablesCollectionRef = fireStore.collection("Tables");
        tablesCollectionRef.addSnapshotListener(((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }
            tableLists.clear();
            if(value!=null){
                for (QueryDocumentSnapshot document : value) {
                    ModelTableList modelTableList = document.toObject(ModelTableList.class);
                    modelTableList.setTableId(document.getId());
                    tableLists.add(modelTableList);
                }
                if(filterSeatsNumber!=0){
                    tableLists = filterTablesBySeatsNumber();
                }
                if(filterStatusNumber!=0){
                    tableLists = filterTablesByStatus();
                }
                initAdapter();
                tableAdapter.notifyDataSetChanged();
                if(tableLists.size() == 0){
                    setStatusVisible(View.VISIBLE, View.GONE);
                }
                rv_tables.scrollToPosition(scrollY);
            }
        }));
    }

    private void setStatusVisible(int status1, int status2){
        rl_tables_not_found.setVisibility(status1);
        nsv_table.setVisibility(status2);
    }

    private List<ModelTableList> filterTablesBySeatsNumber() {
        List<ModelTableList> filteredList = new ArrayList<>();
        for (ModelTableList table : tableLists) {
            if (table.getNumberOfSeats() == filterSeatsNumber) {
                filteredList.add(table);
            }
        }
        return filteredList;
    }

    private List<ModelTableList> filterTablesByStatus(){
        List<ModelTableList> filteredList = new ArrayList<>();
        DocumentReference tableStatusReference = fireStore
                .collection("TableStatus")
                .document(String.valueOf(filterStatusNumber));
        Log.d("tableStatusReference", tableStatusReference.getId());
        for (ModelTableList table : tableLists) {
            Log.d("getIdTableStatus", table.getIdTableStatus().getId());
            if (table.getIdTableStatus().equals(tableStatusReference)) {
                filteredList.add(table);
            }
        }
        return filteredList;
    }

    private void initAdapter() {
        tableAdapter = new TableAdapter(tableLists, TablesFragment.this);
        rv_tables.setAdapter(tableAdapter);
    }

    private void initListeners(){
        spin_filter_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterStatusNumber = pos;
                getTables();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spin_filter_number_of_seats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(pos==0){
                    filterSeatsNumber = pos;
                }else{
                    filterSeatsNumber = Integer.valueOf((String)parent.getItemAtPosition(pos));
                }
                Log.d("filterSeatsNumber", String.valueOf(filterSeatsNumber));
                getTables();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        tv_tables_select.setOnClickListener(view -> {
            initTablesSelectBuilder();
        });
    }

    private void initTablesSelectBuilder() {
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