package com.jackz314.keepfit.views.other;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jackz314.keepfit.views.ExploreFollowingFragment;
import com.jackz314.keepfit.views.FeedFragment;

public class ExploreCollectionAdapter extends FragmentStateAdapter {
    public ExploreCollectionAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)

        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new ExploreFollowingFragment();
                break;
            default:
                fragment = new FeedFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}