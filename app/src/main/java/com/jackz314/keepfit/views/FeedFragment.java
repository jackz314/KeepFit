package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding b;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;

    private LivestreamController livestreamController;

    private final List<Media> mediaList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();

    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        livestreamController = new LivestreamController(getContext());
        feedRecyclerAdapter = new FeedRecyclerAdapter(getContext(), mediaList);
        feedRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            Media media = mediaList.get(position);
            if(media.isLivestream()) {
                livestreamController.setLivestream(media);
                livestreamController.joinLivestream();
            }

            else{
                Intent intent = new Intent(requireActivity(), VideoActivity.class);

                String videoPath = media.getLink();
                //String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample;
                intent.putExtra("uri", videoPath);
                startActivity(intent);
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
        setHasOptionsMenu(true);
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


    // Steven: Added menu option for search activity
    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.feed_search, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_search) {
                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);
        }else{
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


}