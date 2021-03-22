package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.databinding.FragmentMeBinding;

import java.util.Objects;

import us.zoom.sdk.AccountService;
import us.zoom.sdk.ZoomSDK;

import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;

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
                        tab.setText("Liked Videos");
                        break;
                    case 3:
                        tab.setText("Followers");
                        break;
                    case 4:
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