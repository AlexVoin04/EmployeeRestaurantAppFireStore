package com.example.employeerestaurantappfirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.employeerestaurantappfirestore.activities.InputActivity;
import com.example.employeerestaurantappfirestore.fragments.OrdersFragment;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn_logout;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_logout = findViewById(R.id.btn_logout);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        Log.d("FirebaseUser", user.getEmail() + " => " + user.getUid());
        btn_logout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivities(new Intent[]{new Intent(MainActivity.this, InputActivity.class)});
        });
        // Добавьте следующий код для загрузки фрагмента при запуске активности
        if (savedInstanceState == null) {
            loadDefaultFragment(); // Метод для загрузки фрагмента
        }
    }

    private void testModel(){
        CollectionReference ordersCollectionRef = FirebaseFirestore.getInstance().collection("Orders");
        ordersCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<ModelOrder> ordersList = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    ModelOrder modelOrder = document.toObject(ModelOrder.class);
                    modelOrder.setOrderId(document.getId());
                    ordersList.add(modelOrder);
                }
                for (ModelOrder order: ordersList){
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost());
                }

            } else {
                Log.e("Firestore", "Error getting documents: ", task.getException());
            }
        });
    }
    private void loadDefaultFragment() {
        // Используйте FragmentManager для управления фрагментами
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Замените PlaceholderFragment на ваш фрагмент
        OrdersFragment fragment = new OrdersFragment();
        fragmentTransaction.replace(R.id.fcv_fragment, fragment);

        fragmentTransaction.commit();
    }

    private void test(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Dishes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("TAG", document.getId() + " => " + document.getData());
                        }
                    } else {
                        Log.w("TAG", "Error getting documents.", task.getException());
                    }
                });
    }
}