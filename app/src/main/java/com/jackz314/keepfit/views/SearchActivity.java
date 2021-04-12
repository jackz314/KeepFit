package com.jackz314.keepfit.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Query;
//import com.google.firebase.database.snapshot.Index;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.SearchController;
import com.jackz314.keepfit.controllers.SearchHistoryController;
import com.jackz314.keepfit.databinding.ActivitySearchBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.other.SearchRecyclerAdapter;

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
    private final Executor procES = Executors.newSingleThreadExecutor();
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private LivestreamController livestreamController;
    private FirebaseFirestore db;
    private ActivitySearchBinding b;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private com.algolia.search.saas.Index index;

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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        b.searchRecycler.setLayoutManager(layoutManager);

        if (!mList.isEmpty() && b.emptyResultsText.getVisibility() == View.VISIBLE) {
            b.emptyResultsText.setVisibility(View.GONE);
        }

        searchRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent
            SearchResult searchResult = mList.get(position);
            if (searchResult.isUser()) {
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
                User user = searchResult.getUser();
                Intent in = new Intent(this, UserProfileActivity.class);
                in.putExtra(GlobalConstants.USER_PROFILE,user);
                startActivity(in);
            }
            else{
                Media media = searchResult.getMedia();
                if (media.isLivestream()) {
                    livestreamController.setLivestream(media);
                    livestreamController.joinLivestream();
                } else {
                    Intent intent = new Intent(this, VideoActivity.class);


                    //String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample;
                    intent.putExtra("uri", media.getLink());
                    intent.putExtra("media", media.getUid());
                    startActivity(intent);
                }

            }
        });
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.searchRecycler.getContext(),
                layoutManager.getOrientation());
        b.searchRecycler.addItemDecoration(dividerItemDecoration);
        b.searchRecycler.setAdapter(searchRecyclerAdapter);

        db = FirebaseFirestore.getInstance();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        editsearch = findViewById(R.id.search);
        editsearch.setOnQueryTextListener(this);
        editsearch.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        String searchQuery = getIntent().getStringExtra(GlobalConstants.SEARCH_QUERY);
        if(searchQuery != null && !searchQuery.trim().isEmpty()){
            editsearch.setQuery(searchQuery, true);
        } else if(editsearch.requestFocus()) getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static String stripQuery(String query){
        return query.replaceAll("^[ \t]+|[ \t]+$", "");
    }

    public static boolean isValidQuery(String query){
        query = stripQuery(query);
        return !query.isEmpty() && query.matches("[a-zA-Z0-9]*");
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
       query = stripQuery(query);
        if(!isValidQuery(query)){
            b.emptyResultsText.setText("Please input valid search");
            b.emptyResultsText.setVisibility(View.VISIBLE);
            b.searchRecycler.setVisibility(View.INVISIBLE);
            return true;
        }

        processSearch(query);
        Log.d(TAG, query);
        if(b.searchRecycler.getVisibility() == View.INVISIBLE){
           b.emptyResultsText.setVisibility(View.GONE);
            b.searchRecycler.setVisibility(View.VISIBLE);
        }


        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void closeSearch(View view){
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: got new intent");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            String historyClick = intent.getStringExtra(SearchManager.QUERY);
            if(historyClick != null){
                editsearch.setQuery(historyClick,false);
                //Tried to show keyboard
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
            }
        }
        else if (editsearch != null) {
            String searchQuery = intent.getStringExtra(GlobalConstants.SEARCH_QUERY);

            if(searchQuery != null && !searchQuery.trim().isEmpty()){
                editsearch.setQuery(searchQuery, true);
            }

        }
    }

    private void processSearch(String query) {

        // TODO: 3/19/21 Insert searching capabilities on search submit query
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchHistoryController.AUTHORITY, SearchHistoryController.MODE);
        suggestions.saveRecentQuery(query, null);

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
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}