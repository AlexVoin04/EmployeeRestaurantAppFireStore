package com.example.employeerestaurantappfirestore.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.example.employeerestaurantappfirestore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*  Аннотация для подавления предупреждений связанных с использованием
    пользовательского экрана загрузки (custom splash screen)
    Handler - Объект для управления задержкой и переходом на следующую активность
    Context - базовый абстрактный класс, который служит для выполнения операций на уровне приложения
    setFlags - (flags - новые флаги окна
        mask - Какой из битов флага окна изменить
        FLAG_LAYOUT_NO_LIMITS - Флаг окна: разрешить выход окна за пределы экрана.)
    postDelayed - Вызывает добавление Runnable r в очередь сообщений для запуска по истечении указанного времени
        Runnable - Runnable, который будет выполнен
        delayMillis - задержка (в миллисекундах) до выполнения Runnable
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    Handler handler;
    FirebaseAuth mAuth;

    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = SplashActivity.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent;
            if (user == null){
                intent = new Intent(context, InputActivity.class);
            }
            else intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}