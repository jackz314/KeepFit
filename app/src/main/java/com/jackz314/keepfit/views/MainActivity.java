package com.jackz314.keepfit.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;
import static com.jackz314.keepfit.GlobalConstants.RC_SIGN_IN;

public
class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding b;

    private final ActivityResultLauncher<Intent> newUserResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    //do stuff
                    initMainViews();
                }
            });

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        checkAndRequireGooglePlayService();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // not signed in, sign in
            Utils.createSignInIntent(this);
        } else {
            setupAfterSignIn();
        }
    }

    private void setupAfterSignIn() {
        Log.d(TAG, "setupAfterSignIn: signed in, setting up other stuff");
        Disposable disposable = UserControllerKt.getCurrentUser().subscribe(user -> {
            if (findViewById(R.id.container) == null) initMainViews();
            // otherwise it's user change, will be handled by AuthStateListener
        }, throwable -> { // unable to get user from firestore, start new user
            Log.d(TAG, "setupAfterSignIn: unable to get user from firestore, start new user " + throwable.getMessage());
            Intent intent = new Intent(this, NewUserActivity.class);
            newUserResultLauncher.launch(intent);
        });
        compositeDisposable.add(disposable);
    }

    public void initializeZoomSdk(Context context) {
        Disposable disposable = Utils.getZoomJWTToken(context).subscribe(token -> {
            ZoomSDK sdk = ZoomSDK.getInstance();
            ZoomSDKInitParams params = new ZoomSDKInitParams();
            params.jwtToken = token;
            params.domain = "zoom.us";
            params.enableLog = true;
            // TODO: Add functionality to this listener (e.g. logs for debugging)
            ZoomSDKInitializeListener listener = new ZoomSDKInitializeListener() {
                /**
                 * @param errorCode {@link us.zoom.sdk.ZoomError#ZOOM_ERROR_SUCCESS} if the SDK has been initialized successfully.
                 */
                @Override
                public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
                    if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                        Log.e(TAG, "onZoomSDKInitializeResult: zoom sdk initialization failed! Error: " + errorCode + " Internal error code: " + internalErrorCode);
                        Toast.makeText(context, "Failed to initialize Zoom SDK (" + errorCode + "), you might encounter problems with live streams", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "onZoomSDKInitializeResult: zoom sdk initialized");
                    }
                }

                @Override
                public void onZoomAuthIdentityExpired() { }
            };
            sdk.initialize(context, listener, params);
        });
        compositeDisposable.add(disposable);
    }

    private final SpeedDialMenuAdapter speedDialMenuAdapter = new SpeedDialMenuAdapter() {
        @Override
        public int getCount() {
            return 3;
        }

        @NotNull
        @Override
        public SpeedDialMenuItem getMenuItem(@NotNull Context context, int pos) {
            @DrawableRes int icon;
            String label;
            if (pos == 0) {
                icon = R.drawable.ic_baseline_directions_run_24;
                label = "Track exercise";
            } else if (pos == 1) {
                icon = R.drawable.ic_baseline_go_live_24;
                label = "Go live";
            } else { // 2
                icon = R.drawable.ic_baseline_cloud_upload_24;
                label = "Upload a video";
            }
            return new SpeedDialMenuItem(context, icon, label);
        }

        @Override
        public boolean onMenuItemClick(int pos) {
            if (pos == 0) { // exercise
                Intent intent = new Intent(MainActivity.this, PromptActivity.class);
                intent.setAction(GlobalConstants.ACTION_EXERCISE);
                startActivity(intent);
            } else if (pos == 1) { // livestream
                Intent intent = new Intent(MainActivity.this, PromptActivity.class);
                intent.setAction(GlobalConstants.ACTION_LIVESTREAM);
                startActivity(intent);
            } else { // video upload
                Intent intent = new Intent(MainActivity.this, UploadVideoActivity.class);
                intent.putExtra("UserID", "Upload Video");
                startActivity(intent);
            }
            return true;
        }

        @Override
        public float fabRotationDegrees() {
            return 135F;
        }
    };

    private void initMainViews() {
        b = ActivityMainBinding.inflate(getLayoutInflater());
        View rootView = b.getRoot();
        setContentView(rootView);

        initializeZoomSdk(this);

        b.mainActionBtn.setSpeedDialMenuAdapter(speedDialMenuAdapter);

        int dark = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (dark == Configuration.UI_MODE_NIGHT_YES) { // night mode
            b.mainActionBtn.setContentCoverColour(0xcc000000); // faint black
        } else if (dark == Configuration.UI_MODE_NIGHT_NO) { // light mode
            b.mainActionBtn.setContentCoverColour(0xccffffff); // faint white
        }

        //nav view
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_me, R.id.navigation_feed)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navController.getNavigatorProvider().addNavigator(new KeepStateNavigator(this,
                Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))
                        .getChildFragmentManager(), R.id.nav_host_fragment));

        navController.setGraph(R.navigation.mobile_navigation);

        //no need to change action bar titles
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void checkAndRequireGooglePlayService() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.API_UNAVAILABLE) {
            Toast.makeText(this, getString(R.string.toast_play_service_required), Toast.LENGTH_SHORT).show();
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(MainActivity.this);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "onActivityResult: got sign in intent response: " + response);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "Welcome, " + Objects.requireNonNull(user).getDisplayName() + "!",
                        Toast.LENGTH_SHORT).show();
                if (response != null) {
                    String idpToken = response.getIdpToken();
                    // TODO get OAuth token from here
                    Log.d(TAG, "onActivityResult: idp token: " + idpToken);
                }
                setupAfterSignIn();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response != null) { // error
                    Toast.makeText(this, "Sign in failed. " +
                            Objects.requireNonNull(response.getError()).getErrorCode() +
                            "\n" + response.getError().getLocalizedMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        } else if (requestCode == RC_REAUTH_DELETE) {
            AuthUI.getInstance().delete(this).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                    Utils.createSignInIntent(this);
                } else {
                    Exception e = task.getException();
                    Log.e(TAG, "Delete account from Firebase failed", e);
                    Toast.makeText(this, "Error deleting the account: " +
                            Objects.requireNonNull(e).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}