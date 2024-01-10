package com.example.employeerestaurantappfirestore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.employeerestaurantappfirestore.MainActivity;
import com.example.employeerestaurantappfirestore.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InputActivity extends AppCompatActivity {
    EditText et_email;
    EditText te_password;
    Button btn_login;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        init();
    }

    private void init(){
        et_email = findViewById(R.id.et_email);
        te_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        mAuth = FirebaseAuth.getInstance();
        btn_login.setOnClickListener(view -> {
            loginUser();
        });
    }

    private void loginUser(){
        String email = et_email.getText().toString().trim();
        String password = te_password.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("FirebaseAuth", "signInWithEmail:success");
                                startActivities(new Intent[]{new Intent(InputActivity.this, MainActivity.class)});
                            } else {
                                Log.w("FirebaseAuth", "signInWithEmail:failure", task.getException());
                                Toast.makeText(InputActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }
}