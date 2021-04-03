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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jackz314.keepfit.GlobalConstants;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.databinding.FragmentFollowingBinding;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.other.SearchRecyclerAdapter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment  {

    private static final String TAG = "FollowerFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;

    private FragmentFollowingBinding b;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private final List<SearchResult> followingList = new ArrayList<>();
    private final List<DocumentReference> followingRefList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchRecyclerAdapter = new SearchRecyclerAdapter(getContext(), followingList);
        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            SearchResult searchResult = followingList.get(position);
            User user = searchResult.getUser();
            Intent in = new Intent(requireActivity(), UserProfileActivity.class);
            in.putExtra(GlobalConstants.USER_PROFILE,user);
            startActivity(in);
        });
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(b == null) {
            b = FragmentFollowingBinding.inflate(inflater, container, false);

            b.searchRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.searchRecycler.setAdapter(searchRecyclerAdapter);

            ub = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(ub.getUid())
                    .collection("following")
                    .addSnapshotListener((value, e) -> {
                        if (e != null || value == null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        procES.execute(() -> {

                            followingList.clear();
                            try {
                                for (QueryDocumentSnapshot doc : value) {
                                    DocumentSnapshot userDoc = Tasks.await(doc.getDocumentReference("ref").get());
                                    User user = new User(userDoc);
                                    if (user.getUid() == null) {
                                        doc.getReference().delete();
                                        continue;
                                    }
                                    followingList.add(new SearchResult(user));
                                }
                            } catch (ExecutionException | IllegalStateException | InterruptedException executionException) {
                                executionException.printStackTrace();
                            }

                            if (b != null)  {
                                requireActivity().runOnUiThread(() -> {
                                    if (!followingList.isEmpty()) {
                                        b.emptyResultsText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyResultsText.setVisibility(View.VISIBLE);
                                        b.emptyResultsText.setText("No Following - Go Follow Users!");
                                    }
                                    searchRecyclerAdapter.notifyDataSetChanged();
                                });
                            }
                        });
                    });
        }

        return b.getRoot();
    }
}
