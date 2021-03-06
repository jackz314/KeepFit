package com.jackz314.keepfit.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding b;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        b = FragmentFeedBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        b.textDashboard.setText("Hello! Empty feed.");
        db = FirebaseFirestore.getInstance();
        db.collection("media")
//                .whereEqualTo("state", "CA")
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    List<String> cities = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                       //todo
                    }
                });


        return root;
    }
}