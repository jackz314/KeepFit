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
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VideosFragment extends Fragment {

    private static final String TAG = "VideosFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;
    private FragmentFeedBinding b;

    private List<Media> videosList = new ArrayList<>();
    private final List<DocumentReference> videoRefList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feedRecyclerAdapter = new FeedRecyclerAdapter(getContext(), videosList);
        feedRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            Media media = videosList.get(position);
            Intent intent = new Intent(requireActivity(), VideoActivity.class);

            String videoPath = media.getLink();
            String creatorInfo =  media.getCreator().toString();

            intent.putExtra("uri", videoPath);
            intent.putExtra("creator", creatorInfo);
            startActivity(intent);

        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        if (b == null){ // only inflate for the first time being created
            b = FragmentFeedBinding.inflate(inflater, container, false);

            if (!videosList.isEmpty() && b.emptyFeedText.getVisibility() == View.VISIBLE) {
                b.emptyFeedText.setVisibility(View.GONE);
            }

            b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.feedRecycler.setAdapter(feedRecyclerAdapter);

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
                            videoRefList.clear();

                            for (QueryDocumentSnapshot document : value) {
                                videoRefList.add((DocumentReference) document.get("ref"));
                            }
                            Log.d(TAG, "videos collection update: " + videoRefList);

                            videosList.clear();
                            for (DocumentReference createdVideoId : videoRefList) {
                                createdVideoId
                                        .addSnapshotListener((value1, e1) -> {
                                            if (e != null || value1 == null) {
                                                Log.w(TAG, "Listen failed.", e);
                                                return;
                                            }
                                            videosList.add(new  Media (value1));
                                            Log.d(TAG, "videos collection update: " + videosList);
                                        });
                            }

                            if (b != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!videosList.isEmpty()){
                                        b.emptyFeedText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyFeedText.setVisibility(View.VISIBLE);
                                        b.emptyFeedText.setText("Nothing to show here ¯\\_(ツ)_/¯");
                                    }
                                    feedRecyclerAdapter.notifyDataSetChanged();
                                });
                            }
                        });
                    });
        }

        return b.getRoot();
    }
}
