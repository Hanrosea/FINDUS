package com.find.findus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MakeAccActivity extends AppCompatActivity {

    private EditText createIdEditText, createPwEditText, userNameEditText,userEmailEditText;
    private Button createAccountButton, checkingDuplicateButton;
    private TextView checkingMessageText;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_acc);

        // 상태바 색상 변경
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // 상태바 글씨 색상을 어두운 색으로 변경 (흰색 배경에 맞춤)
        getWindow().setStatusBarColor(Color.WHITE); // 상태바 배경을 흰색으로 변경

        // UI 컴포넌트 초기화
        createIdEditText = findViewById(R.id.create_id);
        createPwEditText = findViewById(R.id.create_pw);
        createAccountButton = findViewById(R.id.create_account);
        userNameEditText = findViewById(R.id.userName);
        userEmailEditText = findViewById(R.id.userEmail);
        checkingMessageText = findViewById(R.id.checking_message);

        checkingDuplicateButton = findViewById(R.id.checking_duplicate);
        // 중복 확인 버튼 클릭 이벤트 처리
        checkingDuplicateButton.setOnClickListener(view -> {
            String userId = createIdEditText.getText().toString().trim();
            if(userId.equals("")){
                checkingMessageText.setText("아이디 중복 확인");
                checkingMessageText.setTextColor(Color.parseColor("#000000")); // 검정색 설정
            } else {
                checkIfUserIdExists(userId, exists -> {
                    if (exists) {
                        checkingMessageText.setText("이미 존재하는 아이디입니다");
                        checkingMessageText.setTextColor(Color.parseColor("#E60000")); // 빨간색 설정
                    } else {
                        checkingMessageText.setText("적절한 아이디입니다");
                        checkingMessageText.setTextColor(Color.parseColor("#009688")); // 초록색 설정
                    }
                });
            }
        });
        createAccount(checkingMessageText);
    }

    // ID가 이미 존재하는지 확인하는 함수
    private void checkIfUserIdExists(String userId, final OnCheckUserListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child("user").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onChecked(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                listener.onChecked(false);
            }
        });
    }

    // 결과 처리를 위한 리스너 인터페이스
    public interface OnCheckUserListener {
        void onChecked(boolean exists);
    }

    private void createAccount(TextView checkingMessageText){
        // Firebase 초기화
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 계정 생성 버튼 클릭 리스너
        createAccountButton.setOnClickListener(view -> {
            if(checkingMessageText.getText().equals("이미 존재하는 아이디입니다") || checkingMessageText.getText().equals("아이디 중복 확인")){
                Toast.makeText(MakeAccActivity.this, "아이디 중복을 확인해주세요", Toast.LENGTH_SHORT).show();
            }else if(checkingMessageText.getText().equals("적절한 아이디입니다")){

                String id = createIdEditText.getText().toString().trim();
                String pw = createPwEditText.getText().toString().trim();
                String userName = userNameEditText.getText().toString().trim();
                String userEmail = userEmailEditText.getText().toString().trim();

                // 입력값 검증
                if (!id.isEmpty() && !pw.isEmpty() && !userName.isEmpty() && !userEmail.isEmpty()) {
                    // Users/user/ 하위에 데이터 생성
                    DatabaseReference userRef = mDatabase.child("Users").child("user").child(id);

                    // 기본값 설정
                    userRef.child("account_id").setValue(id);
                    userRef.child("name").setValue(userName);
                    userRef.child("userEmail").setValue(userEmail);
                    userRef.child("profileImageUrl").setValue("");
                    userRef.child("totalLikes").setValue(0);
                    userRef.child("password").setValue(pw);

                    Toast.makeText(MakeAccActivity.this, "계정이 생성되었습니다.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MakeAccActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(MakeAccActivity.this, "ID와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
