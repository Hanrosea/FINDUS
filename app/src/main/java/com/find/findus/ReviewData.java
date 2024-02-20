package com.find.findus;

import android.net.Uri;

public class ReviewData {

    private Uri user_profile;
    private String user_name;
    private String review_keyword;
    private String review_title;
    private String review_address;
    private long review_like_nb;
    private String randomUserId;
    private String randomReviewId;
    private String review_image1;
    private String review_image2;
    private String review_image3;

    public ReviewData(Uri user_profile, String user_name, String review_keyword,
                      String review_title, String review_address, long review_like_nb,
                      String randomUserId, String randomReviewId,
                      String review_image1, String review_image2, String review_image3) {

        this.user_profile = user_profile;
        this.user_name = user_name;
        this.review_keyword = review_keyword;
        this.review_title = review_title;
        this.review_address = review_address;
        this.review_like_nb = review_like_nb;
        this.randomUserId = randomUserId;
        this.randomReviewId = randomReviewId;
        this.review_image1 = review_image1;
        this.review_image2 = review_image2;
        this.review_image3 = review_image3;
    }

    public Uri getProfileImg() {
        return user_profile;
    }
    public String getUserName() {
        return user_name;
    }
    public String getReviewKeyword() {
        return review_keyword;
    }
    public String getReviewTitle() {
        return review_title;
    }
    public String getReviewAddress() {
        return review_address;
    }
    public long getReviewLikeCount() {
        return review_like_nb;
    }
    public String getRandomUserId() {
        return randomUserId;
    }
    public String getRandomReviewId() {
        return randomReviewId;
    }

    public String getFstImg() {
        return review_image1;
    }
    public String getTndImg() {
        return review_image2;
    }
    public String getTrdImg() {
        return review_image3;
    }

}