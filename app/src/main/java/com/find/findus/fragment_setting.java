package com.find.findus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class fragment_setting extends BottomSheetDialogFragment {
    private FirebaseAuth mAuth ;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public fragment_setting() {
        // Required empty public constructor
    }

    public static fragment_setting newInstance(String param1, String param2) {
        fragment_setting fragment = new fragment_setting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Intent intent;

        ImageButton logout_account = view.findViewById(R.id.logout_account);
        ImageButton delete_account = view.findViewById(R.id.delete_account);

        logout_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutAcountPopupDialog();
            }
        });

        delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAcountPopupDialog();
            }
        });

        return view;
    }

    private void revokeAccess() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Firebase Authentication에서 사용자 삭제 성공 시
                        deleteUserDataFromDatabase(userId); // Realtime Database에서 사용자 정보 삭제
                        // Delete images from Firebase Storage
                        StorageReference imagesRef = FirebaseStorage.getInstance().getReference().child("review_images/" + userId);
                        imagesRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
                            @Override
                            public void onComplete(@NonNull Task<ListResult> task) {
                                if (task.isSuccessful()) {
                                    for (StorageReference item : task.getResult().getItems()) {
                                        item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // 파일 삭제 성공 시
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // 파일 삭제 실패 시
                                            }
                                        });
                                    }
                                } else { // 오류 처리
                                }
                            }
                        });
                        // Sign out from Google Sign In
                        mGoogleSignInClient.signOut();
                    } else {
                    }
                }
            });
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
    }

    private void DeleteAcountPopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 팝업 확인 버튼 클릭 시 수행할 작업 추가
                        revokeAccess();

                        getActivity().finish();

                        Intent intent = new Intent(getActivity(), StartingApp.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 팝업 취소 버튼 클릭 시 수행할 작업 추가
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void LogoutAcountPopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 팝업 확인 버튼 클릭 시 수행할 작업 추가
                        signOut();

                        getActivity().finish();

                        Intent intent = new Intent(getActivity(), StartingApp.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 팝업 취소 버튼 클릭 시 수행할 작업 추가
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteUserDataFromDatabase(String userId) {
        DatabaseReference userRef = mDatabase.child("Users").child("user").child(userId);
        DatabaseReference reviewRef = mDatabase.child("Users").child("reviews").child(userId);

        deleteUserData(userRef, "사용자 정보");
        deleteUserData(reviewRef, "사용자 리뷰");
    }
    private void deleteUserData(DatabaseReference ref, String dataType) {
        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 데이터 삭제 성공 시 로그 또는 사용자에게 알림
                Log.d("FirebaseOperation", dataType + " 삭제 성공");
            } else {
                // 데이터 삭제 실패 시 로그 또는 사용자에게 오류 알림
                Log.e("FirebaseOperation", dataType + " 삭제 실패", task.getException());
            }
        });
    }
}