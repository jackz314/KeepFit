package com.jackz314.keepfit.ui.feed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FeedViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FeedViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the feed fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}