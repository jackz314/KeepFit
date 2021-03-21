package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends ListFragment {

    private static final String TAG = "FollowerFragment";

    private FragmentFeedBinding b;
    private FirebaseFirestore db;

    private List<String> followersList = new ArrayList<>();
    private Executor procES = Executors.newSingleThreadExecutor();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        followersList.add("Patrick");
        followersList.add("Jack");
        followersList.add("Steven");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                R.layout.activity_listview, followersList);

        setListAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("users", FirebaseAuth.getInstance().getCurrentUser())
                .collection("followers")
                .get()
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        followersList.clear();
                        for (DocumentSnapshot DocumentSnapshot : value) {
                            followersList.add(DocumentSnapshot);
                        }

                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                        Log.d(TAG, "followers colection update: " + followersList);
                    });
                });

        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        return view;
    }

}
