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

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LikedVideosFragment extends Fragment {

    private static final String TAG = "LikedVideosFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;
    private FragmentFeedBinding b;

    private List<Media> likedVideosList = new ArrayList<>();

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
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (b == null){ // only inflate for the first time being created
            b = FragmentFeedBinding.inflate(inflater, container, false);

            b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.feedRecycler.setAdapter(feedRecyclerAdapter);

            UserControllerKt.getCurrentUserDoc()
                    .collection("liked_videos").orderBy("liked_time", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, e) -> {
                        if (e != null || value == null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        procES.execute(() -> {
                            likedVideosList.clear();
                            try {
                                for (QueryDocumentSnapshot doc : value) {
                                    DocumentSnapshot mediaDoc = Tasks.await(db.collection("media").document(doc.getId()).get());
                                    likedVideosList.add(new Media(mediaDoc));
                                }
                            } catch (ExecutionException | IllegalStateException | InterruptedException executionException) {
                                executionException.printStackTrace();
                            }
                            getActivity().runOnUiThread(() -> {
                                if (b != null) {
                                    if (!likedVideosList.isEmpty()){
                                        b.emptyFeedText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyFeedText.setVisibility(View.VISIBLE);
                                        b.emptyFeedText.setText("No Liked Videos ¯\\_(ツ)_/¯");
                                    }
                                }
                                feedRecyclerAdapter.notifyDataChanged();
                            });
                        });
                    });
        }

        return b.getRoot();
    }
}

