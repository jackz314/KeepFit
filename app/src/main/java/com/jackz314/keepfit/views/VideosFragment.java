package com.jackz314.keepfit.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.views.other.VideosRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VideosFragment extends Fragment {

    private static final String TAG = "VideosFragment";

    private FirebaseUser ub;
    private FirebaseFirestore db;
    private FirebaseStorage fs;
    private VideosRecyclerAdapter videoRecyclerAdapter;
    private FragmentFeedBinding b;

    private final List<Media> videosList = new ArrayList<>();
    private final List<DocumentReference> videoRefList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoRecyclerAdapter = new VideosRecyclerAdapter(getContext(), videosList, this);
        videoRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            Media media = videosList.get(position);
            Log.d(TAG,"Uploaded Video: "+media);
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
            b = FragmentFeedBinding.inflate(inflater, container, false);

            b.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            b.feedRecycler.setAdapter(videoRecyclerAdapter);

            ub = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(ub.getUid())
                    .collection("videos")
                    .addSnapshotListener((value, e) -> {
                        if (e != null || value == null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        procES.execute(() -> {

                            videosList.clear();
                            try {
                                for (QueryDocumentSnapshot doc : value) {
                                    DocumentSnapshot mediaDoc = Tasks.await(doc.getDocumentReference("ref").get());
                                    Media media = new Media(mediaDoc);
                                    if (TextUtils.isEmpty(media.getUid())) {
                                        doc.getReference().delete();
                                        continue;
                                    }
                                    videosList.add(media);
                                }
                            } catch (ExecutionException | IllegalStateException | InterruptedException executionException) {
                                executionException.printStackTrace();
                            }

                            videosList.sort(Collections.reverseOrder(Comparator.comparing(Media::getStartTime)));

                            if (b != null && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (!videosList.isEmpty()){
                                        b.emptyFeedText.setVisibility(View.GONE);
                                    } else {
                                        b.emptyFeedText.setVisibility(View.VISIBLE);
                                        b.emptyFeedText.setText("No Created Videos ¯\\_(ツ)_/¯");
                                    }
                                    videoRecyclerAdapter.notifyDataChanged();
                                });
                            }
                        });
                    });
        }

        return b.getRoot();
    }

    public void deleteVideo(String mediaID, String CreatorID, String StorageLink){

        db.collection("users").document(CreatorID).collection("videos")
                .whereEqualTo("ref", db.collection("media").document(mediaID)).get()
                .continueWithTask(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                return doc.getReference().delete();
                            }
                        }
                        return null;
                }).continueWithTask(task -> {
                    return db.collection("media").document(mediaID).delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                }).continueWithTask(task -> {
                    StorageReference videoRef = fs.getReferenceFromUrl(StorageLink);
                    return videoRef.delete();
                }).addOnSuccessListener(aVoid -> {
                    // File deleted successfully
                    Log.d(TAG, "onSuccess: deleted file");
                }).addOnFailureListener(exception -> {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "onFailure: did not delete file");
                });
    }
}
