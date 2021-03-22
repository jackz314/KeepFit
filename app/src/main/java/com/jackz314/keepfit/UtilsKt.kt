package com.jackz314.keepfit

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jackz314.keepfit.controllers.UserControllerKt
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import us.zoom.sdk.ZoomApiError
import us.zoom.sdk.ZoomAuthenticationError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKAuthenticationListener
import java.time.Duration
import javax.security.auth.login.LoginException

private const val TAG = "UtilsKt"

object UtilsKt {
    @JvmStatic
    fun formatDurationString(duration: Long?): String {
        if (duration == null) return "0:00"
        val d = Duration.ofSeconds(duration)
        val hrs = d.toHours()
        val mins = d.minusHours(hrs).toMinutes()
        val secs = d.minusMinutes(mins).seconds
        return if (hrs > 0){
            "${hrs}:${"%02d".format(mins)}:${"%02d".format(secs)}"
        } else {
            "${mins}:${"%02d".format(secs)}"
        }
    }

    // returns format like 1 h 24 min 3 s
    @JvmStatic
    fun formatDurationTextString(duration: Long): String {
        if (duration < 60) {
            return "$duration sec"
        }
        val d = Duration.ofSeconds(duration)
        val hrs = d.toHours()
        val mins = d.minusHours(hrs).toMinutes()
        val secs = d.minusMinutes(mins).seconds
        return if (hrs > 0){
            "$hrs hr $mins min"
        } else {
            "$mins min $secs s"
        }
    }

    @JvmStatic
    fun tryLoginToZoom(sdk: ZoomSDK, username: String? = null, password: String? = null, ssoToken: String? = null): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val sdkAuthListener: ZoomSDKAuthenticationListener = object : ZoomSDKAuthenticationListener {
                override fun onZoomSDKLoginResult(result: Long) {
                    sdk.removeAuthenticationListener(this)
                    if (result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS.toLong() && sdk.isLoggedIn) {
//                        if (username != null && password != null) { // save new working credential
//                            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//                            prefs.edit().putString(GlobalConstants.ZOOM_USERNAME_KEY, username)
//                                    .putString(GlobalConstants.ZOOM_PASSWORD_KEY, password)
//                                    .apply()
//                        } else if (ssoToken != null) { // save new working credential
//                            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//                            prefs.edit().putString(GlobalConstants.ZOOM_SSO_TOKEN_KEY, ssoToken)
//                                    .apply()
//                        }
                        emitter.onComplete()
                    } else {
                        emitter.onError(LoginException("Login failed! Error code: $result"))
                    }
                }

                override fun onZoomSDKLogoutResult(l: Long) {}
                override fun onZoomIdentityExpired() {}
                override fun onZoomAuthIdentityExpired() {}
            }
            Log.d(TAG, "tryLoginToZoom: starting")
            var result = if (username != null && password != null) { // user supplied
                sdk.loginWithZoom(username, password)
            } else if (ssoToken != null) { // user supplied
                sdk.loginWithSSOToken(ssoToken)
            } else { // try from saved preferences
                sdk.tryAutoLoginZoom();
//                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//                val savedSSOToken = prefs.getString(GlobalConstants.ZOOM_SSO_TOKEN_KEY, null)
//                if (savedSSOToken == null) { // token doesn't exist, try getting username and password
//                    val savedUsername = prefs.getString(GlobalConstants.ZOOM_USERNAME_KEY, null)
//                    val savedPassword = prefs.getString(GlobalConstants.ZOOM_PASSWORD_KEY, null)
//                    if (savedUsername == null || savedPassword == null) {
//                        sdk.removeAuthenticationListener(sdkAuthListener);
//                        emitter.onError(Resources.NotFoundException("Zoom credentials not found"))
//                    } else { // got user and pass, try login that way
//                        sdk.loginWithZoom(savedUsername, savedPassword)
//                    }
//                } else { // try signing in with sso token
//                    sdk.loginWithSSOToken(savedSSOToken)
//                }
            }
            if (result == ZoomApiError.ZOOM_API_ERROR_SUCCESS){
                sdk.addAuthenticationListener(sdkAuthListener)
            } else {
                emitter.onError(LoginException("Login attempt failed! Error code: $result"))
            }
        }
    }

    @JvmStatic
    fun createLivestream(link: String, title: String, exerciseCategories: String, thumbnail: String = "") {
        val categories = exerciseCategories.split(",").map { it.trim() }
        Log.d(TAG, "createLivestream: link: $link, categories: $categories, title: $title, thumbnail: $thumbnail")
        val db = FirebaseFirestore.getInstance()
        val docData = hashMapOf(
                "categories" to categories,
                "creator" to UserControllerKt.currentUserDoc,
                "is_livestream" to true,
                "link" to link,
                "start_time" to FieldValue.serverTimestamp(),
                "thumbnail" to thumbnail,
                "title" to title,
                "view_count" to 0
        )
        val livestreamDoc = db.collection("media").document(Utils.getMD5(link))
        livestreamDoc.set(docData)
        UserControllerKt.currentUserDoc.collection("videos").add(hashMapOf("ref" to livestreamDoc))
    }

    @JvmStatic
    fun removeLivestream(link: String) {
        Log.d(TAG, "removeLivestream: link: $link")
        val db = FirebaseFirestore.getInstance()
        val livestreamDoc = db.collection("media").document(Utils.getMD5(link))
        UserControllerKt.currentUserDoc.collection("liked_videos").document(livestreamDoc.id).delete()
        UserControllerKt.currentUserDoc.collection("videos").whereEqualTo("ref", livestreamDoc)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.forEach {
                            it.reference.delete()
                        }
                        livestreamDoc.delete()
                    }
                }
    }
}