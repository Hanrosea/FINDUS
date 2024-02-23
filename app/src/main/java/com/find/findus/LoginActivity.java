package com.find.findus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DatabaseReference mDatabase;
    private EditText edit_id, edit_pw;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 상태바 색상 변경
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // 상태바 글씨 색상을 어두운 색으로 변경 (흰색 배경에 맞춤)
        getWindow().setStatusBarColor(Color.WHITE); // 상태바 배경을 흰색으로 변경

        init();

        Button make_account = findViewById(R.id.make_account);
        make_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MakeAccActivity.class);
                startActivity(intent);
            }
        });

        // UI 컴포넌트 초기화
        edit_id = findViewById(R.id.edit_id);
        edit_pw = findViewById(R.id.edit_pw);
        login = findViewById(R.id.login);

        // 로그인 버튼 클릭 리스너 설정
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyUser();
            }
        });
    }

    private void init() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result){
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intent = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e){ }
                }
            }
        });

        configSignIn();
        initAuth();
        initDatabase(); // 이 줄을 추가하여 mDatabase를 초기화합니다.

        // signIn_btn에 대한 클릭 리스너 추가
        Button signIn_btn = findViewById(R.id.signIn_btn);
        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(); // signIn 메서드 호출
            }
        });
    }

    private void configSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initAuth(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void onStart(){
        super.onStart();
        if(isUserNonNull()){
            updateUI();
        }
    }

    private boolean isUserNonNull(){
        if (mAuth.getCurrentUser() == null){
            return false;
        } else {
            return true;
        }
    }

    private void updateUI(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);

        finish();
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activityResultLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            public void onComplete(@NonNull Task<AuthResult>task){
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();

                    // 추가된 부분: 데이터베이스에 사용자 정보 저장
                    if (user != null) {
                        String userId = user.getUid();
                        String userName = user.getDisplayName();
                        String userEmail = user.getEmail();
                        String profileImageUrl = user.getPhotoUrl().toString(); // 프로필 사진 URL
                        long totalLikes = 0;

                        // 사용자 정보를 HashMap으로 만들기
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("account_id", userId);
                        userInfo.put("name", userName);
                        userInfo.put("email", userEmail);
                        userInfo.put("profileImageUrl", profileImageUrl);
                        userInfo.put("totalLikes", totalLikes);
                        userInfo.put("password", "");

                        // 데이터베이스에 사용자 정보 저장
                        saveUserInfoToDatabase(userInfo);
                    }
                    Toast.makeText(getApplicationContext(), "환영합니다", Toast.LENGTH_LONG).show();
                    updateUI();

                } else {
                    Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initDatabase() {
        // mDatabase를 초기화하기 위해 이 메서드를 추가합니다.
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void saveUserInfoToDatabase(Map<String, Object> userInfo) {
        // 데이터베이스에서 user 레퍼런스를 가져옴
        DatabaseReference userRef = mDatabase.child("Users").child("user");

        // 사용자 정보 업데이트
        userRef.child(userInfo.get("account_id").toString()).setValue(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "사용자 정보가 성공적으로 저장되었습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "사용자 정보 저장에 실패했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void verifyUser() {
        final String userId = edit_id.getText().toString().trim();
        final String userPw = edit_pw.getText().toString().trim();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child("user");

        // 사용자 ID 존재 여부 확인
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 사용자 ID가 존재할 경우, 비밀번호 확인
                    String password = dataSnapshot.child("password").getValue(String.class);
                    if (password != null && password.equals(userPw)) {
                        // 비밀번호 일치
                        updateUI();
                    } else {
                        // 비밀번호 불일치
                        Toast.makeText(LoginActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 사용자 ID가 존재하지 않음
                    Toast.makeText(LoginActivity.this, "존재하지 않는 사용자 ID입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류
                Toast.makeText(LoginActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}