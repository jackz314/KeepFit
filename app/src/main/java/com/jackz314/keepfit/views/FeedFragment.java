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

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding b;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;

    private final List<Media> mediaList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called");

        feedRecyclerAdapter = new FeedRecyclerAdapter(getContext(), mediaList);
        feedRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent
            // Andrew Edited this : clicking image feed links to sample video

            String ResourceIdAsString = view.getResources().getResourceName(view.getId());

            if(view.getId() == R.id.constraintLayout) {

                Intent intent = new Intent(requireActivity(), VideoActivity.class);

                String videoPath = mediaList.get(position).getLink();
                //String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample;
                intent.putExtra("uri", videoPath);
                startActivity(intent);
            }

            else{
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
            }

            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
        });

        db = FirebaseFirestore.getInstance();
        db.collection("media")
//                .whereEqualTo("state", "CA")
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        mediaList.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                            mediaList.add(new Media(queryDocumentSnapshot));
                        }

                        // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
                        requireActivity().runOnUiThread(() -> {

                            if (b != null) {
                                if (!mediaList.isEmpty()){
                                    b.emptyFeedText.setVisibility(View.GONE);
                                } else {
                                    b.emptyFeedText.setVisibility(View.VISIBLE);
                                    b.emptyFeedText.setText("Hello! Empty feed.");
                                }
                            }

                            feedRecyclerAdapter.notifyDataSetChanged();
                        });
                        Log.d(TAG, "media collection update: " + mediaList);
                    });
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (b == null){ // only inflate for the first time being created
            b = FragmentFeedBinding.inflate(inflater, container, false);

            if (!mediaList.isEmpty() && b.emptyFeedText.getVisibility() == View.VISIBLE) {
                b.emptyFeedText.setVisibility(View.GONE);
            }

            b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.feedRecycler.setAdapter(feedRecyclerAdapter);
        }

        return b.getRoot();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
    }
}