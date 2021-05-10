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
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentHistoryBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.views.other.HistoryRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;
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

    private final List<Media> watchedList = new ArrayList<>();
    private final List<DocumentReference> videoRefList = new ArrayList<>();
    private final List<ListenerRegistration> itemListenerList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        historyRecyclerAdapter = new HistoryRecyclerAdapter(getContext(), watchedList);
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
        db = FirebaseFirestore.getInstance();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (b == null){ // only inflate for the first time being created
            b = FragmentHistoryBinding.inflate(inflater, container, false);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            b.historyRecycler.setLayoutManager(layoutManager);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.historyRecycler.getContext(),
                    layoutManager.getOrientation());
            b.historyRecycler.addItemDecoration(dividerItemDecoration);

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
                            List<Media> tempList = new ArrayList<>();
                            for(Media media:watchedList) {
                                tempList.add(media);
                            }
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
                                    if (!watchedList.isEmpty()) {
                                        b.emptyHistoryText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyHistoryText.setVisibility(View.VISIBLE);
                                        b.emptyHistoryText.setText(R.string.empty_watch_history_list);
                                    }
                                }
                                historyRecyclerAdapter.notifyDataChanged();
                            });
                        });
                    });
        }

        return b.getRoot();
    }
}
