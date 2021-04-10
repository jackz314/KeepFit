package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.databinding.FragmentHistoryBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;
    private FirebaseStorage fs;
    private HistoryRecyclerAdapter historyRecyclerAdapter;
    private FragmentHistoryBinding b;

    private List<Media> watchedList = new ArrayList<>();
    private final List<DocumentReference> videoRefList = new ArrayList<>();

    private Executor procES = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        historyRecyclerAdapter = new HistoryRecyclerAdapter(getContext(), watchedList, this);
        historyRecyclerAdapter.setClickListener((view, position) -> {

            Media media = watchedList.get(position);
            Intent intent = new Intent(getActivity(), VideoActivity.class);

            String videoPath = media.getLink();
            String mediaID =  media.getUid();

            intent.putExtra("uri", videoPath);
            intent.putExtra("media", mediaID);
            startActivity(intent);

        });
        fs = FirebaseStorage.getInstance();

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (b == null){ // only inflate for the first time being created
            b = FragmentHistoryBinding.inflate(inflater, container, false);

            b.historyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.historyRecycler.setAdapter(historyRecyclerAdapter);

            ub = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();

            UserControllerKt.getCurrentUserDoc()
                    .collection("history")
                    .addSnapshotListener((value, e) -> {
                        if (e != null || value == null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        procES.execute(() -> {
                            watchedList.clear();
                            try {
                                for (QueryDocumentSnapshot doc : value) {
                                    DocumentSnapshot mediaDoc = Tasks.await(db.collection("media").document(doc.getId()).get());
                                    Media media = new Media(mediaDoc);
                                    if (media.getUid().isEmpty()) {
                                        doc.getReference().delete();
                                        continue;
                                    }
                                    watchedList.add(media);
                                }
                            } catch (ExecutionException | IllegalStateException | InterruptedException executionException) {
                                executionException.printStackTrace();
                            }
                            FragmentActivity activity = getActivity();
                            if (activity == null) return;
                            activity.runOnUiThread(() -> {
                                if (b != null) {
                                    if (!watchedList.isEmpty()){
                                        b.emptyHistoryText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyHistoryText.setVisibility(View.VISIBLE);
                                        b.emptyHistoryText.setText("No Watched Videos ¯\\_(ツ)_/¯");
                                    }
                                }
                                historyRecyclerAdapter.notifyDataChanged();
                            });
                        });
                    });
        }

        return b.getRoot();
    }

    protected void deleteHistoryVideo(String mediaID){
        UserControllerKt.getCurrentUserDoc().collection("history").document(mediaID).delete();
    }
}
