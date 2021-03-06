package com.jackz314.keepfit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.util.Objects;

import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;
import static com.jackz314.keepfit.GlobalConstants.RC_SIGN_IN;

public
class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()){
            //new user, show additional setup stuff
            ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            //do stuff
                            initMainViews();
                        }
                    });

            Intent intent = new Intent(this, NewUserActivity.class);
            resultLauncher.launch(intent);

        }else if (findViewById(R.id.container) == null) { // first time login
            initMainViews();
        } // otherwise it's user change, will be handled by AuthStateListener
    }

    private void initMainViews() {
        setContentView(R.layout.activity_main);
        //nav view
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_me, R.id.navigation_feed)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
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
}