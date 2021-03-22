package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LikedVideosFragment extends Fragment {

    private static final String TAG = "LikedVideosFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;
    private FragmentFeedBinding b;

    private List<Media> likedVideosList = new ArrayList<>();
    private List<String> likedVideoRefList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feedRecyclerAdapter = new FeedRecyclerAdapter(getContext(), likedVideosList);
        feedRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            Media media = likedVideosList.get(position);
            Intent intent = new Intent(requireActivity(), VideoActivity.class);

            String videoPath = media.getLink();
            String creatorInfo =  media.getCreator().toString();

            intent.putExtra("uri", videoPath);
            intent.putExtra("creator", creatorInfo);
            startActivity(intent);

        });

        ub = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(ub.getUid())
                .collection("videos")
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        likedVideoRefList.clear();

                        for (QueryDocumentSnapshot document : value) {
                            likedVideoRefList.add((String) document.get("ref"));
                        }
                        requireActivity().runOnUiThread(() -> feedRecyclerAdapter.notifyDataSetChanged());
                        Log.d(TAG, "videos collection update: " + likedVideoRefList);

                        likedVideosList.clear();
                        for (String createdVideoId : likedVideoRefList) {
                            db.document(createdVideoId)
                                    .addSnapshotListener((value1, e1) -> {
                                        if (e != null || value1 == null) {
                                            Log.w(TAG, "Listen failed.", e);
                                            return;
                                        }
                                        procES.execute(() -> {
                                            likedVideosList.add(new  Media (value1));
                                            Log.d(TAG, "videos collection update: " + likedVideosList);
                                        });
                                    });
                        }

                        if (b != null) {
                            if (!likedVideosList.isEmpty()){
                                b.emptyFeedText.setVisibility(View.GONE);
                            } else {
                                b.emptyFeedText.setVisibility(View.VISIBLE);
                                b.emptyFeedText.setText("Nothing to show here ¯\\_(ツ)_/¯");
                            }
                        }
                        feedRecyclerAdapter.notifyDataSetChanged();
                    });
                });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = FragmentFeedBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        b.feedRecycler.setAdapter(feedRecyclerAdapter);

        if (b == null){ // only inflate for the first time being created
            b = FragmentFeedBinding.inflate(inflater, container, false);

            if (!likedVideoRefList.isEmpty() && b.emptyFeedText.getVisibility() == View.VISIBLE) {
                b.emptyFeedText.setVisibility(View.GONE);
            }

            b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.feedRecycler.setAdapter(feedRecyclerAdapter);
        }

        return root;
    }
}

