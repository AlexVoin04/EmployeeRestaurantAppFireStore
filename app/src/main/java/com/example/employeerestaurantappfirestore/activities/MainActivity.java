package com.example.employeerestaurantappfirestore.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.OrderFragment;
import com.example.employeerestaurantappfirestore.fragments.OrdersFragment;
import com.example.employeerestaurantappfirestore.fragments.TablesFragment;
import com.example.employeerestaurantappfirestore.interfaces.OnOrderItemClickListener;
import com.example.employeerestaurantappfirestore.interfaces.OnScrollListener;
import com.example.employeerestaurantappfirestore.model.ModelOrderList;
import com.example.employeerestaurantappfirestore.utils.NetworkUtils;
import com.example.employeerestaurantappfirestore.utils.WakeLockManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements OnScrollListener, OnOrderItemClickListener {
    private FirebaseAuth mAuth;
    private LinearLayout ll_orders, ll_tables, ll_user;
    private TextView tv_title;
    private ConstraintLayout cl_menu;
    private ImageView iv_menu_selector, iv_back;

    @SuppressLint({"MissingInflatedId", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_smart);
            initSmartConfig();
        }
        initViews();
        initListeners();
        getUser();
        startNetwork();
        WakeLockManager.acquire(this);
        activityStart(savedInstanceState);
    }

    private void activityStart(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            updateMenuUI(ll_orders, "Заказы");
            loadDefaultFragment(new OrdersFragment()); // Метод для загрузки фрагмента
        }
        else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcv_fragment);
            if (currentFragment instanceof TablesFragment) {
                updateMenuUI(ll_tables, "Столы");
                loadDefaultFragment(currentFragment);
            }
            if (currentFragment instanceof OrderFragment){
                orderBack();
            }

        }
    }
    private void getUser(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        Log.d("FirebaseUser", user.getEmail() + " => " + user.getUid());
    }
    private void startNetwork(){
        NetworkUtils.setupNetworkMonitoring(this, this::onNetworkAvailable, this::onNetworkLost);
        NetworkUtils.checkNetworkAndShowSnackbar(this);
    }
    private void initSmartConfig(){
        cl_menu = findViewById(R.id.cl_menu);
        View v_menu_shadow = findViewById(R.id.v_menu_shadow);
        Drawable drawable;
        if (isDarkTheme()) {
            drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.shadow_menu);
        } else {
            drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.shadow_menu_light);
        }
        v_menu_shadow.setBackground(drawable);
    }
    private void initViews(){
        ll_orders = findViewById(R.id.ll_orders);
        ll_tables = findViewById(R.id.ll_tables);
        ll_user = findViewById(R.id.ll_user);
        tv_title = findViewById(R.id.tv_title);
        iv_menu_selector = findViewById(R.id.iv_menu_selector);
        iv_back = findViewById(R.id.iv_back);
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fcv_fragment, fragment);
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
        iv_back.setVisibility(View.GONE);
        if(getResources().getConfiguration().smallestScreenWidthDp < 600){
            showMenu();
        }
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

    public boolean isDarkTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void showMenu(){
        if(cl_menu.getVisibility() != View.VISIBLE){
            cl_menu.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(0, 0, cl_menu.getHeight(), 0);
            // duration of animation
            animate.setDuration(500);
            animate.setFillAfter(true);
            cl_menu.startAnimation(animate);
            iv_menu_selector.setImageResource(R.drawable.ic_menu_show);
        }
    }

    private void hideMenu(){
        if(cl_menu.getVisibility() != View.GONE){
            TranslateAnimation animate = new TranslateAnimation(0, 0, 0, cl_menu.getHeight()+100);
            animate.setDuration(500);

            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Начало анимации
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Завершение анимации
                    cl_menu.setVisibility(View.GONE);
                    iv_menu_selector.setImageResource(R.drawable.ic_menu_hide);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Повторение анимации
                }
            });
            cl_menu.startAnimation(animate);
        }
    }
    @Override
    public void onScrollDown() {
        hideMenu();
    }

    @Override
    public void onScrollUp() {
        showMenu();
    }

    // Метод, вызываемый при доступности сети
    private void onNetworkAvailable() {

    }
    // Метод, вызываемый при потере сети
    private void onNetworkLost() {
        Snackbar.make(findViewById(android.R.id.content), "Отсутствует соединение сети", Snackbar.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WakeLockManager.release();
    }

    @Override
    public void onOrderItemClicked(String orderId) {
        updateMenuUI(ll_orders, "Заказ");
        loadDefaultFragment(OrderFragment.newInstance(orderId));
        orderBack();
    }

    @Override
    public void onNewItemClicked() {
        updateMenuUI(ll_orders, "Заказ");
        loadDefaultFragment(OrderFragment.newInstance(null));
        orderBack();
    }

    private void orderBack(){
        iv_back.setVisibility(View.VISIBLE);
        iv_back.setOnClickListener(view -> {
            updateMenuUI(ll_orders, "Заказы");
            loadDefaultFragment(new OrdersFragment());
        });
    }

}