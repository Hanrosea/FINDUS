package com.find.findus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class fragment_rank extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public fragment_rank() {
    }

    public class UserRank {
        public String userProfile;
        public String userId;
        public String userName;
        public int totalLikes;

        public UserRank(String userProfile, String userId, String userName, int totalLikes) {
            this.userProfile = userProfile;
            this.userId = userId;
            this.userName = userName;
            this.totalLikes = totalLikes;
        }
    }

    public static fragment_rank newInstance(String param1, String param2) {
        fragment_rank fragment = new fragment_rank();
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
        View view = inflater.inflate(R.layout.fragment_rank, container, false);
        LinearLayout usersContainer = view.findViewById(R.id.userRankContainer);

        // Firebase 데이터베이스 참조
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("user");

        // UserRank 객체를 담을 리스트
        List<UserRank> userRankList = new ArrayList<>();

        // 데이터 가져오기 및 처리
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userRankList.clear(); // 리스트 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userProfile = snapshot.child("profileImageUrl").getValue(String.class);
                    String userId = snapshot.getKey();
                    String userName = snapshot.child("name").getValue(String.class);
                    int totalLikes = snapshot.child("totalLikes").getValue(Integer.class);
                    Log.d("유저랭킹", userId + userName + totalLikes);

                    userRankList.add(new UserRank(userProfile, userId, userName, totalLikes));
                }

                // totalLikes를 기준으로 내림차순 정렬
                Collections.sort(userRankList, (user1, user2) -> Integer.compare(user2.totalLikes, user1.totalLikes));
                
                usersContainer.removeAllViews();
                for (int i = 0; i < userRankList.size(); i++) {
                    View userView = inflater.inflate(R.layout.rank_item, usersContainer, false);

                    ImageView userProfileImageView = userView.findViewById(R.id.userProfile);
                    TextView rankingTextView = userView.findViewById(R.id.ranking);
                    TextView userNameTextView = userView.findViewById(R.id.userName);
                    TextView allLikesTextView = userView.findViewById(R.id.allLikes);

                    if (i == 0) {
                        rankingTextView.setBackgroundResource(R.drawable.vt_gold_medal); // 첫 번째 뷰에 금메달 배경 적용
                    } else if (i == 1) {
                        rankingTextView.setBackgroundResource(R.drawable.vt_silver_medal); // 두 번째 뷰에 은메달 배경 적용
                    } else if (i == 2) {
                        rankingTextView.setBackgroundResource(R.drawable.vt_bronze_medal); // 세 번째 뷰에 동메달 배경 적용
                    } else {
                        // 4번째부터 배경 속성 제거
                        rankingTextView.setBackground(null);
                    }

                    if (i >= 3) {
                        // 4번째부터 순위 텍스트 설정
                        rankingTextView.setText(String.valueOf(i + 1));
                    }

                    // 프로필 이미지 설정
                    if (userRankList.get(i).userProfile != null && !userRankList.get(i).userProfile.isEmpty()) {
                        Picasso.get().load(userRankList.get(i).userProfile).into(userProfileImageView);
                    } else {
                        userProfileImageView.setImageResource(R.drawable.app_icon);
                    }

                    userNameTextView.setText(userRankList.get(i).userName);
                    allLikesTextView.setText(String.valueOf(userRankList.get(i).totalLikes));

                    Log.d("반복확인", userRankList.get(i).userName);

                    usersContainer.addView(userView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 에러 처리
            }
        });

        return view;
    }
}