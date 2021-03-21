package com.jackz314.keepfit.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivitySearchBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "SearchActivity";
    private SearchView editsearch;
    private final List<Object> mList = new ArrayList<>();
    private Executor procES = Executors.newSingleThreadExecutor();
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private LivestreamController livestreamController;
    private FirebaseFirestore db;
    private ActivitySearchBinding b;
    private User self;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        b = ActivitySearchBinding.inflate(getLayoutInflater());
        View v =b.getRoot();
        setContentView(v);

        searchRecyclerAdapter = new SearchRecyclerAdapter(this, mList);
        livestreamController = new LivestreamController(this);
        //if (b == null){ // only inflate for the first time being created

        b.searchRecycler.setLayoutManager(new LinearLayoutManager(this));


        if (!mList.isEmpty() && b.emptyResultsText.getVisibility() == View.VISIBLE) {
            b.emptyResultsText.setVisibility(View.GONE);
            b.searchRecycler.setVisibility(View.VISIBLE);
        }

        b.searchRecycler.setAdapter(searchRecyclerAdapter);


        //}
        editsearch = findViewById(R.id.search);
        editsearch.setOnQueryTextListener((SearchView.OnQueryTextListener) this);
        if(editsearch.requestFocus()) getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        processSearch(query);
        // TODO: Add search results display (Set up fragment? on same activity?
        //feedRecyclerAdapter.notifyDataSetChanged();
        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent
            if (mList.get(position)instanceof Media) {
                Media media = (Media) mList.get(position);
                if (media.isLivestream()) {
                    livestreamController.setLivestream(media);
                    livestreamController.joinLivestream();
                } else {
                    Intent intent = new Intent(this, VideoActivity.class);

                    String videoPath = media.getLink();
                    //String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample;
                    intent.putExtra("uri", videoPath);
                    startActivity(intent);
                }
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
            }
            else{
                User user = (User) mList.get(position);
                Intent in = new Intent(this,FollowActivity.class);
                in.putExtra("other",user);
                startActivity(in);
            }
        });

        Log.d(TAG, query);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(b.searchRecycler.getVisibility() == View.VISIBLE){
            b.searchRecycler.setVisibility(View.GONE);
        }
        return false;
    }


    private void processSearch(String query) {

        // TODO: 3/19/21 Insert searching capabilities on search submit query
        db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereLessThanOrEqualTo("name",query)
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        mList.clear();


                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                            User user = new User(queryDocumentSnapshot);
//                            Log.d(TAG,"CurrId = "+UserControllerKt.getCurrentUserDoc().getId()+".");
//                            Log.d(TAG,"SearchId = "+user.getUid()+".");
                            if(!user.getUid().equals(UserControllerKt.getCurrentUserDoc().getId())){
                                mList.add(user);
                            }
                        }



                        // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
                        this.runOnUiThread(() -> {

                            if (b != null) {
                                if (!mList.isEmpty()){
                                    b.emptyResultsText.setVisibility(View.GONE);
                                    b.searchRecycler.setVisibility(View.VISIBLE);
                                } else {
                                    b.emptyResultsText.setVisibility(View.VISIBLE);
                                    b.emptyResultsText.setText("No Results");
                                }
                            }

                            searchRecyclerAdapter.notifyDataSetChanged();
                        });
                        Log.d(TAG, "profile collection update: " + mList);
                    });
                });
        db.collection("media")
                .whereGreaterThanOrEqualTo("title", query)
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                            mList.add(new Media(queryDocumentSnapshot));
                        }

                        // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
                        this.runOnUiThread(() -> {

                            if (b != null) {
                                if (!mList.isEmpty()){
                                    b.emptyResultsText.setVisibility(View.GONE);
                                    b.searchRecycler.setVisibility(View.VISIBLE);
                                } else {
                                    b.emptyResultsText.setVisibility(View.VISIBLE);
                                    b.emptyResultsText.setText("No Results");
                                }
                            }

                            searchRecyclerAdapter.notifyDataSetChanged();
                        });
                        Log.d(TAG, "media collection update: " + mList);
                    });
                });
    }

}