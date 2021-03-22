package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.ActivitySearchBinding;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {

    private static final String TAG = "FollowerFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;

    private ActivitySearchBinding b;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private List<SearchResult> followersList = new ArrayList<>();
    private List<DocumentReference> followersRefList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchRecyclerAdapter searchRecyclerAdapter = new SearchRecyclerAdapter(getContext(), followersList);
        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            SearchResult searchResult = followersList.get(position);
            Intent intent = new Intent(requireActivity(), VideoActivity.class);

            startActivity(intent);

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
                            followersRefList.clear();
                            for (QueryDocumentSnapshot document : value) {
                                followersRefList.add((DocumentReference)document.get("ref"));
                            }
                            requireActivity().runOnUiThread(searchRecyclerAdapter::notifyDataSetChanged);
                            Log.d(TAG, "following collection update: " + followersRefList);

                            for (DocumentReference followerUserId : followersRefList) {
                                followerUserId
                                        .addSnapshotListener((value1, e1) -> {
                                            if (e != null || value == null) {
                                                Log.w(TAG, "Listen failed.", e);
                                                return;
                                            }
                                            procES.execute(() -> {
                                                followersList.clear();
                                                followersList.add(new SearchResult(new User(value1)));
                                                requireActivity().runOnUiThread(() -> searchRecyclerAdapter.notifyDataSetChanged());
                                                Log.d(TAG, "videos collection update: " + followersList);
                                            });
                                        });
                            }
                        });
                    });
        });
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = ActivitySearchBinding.inflate(getLayoutInflater());
        View root = b.getRoot();

        if (!followersList.isEmpty() && b.emptyResultsText.getVisibility() == View.VISIBLE) {
            b.emptyResultsText.setVisibility(View.GONE);
        }

        b.searchRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        b.searchRecycler.setAdapter(searchRecyclerAdapter);

        return root;
    }

}
