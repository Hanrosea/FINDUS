package com.find.findus;

public class MypageData {

    private String title_img;
    private String review_title;
    private String review_address;
    private long review_like_count;

    public MypageData(String title_img, String review_title, String review_address, long review_like_count) {
        this.title_img = title_img;
        this.review_title = review_title;
        this.review_address = review_address;
        this.review_like_count = review_like_count;
    }

    public String getTitleImg() {
        return title_img;
    }

    public String getReviewTitle() {
        return review_title;
    }

    public String getReviewAddress() {
        return review_address;
    }

    public long getReviewLikeCount() {
        return review_like_count;
    }

}
