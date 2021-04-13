package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.views.other.FeedRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Patrick: used as a listener to detect when a new category has been selected (filtering)
class categoryVariable {
    private String category = "All";
    private ChangeListener listener;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        if (listener != null) listener.onChange();
    }

    public ChangeListener getListener() {
        return listener;
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange();
    }
}

public class FeedFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding b;
    private FirebaseFirestore db;
    private FeedRecyclerAdapter feedRecyclerAdapter;

    private LivestreamController livestreamController;

    private final List<Media> mediaList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();


    private ListenerRegistration registration;

    private categoryVariable category = new categoryVariable();

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

        category.setListener(() -> {
            if (category.getCategory() == "All") {
                registration = db.collection("media").orderBy("start_time", Query.Direction.DESCENDING)

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
            else {
                registration = db.collection("media").whereArrayContains("categories", category.getCategory())

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

        // Patrick: Added spinner to menu for filtering functionality
        MenuItem menuItem = menu.findItem(R.id.categories_spinner);
        Spinner spinner = (Spinner)menuItem.getActionView();

        String exercise_categories[] = getResources().getStringArray(R.array.exercise_categories);

        int n = exercise_categories.length;
        String newarr[] = new String[n + 1];

        for (int i = 1; i < n; i++)
            newarr[i] = exercise_categories[i];

        newarr[0] = "All";

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(getActivity().getApplicationContext()
                , R.layout.spinner_category_item, newarr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.app_bar_search) {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        } else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Patrick: function determines what happens when an category is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        category.setCategory((String)parent.getItemAtPosition(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {

    }

    @Override
    public void onDestroy() {
        if (registration != null) registration.remove();
        super.onDestroy();
    }
}