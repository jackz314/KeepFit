package com.jackz314.keepfit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.preference.PreferenceManager;

import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;

import com.facebook.AccessToken;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.security.auth.login.LoginException;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import us.zoom.sdk.ZoomAuthenticationError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;

import static com.jackz314.keepfit.GlobalConstants.PRIVACY_POLICY_URL;
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

    public static String getHighResProfilePicUrl() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

    // get the token from local cache first, if expired, get a new one from server
    public static Single<String> getZoomJWTToken(Context context){
         return Single.create((SingleEmitter<String> emitter) -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String savedToken = prefs.getString(GlobalConstants.ZOOM_JWT_TOKEN_KEY, "");
            Date expirationDate = getJWTExpirationDate(savedToken);
            if(expirationDate == null) {
                emitter.onSuccess("");
                return;
            }
            else expirationDate = new Date(expirationDate.getTime() - 10 * DateUtils.MINUTE_IN_MILLIS); // assume expiration 10 min before actual exp
            if(new Date().after(expirationDate)){ // invalid, get a new one
                emitter.onSuccess("");
            }else {
                emitter.onSuccess(savedToken);
            }
        }).flatMap(token -> {
            if (token.isEmpty()){
                return getZoomJWTTokenFromServer() // save token after getting it
                        .doOnSuccess(newToken -> PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString(GlobalConstants.ZOOM_JWT_TOKEN_KEY, newToken).apply());
            }else{
                return Single.just(token);
            }
         });
    }

    private static Single<String> getZoomJWTTokenFromServer() {
        // Create the arguments to the callable function.
        FirebaseFunctions functions = FirebaseFunctions.getInstance();
        return Single.create(emitter -> functions
                .getHttpsCallable("getZoomJWTToken")
                .call()
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    HttpsCallableResult taskResult = task.getResult();
                    if (taskResult == null) return "";
                    return (String) taskResult.getData();
                }).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()){
                        Exception e = task.getException();
                        if (e == null) {
                            e = new Exception("Original exception was null");
                        }
                        Log.e(TAG, "getZoomJWTToken: failed to get token, error: ", e);
                        emitter.onError(e);
                    } else {
                        emitter.onSuccess(Objects.requireNonNull(task.getResult()));
                    }
                })
        );
    }

    public static Date getJWTExpirationDate(String jwtEncoded) {
        if (jwtEncoded == null || jwtEncoded.isEmpty()) {
            return null;
        }
        try {
            String[] split = jwtEncoded.split("\\.");
            Log.d("JWT_DECODED", "Header: " + decodeJwtPart(split[0]));
            String body = decodeJwtPart(split[1]);
            Log.d("JWT_DECODED", "Body: " + body);
            JSONObject json = new JSONObject(body);
            long expTime = json.getLong("exp");
            return new Date(expTime * 1000);
        } catch (Exception e) {
            //Error
            Log.e(TAG, "getJWTExpirationDate: Error parsing JWT: ", e);
            return null;
        }
    }

    private static String decodeJwtPart(String strEncoded) throws IllegalArgumentException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static boolean isValidEmail(CharSequence charSequence){
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
    }

    // from https://stackoverflow.com/a/21333739/8170714
    public static String getMD5(String str) {
        if (str == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return ""; // should never happen
    }

    public static String toTitleCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : str.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }
}
