package com.jackz314.keepfit.views;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jackz314.keepfit.R;

public class ProfilePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        FollowersFragment frg = new FollowersFragment();
        FollowingFragment frg1 = new FollowingFragment();
        VideosFragment frg2 = new VideosFragment();
        LikedVideosFragment frg3 = new LikedVideosFragment();
        MeFragment frg4 = new MeFragment();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.followers_fragment, frg).commit();
        fm.beginTransaction().replace(R.id.following_fragment, frg1).commit();
        fm.beginTransaction().replace(R.id.videos_fragment, frg2).commit();
        fm.beginTransaction().replace(R.id.liked_videos_fragment, frg3).commit();
        fm.beginTransaction().replace(R.id.user_info_fragment, frg4).commit();
    }
}
