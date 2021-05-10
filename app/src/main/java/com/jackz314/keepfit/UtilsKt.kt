package com.jackz314.keepfit

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.annotation.WorkerThread
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.jackz314.keepfit.controllers.UserControllerKt
import com.prolificinteractive.materialcalendarview.CalendarDay
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import us.zoom.sdk.ZoomApiError
import us.zoom.sdk.ZoomAuthenticationError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKAuthenticationListener
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Executor
import javax.security.auth.login.LoginException

private const val TAG = "UtilsKt"

object UtilsKt {
    @JvmStatic
    fun formatDurationString(duration: Long?): String {
        if (duration == null || duration < 0) return "0:00"
        val d = Duration.ofSeconds(duration)
        val hrs = d.toHours()
        val minusHours = d.minusHours(hrs)
        val mins = minusHours.toMinutes()
        val secs = minusHours.minusMinutes(mins).seconds
        return if (hrs > 0) {
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
        val minusHours = d.minusHours(hrs)
        val mins = minusHours.toMinutes()
        val secs = minusHours.minusMinutes(mins).seconds
        return if (hrs > 0) {
            "$hrs hr${if (mins != 0L) " $mins min" else ""}"
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
                sdk.tryAutoLoginZoom()
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
            if (result == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
                sdk.addAuthenticationListener(sdkAuthListener)
            } else {
                emitter.onError(LoginException("Login attempt failed! Error code: $result"))
            }
        }
    }

    @JvmStatic
    fun createLivestream(link: String, title: String, exerciseCategories: String, maxParticipants: String, thumbnail: String = "") {
        if (link.trim().isEmpty() || !isValidUrl(link)) return
        val categories = exerciseCategories.split(",").map { it.trim().capitalize(Locale.getDefault()) }
        Log.d(TAG, "createLivestream: link: $link, categories: $categories, title: $title, thumbnail: $thumbnail")
        val db = FirebaseFirestore.getInstance()
        val docData = hashMapOf(
                "categories" to categories,
                "creator" to UserControllerKt.currentUserDoc,
                "is_livestream" to true,
                "likes" to 0,
                "link" to link,
                "start_time" to FieldValue.serverTimestamp(),
                "thumbnail" to thumbnail,
                "title" to title,
                "view_count" to 0,
                "max_participants" to maxParticipants.toInt()
        )
        val livestreamDoc = db.collection("media").document(Utils.getMD5(link))
        livestreamDoc.set(docData)
        UserControllerKt.currentUserDoc.collection("videos").add(hashMapOf("ref" to livestreamDoc))
    }

    @JvmStatic
    fun removeLivestream(link: String) {
        if (link.trim().isEmpty() || !isValidUrl(link)) return
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

    @JvmStatic
    fun deleteAccountFromFirestore(): Task<Void> {
        val db = FirebaseFirestore.getInstance()
        val fs = FirebaseStorage.getInstance()
        val currUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: "non_existence_user"
        return UserControllerKt.currentUserDoc.collection("followers").get().continueWithTask { task ->
            return@continueWithTask if (task.isSuccessful) {
                val batch = db.batch()
                Tasks.whenAll(
                        task.result?.map { doc ->
                            val followerRef = doc.getDocumentReference("ref")!!
                            return@map db.document(followerRef.path).collection("following").whereEqualTo("ref", UserControllerKt.currentUserDoc).get()
                                    .continueWithTask { task ->
                                        Tasks.call {
                                            if (task.result!!.documents.size != 0)
                                                batch.delete(task.result!!.documents[0].reference)
                                        }
                                    }
                        }
                ).continueWithTask { batch.commit() }
            } else Tasks.forException(task.exception ?: Exception("Original exception was null"))
        }.continueWithTask { task ->
            return@continueWithTask if (task.isSuccessful) {
                UserControllerKt.currentUserDoc.collection("following").get()
            } else Tasks.forException(task.exception ?: Exception("Original exception was null"))
        }.continueWithTask { task ->
            return@continueWithTask if (task.isSuccessful) {
                val batch = db.batch()
                Tasks.whenAll(
                        task.result?.map { doc ->
                            val followingRef = doc.getDocumentReference("ref")!!
                            return@map db.document(followingRef.path).collection("followers").whereEqualTo("ref", UserControllerKt.currentUserDoc).get()
                                    .continueWithTask { task ->
                                        Tasks.call {
                                            if (task.result!!.documents.size != 0)
                                                batch.delete(task.result!!.documents[0].reference)
                                        }
                                    }
                        }
                ).continueWithTask { batch.commit() }
            } else Tasks.forException(task.exception ?: Exception("Original exception was null"))
        }.continueWithTask { task ->
            return@continueWithTask if (task.isSuccessful) {
                UserControllerKt.currentUserDoc.collection("videos").get()
            } else Tasks.forException(task.exception ?: Exception("Original exception was null"))
        }.continueWithTask { task -> // delete all videos
            return@continueWithTask if (task.isSuccessful) {
                val batch = db.batch()
                return@continueWithTask Tasks.whenAll(
                        task.result?.map { doc ->
                            val ref = doc.getDocumentReference("ref")!!
                            return@map ref.get().continueWithTask { videoTask ->
                                fs.getReferenceFromUrl(videoTask.result!!.getString("link")!!).delete()
                            }.continueWithTask { Tasks.call { batch.delete(ref) } }
                        }
                ).continueWithTask { batch.commit() }
            } else Tasks.forException(task.exception ?: Exception("Original exception was null"))
        }.continueWithTask { task ->
            return@continueWithTask if (task.isSuccessful) {
                UserControllerKt.currentUserDoc.delete()
            } else Tasks.forException(task.exception ?: Exception("Original exception was null"))
        }
    }

    @JvmStatic
    fun isValidUrl(url: String): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: Exception) {
            false
        }
    }

    // from https://stackoverflow.com/a/52146033/8170714
    @JvmStatic
    fun deleteCollection(collection: CollectionReference, executor: Executor) {
        Tasks.call(executor) {
            val batchSize = 10
            var query = collection.orderBy(FieldPath.documentId()).limit(batchSize.toLong())
            var deleted = deleteQueryBatch(query)

            while (deleted.size >= batchSize) {
                val last = deleted[deleted.size - 1]
                query = collection.orderBy(FieldPath.documentId()).startAfter(last.id).limit(batchSize.toLong())

                deleted = deleteQueryBatch(query)
            }
            null
        }
    }

    @WorkerThread
    @Throws(Exception::class)
    private fun deleteQueryBatch(query: Query): List<DocumentSnapshot> {
        val querySnapshot = Tasks.await(query.get())

        val batch = query.firestore.batch()
        for (snapshot in querySnapshot) {
            batch.delete(snapshot.reference)
        }
        Tasks.await(batch.commit())

        return querySnapshot.documents
    }

    @JvmStatic
    fun millisToCalendarDay(millis: Long): CalendarDay {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        return CalendarDay.from(date.year, date.monthValue, date.dayOfMonth)
    }

    @JvmStatic
    fun calendarDayToDate(day: CalendarDay?): Date {
        if (day == null) return Date()
        val calendar = Calendar.getInstance()
        calendar.set(day.year, day.month - 1, day.day)
        return calendar.time
    }

    @JvmStatic
    fun nowOrFuture(date: Date?): Date {
        val now = Date()
        return if (date == null || date.before(now)) now else date
    }

    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }
}