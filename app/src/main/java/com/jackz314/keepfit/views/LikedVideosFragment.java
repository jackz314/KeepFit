package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LikedVideosFragment extends Fragment {

    private static final String TAG = "VideosFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;
    private com.jackz314.keepfit.databinding.FragmentFeedBinding b;

    private List<Media> videosList = new ArrayList<>();
    private List<String> videoIdList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = com.jackz314.keepfit.databinding.FragmentFeedBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        b.emptyFeedText.setText("Hello! Empty feed.");
        b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        feedRecyclerAdapter = new FeedRecyclerAdapter(getContext(), videosList);
        feedRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videosList.get(position).getLink())));
        });
        b.feedRecycler.setAdapter(feedRecyclerAdapter);

        ub = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        System.out.println("here");

        db.collection("users")
                .document(ub.getEmail())
                .collection("liked_videos")
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        videoIdList.clear();

                        for (QueryDocumentSnapshot document : value) {
                            videoIdList.add((String) document.get("id"));
                        }
                        requireActivity().runOnUiThread(() -> feedRecyclerAdapter.notifyDataSetChanged());
                        Log.d(TAG, "videos collection update: " + videoIdList);

                        for (String videoId : videoIdList) {
                            System.out.println("HERE");
                            db.collection("media")
                                    .document(videoId)
                                    .addSnapshotListener((value1, e1) -> {
                                        if (e != null || value1 == null) {
                                            Log.w(TAG, "Listen failed.", e);
                                            return;
                                        }
                                        procES.execute(() -> {
                                            videosList.clear();
                                            videosList.add(new  Media (value1));
                                            requireActivity().runOnUiThread(() -> feedRecyclerAdapter.notifyDataSetChanged());
                                            Log.d(TAG, "videos collection update: " + videosList);
                                        });
                                    });
                        }
                    });
                });
        return root;
    }
}

