package com.find.findus;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class fragment_review extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private ReviewAdapter adapter;
    private DatabaseReference mDatabase;
    private List<ReviewData> reviewList = new ArrayList<>();
    private Set<String> selectedUserIds = new HashSet<>();
    private Set<String> selectedReviewIds = new HashSet<>();

    public fragment_review() {
        // Required empty public constructor
    }

    public static fragment_review newInstance(String param1, String param2) {
        fragment_review fragment = new fragment_review();
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
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        initializeRecyclerView(view);
        loadReviews();

        setupSwipeRefreshLayout(view);

        return view;
    }

    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReviewAdapter(requireContext(), reviewList);
        recyclerView.setAdapter(adapter);
    }

    private void loadReviews() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("reviews");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userIds.add(userSnapshot.getKey());
                }
                selectRandomUsers(userIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void selectRandomUsers(List<String> userIds) {
        Random random = new Random();
        while (selectedUserIds.size() < 10 && userIds.size() > selectedUserIds.size()) {
            String randomUserId = userIds.get(random.nextInt(userIds.size()));
            selectedUserIds.add(randomUserId);

            loadUserReviews(randomUserId);
        }
    }

    private void loadUserReviews(String userId) {
        mDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    String reviewId = reviewSnapshot.getKey();
                    if (!selectedReviewIds.contains(reviewId)) {
                        selectedReviewIds.add(reviewId);
                        loadReviewData(userId, reviewId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadReviewData(String userId, String reviewId) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference().child("Users").child("reviews").child(userId).child(reviewId);
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 리뷰 데이터를 ReviewData 객체로 변환
                String address_info = snapshot.child("address_info").getValue(String.class);
                String keyword = snapshot.child("keyword").getValue(String.class);
                String reviewTitle = snapshot.child("reviewTitle").getValue(String.class);
                String profileImageUriString = snapshot.child("profileImageUrl").getValue(String.class);
                Uri profileImageUri = Uri.parse(profileImageUriString);

                String name = snapshot.child("userName").getValue(String.class);

                Log.d("유저 아이디를 불러온 목록", String.valueOf(address_info + " " + keyword + " " + reviewTitle));

                Long likeCountObject = snapshot.child("likeCount").getValue(Long.class);
                long likeCount = (likeCountObject != null) ? likeCountObject.longValue() : 0; // 기본값은 0으로 설정하거나, 다른 값으로 설정 가능

                // 파이어스토어에서 이미지 다운로드 URL 가져오기
                StorageReference imagesRef = FirebaseStorage.getInstance().getReference()
                        .child("review_images")
                        .child(userId)
                        .child(reviewId);

                // 기본 이미지 설정
                StorageReference defaultRef = FirebaseStorage.getInstance().getReference()
                        .child("review_images")
                        .child("default_title_img")
                        .child("app_icon.png");

                imagesRef.listAll().addOnSuccessListener(listResult -> {
                    List<Task<Uri>> urlTasks = new ArrayList<>();
                    for (StorageReference itemRef : listResult.getItems().subList(0, Math.min(3, listResult.getItems().size()))) {
                        urlTasks.add(itemRef.getDownloadUrl());
                    }

                    // 기본 이미지 URL들을 미리 추가
                    while (urlTasks.size() < 3) {
                        urlTasks.add(defaultRef.getDownloadUrl());
                    }

                    Tasks.whenAllSuccess(urlTasks).addOnSuccessListener(urls -> {
                        String fstImg = urls.size() > 0 ? urls.get(0).toString() : defaultRef.toString();
                        String tndImg = urls.size() > 1 ? urls.get(1).toString() : defaultRef.toString();
                        String trdImg = urls.size() > 2 ? urls.get(2).toString() : defaultRef.toString();
                        Log.d("이미지 url", fstImg);
                        Log.d("이미지 url", tndImg);
                        Log.d("이미지 url", trdImg);
                        reviewList.add(new ReviewData(profileImageUri, name, keyword, reviewTitle, address_info, likeCount, userId, reviewId, fstImg, tndImg, trdImg));
                        adapter.notifyDataSetChanged(); // 데이터 변경 알림
                    });
                }).addOnFailureListener(e -> {
                    // 이미지 목록 가져오기 실패 처리
                    String defaultUrl = defaultRef.toString();
                    reviewList.add(new ReviewData(profileImageUri, name, keyword, reviewTitle, address_info, likeCount, userId, reviewId, defaultUrl, defaultUrl, defaultUrl));
                    adapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setupSwipeRefreshLayout(@NonNull View view) {
        SwipeRefreshLayout mSwipeRefreshLayout = view.findViewById(R.id.review_swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            selectedUserIds.clear();
            selectedReviewIds.clear();
            reviewList.clear();
            loadReviews();
            new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 2000);
        });
    }
}