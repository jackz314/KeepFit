package com.jackz314.keepfit.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentLikedVideosBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.views.other.SearchRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LikedVideosFragment extends Fragment {

    private static final String TAG = "LikedVideosFragment";
    private final List<SearchResult> likedVideosList = new ArrayList<>();
    private final Executor procES = Executors.newSingleThreadExecutor();
    private FirebaseUser ub;
    private FirebaseFirestore db;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private FragmentLikedVideosBinding b;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchRecyclerAdapter = new SearchRecyclerAdapter(getContext(), likedVideosList);
        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            Media media = likedVideosList.get(position).getMedia();
            Intent intent = new Intent(requireActivity(), VideoActivity.class);

            intent.putExtra("uri", media.getLink());
            intent.putExtra("media", media.getUid());
            startActivity(intent);

        });

        ub = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (b == null) { // only inflate for the first time being created
            b = FragmentLikedVideosBinding.inflate(inflater, container, false);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            b.feedRecycler.setLayoutManager(layoutManager);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.feedRecycler.getContext(),
                    layoutManager.getOrientation());
            b.feedRecycler.addItemDecoration(dividerItemDecoration);
            b.feedRecycler.setAdapter(searchRecyclerAdapter);
            b.feedRecycler.setAdapter(searchRecyclerAdapter);

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
                                    Media media = new Media(mediaDoc);
                                    if (media.getUid().isEmpty()) {
                                        doc.getReference().delete();
                                        continue;
                                    }
                                    likedVideosList.add(new SearchResult(media));
                                }
                            } catch (ExecutionException | IllegalStateException | InterruptedException executionException) {
                                executionException.printStackTrace();
                            }
                            FragmentActivity activity = getActivity();
                            if (activity == null) return;
                            activity.runOnUiThread(() -> {
                                if (b != null) {
                                    if (!likedVideosList.isEmpty()) {
                                        b.emptyFeedText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyFeedText.setVisibility(View.VISIBLE);
                                        b.emptyFeedText.setText("No Liked Videos ¯\\_(ツ)_/¯");
                                    }
                                }
                                searchRecyclerAdapter.notifyDataChanged();
                            });
                        });
                    });
        }

        return b.getRoot();
    }
}

