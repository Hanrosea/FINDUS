package com.find.findus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class modal_CreateReview extends DialogFragment implements AdapterView.OnItemSelectedListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private EditText reviewTitle;
    private EditText reviewObject;
    private Spinner spinnerKeyword;
    private TextView textCounter;
    private TextView address_info;
    private ImageView reviewImage1;
    private ImageView reviewImage2;
    private ImageView reviewImage3;
    private int likeCount;
    private Button search_add;
    private Button plusPicture;
    private Button removeImage1;
    private Button removeImage2;
    private Button removeImage3;
    private String spinnerResult;  // 사용자가 선택한 스피너 값 저장 변수
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private static final int PICK_IMAGE_REQUEST = 1;

    public modal_CreateReview() {
        // Required empty public constructor
    }

    public static modal_CreateReview newInstance(String param1, String param2) {
        modal_CreateReview fragment = new modal_CreateReview();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_review, container, false);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // onCreateView 메소드 내에서 user 및 reviewRef 초기화
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reviewRef = null;

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        // R.Id define
        reviewTitle = view.findViewById(R.id.reviewTitle);

        search_add = view.findViewById(R.id.search_add);
        spinnerKeyword = view.findViewById(R.id.spinner_keyword);
        address_info = view.findViewById(R.id.address_info);

        reviewObject = view.findViewById(R.id.reviewObject);
        textCounter = view.findViewById(R.id.textCounter);

        reviewImage1 = view.findViewById(R.id.reviewImage1);
        reviewImage2 = view.findViewById(R.id.reviewImage2);
        reviewImage3 = view.findViewById(R.id.reviewImage3);

        plusPicture = view.findViewById(R.id.plus_picture);

        Button cancelReview = view.findViewById(R.id.cancel_review);
        Button uploadReview = view.findViewById(R.id.upload_review);

        // removeImages define
        removeImage1 = view.findViewById(R.id.removeImage1);
        removeImage2 = view.findViewById(R.id.removeImage2);
        removeImage3 = view.findViewById(R.id.removeImage3);

        removeImage1.setVisibility(View.GONE);
        removeImage2.setVisibility(View.GONE);
        removeImage3.setVisibility(View.GONE);

        // getArguments() 메소드를 통해 데이터를 받아옴
        Bundle bundle = getArguments();
        if (bundle != null) {
            String data = bundle.getString("key");
            // data를 사용하여 작업 수행
        }

        // 리뷰 작성 취소
        cancelReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded() && getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(modal_CreateReview.this).commit();
                }
            }
        });

        // 리뷰 업로드
        // uploadReview 클릭 이벤트 핸들러에서 user 및 reviewRef 초기화
        uploadReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference reviewRef = null;

                // 리뷰 정보 가져오기
                String title = reviewTitle.getText().toString();
                String keyword = spinnerKeyword.getSelectedItem().toString();
                String addressInfo = address_info.getText().toString();
                String reviewObjectText = reviewObject.getText().toString();

                if(!title.equals("") && !keyword.equals("카테고리") && !reviewObjectText.equals("")){
                    // 리뷰 정보를 Map으로 만들기
                    Map<String, Object> reviewInfo = new HashMap<>();
                    reviewInfo.put("reviewTitle", title);
                    reviewInfo.put("keyword", keyword);
                    reviewInfo.put("address_info", addressInfo);
                    reviewInfo.put("reviewObject", reviewObjectText);
                    reviewInfo.put("likeCount", likeCount);

                    // 사용자 정보 및 리뷰 참조 가져오기
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    reviewRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child("reviews")
                            .child(user.getUid())
                            .push(); // push를 사용하여 고유한 식별자 생성

                    reviewInfo.put("userId", user.getUid());
                    reviewInfo.put("userName", user.getDisplayName());
                    reviewInfo.put("profileImageUrl", user.getPhotoUrl().toString());

                    // 리뷰 이미지를 Firebase Storage에 업로드하고 경로를 리뷰 정보에 추가
                    uploadImageToFirebaseStorage(reviewImage1, reviewInfo, user.getUid(), reviewRef.getKey(), "image1");
                    uploadImageToFirebaseStorage(reviewImage2, reviewInfo, user.getUid(), reviewRef.getKey(), "image2");
                    uploadImageToFirebaseStorage(reviewImage3, reviewInfo, user.getUid(), reviewRef.getKey(), "image3");

                    // 현재 시간 가져오기
                    long timestamp = System.currentTimeMillis();

                    // 리뷰 정보에 timestamp 추가
                    reviewInfo.put("timestamp", timestamp);

                    // Firebase에 리뷰 정보 업로드
                    if (user != null && reviewRef != null) {
                        reviewRef.setValue(reviewInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "리뷰가 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show();
                                            dismiss();
                                        } else {
                                            Toast.makeText(getContext(), "리뷰 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else if(title.equals("")){
                    Toast.makeText(getContext(), "리뷰 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (keyword.equals("카테고리")) {
                    Toast.makeText(getContext(), "키워드를 선택해주세요.", Toast.LENGTH_SHORT).show();
                } else if(reviewObjectText.equals("")){
                    Toast.makeText(getContext(), "리뷰를 작성해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // TextWatcher to count characters
        reviewObject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Called before text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Called when text is changed
                int currentLength = charSequence.length();
                textCounter.setText(currentLength + "/500");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Called after text is changed
            }
        });


        // Image selection button click listener

        plusPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery
                if (reviewImage1.getDrawable() == null || reviewImage2.getDrawable() == null || reviewImage3.getDrawable() == null) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                } else {
                    overImagePopupDialog();
                }
            }
        });

        removeImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImagePopupDialog(removeImage1);
            }
        });
        removeImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImagePopupDialog(removeImage2);
            }
        });
        removeImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImagePopupDialog(removeImage3);
            }
        });

        // ArrayAdapter를 이용하여 스피너에 데이터 설정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.spinner_keyword,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKeyword.setAdapter(adapter);

        // OnItemSelectedListener를 설정
        spinnerKeyword.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        return view;
    }

    private void setSelectedImage(ImageView imageView, Bitmap selectedImageBitmap, Button removeButton) {
        imageView.setImageBitmap(selectedImageBitmap);
        removeButton.setVisibility(View.VISIBLE);
    }

    // Handle result from image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Get selected image Uri
            Uri selectedImageUri = data.getData();

            // Convert Uri to Bitmap
            Bitmap selectedImageBitmap = null;
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Set selected image Bitmap to the first available ImageView
            if (reviewImage1.getDrawable() == null) {
                setSelectedImage(reviewImage1, selectedImageBitmap, removeImage1);
            } else if (reviewImage2.getDrawable() == null) {
                setSelectedImage(reviewImage2, selectedImageBitmap, removeImage2);
            } else if (reviewImage3.getDrawable() == null) {
                setSelectedImage(reviewImage3, selectedImageBitmap, removeImage3);
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Adjust size (e.g., 80% width, 80% height)
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.95);

        getDialog().getWindow().setLayout(width, height);

    }

    private void removeImages(Button remove_btn) {
        if(remove_btn == removeImage1){
            reviewImage1.setImageDrawable(null);
            removeImage1.setVisibility(View.GONE);
        } else if(remove_btn == removeImage2){
            reviewImage2.setImageDrawable(null);
            removeImage2.setVisibility(View.GONE);
        } else {
            reviewImage3.setImageDrawable(null);
            removeImage3.setVisibility(View.GONE);
        }
    }

    private void overImagePopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("사진은 최대 3장만 가능합니다")
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 팝업 취소 버튼 클릭 시 수행할 작업 추가
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeImagePopupDialog(Button removeImage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 팝업 확인 버튼 클릭 시 수행할 작업 추가
                        removeImages(removeImage);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // 선택된 카테고리를 가져와서 spinnerResult에 저장함
        spinnerResult = parent.getItemAtPosition(position).toString();
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 아무것도 선택되지 않았을 때의 동작을 구현
    }

    // 이미지를 Firebase Storage에 업로드하고 해당 다운로드 URL을 리뷰 정보에 추가하는 메서드 추가
    private void uploadImageToFirebaseStorage(ImageView imageView, Map<String, Object> reviewInfo, String userId, String reviewId, String imageKey) {
        if (imageView.getDrawable() != null) {
            // 사용자 ID와 리뷰 ID를 사용하여 폴더 구조를 생성합니다.
            String folderPath = "review_images/" + userId + "/" + reviewId + "/";

            // Firebase Storage에서 폴더에 대한 참조를 얻습니다.
            StorageReference folderRef = storageRef.child(folderPath);

            // 이미지를 폴더 내에서 고유한 파일 이름으로 만듭니다.
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = folderRef.child(imageName);

            // 이미지를 ByteArray로 변환합니다.
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Firebase Storage에 이미지를 업로드합니다.
            UploadTask uploadTask = imageRef.putBytes(data);

            // 업로드가 완료되면 이미지의 다운로드 URL을 리뷰 정보에 추가합니다.
            uploadTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        reviewInfo.put(imageKey, imageUrl);
                    });
                }
            });
        }
    }

}