package com.find.findus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
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

                        // 데이터베이스에 사용자 정보 저장
                        saveUserInfoToDatabase(userInfo);
                    }

                    Toast.makeText(getApplicationContext(), "환영합니다", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // mDatabase를 초기화하기 위해 이 메서드를 추가합니다.
    private void initDatabase() {
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
}