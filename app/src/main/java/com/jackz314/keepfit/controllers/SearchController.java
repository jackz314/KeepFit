package com.jackz314.keepfit.controllers;

import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.SearchResult;
import com.jackz314.keepfit.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchController {


    /**
     * Parse the root result JSON object into a list of results.
     *
     * @param jsonObject The result's root object.
     * @return A list of results (potentially empty), or null in case of error.
     */
    public static List<SearchResult> parseResults(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        List<SearchResult> results = new ArrayList<>();
        JSONArray hits = jsonObject.optJSONArray("hits");
        if (hits == null) return null;

        for (int i = 0; i < hits.length(); ++i) {
            JSONObject hit = hits.optJSONObject(i);
            if (hit == null) continue;

            SearchResult result;
            if ("user".equals(hit.optString("type"))){
                User user = User.populateFromUid(hit.optString("objectID"));
                if (user == null) continue;
                result = new SearchResult(user);
            } else {
                Media media = Media.populateFromUid(hit.optString("objectID"));
                if (media == null) continue;
                result = new SearchResult(media);
            }

            results.add(result);
        }
        return results;
    }
}
