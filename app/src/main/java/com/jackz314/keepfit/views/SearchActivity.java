package com.jackz314.keepfit.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = "SearchActivity";
    private SearchView editsearch;
    private List<Media> mediaList;
    private Executor procES = Executors.newSingleThreadExecutor();
    private FragmentFeedBinding b;
    private FeedRecyclerAdapter feedRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editsearch = findViewById(R.id.search);
        editsearch.setOnQueryTextListener((SearchView.OnQueryTextListener) this);
    }



    @Override
    public boolean onQueryTextSubmit(String query) {

        proccessSearch(query);
        // TODO: Add search results display (Set up fragment? on same activity?)
        finish();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
    }

    private void proccessSearch(String query){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference media = db.collection("media");
        // TODO: 3/19/21 Insert searching capabilities on search submit query

    }

}
