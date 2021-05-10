package com.jackz314.keepfit.views;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.format.DateFormat;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.firestore.Query;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.UtilsKt;
import com.jackz314.keepfit.controllers.ExerciseController;
import com.jackz314.keepfit.controllers.SchedulingController;
import com.jackz314.keepfit.controllers.SearchHistoryController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.FragmentUserInfoBinding;
import com.jackz314.keepfit.models.Exercise;
import com.jackz314.keepfit.views.other.ExerciseRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import us.zoom.sdk.AccountService;
import us.zoom.sdk.ZoomSDK;

import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;

public class UserInfoFragment extends Fragment {

    private static final String TAG = "userInfoFragment";
    private final List<Exercise> exerciseList = new ArrayList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private FragmentUserInfoBinding b;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ExerciseRecyclerAdapter exerciseRecyclerAdapter;
    private MenuItem clearExerciseItem;


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
        if (b == null) {
            // view binding ftw!
            b = FragmentUserInfoBinding.inflate(inflater, container, false);

            authStateListener = auth -> populateUserInfo();
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

            // exercise stuff
            b.exerciseLogRecycler.setAdapter(exerciseRecyclerAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            b.exerciseLogRecycler.setLayoutManager(layoutManager);
            b.exerciseLogRecycler.setNestedScrollingEnabled(false);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.exerciseLogRecycler.getContext(),
                    layoutManager.getOrientation());
            b.exerciseLogRecycler.addItemDecoration(dividerItemDecoration);

            UserControllerKt.getCurrentUserDoc().collection("exercises").orderBy("starting_time", Query.Direction.DESCENDING).addSnapshotListener(((value, error) -> {
                if (error != null || value == null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                exerciseList.clear();
                exerciseList.addAll(value.toObjects(Exercise.class));
                exerciseRecyclerAdapter.notifyDataSetChanged();

                if (!exerciseList.isEmpty()) {
                    if (clearExerciseItem != null) clearExerciseItem.setVisible(true);
                    b.emptyExerciseLogText.setVisibility(View.GONE);
                } else {
                    if (clearExerciseItem != null) clearExerciseItem.setVisible(false);
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

            b.calendarBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CalendarActivity.class);
                intent.putExtra(GlobalConstants.CALENDAR_DATE, System.currentTimeMillis());
                startActivity(intent);
            });

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            String date = df.format(c);
            String str = sharedPref.getString("LAST_LAUNCH_DATE", "");
            if (!str.equals(date)) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("LAST_LAUNCH_DATE", date);
                editor.apply();
                String recentExercise = ExerciseController.getMostRecentExercise(getContext());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Today's recommended workout")
                        .setMessage(recentExercise)
                        .setPositiveButton("GO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getContext(), PromptActivity.class);
                                intent.setAction(GlobalConstants.ACTION_EXERCISE);
                                getContext().startActivity(intent);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        })
                        .create()
                        .show();
            }
        }


        return b.getRoot();
    }

    private void populateUserInfo() {
        Disposable disposable = UserControllerKt.getCurrentUser().subscribe(user -> {
            if (user != null) {
                b.userNameText.setText(getGreetingMsg() + user.getName());
                b.userEmailText.setText(user.getEmail());
                b.userHeightText.setText(Utils.centimeterToFeet(user.getHeight()));
                b.userWeightText.setText((int) (user.getWeight() * 2.205) + " lbs");
                b.userNameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, user.getSex() ? R.drawable.ic_baseline_male_24 : R.drawable.ic_baseline_female_24, 0);
                b.userBirthdayText.setText(DateUtils.formatDateTime(getContext(), user.getBirthday().getTime(),
                        DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                b.userBiographyText.setText(user.getBiography());

                if (getActivity() != null) {
                    Glide.with(requireActivity().getApplicationContext())
                            .load(user.getProfilePic())
                            .fitCenter()
                            .placeholder(R.drawable.ic_outline_account_circle_24)
                            .into(b.userProfilePicture);
                }
            }
        }, throwable -> {
            Log.d(TAG, "no current user, sign in required");
        });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        if (authStateListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        if (!es.isShutdown()) es.shutdown();
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
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        requireActivity().invalidateOptionsMenu();
        inflater.inflate(R.menu.menu_user_info_fragment, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        clearExerciseItem = menu.findItem(R.id.clear_exercise_log_btn);
        //testing
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
        } else if (item.getItemId() == R.id.edit_profile_btn) {
            editProfile();
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
        } else if (item.getItemId() == R.id.clear_exercise_log_btn) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.clear_exercise_log_confirm_msg)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> clearExerciseLog())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (item.getItemId() == R.id.clear_search_history_btn) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.clear_search_log_confirm_msg)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this.getContext(),
                                SearchHistoryController.AUTHORITY, SearchHistoryController.MODE);
                        suggestions.clearHistory();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (item.getItemId() == R.id.daily_notif_btn) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.daily_notif_setting_msg)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                        long lastDailyNotifScheduledTime = prefs.getLong(GlobalConstants.DAILY_NOTIF_SCHEDULED_TIME, 0);
                        if (lastDailyNotifScheduledTime == 0)
                            lastDailyNotifScheduledTime = System.currentTimeMillis();
                        ZonedDateTime time = Instant.ofEpochMilli(lastDailyNotifScheduledTime).atZone(ZoneId.systemDefault());
                        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
                            Instant scheduleTime = time.with(LocalTime.of(hour, minute)).toInstant();
                            long scheduledMillis = scheduleTime.toEpochMilli();
                            SchedulingController.scheduleDailyNotifications(requireContext(), scheduledMillis);
                            Toast.makeText(requireContext(), "Daily notifications scheduled at " +
                                    DateUtils.formatDateTime(requireContext(), scheduledMillis, DateUtils.FORMAT_SHOW_TIME), Toast.LENGTH_SHORT).show();
                        }, time.getHour(), time.getMinute(), DateFormat.is24HourFormat(requireContext())).show();
                    }).setNegativeButton("No", (dialog, which) -> {
                SchedulingController.cancelDailyNotifications(requireContext());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                prefs.edit().putLong(GlobalConstants.DAILY_NOTIF_SCHEDULED_TIME, 0).apply();
            })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void editProfile() {
        Intent activity2Intent = new Intent(getActivity(), UpdateProfileActivity.class);
        startActivity(activity2Intent);
    }

    private void clearExerciseLog() {
        UtilsKt.deleteCollection(UserControllerKt.getCurrentUserDoc().collection("exercises"), es);
        Toast.makeText(getContext(), "Exercise log cleared!", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "You signed out", Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
//                        Utils.createSignInIntent(requireActivity());
                    } else {
                        Log.e(TAG, "signOut: failure", task.getException());
                        Toast.makeText(requireContext(), "Failed to sign out: " +
                                        Objects.requireNonNull(task.getException()).getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAccount() {
        UtilsKt.deleteAccountFromFirestore()
                .continueWithTask(task -> {
                    if (task.isSuccessful())
                        return AuthUI.getInstance().delete(requireContext());
                    else return task;
                })
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

    @Override
    public void onResume() {
        super.onResume();
        populateUserInfo();
    }

}
