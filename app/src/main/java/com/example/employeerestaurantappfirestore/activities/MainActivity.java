package com.example.employeerestaurantappfirestore.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.OrdersFragment;
import com.example.employeerestaurantappfirestore.fragments.TablesFragment;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button btn_logout;
    private FirebaseAuth mAuth;
    private LinearLayout ll_orders, ll_tables;
    private TextView tv_title;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        Log.d("FirebaseUser", user.getEmail() + " => " + user.getUid());


        updateMenuUI(ll_orders, "Заказы");
        if (savedInstanceState == null) {
            loadDefaultFragment(new OrdersFragment()); // Метод для загрузки фрагмента
        }
    }

    private void initViews(){
        ll_orders = findViewById(R.id.ll_orders);
        ll_tables = findViewById(R.id.ll_tables);
        btn_logout = findViewById(R.id.btn_logout);
        tv_title = findViewById(R.id.tv_title);
    }

    private void initListeners(){
        ll_orders.setOnClickListener(view -> {
            view.requestFocus();
            updateMenuUI(view, "Заказы");
            loadDefaultFragment(new OrdersFragment());
        });
        ll_tables.setOnClickListener(view -> {
            view.requestFocus();
            updateMenuUI(view, "Столы");
            loadDefaultFragment(new TablesFragment());
        });
        btn_logout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivities(new Intent[]{new Intent(MainActivity.this, InputActivity.class)});
        });
    }
    private void loadDefaultFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fcv_fragment, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    private void updateMenuUI(View selectedView, String titleText) {
        resetMenuUI();
        tv_title.setText(titleText);
        selectedView.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColor));
    }
    private void resetMenuUI() {
        // Сброс цвета и текста для всех элементов меню
        ll_orders.setBackgroundColor(ContextCompat.getColor(this, R.color.back));
        ll_tables.setBackgroundColor(ContextCompat.getColor(this, R.color.back));
        }
}