package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.ExerciseController;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.views.other.FeedRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExploreFollowingFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding b;
    private FirebaseFirestore db;
    private FirebaseUser ub;
    private FeedRecyclerAdapter feedRecyclerAdapter;

    private LivestreamController livestreamController;

    private final List<Media> mediaList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();

    private ListenerRegistration registration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                //String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample;
                intent.putExtra("uri", media.getLink());
                intent.putExtra("media", media.getUid());
                startActivity(intent);
            }

            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
        });

        db = FirebaseFirestore.getInstance();

        setupFeedListener(null);
    }

    private void setupFeedListener(String category) {
        ub = FirebaseAuth.getInstance().getCurrentUser();
        Query feedQuery = db.collection("media").orderBy("likes", Query.Direction.DESCENDING)
                .orderBy("start_time", Query.Direction.DESCENDING);
        if (category != null) feedQuery = feedQuery.whereArrayContains("categories", category);

        if (registration != null) registration.remove();
        registration = feedQuery.addSnapshotListener((value, e) -> {
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            procES.execute(() -> {
                mediaList.clear();
                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                    Media med = new Media(queryDocumentSnapshot);
                    try {
                       String uid = Tasks.await(med.getCreatorRef().get()).getId();
                        UserControllerKt.getCurrentUserDoc()
                                .collection("following")
                                .whereEqualTo("ref", db.collection("users").document(uid))
                                .addSnapshotListener((val, e1) -> {
                                    if (val != null && !val.isEmpty()) {
                                        mediaList.add(med);
                                    }});
                    } catch (ExecutionException executionException) {
                        executionException.printStackTrace();
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    //mediaList.add(med);
                }
                // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
                this.requireActivity().runOnUiThread(() -> {

                    if (b != null) {
                        if (!mediaList.isEmpty()) {
                            b.emptyFeedText.setVisibility(View.GONE);
                        } else {
                            b.emptyFeedText.setVisibility(View.VISIBLE);
                            b.emptyFeedText.setText("Nothing to show here ¯\\_(ツ)_/¯");
                        }
                    }
                    feedRecyclerAdapter.notifyDataChanged();
                });
                Log.d(TAG, "media collection update: " + mediaList);
            });
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (b == null) { // only inflate for the first time being created
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
        SubMenu categoryMenu = menu.findItem(R.id.filter_menu).getSubMenu();
        categoryMenu.add("All");
        for (String category : ExerciseController.getExerciseCategoryArray(getContext()))
            categoryMenu.add(category);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_search) {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        } else if (item.getItemId() != R.id.filter_menu) { // filtering categories
            String category = item.getTitle().toString();
            if (category.equals("All")) category = null;
            setupFeedListener(category);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (registration != null) registration.remove();
        super.onDestroy();
    }
}