package com.find.findus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MypageAdapter extends RecyclerView.Adapter<MypageAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<MypageData> dataList;
    private Context context;

    public MypageAdapter(Context context, List<MypageData> dataList) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_mypage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MypageData currentItem = dataList.get(position);

        String imagePath = currentItem.getTitleImg();

        if (imagePath != null && !imagePath.isEmpty()) {
            // 이미지 경로가 유효한 경우, Glide나 Picasso 등의 라이브러리를 사용하여 이미지를 로드하여 이미지뷰에 설정합니다.
            Glide.with(context)
                    .load(imagePath)
                    .into(holder.titleImg);
        } else {
            // 이미지 경로가 없는 경우 또는 유효하지 않은 경우, 기본 이미지나 특정 이미지를 설정할 수 있습니다.
            holder.titleImg.setImageResource(R.drawable.app_icon);
        }

        holder.reviewTitle.setText(currentItem.getReviewTitle());
        holder.reviewAddress.setText(currentItem.getReviewAddress());
        holder.reviewLikeCount.setText(String.valueOf(currentItem.getReviewLikeCount()));

        int likeCount = Integer.parseInt(holder.reviewLikeCount.getText().toString());
        if (likeCount != 0) {
            holder.review_like_state.setBackgroundResource(R.drawable.vt_like_filled);
        } else {
            holder.review_like_state.setBackgroundResource(R.drawable.vt_like_empty);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView titleImg;
        TextView reviewTitle;
        TextView reviewAddress;
        TextView reviewLikeCount;
        ImageView review_like_state;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleImg = itemView.findViewById(R.id.title_img);
            reviewTitle = itemView.findViewById(R.id.review_title);
            reviewAddress = itemView.findViewById(R.id.review_address);
            reviewLikeCount = itemView.findViewById(R.id.review_like_count);
            review_like_state = itemView.findViewById(R.id.review_like_state);
        }
    }
    // 데이터 설정 메서드 추가
    public void setData(List<MypageData> dataList) {

        this.dataList = dataList;
        notifyDataSetChanged(); // 데이터가 변경되었음을 알림
    }
}
