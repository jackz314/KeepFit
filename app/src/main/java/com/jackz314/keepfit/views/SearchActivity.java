package com.jackz314.keepfit.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Query;
import com.google.android.material.chip.Chip;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

//import com.google.firebase.database.snapshot.Index;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "SearchActivity";
    private SearchView editSearch;
    private final List<SearchResult> mList = new ArrayList<>();
    private final List<SearchResult> fullmList = new ArrayList<>();
    private final Executor procES = Executors.newSingleThreadExecutor();
    private SearchRecyclerAdapter searchRecyclerAdapter;
    private LivestreamController livestreamController;
    private ActivitySearchBinding b;
    private Chip user_chip;
    private Chip video_chip;

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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        editSearch = findViewById(R.id.search);


        user_chip = findViewById(R.id.user_chip);
        video_chip = findViewById(R.id.video_chip);
        //Interface\
        CompoundButton.OnCheckedChangeListener filt = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // this method is for when a search is already processed
                if (isChecked) { //if checked a chip
                    if (buttonView == (CompoundButton) user_chip) { // if user chip is checked, need to uncheck videochip
                        if (video_chip.isChecked()) {
                            video_chip.setChecked(false);
                        }
                        if(!mList.isEmpty())
                            filter(1);
                    }
                    else if(buttonView == (CompoundButton)video_chip){//if video chip is checked, need to uncheck user chip
                        if(user_chip.isChecked()){
                            user_chip.setChecked(false);
                        }
                        if(!mList.isEmpty())
                            filter(2);
                    }
                }
                else // if unchecked chip, other one is already unchecked, just filter 0
                    filter(0);
            }

        };
        user_chip.setOnCheckedChangeListener(filt);
        video_chip.setOnCheckedChangeListener(filt);

        editSearch.setOnQueryTextListener(this);
        editSearch.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        String searchQuery = getIntent().getStringExtra(GlobalConstants.SEARCH_QUERY);
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            editSearch.setQuery(searchQuery, true);
        } else if (editSearch.requestFocus())
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static String stripQuery(String query){
        return query.replaceAll("^[ \t]+|[ \t]+$", "");
    }

    public static boolean isValidQuery(String query){
        query = stripQuery(query);
        return !query.isEmpty() && query.matches("[a-zA-Z0-9 ]*");
    }

    private void filter(int mode){
        if (mode == 1){ // filter by users
            mList.clear();
            mList.addAll(fullmList);
            for (Iterator<SearchResult> iter = mList.listIterator(); iter.hasNext(); ) {
                SearchResult searchResult = iter.next();
                if (!searchResult.isUser()) {
                    Log.d(TAG,"tries to remove video");
                    iter.remove();
                }
            }
        }
        else if(mode == 2){ //filter by videos
            mList.clear();
            mList.addAll(fullmList);
            for (Iterator<SearchResult> iter = mList.listIterator(); iter.hasNext(); ) {
                SearchResult searchResult = iter.next();
                if (searchResult.isUser()) {
                    Log.d(TAG,"tries to remove user");
                    iter.remove();
                }
            }
        }
        else{ // use both
            mList.clear();
            mList.addAll(fullmList); //must clear and add full list in case it was filtered previously
        }
        if(mList.isEmpty()){
            b.emptyResultsText.setVisibility(View.VISIBLE);
            b.emptyResultsText.setText("No Filtered Results");
        }
        else{
            b.emptyResultsText.setVisibility(View.GONE);
            b.searchRecycler.setVisibility(View.VISIBLE);
        }
        searchRecyclerAdapter.notifyDataChanged();
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
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String historyClick = intent.getStringExtra(SearchManager.QUERY);
            if (historyClick != null && editSearch != null) {
                editSearch.setQuery(historyClick, false);
                onQueryTextSubmit(historyClick);
                new Handler().postDelayed(() -> {
                    editSearch.clearFocus();
                    b.searchRecycler.requestFocus();
                }, 500);
                //Tried to show keyboard
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
            }
        } else if (editSearch != null) {
            String searchQuery = intent.getStringExtra(GlobalConstants.SEARCH_QUERY);

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                editSearch.setQuery(searchQuery, true);
            }

        }
    }

    private void processSearch(String query) {

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
                    fullmList.clear();
                    fullmList.addAll(mList);
//                    setUpItemListeners();
                    if (b != null) {
                        if (!mList.isEmpty()){
                            b.emptyResultsText.setVisibility(View.GONE);
                            if(user_chip.isChecked()){ // checks if a chip is already clicked to filter before displaying full results
                                filter(1);
                            }
                            else if(video_chip.isChecked()){
                                filter(2);
                            }
                        } else {
                            b.emptyResultsText.setVisibility(View.VISIBLE);
                            b.emptyResultsText.setText("No Results");
                        }
                    }

                    searchRecyclerAdapter.notifyDataChanged();
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