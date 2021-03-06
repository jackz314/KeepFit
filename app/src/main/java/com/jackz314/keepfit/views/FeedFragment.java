package com.jackz314.keepfit.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jackz314.keepfit.databinding.FragmentFeedBinding;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding b;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = FragmentFeedBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        b.textDashboard.setText("Hello! Empty dashboard.");

        return root;
    }
}