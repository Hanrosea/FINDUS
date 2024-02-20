package com.find.findus;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<ReviewData> dataList;
    private Context context;

    public ReviewAdapter(Context context, List<ReviewData> dataList) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewData currentItem = dataList.get(position);

        Uri imagePath = currentItem.getProfileImg();
        if (imagePath != null) {
            // Glide를 사용하여 Uri 형식의 이미지를 로드하여 이미지뷰에 설정합니다.
            Glide.with(context)
                    .load(imagePath)
                    .into(holder.user_profile);
        } else {
            // 이미지 경로가 없는 경우 또는 유효하지 않은 경우, 기본 이미지나 특정 이미지를 설정할 수 있습니다.
            holder.user_profile.setImageResource(R.drawable.app_icon);
        }

        holder.user_name.setText(currentItem.getUserName());
        holder.review_keyword.setText(String.valueOf(currentItem.getReviewKeyword()));
        holder.review_title.setText(String.valueOf(currentItem.getReviewTitle()));
        holder.review_address.setText(String.valueOf(currentItem.getReviewAddress()));

        // 리뷰 이미지 로드
        loadImageIntoView(currentItem.getFstImg(), holder.review_image1);
        loadImageIntoView(currentItem.getTndImg(), holder.review_image2);
        loadImageIntoView(currentItem.getTrdImg(), holder.review_image3);

        // 버튼 클릭 이벤트 처리
        holder.review_like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebase 데이터베이스에서 해당 리뷰의 좋아요 카운트를 가져와서 변경
                DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child("reviews").child(currentItem.getRandomUserId())
                        .child(currentItem.getRandomReviewId()).child("likeCount");

                reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // 현재 좋아요 카운트 가져오기
                        Long currentLikeCount = snapshot.getValue(Long.class);
                        if (currentLikeCount != null) {
                            long newLikeCount;
                            if (holder.review_like_btn.getBackground().getConstantState()
                                    .equals(context.getResources().getDrawable(R.drawable.vt_like_empty).getConstantState())) {
                                // vt_like_empty인 경우: 좋아요 카운트 증가
                                newLikeCount = currentLikeCount + 1;
                                // 백그라운드를 vt_like_filled로 변경
                                holder.review_like_btn.setBackgroundResource(R.drawable.vt_like_filled);
                            } else {
                                // vt_like_filled인 경우: 좋아요 카운트 감소
                                newLikeCount = currentLikeCount - 1;
                                // 백그라운드를 vt_like_empty로 변경
                                holder.review_like_btn.setBackgroundResource(R.drawable.vt_like_empty);
                            }
                            // Firebase 데이터베이스에 업데이트
                            reviewRef.setValue(newLikeCount);
                            // UI 업데이트
                            holder.review_like_nb.setText(String.valueOf(newLikeCount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 데이터베이스 읽기 실패 처리
                        Log.e("Firebase", "Failed to read value.", error.toException());
                    }
                });
            }
        });

        holder.review_like_nb.setText(String.valueOf(currentItem.getReviewLikeCount()));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView user_profile;
        TextView user_name;
        TextView review_keyword;
        TextView review_title;
        TextView review_address;
        Button review_like_btn;
        TextView review_like_nb;

        ImageView review_image1;
        ImageView review_image2;
        ImageView review_image3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_profile = itemView.findViewById(R.id.user_profile);
            user_name = itemView.findViewById(R.id.user_name);
            review_keyword = itemView.findViewById(R.id.review_keyword);
            review_title = itemView.findViewById(R.id.review_title);
            review_address = itemView.findViewById(R.id.review_address);
            review_like_btn = itemView.findViewById(R.id.review_like_btn);
            review_like_nb = itemView.findViewById(R.id.review_like_nb);

            review_image1 = itemView.findViewById(R.id.review_image1);
            review_image2 = itemView.findViewById(R.id.review_image2);
            review_image3 = itemView.findViewById(R.id.review_image3);
        }
    }
    // 데이터 설정 메서드 추가
    public void setData(List<ReviewData> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged(); // 데이터가 변경되었음을 알림
    }
    // 이미지 URL을 ImageView에 로드하는 도우미 메서드
    private void loadImageIntoView(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.logo); // 기본 이미지 설정
        }
    }
}
