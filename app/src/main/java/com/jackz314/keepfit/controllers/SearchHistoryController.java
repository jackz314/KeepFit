package com.jackz314.keepfit.controllers;

import android.content.SearchRecentSuggestionsProvider;

public class SearchHistoryController extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.jackz314.keepfit.controllers.SearchHistoryController";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchHistoryController() {
        setupSuggestions(AUTHORITY, MODE);
    }
}