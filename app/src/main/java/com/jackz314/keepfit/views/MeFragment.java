package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.databinding.FragmentMeBinding;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;

public class MeFragment extends Fragment {

    private static final String TAG = "MeFragment";

    private FragmentMeBinding b;

    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //        View root = inflater.inflate(R.layout.fragment_me, container, false);
        // view binding ftw!
        b = FragmentMeBinding.inflate(inflater, container, false);
        View root = b.getRoot();

        authStateListener = auth -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                b.userNameText.setText(getGreetingMsg() + user.getDisplayName());
                b.userEmailText.setText(user.getEmail());
                Glide.with(root)
                        .load(Utils.getHighResProfilePicUrl(user))
                        .fitCenter()
                        .placeholder(R.drawable.ic_outline_account_circle_24)
                        .into(b.userProfilePicture);
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        return root;
    }

    @NotNull
    private String getGreetingMsg() {
        Instant now = Instant.now();
        int hr = now.atZone(ZoneId.systemDefault()).getHour();
        String timeStr;
        if (hr >= 5 && hr < 12) {
            timeStr = "Morning";
        } else if (hr >= 12 && hr <= 17) {
            timeStr = "Afternoon";
        } else {
            timeStr = "Evening";
        }
        return "Good " + timeStr + ", ";
    }

    @SuppressLint("RestrictedApi")
    // setOptionalIconsVisible bug, see https://stackoverflow.com/q/41150995/8170714
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_me_fragment, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sign_out_btn) {
            //sign out
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.sign_out_confirm_msg)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> signOut())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (item.getItemId() == R.id.delete_account_btn) {
            //delete account
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.delete_account_confirm_msg)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> deleteAccount())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "You signed out", Toast.LENGTH_SHORT).show();
                        Utils.createSignInIntent(requireActivity());
                    } else {
                        Log.e(TAG, "signOut: failure", task.getException());
                        Toast.makeText(requireContext(), "Failed to sign out: " +
                                        Objects.requireNonNull(task.getException()).getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(requireContext())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                        Utils.createSignInIntent(requireActivity());
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                            //need reauthentication
                            Log.w(TAG, task.getException().getLocalizedMessage());

                            //reauthenticate with authcredentials and try delete again
                            reauthWithProviderThenDelete();
                        } else {
                            //other problems
                            Log.e(TAG, "Delete account from Firebase failed", e);
                            Toast.makeText(requireContext(), "Error deleting the account: " +
                                    Objects.requireNonNull(e).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void reauthWithProviderThenDelete() {
        Toast.makeText(getContext(), "Reauthenticating before sensitive action", Toast.LENGTH_SHORT).show();
        Utils.createReauthenticationIntent(requireActivity(), RC_REAUTH_DELETE);
    }
}