package com.jackz314.keepfit.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.SearchController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivitySearchBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "SearchActivity";
    private SearchView editsearch;
    private final List<SearchResult> mList = new ArrayList<>();
    private Executor procES = Executors.newSingleThreadExecutor();
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private LivestreamController livestreamController;
    private FirebaseFirestore db;
    private ActivitySearchBinding b;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Index index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        b = ActivitySearchBinding.inflate(getLayoutInflater());
        View v =b.getRoot();
        setContentView(v);

        searchRecyclerAdapter = new SearchRecyclerAdapter(this, mList);
        livestreamController = new LivestreamController(this);
        //if (b == null){ // only inflate for the first time being created

        Disposable disposable = Utils.getAlgoliaSearchKey(this).subscribe(key -> {
            Client client = new Client(GlobalConstants.ALGOLIA_APP_ID, key);
            index = client.getIndex(GlobalConstants.ALGOLIA_INDEX_NAME);
        }, throwable -> {
            Log.e(TAG, "onCreate: couldn't get search key for algolia", throwable);
            Toast.makeText(this, "Couldn't connect to search service, try again later.", Toast.LENGTH_SHORT).show();
        });
        compositeDisposable.add(disposable);

        b.searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        if (!mList.isEmpty() && b.emptyResultsText.getVisibility() == View.VISIBLE) {
            b.emptyResultsText.setVisibility(View.GONE);
        }

        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent
            SearchResult searchResult = mList.get(position);
            if (searchResult.isUser()) {
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
                User user = searchResult.getUser();
                Intent in = new Intent(this,FollowActivity.class);
                in.putExtra("other",user);
                startActivity(in);
            }
            else{
                Media media = searchResult.getMedia();
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

            }
        });

        b.searchRecycler.setAdapter(searchRecyclerAdapter);

        db = FirebaseFirestore.getInstance();

        editsearch = findViewById(R.id.search);
        editsearch.setOnQueryTextListener(this);
        if(editsearch.requestFocus()) getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        processSearch(query);
        Log.d(TAG, query);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    private void processSearch(String query) {

        // TODO: 3/19/21 Insert searching capabilities on search submit query

        if (index == null) {
            Log.w(TAG, "processSearch: couldn't search because search service is unavailable");
            Toast.makeText(this, "Couldn't search because search service is unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        procES.execute(() -> {
            try {
                JSONObject content = index.search(new Query(query), null);
                Log.d(TAG, "processSearch: search result: " + content);
                List<SearchResult> searchResults = SearchController.parseResults(content);
                this.runOnUiThread(() -> {
                    mList.clear();
                    mList.addAll(searchResults);
                    if (b != null) {
                        if (!mList.isEmpty()){
                            b.emptyResultsText.setVisibility(View.GONE);
                        } else {
                            b.emptyResultsText.setVisibility(View.VISIBLE);
                            b.emptyResultsText.setText("No Results");
                        }
                    }
                    searchRecyclerAdapter.notifyDataSetChanged();
                });
            } catch (AlgoliaException e) {
                Log.e(TAG, "processSearch: search error: ", e);
                Toast.makeText(this, "Error while searching " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

//        db.collection("users")
//                .whereLessThanOrEqualTo("name",query)
//                .addSnapshotListener((value, e) -> {
//                    if (e != null || value == null) {
//                        Log.w(TAG, "Listen failed.", e);
//                        return;
//                    }
//
//                    procES.execute(() -> {
//                        mList.clear();
//
//
//                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
//                            User user = new User(queryDocumentSnapshot);
////                            Log.d(TAG,"CurrId = "+UserControllerKt.getCurrentUserDoc().getId()+".");
////                            Log.d(TAG,"SearchId = "+user.getUid()+".");
//                            if(!user.getUid().equals(UserControllerKt.getCurrentUserDoc().getId())){
//                                mList.add(user);
//                            }
//                        }
//
//
//
//                        // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
//                        this.runOnUiThread(() -> {
//
//                            if (b != null) {
//                                if (!mList.isEmpty()){
//                                    b.emptyResultsText.setVisibility(View.GONE);
//                                    b.searchRecycler.setVisibility(View.VISIBLE);
//                                } else {
//                                    b.emptyResultsText.setVisibility(View.VISIBLE);
//                                    b.emptyResultsText.setText("No Results");
//                                }
//                            }
//
//                            searchRecyclerAdapter.notifyDataSetChanged();
//                        });
//                        Log.d(TAG, "profile collection update: " + mList);
//                    });
//                });
//        db.collection("media")
//                .whereGreaterThanOrEqualTo("title", query)
//                .addSnapshotListener((value, e) -> {
//                    if (e != null || value == null) {
//                        Log.w(TAG, "Listen failed.", e);
//                        return;
//                    }
//
//                    procES.execute(() -> {
//                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
//                            mList.add(new Media(queryDocumentSnapshot));
//                        }
//
//                        // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
//                        this.runOnUiThread(() -> {
//
//                            if (b != null) {
//                                if (!mList.isEmpty()){
//                                    b.emptyResultsText.setVisibility(View.GONE);
//                                    b.searchRecycler.setVisibility(View.VISIBLE);
//                                } else {
//                                    b.emptyResultsText.setVisibility(View.VISIBLE);
//                                    b.emptyResultsText.setText("No Results");
//                                }
//                            }
//
//                            searchRecyclerAdapter.notifyDataSetChanged();
//                        });
//                        Log.d(TAG, "media collection update: " + mList);
//                    });
//                });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}