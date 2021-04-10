package com.jackz314.keepfit.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.jackz314.keepfit.databinding.FragmentMeBinding;
import com.jackz314.keepfit.views.other.MeCollectionAdapter;

public class MeFragment extends Fragment {

    private static final String TAG = "MeFragment";

    private FragmentMeBinding b;

    private MeCollectionAdapter meCollectionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (b == null){
            // view binding ftw!
            b = FragmentMeBinding.inflate(inflater, container, false);
            meCollectionAdapter = new MeCollectionAdapter(this);
            b.meViewPager.setAdapter(meCollectionAdapter);
            b.meViewPager.setSaveEnabled(false);
            TabLayoutMediator mediator = new TabLayoutMediator(b.meTabLayout, b.meViewPager, ((tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Info");
                        break;
                    case 1:
                        tab.setText("My Videos");
                        break;
                    case 2:
                        tab.setText("Watch History");
                        break;
                    case 3:
                        tab.setText("Liked Videos");
                        break;
                    case 4:
                        tab.setText("Followers");
                        break;
                    case 5:
                        tab.setText("Following");
                        break;
                }
            }));
            mediator.attach();
        }
        return b.getRoot();
//        return inflater.inflate(R.layout.fragment_me, container, false);
    }
}