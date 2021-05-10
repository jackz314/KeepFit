package com.jackz314.keepfit.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayoutMediator;
import com.jackz314.keepfit.databinding.FragmentExploreBinding;
import com.jackz314.keepfit.views.other.ExploreCollectionAdapter;

public class ExploreFragment extends Fragment {

    private static final String TAG = "ExploreFragment";

    private FragmentExploreBinding b;
    private ViewPager viewPager;

    private ExploreCollectionAdapter exploreCollectionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (b == null) {
            // view binding ftw!
            b = FragmentExploreBinding.inflate(inflater, container, false);
            exploreCollectionAdapter = new ExploreCollectionAdapter(this);
            b.exploreViewPager.setAdapter(exploreCollectionAdapter);
            b.exploreViewPager.setSaveEnabled(false);
            b.exploreViewPager.setCurrentItem(1);
            //b.exploreTabLayout.selectTab(b.exploreTabLayout.getTabAt(1));

            TabLayoutMediator mediator = new TabLayoutMediator(b.exploreTabLayout, b.exploreViewPager, ((tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Following");
                        break;
                    case 1:
                        tab.setText("Feed");
                        break;
                }
            }));
            mediator.attach();
        }
        return b.getRoot();
//        return inflater.inflate(R.layout.fragment_me, container, false);
    }
}