package com.jackz314.keepfit.views.other;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jackz314.keepfit.views.ExploreFollowingFragment;
import com.jackz314.keepfit.views.FeedFragment;
import com.jackz314.keepfit.views.FollowersFragment;
import com.jackz314.keepfit.views.FollowingFragment;
import com.jackz314.keepfit.views.HistoryFragment;
import com.jackz314.keepfit.views.LikedVideosFragment;
import com.jackz314.keepfit.views.UserInfoFragment;
import com.jackz314.keepfit.views.VideosFragment;

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