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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.ActivitySearchBinding;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.databinding.FragmentFollowersBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.databinding.FragmentFollowingBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

public class FollowersFragment extends Fragment {

    private static final String TAG = "FollowerFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;

    private FragmentFollowersBinding b;
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private List<SearchResult> followersList = new ArrayList<>();
    private List<DocumentReference> followersRefList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchRecyclerAdapter = new SearchRecyclerAdapter(getContext(), followersList);
        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            SearchResult searchResult = followersList.get(position);
            User user = searchResult.getUser();
            Intent in = new Intent(requireActivity(), UserProfileActivity.class);
            in.putExtra(GlobalConstants.USER_PROFILE,user);
            startActivity(in);
        });
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(b == null) {
            b = FragmentFollowersBinding.inflate(inflater, container, false);

            b.searchRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.searchRecycler.setAdapter(searchRecyclerAdapter);

            ub = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(ub.getUid())
                    .collection("followers")
                    .addSnapshotListener((value, e) -> {
                        if (e != null || value == null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        procES.execute(() -> {

                            followersList.clear();
                            try {
                                for (QueryDocumentSnapshot doc : value) {
                                    DocumentSnapshot userDoc = Tasks.await(doc.getDocumentReference("ref").get());
                                    User user = new User(userDoc);
                                    if (user.getUid() == null) {
                                        doc.getReference().delete();
                                        continue;
                                    }
                                    followersList.add(new SearchResult(user));
                                }
                            } catch (ExecutionException | IllegalStateException | InterruptedException executionException) {
                                executionException.printStackTrace();
                            }

                            if (b != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!followersList.isEmpty()) {
                                        b.emptyResultsText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyResultsText.setVisibility(View.VISIBLE);
                                        b.emptyResultsText.setText("No Followers - Go Get Some!");
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
