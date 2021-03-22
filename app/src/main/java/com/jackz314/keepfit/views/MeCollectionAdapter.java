package com.jackz314.keepfit.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MeCollectionAdapter extends FragmentStateAdapter {
    public MeCollectionAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)

        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new UserInfoFragment();
                break;
            case 1:
                fragment = new VideosFragment();
                break;
            case 2:
                fragment = new LikedVideosFragment();
                break;
            case 3:
                fragment = new FollowersFragment();
                break;
            default:
                fragment = new FollowingFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}