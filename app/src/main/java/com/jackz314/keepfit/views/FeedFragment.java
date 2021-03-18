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

    private List<Media> mediaList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = FragmentFeedBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        b.emptyFeedText.setText("Hello! Empty feed.");
        b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
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
        b.feedRecycler.setAdapter(feedRecyclerAdapter);

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
                        requireActivity().runOnUiThread(() -> feedRecyclerAdapter.notifyDataSetChanged());
                        Log.d(TAG, "media collection update: " + mediaList);
                    });
                });

        return root;
    }
}