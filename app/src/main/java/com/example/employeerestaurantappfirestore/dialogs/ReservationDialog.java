package com.example.employeerestaurantappfirestore.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.interfaces.OnOrderItemClickListener;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.example.employeerestaurantappfirestore.model.ModelReservations;
import com.example.employeerestaurantappfirestore.model.ModelReservationsList;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class ReservationDialog extends Dialog {
    private final Context context;
    private EditText et_reserv_name, et_reserv_telephone,
            et_reserv_persons, et_reserv_time_of_seats;
    private TextView tv_date_time;
    private LinearLayout ll_date_btn, ll_time_btn, ll_btn_cancel, ll_btn_added_reserv;
    private final Calendar dateAndTime = Calendar.getInstance();
    private FirebaseFirestore fireStore;
    private final String idTable;
    public ReservationDialog(@NonNull Context context, String idTable) {
        super(context);
        this.context = context;
        this.idTable = idTable;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_adding_reservation);
        setCanceledOnTouchOutside(false);
        initViews();
        fireStore = FirebaseFirestore.getInstance();
        initListeners();

    }

    private void initViews(){
        et_reserv_name = findViewById(R.id.et_reserv_name);
        et_reserv_telephone = findViewById(R.id.et_reserv_telephone);
        et_reserv_persons = findViewById(R.id.et_reserv_persons);
        et_reserv_time_of_seats = findViewById(R.id.et_reserv_time_of_seats);
        tv_date_time = findViewById(R.id.tv_date_time);
        ll_date_btn = findViewById(R.id.ll_date_btn);
        ll_time_btn = findViewById(R.id.ll_time_btn);
        ll_btn_cancel = findViewById(R.id.ll_btn_cancel);
        ll_btn_added_reserv = findViewById(R.id.ll_btn_added_reserv);
    }

    private void initListeners(){
        TimePickerDialog.OnTimeSetListener t= (view, hourOfDay, minute) -> {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            setInitialDateTime();
        };
        DatePickerDialog.OnDateSetListener d = (view, year, monthOfYear, dayOfMonth) -> {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        };
        ll_date_btn.setOnClickListener(view ->
                new DatePickerDialog(context, d,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH))
                        .show()
        );
        ll_time_btn.setOnClickListener(view ->
                new TimePickerDialog(context, t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE), true)
                        .show()
        );
        ll_btn_cancel.setOnClickListener(view -> dismiss());
        ll_btn_added_reserv.setOnClickListener(view -> addReservation());
    }

    private void addReservation(){
        List<EditText> editTexts = new ArrayList<>(Arrays.asList(et_reserv_name, et_reserv_telephone, et_reserv_persons, et_reserv_time_of_seats));
        if (isEmptyViews(editTexts, tv_date_time)){
            Snackbar.make(ll_btn_added_reserv, "Заполните все поля", Snackbar.LENGTH_SHORT).show();
            return;
        }
        CollectionReference reservationsCollection = fireStore.collection("Reservations");
        DocumentReference tableReference = fireStore.collection("Tables").document(idTable);
        int persons = Integer.parseInt(et_reserv_persons.getText().toString().trim());
        int timeOfStay = Integer.parseInt(et_reserv_time_of_seats.getText().toString().trim());
        ModelReservations modelReservations = new ModelReservations(
                dateAndTime.getTime(),
                et_reserv_name.getText().toString().trim(),
                et_reserv_telephone.getText().toString().trim(),
                persons,
                timeOfStay,
                tableReference
        );
        reservationsCollection.add(modelReservations)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(ll_btn_added_reserv, "Бронь добавлена", Snackbar.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("FireStore", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                });
    }

    private boolean isEmptyViews(List<EditText> editTexts, TextView textView) {
        for (EditText editText : editTexts) {
            if (editText.getText().toString().isEmpty()) {
                return true; // если хотя бы одно поле EditText пустое, возвращаем true
            }
        }
        if (textView.getText().toString().isEmpty()) {
            return true;
        }
        return false; // если все поля заполнены, возвращаем false
    }

    private void setInitialDateTime() {
        tv_date_time.setText(DateUtils.formatDateTime(context,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
    }
}
