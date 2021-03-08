package com.jackz314.keepfit;

import android.app.Activity;
import android.util.Log;

import com.facebook.AccessToken;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jackz314.keepfit.GlobalConstants.PRIVACY_POLICY_URL;
import static com.jackz314.keepfit.GlobalConstants.RC_REAUTH_DELETE;
import static com.jackz314.keepfit.GlobalConstants.RC_SIGN_IN;
import static com.jackz314.keepfit.GlobalConstants.TOS_URL;

public class Utils {
    private static final String TAG = "Utils";

    public static void createSignInIntent(Activity activity) {
        // Create and launch sign-in intent
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(getSupportedProviders())
                        .setLogo(R.mipmap.ic_launcher_round)
                        .setTosAndPrivacyPolicyUrls(TOS_URL, PRIVACY_POLICY_URL)
                        .build(),
                RC_SIGN_IN);
    }

    @NotNull
    private static List<AuthUI.IdpConfig> getSupportedProviders() {
        return Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GitHubBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());
    }

    public static void createReauthenticationIntent(Activity activity, int requestCode) {

        // Create and launch sign-in intent
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(getProviderIdpConfig())
                        .setLogo(R.mipmap.ic_launcher_round)
                        .build(),
                requestCode);
    }

    public static List<AuthUI.IdpConfig> getProviderIdpConfig() {
        // Get authentication provider
        List<AuthUI.IdpConfig> providers;
        String provider = getPrimaryProviderOfFirebaseUser();
        Log.d(TAG, "getProviderIdpConfig: getting primary idp provider: " + provider);
        switch (provider) {
            case "google.com":
                providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
                break;
            case "facebook.com":
                providers = Collections.singletonList(new AuthUI.IdpConfig.FacebookBuilder().build());
                break;
            case "twitter.com":
                providers = Collections.singletonList(new AuthUI.IdpConfig.TwitterBuilder().build());
                break;
            //todo email and phone
            default:
                providers = getSupportedProviders();
        }
        return providers;
    }

    public static String getPrimaryProviderOfFirebaseUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo info : user.getProviderData()) {
                if (!info.getProviderId().equals("firebase"))
                    return info.getProviderId();
            }
        }
        return "firebase";
    }

    public static String getHighResProfilePicUrl(FirebaseUser user) {
        String originalUrl = "";
        if (user.getPhotoUrl() != null) {
            originalUrl = user.getPhotoUrl().toString();
            if (originalUrl.contains("graph.facebook.com")) {//Facebook
                return originalUrl + "?height=5000&access_token=" + AccessToken.getCurrentAccessToken().getToken();//get the highest res photo
            } else if (originalUrl.contains("googleusercontent.com")) {//Google
                return originalUrl.replace("s96-c", "s5000-c");//get the highest res photo
            } else if (originalUrl.contains("pbs.twimg.com")) {//Twitter
                return originalUrl.replace("_normal", "");//get original photo
            } else {//Others
                return originalUrl;
            }
        }
        return originalUrl;
    }


}
