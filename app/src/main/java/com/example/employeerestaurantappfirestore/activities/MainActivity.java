package com.example.employeerestaurantappfirestore.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.OrdersFragment;
import com.example.employeerestaurantappfirestore.fragments.TablesFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private LinearLayout ll_orders, ll_tables, ll_user;
    private TextView tv_title;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_smart);
        }
//        setContentView(R.layout.activity_main);
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
        ll_user = findViewById(R.id.ll_user);
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
        ll_user.setOnClickListener(view -> {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            final AlertDialog alertDialog = builder.create();
            builder.setMessage("Выйти из системы?")
                    .setCancelable(false)
                    .setTitle("Подтвердить?")
                    .setPositiveButton("Да", (dialog, id) -> logOut())
                    .setNegativeButton("Нет", (dialog, which) -> alertDialog.dismiss())
                    .show();
        });
    }

    private void logOut(){
        mAuth.signOut();
        startActivities(new Intent[]{new Intent(MainActivity.this, InputActivity.class)});
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
        selectedView.setBackgroundResource(R.drawable.rounded_button);
    }
    private void resetMenuUI() {
        // Сброс цвета и текста для всех элементов меню
        ll_orders.setBackgroundColor(ContextCompat.getColor(this, R.color.back));
        ll_tables.setBackgroundColor(ContextCompat.getColor(this, R.color.back));
        }
}