package com.example.employeerestaurantappfirestore.fragments;

import androidx.core.widget.NestedScrollView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.dialogs.TablesDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.TableAdapter;
import com.example.employeerestaurantappfirestore.model.ModelTable;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.example.employeerestaurantappfirestore.utils.Animations;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TablesFragment extends Fragment{
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
    private TextView tv_tables_select, tv_clear_filter;
    private LinearLayout ll_settings_btn, ll_settings;
    private boolean opened;
    private String[] tableArrayForFilter;
    private boolean[] selectedTableForFilter;
    private ArrayList<Integer> tableListForFilter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            view =  inflater.inflate(R.layout.fragment_tables, container, false);
            initViews();
        } else {
            view =  inflater.inflate(R.layout.fragment_tables_smart, container, false);
            initViews();
            Animations.smartScroll(context, nsv_table);
        }
        initAdapterForSpinners();
        getTablesForFilter();
        initListeners();
        getTables();
        return view;
    }

    private void initViews(){
        context = getContext();
        fireStore = FirebaseFirestore.getInstance();
        tableListForFilter = new ArrayList<>();
        tableLists = new ArrayList<>();
        filterSeatsNumber = 0;
        filterStatusNumber = 0;
        ll_settings_btn = view.findViewById(R.id.ll_settings_btn);
        ll_settings = view.findViewById(R.id.ll_settings);
        tv_tables_select = view.findViewById(R.id.tv_tables_select);
        tv_clear_filter = view.findViewById(R.id.tv_clear_filter);
        nsv_table = view.findViewById(R.id.nsv_table);
        spin_filter_status = view.findViewById(R.id.spin_filter_status);
        spin_filter_number_of_seats = view.findViewById(R.id.spin_filter_number_of_seats);
        rl_tables_not_found = view.findViewById(R.id.rl_tables_not_found);
        rv_tables = view.findViewById(R.id.rv_tables);
        rv_tables.setLayoutManager(new LinearLayoutManager(context));
        initAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getTables(){
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
                tableLists.sort(Comparator.comparingInt(ModelTable::getNumber));
                if(filterSeatsNumber!=0){
                    tableLists = filterTablesBySeatsNumber();
                }
                if(filterStatusNumber!=0){
                    tableLists = filterTablesByStatus();
                }
                tablesSelect();
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
        tv_tables_select.setOnClickListener(view -> TablesDialog.initTablesSelectBuilder(
                context,
                tableArrayForFilter,
                selectedTableForFilter,
                tableListForFilter,
                tv_tables_select,
                this::getTables)
        );
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

        ll_settings_btn.setOnClickListener(view -> opened = Animations.hideAndShowSettings(opened, ll_settings, context));
        tv_clear_filter.setOnClickListener(view1 -> {
            spin_filter_status.setSelection(0);
            spin_filter_number_of_seats.setSelection(0);
            TablesDialog.clearAll(selectedTableForFilter, tableListForFilter, tv_tables_select, this::getTables);
        });
    }

    private void tablesSelect(){
        if (tableListForFilter.size()!=0){
            List<ModelTableList> tablesToRemove = new ArrayList<>();
            for (ModelTableList table : tableLists) {
                boolean shouldRemove = true;
                for (int j = 0; j < tableListForFilter.size(); j++) {
                    if (tableArrayForFilter[tableListForFilter.get(j)].equals(table.getTableId())) {
                        shouldRemove = false;
                        break;
                    }
                }
                if (shouldRemove) {
                    tablesToRemove.add(table);
                }
            }

            tableLists.removeAll(tablesToRemove);
        }
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

    private void getTablesForFilter(){
        TablesDialog.getTables(fireStore, new TablesDialog.OnTablesLoadedListener() {
            @Override
            public void onTablesLoaded(String[] tables) {
                tableArrayForFilter = tables;
                selectedTableForFilter = new boolean[tableArrayForFilter.length];
            }

            @Override
            public void onTablesLoadFailed(Exception e) {
                // Обработка ошибки при загрузке таблиц
            }
        });
    }

}