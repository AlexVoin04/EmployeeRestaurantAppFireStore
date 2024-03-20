package com.example.employeerestaurantappfirestore.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.ReservationAdapter;
import com.example.employeerestaurantappfirestore.dialogs.ReservationDialog;
import com.example.employeerestaurantappfirestore.model.ModelReservationsList;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
import com.example.employeerestaurantappfirestore.utils.Animations;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment {
    private View view;
    private Context context;
    private static final String ARG_TABLE = "tableId";
    private TextView tv_table, tv_number_of_seats, tv_selected_date;
    private RadioGroup rg_table_status;
    private LinearLayout ll_call_status, ll_before_date, ll_next_date, ll_btn_added_reservation;
    private NestedScrollView nsv_reservation;
    private RecyclerView rv_reservations;
    private FirebaseFirestore db;
    private String id;
    private ModelTableList table;
    private ListenerRegistration snapshotListenerRegistration;
    private List<ModelReservationsList> reservationsList;
    private final Calendar dateAndTime = Calendar.getInstance();
    private RelativeLayout rl_reservations_not_found;
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
            view = inflater.inflate(R.layout.fragment_table_smart, container, false);
            initViews();
            Animations.smartScroll(context, nsv_reservation);
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
//        TableManager tableManager = new TableManager();
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
        DatePickerDialog.OnDateSetListener d = (view, year, monthOfYear, dayOfMonth) -> {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
            getReservations();
            Animations.showMenuFragment(context);
        };
        tv_selected_date.setOnClickListener(view -> new DatePickerDialog(context, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show());
        ll_before_date.setOnClickListener(view1 -> {
            dateAndTime.add(Calendar.DAY_OF_MONTH, -1);
            setInitialDateTime();
            getReservations();
            Animations.showMenuFragment(context);
        });
        ll_next_date.setOnClickListener(view1 -> {
            dateAndTime.add(Calendar.DAY_OF_MONTH, 1);
            setInitialDateTime();
            getReservations();
            Animations.showMenuFragment(context);
        });
    }


    private void setStatusVisible(int status1, int status2){
        rl_reservations_not_found.setVisibility(status1);
        nsv_reservation.setVisibility(status2);
    }

    private void getReservations(){
        int scrollY = rv_reservations.getScrollY();
        CollectionReference reservationsCollectionRef = db.collection("Reservations");
        DocumentReference tableRef = db.collection("Tables").document(table.getTableId());
        reservationsCollectionRef.whereEqualTo("idTable", tableRef)
                .addSnapshotListener(((value, error) -> {
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
                filterReservationsByDate(dateAndTime);
                reservationsList.sort(Comparator.comparing(ModelReservationsList::getDateTime));
                initAdapter();
                if(reservationsList.size()==0){
                    setStatusVisible(View.VISIBLE, View.GONE);
                }else {
                    setStatusVisible(View.GONE, View.VISIBLE);
                }
                rv_reservations.scrollToPosition(scrollY);
            }
        }));
    }

    private void filterReservationsByDate(Calendar calendar) {
        // Проверяем, совпадает ли дата в элементе списка с calendar
        // Если дата не совпадает, удаляем элемент из списка
        reservationsList.removeIf(reservation -> !isSameDate(reservation.getDateTime(), calendar));
    }
    private boolean isSameDate(Date timestamp, Calendar calendar) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(timestamp);
        return cal1.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void initViews(){
        context = getContext();
        tv_table = view.findViewById(R.id.tv_table);
        tv_selected_date = view.findViewById(R.id.tv_selected_date);
        tv_number_of_seats = view.findViewById(R.id.tv_number_of_seats);
        rg_table_status = view.findViewById(R.id.rg_table_status);
        ll_call_status = view.findViewById(R.id.ll_call_status);
        ll_before_date = view.findViewById(R.id.ll_before_date);
        ll_next_date = view.findViewById(R.id.ll_next_date);
        nsv_reservation = view.findViewById(R.id.nsv_reservation);
        rv_reservations = view.findViewById(R.id.rv_reservations);
        rv_reservations.setLayoutManager(new GridLayoutManager(context, 1));
        rv_reservations.setHasFixedSize(true);
        ll_btn_added_reservation = view.findViewById(R.id.ll_btn_added_reservation);
        rl_reservations_not_found = view.findViewById(R.id.rl_reservations_not_found);
        setInitialDateTime();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initAdapter(){
        ReservationAdapter reservationAdapter = new ReservationAdapter(reservationsList, TableFragment.this);
        reservationAdapter.notifyDataSetChanged();
        rv_reservations.setAdapter(reservationAdapter);
    }

    private void setInitialDateTime() {
        tv_selected_date.setText(DateUtils.formatDateTime(context,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
        ));
    }
}