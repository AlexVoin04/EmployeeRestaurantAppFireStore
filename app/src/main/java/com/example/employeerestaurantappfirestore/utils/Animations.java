package com.example.employeerestaurantappfirestore.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.interfaces.OnScrollListener;

public class Animations {

    public static void smartScroll(Context context, NestedScrollView nsv) {
        OnScrollListener onScrollListener = null;
        if (context instanceof MainActivity) {
            onScrollListener = (OnScrollListener) context;
        }
        OnScrollListener finalOnScrollListener = onScrollListener;
        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                // Скроллинг вниз
                if (finalOnScrollListener != null) {
                    finalOnScrollListener.onScrollDown();
                }
            } else if (scrollY < oldScrollY) {
                // Скроллинг вверх
                if (finalOnScrollListener != null) {
                    finalOnScrollListener.onScrollUp();
                }
            } else if(scrollY==0) {
                if (finalOnScrollListener != null) {
                    finalOnScrollListener.onScrollUp();
                }
            }
        });
    }

    public static void showMenuFragment(Context context){
        if (context instanceof MainActivity) {
            OnScrollListener onScrollListener = (OnScrollListener) context;
            onScrollListener.onScrollUp();
        }
    }

    public static boolean hideAndShowSettings(boolean opened, View ll_settings, Context context){
        if (!opened) {
            // Показываем представление
            ll_settings.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(-ll_settings.getWidth(), 0, 0, 0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            ll_settings.startAnimation(animate);
        } else {
            // Скрываем представление
            TranslateAnimation animate = new TranslateAnimation(0, -ll_settings.getWidth()-100, 0, 0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Начало анимации
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Завершение анимации
                    ll_settings.setVisibility(View.GONE);
                    Animations.showMenuFragment(context);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Повторение анимации
                }
            });
            ll_settings.startAnimation(animate);
        }
        opened = !opened;
        return opened;
    }
}
