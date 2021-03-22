package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.controllers.ExerciseController;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentMeBinding;
import com.jackz314.keepfit.models.Exercise;
import com.jackz314.keepfit.databinding.FragmentUserInfoBinding;
import com.jackz314.keepfit.models.User;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import us.zoom.sdk.AccountService;
import us.zoom.sdk.ZoomSDK;

import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;

public class userInfoFragment extends Fragment {

    private static final String TAG = "userInfoFragment";

    private FragmentUserInfoBinding b;

    private FirebaseAuth.AuthStateListener authStateListener;

    private UserController userController = new UserController();

    private final List<Exercise> exerciseList = new ArrayList<>();
    private ExerciseRecyclerAdapter exerciseRecyclerAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        exerciseRecyclerAdapter = new ExerciseRecyclerAdapter(getContext(), exerciseList);
        exerciseRecyclerAdapter.setClickListener((view, position) -> {
            Exercise exercise = exerciseList.get(position);
            Intent intent = new Intent(requireActivity(), ViewExerciseActivity.class);
            intent.putExtra(GlobalConstants.EXERCISE_OBJ, exercise);
            startActivity(intent);
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //        View root = inflater.inflate(R.layout.fragment_me, container, false);
        if (b == null){
            // view binding ftw!
            b = FragmentUserInfoBinding.inflate(inflater, container, false);

            authStateListener = auth -> {
                User user = userController.getLocalUser();

                if (user != null) {
                    b.userNameText.setText("Name: " + user.getName());
                    b.userEmailText.setText("Email: " + user.getEmail());
                    b.userHeightText.setText("Height: " + user.getHeight());
                    b.userWeightText.setText("Weight: " + user.getWeight());
                    b.userSexText.setText("Sex: " + user.getSex());
                    b.userBirthdayText.setText("Birthday: " + user.getBirthday());
                    b.userBiographyText.setText("Biography: " + user.getBiography());

                    Glide.with(b.getRoot())
                            .load(Utils.getHighResProfilePicUrl())
                            .fitCenter()
                            .placeholder(R.drawable.ic_outline_account_circle_24)
                            .into(b.userProfilePicture);
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

            // exercise stuff
            b.exerciseLogRecycler.setAdapter(exerciseRecyclerAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            b.exerciseLogRecycler.setLayoutManager(layoutManager);
            b.exerciseLogRecycler.setNestedScrollingEnabled(false);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.exerciseLogRecycler.getContext(),
                    layoutManager.getOrientation());
            b.exerciseLogRecycler.addItemDecoration(dividerItemDecoration);

            UserControllerKt.getCurrentUserDoc().collection("exercises").addSnapshotListener(((value, error) -> {
                if (error != null || value == null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                exerciseList.clear();
                exerciseList.addAll(value.toObjects(Exercise.class));
                exerciseRecyclerAdapter.notifyDataSetChanged();


                if (!exerciseList.isEmpty()){
                    b.emptyExerciseLogText.setVisibility(View.GONE);
                } else {
                    b.emptyExerciseLogText.setVisibility(View.VISIBLE);
                    b.emptyExerciseLogText.setText("Go out and exercise more? ¯\\_(ツ)_/¯");
                }
                // today exercise summary stuff
                List<Exercise> todayExercises = ExerciseController.getTodayExercises(exerciseList);
                double todayCal = ExerciseController.getTotalCalories(todayExercises);
                long todayExTime = ExerciseController.getTotalExerciseTime(todayExercises);
                b.meCaloriesText.setText(String.format(Locale.getDefault(), "Calories: %.3f", todayCal));
                b.meExerciseTimeText.setText("Exercise: " + UtilsKt.formatDurationTextString(todayExTime / DateUtils.SECOND_IN_MILLIS));
            }));
        }

        return b.getRoot();
    }

    @Override
    public void onDestroy() {
        if (authStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        super.onDestroy();
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
        } else if (item.getItemId() == R.id.sign_out_zoom_btn) {
            ZoomSDK sdk = ZoomSDK.getInstance();
            AccountService accountService = sdk.getAccountService();
            String userEmail = "";
            if (accountService != null) userEmail = accountService.getAccountEmail();
            sdk.logoutZoom();
            Toast.makeText(getContext(), "Signed " + userEmail + " out of Zoom", Toast.LENGTH_SHORT).show();
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