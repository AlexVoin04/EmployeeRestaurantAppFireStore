package com.example.employeerestaurantappfirestore.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.OrderAdapter;
import com.example.employeerestaurantappfirestore.adapters.ReservationAdapter;
import com.example.employeerestaurantappfirestore.dialogs.DishesDialog;
import com.example.employeerestaurantappfirestore.dialogs.ReservationDialog;
import com.example.employeerestaurantappfirestore.managers.TableManager;
import com.example.employeerestaurantappfirestore.model.ModelOrderList;
import com.example.employeerestaurantappfirestore.model.ModelReservationsList;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private View view;
    private Context context;
    private static final String ARG_TABLE = "tableId";
    private TextView tv_table, tv_number_of_seats;
    private RadioGroup rg_table_status;
    private LinearLayout ll_call_status, ll_before_date, ll_next_date, ll_btn_added_reservation;
    private NestedScrollView nsv_dish;
    private RecyclerView rv_reservations;
    private FirebaseFirestore db;
    private String id;
    private ModelTableList table;
    private ListenerRegistration snapshotListenerRegistration;
    private List<ModelReservationsList> reservationsList;
    public TableFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tableId Parameter 1.
     * @return A new instance of fragment TableFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TableFragment newInstance(String tableId) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TABLE, tableId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString(ARG_TABLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            view = inflater.inflate(R.layout.fragment_table, container, false);
            initViews();
        } else {
            view = inflater.inflate(R.layout.fragment_table, container, false);
            initViews();
//            Animations.smartScroll(context, nsv_dish);
        }
        initListeners();
        if(id!=null){
            db = FirebaseFirestore.getInstance();
            getTable();
        }
        return view;
    }

    private void getTable(){
        snapshotListenerRegistration = db.collection("Tables").document(id)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        table = snapshot.toObject(ModelTableList.class);
                        if (table != null) {
                            table.setTableId(snapshot.getId());
                            getTableData();
                            reservationsList = new ArrayList<>();
                            getReservations();
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
    }

    private void updateTableStatus(String tableId, String newIdTableStatus) {
        // Удаляем подписку на изменения, чтобы временно отключить onSnapshotListener
        if (snapshotListenerRegistration != null) {
            snapshotListenerRegistration.remove();
        }
        if (newIdTableStatus.equals(table.getIdTableStatus().getId())) {
            getTable();
            return;
        }

        DocumentReference tableReference = db
                .collection("Tables")
                .document(tableId);

        DocumentReference tableStatusReference = db
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
                    getTable();
                });
    }

    private void getTableData(){
        tv_table.setText(table.getTableId());
        tv_number_of_seats.setText(String.valueOf(table.getNumberOfSeats()));

        switch (table.getIdTableStatus().getId()) {
            case "1":
                rg_table_status.check(R.id.rBtnFree);
                break;
            case "2":
                rg_table_status.check(R.id.rBtnOccupied);
                break;
            case "3":
                rg_table_status.check(R.id.rBtnReserved);
                break;
            default:
                // Если значение не соответствует ни одному из ожидаемых, оставляем состояние по умолчанию
                rg_table_status.clearCheck();
                break;
        }

        Drawable circleBackground;
        if(table.getIdCallStatus().getId().equals("1")){
            circleBackground = ContextCompat.getDrawable(context, R.drawable.circle_background_active);
        }
        else {
            circleBackground = ContextCompat.getDrawable(context, R.drawable.circle_background);
        }

        ll_call_status.setBackground(circleBackground);
    }

    private void initListeners(){
        TableManager tableManager = new TableManager();
//        ll_call_status.setOnClickListener(view -> tableManager.changCallStatus(table));
//        rg_table_status.setOnCheckedChangeListener((group, checkedId) -> {
//            String newIdTableStatus = "1";
//            if (checkedId == R.id.rBtnFree) {
//                newIdTableStatus = "1";
//            } else if (checkedId == R.id.rBtnOccupied) {
//                newIdTableStatus = "2";
//            } else if (checkedId == R.id.rBtnReserved) {
//                newIdTableStatus = "3";
//            }
//            // Обновление значения idTableStatus в объекте table
//            updateTableStatus(id, newIdTableStatus);
//        });
        ll_btn_added_reservation.setOnClickListener(view1 -> {
            ReservationDialog reservationDialog = new ReservationDialog(requireContext(), table.getTableId());
            reservationDialog.show();
        });
    }

    private void getReservations(){
        int scrollY = rv_reservations.getScrollY();
        CollectionReference reservationsCollectionRef = db.collection("Reservations");
        reservationsCollectionRef.addSnapshotListener(((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }
            if (value != null){
                reservationsList.clear();
                for (DocumentSnapshot document : value) {
                    ModelReservationsList modelReservationsList = document.toObject(ModelReservationsList.class);
                    assert modelReservationsList != null;
                    modelReservationsList.setReservationId(document.getId());
                    reservationsList.add(modelReservationsList);
                }
                reservationsList.sort(Comparator.comparing(ModelReservationsList::getDateTime));
                initAdapter();
                rv_reservations.scrollToPosition(scrollY);
            }
        }));
    }
    private void initViews(){
        context = getContext();
        tv_table = view.findViewById(R.id.tv_table);
        tv_number_of_seats = view.findViewById(R.id.tv_number_of_seats);
        rg_table_status = view.findViewById(R.id.rg_table_status);
        ll_call_status = view.findViewById(R.id.ll_call_status);
        ll_before_date = view.findViewById(R.id.ll_before_date);
        ll_next_date = view.findViewById(R.id.ll_next_date);
        nsv_dish = view.findViewById(R.id.nsv_dish);
        rv_reservations = view.findViewById(R.id.rv_reservations);
        rv_reservations.setLayoutManager(new GridLayoutManager(context, 1));
        rv_reservations.setHasFixedSize(true);
        ll_btn_added_reservation = view.findViewById(R.id.ll_btn_added_reservation);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initAdapter(){
        ReservationAdapter reservationAdapter = new ReservationAdapter(reservationsList, TableFragment.this);
        reservationAdapter.notifyDataSetChanged();
        rv_reservations.setAdapter(reservationAdapter);
    }
}