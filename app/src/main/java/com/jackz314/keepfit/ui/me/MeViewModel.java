package com.jackz314.keepfit.ui.me;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MeViewModel extends ViewModel {

    private final MutableLiveData<FirebaseUser> mUser;
    private final FirebaseAuth.AuthStateListener authStateListener;

    public MeViewModel() {
        mUser = new MutableLiveData<>();
        authStateListener = firebaseAuth -> mUser.setValue(FirebaseAuth.getInstance().getCurrentUser());
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    public LiveData<FirebaseUser> getUser() {
        return mUser;
    }

    @Override
    protected void onCleared() {
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        super.onCleared();
    }
}