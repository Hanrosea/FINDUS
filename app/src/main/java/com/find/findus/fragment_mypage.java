package com.find.findus;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


public class fragment_mypage extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private List<MypageData> mypageList = new ArrayList<>();
    private MypageAdapter adapter; // 어댑터 변수 추가
    private SwipeRefreshLayout swipeRefreshLayout;

    public fragment_mypage() {
        // Required empty public constructor
    }

    public static fragment_mypage newInstance(String param1, String param2) {
        fragment_mypage fragment = new fragment_mypage();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        setupUI(view);

        initializeRecyclerView(view);
        loadReviews();

        setupSwipeRefreshLayout(view);

        return view;
    }

    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.myPageRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MypageAdapter(requireContext(), mypageList);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefreshLayout(View view) {
        SwipeRefreshLayout mSwipeRefreshLayout = view.findViewById(R.id.mypage_swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mypageList.clear();
            loadReviews();
            new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 2000);
        });
    }

    private void setupUI(View view) {
        setupUserProfile(view);
        setupButtons(view);
    }

    private void setupUserProfile(View view) {
        TextView reviews_count = view.findViewById(R.id.reviews_count);
        TextView reviews_followers = view.findViewById(R.id.reviews_followers);
        TextView reviews_likes = view.findViewById(R.id.reviews_likes);

        ImageView profile_Image = view.findViewById(R.id.profile_Image);
        TextView userName = view.findViewById(R.id.userName);

        // Firebase에서 사용자 정보 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Realtime Database에서 사용자 정보 가져오기
            DatabaseReference userReviewRef = FirebaseDatabase.getInstance().getReference().child("Users").child("reviews").child(user.getUid());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("user").child(user.getUid());

            // 리뷰 목록 개수 세기
            userReviewRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long reviewsCount = dataSnapshot.getChildrenCount();
                    reviews_count.setText(String.valueOf(reviewsCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 처리 중 에러 발생 시
                }
            });

            // 모든 리뷰의 likeCount 값 합산하기
            userReviewRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long totalLikes = 0;
                    for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                        // Null 체크를 추가하여 NullPointerException 방지
                        Object likeCountObj = reviewSnapshot.child("likeCount").getValue();
                        if (likeCountObj != null) {
                            long likeCount = (long) likeCountObj;
                            totalLikes += likeCount;
                        }
                    }
                    reviews_likes.setText(String.valueOf(totalLikes));

                    // totalLikes 값을 현재 사용자의 "totalLikes" 필드에 저장
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("user").child(user.getUid());
                    userRef.child("totalLikes").setValue(totalLikes);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 처리 중 에러 발생 시
                }
            });

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // 사용자의 이름 가져오기
                        String displayName = dataSnapshot.child("name").getValue(String.class);
                        if (displayName != null && !displayName.isEmpty()) {
                            userName.setText(displayName);
                        }

                        // 프로필 이미지 URL 가져오기
                        String photoUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        // 프로필 이미지 설정
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Picasso.get().load(photoUrl).into(profile_Image);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 에러 처리를 적절히 수행하세요.
                }
            });
        }
    }

    private void setupButtons(View view) {
        Button setting = view.findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingBottomSheetDialog();
            }
        });

        Button showAlertButton = view.findViewById(R.id.newReview);
        showAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator rotation = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
                rotation.setDuration(500); // 500 milliseconds
                rotation.start();

                showPopupDialog();
            }
        });
    }

    private void SettingBottomSheetDialog() {
        BottomSheetDialogFragment bottomSheetFragment = new fragment_setting();
        bottomSheetFragment.setStyle(fragment_setting.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme);
        bottomSheetFragment.show(getFragmentManager(), "create_review_dialog");
    }

    // 팝업
    private void showPopupDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage("리뷰를 추가하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> NewReviewModalDialog())
                .setNegativeButton("취소", null)
                .show();
    }

    // fragment_create_review를 모달 다이얼로그로 띄우는 메서드
    private void NewReviewModalDialog() {
        DialogFragment dialogFragment = new modal_CreateReview();
        dialogFragment.show(getFragmentManager(), "create_review_dialog");
    }

    private void loadReviews() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Firebase에서 사용자의 리뷰 데이터 가져오기
        if (user != null) {
            String currentUserId = user.getUid();
            DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child("reviews")
                    .child(currentUserId);

            Query query = reviewsRef.orderByChild("timestamp"); // timestamp를 기준으로 정렬하고, 마지막 10개의 리뷰만 가져오기

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mypageList.clear(); // 데이터 로드 전 목록 클리어

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // 리뷰 데이터 가져와서 mypageList에 추가
                            String reviewTitle = snapshot.child("reviewTitle").getValue(String.class);
                            String addressInfo = snapshot.child("address_info").getValue(String.class);
                            Long likeCount = snapshot.child("likeCount").getValue(Long.class);
                            String userId = currentUserId; // 유저 ID 가져오기
                            String reviewId = snapshot.getKey(); // 리뷰 ID 가져오기

                            // 이미지 다운로드 URL 가져오기
                            StorageReference imagesRef = FirebaseStorage.getInstance().getReference()
                                    .child("review_images")
                                    .child(userId)
                                    .child(reviewId);
                            fetchImageUrl(imagesRef, imageUrl -> {
                                // 이미지 URL을 가져온 후 리스트에 아이템 추가
                                mypageList.add(new MypageData(imageUrl, reviewTitle, addressInfo, likeCount != null ? likeCount : 0));
                                // 어댑터에 데이터 변경 알림
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 에러 처리를 적절히 수행하세요.
                }
            });
        }
    }

    private void fetchImageUrl(StorageReference imagesRef, Consumer<String> onImageLoaded) {
        imagesRef.listAll().addOnSuccessListener(listResult -> {
            if (!listResult.getItems().isEmpty()) {
                listResult.getItems().get(0).getDownloadUrl().addOnSuccessListener(uri -> {
                    // 이미지 URL 로드 성공
                    onImageLoaded.accept(uri.toString());
                }).addOnFailureListener(e -> {
                    // 실패 처리, 기본 이미지 사용
                    onImageLoaded.accept("https://firebasestorage.googleapis.com/v0/b/findus-f954a.appspot.com/o/review_images%2Fdefault_title_img%2Fapp_icon.png?alt=media&token=94272165-d69e-4a6a-b344-da8836048cc6");
                });
            } else {
                // 파일이 없는 경우, 기본 이미지 사용
                onImageLoaded.accept("https://firebasestorage.googleapis.com/v0/b/findus-f954a.appspot.com/o/review_images%2Fdefault_title_img%2Fapp_icon.png?alt=media&token=94272165-d69e-4a6a-b344-da8836048cc6");
            }
        }).addOnFailureListener(e -> {
            // 경로 목록 가져오기 실패 처리
            onImageLoaded.accept("https://firebasestorage.googleapis.com/v0/b/findus-f954a.appspot.com/o/review_images%2Fdefault_title_img%2Fapp_icon.png?alt=media&token=94272165-d69e-4a6a-b344-da8836048cc6");
        });
    }
}