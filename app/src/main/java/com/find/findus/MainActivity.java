package com.find.findus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyFragmentAdapter(this));

        new TabLayoutMediator((TabLayout) findViewById(R.id.tabLayout), viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("랭킹");
                    break;
                case 1:
                    tab.setText("리뷰");
                    break;
                case 2:
                    tab.setText("내정보");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
        }).attach();

        viewPager.setCurrentItem(1, false);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish(); // 앱 종료
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 다이얼로그를 닫음 (아무 동작 없음)
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class MyFragmentAdapter extends FragmentStateAdapter {
        public MyFragmentAdapter(AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public int getItemCount() {
            return 3; // 프레그먼트 개수
        }

        @Override
        public Fragment createFragment(int position) {
            // 해당 위치에 따라 다른 프레그먼트를 반환
            switch (position) {
                case 0:
                    return new fragment_rank();
                case 1:
                    return new fragment_review();
                case 2:
                    return new fragment_mypage();
                default:
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
        }
    }
}
