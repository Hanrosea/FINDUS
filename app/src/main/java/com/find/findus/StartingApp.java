package com.find.findus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartingApp extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_app);

        mAuth = FirebaseAuth.getInstance();

        // 일정 시간(예: 2000 밀리초) 후에 LoginActivity 또는 MainActivity로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Intent intent;

                if (currentUser != null) {
                    // 이미 로그인된 사용자가 있는 경우
                    intent = new Intent(StartingApp.this, MainActivity.class);
                } else {
                    // 사용자가 로그인되어 있지 않은 경우
                    intent = new Intent(StartingApp.this, LoginActivity.class);
                }

                startActivity(intent);

                // 현재 액티비티를 종료하려면 아래 코드를 추가
                finish();
            }
        }, 2000); // 2000 밀리초 (2초) 지연
    }
}
