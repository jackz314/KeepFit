package com.jackz314.keepfit.models

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import java.io.Serializable
import java.util.*
import java.util.concurrent.ExecutionException

private const val TAG = "Media"

class Media(doc: DocumentSnapshot): Serializable {

    @DocumentId
    var uid: String? = null
    var creator = MutableLiveData(User())
    var creatorRef: DocumentReference?

    @PropertyName("is_livestream")
    var isLivestream = false
    var link: String? = null
    var categories: List<String>? = null

    @PropertyName("start_time")
    var startTime: Date? = null
    var duration: Long? = null
    var thumbnail: String? = null
    var title: String? = null

    @PropertyName("view_count")
    var viewCount: Int = 0

    @Exclude
    var liked = false

    init {
        if (!doc.exists()) throw IllegalStateException("Media doesn't exist!")
        uid = doc.id
        isLivestream = doc.getBoolean("is_livestream") == true
        link = doc.getString("link")
        categories = doc.get("categories") as List<String>
        startTime = doc.getDate("start_time")
        if(!isLivestream) duration = doc.getLong("duration")
        thumbnail = doc.getString("thumbnail")
        title = doc.getString("title")
        viewCount = doc.getLong("view_count")?.toInt()?:0
        creatorRef = doc.getDocumentReference("creator")
        val task = creatorRef!!.get()
        task.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "error getting creater ref: ", task.exception)
            }else{
                creator.value = User(task.result)
                Log.d(TAG, "creator: " + creator.value)
            }
        }

        //synchronous alternative
//        try {
//            val ds = Tasks.await(task)
//            creator = User(ds)
//        } catch (e: Exception) {
//            Log.e(TAG, "error getting creator ref: ", e)
//            creator = User()
//        }
    }

    fun getDetailString(): String {
        val creator = creator.value ?: User()
        val startTimeStr = startTime?.let { DateUtils.getRelativeTimeSpanString(it.time) }
        if (isLivestream) return "${creator.name} · ${viewCount} watching · Started $startTimeStr"
        else return "${creator.name} · ${viewCount} views · $startTimeStr"
    }
    fun getProfileString():String{
        if (isLivestream) return "${viewCount} watching · Started ${startTime?.let { DateUtils.getRelativeTimeSpanString(it.time) }}"
        else return "${viewCount} views · ${startTime?.let { DateUtils.getRelativeTimeSpanString(it.time) }}"
    }

    override fun toString(): String {
        return "Media{" +
                "uid='" + uid + '\'' +
                ", creater=" + creator +
                ", isLivestream=" + isLivestream +
                ", link='" + link + '\'' +
                ", startTime=" + startTime +
                ", thumbnail='" + thumbnail + '\'' +
                ", title='" + title + '\'' +
                ", viewCount=" + viewCount +
                '}'
    }

    companion object {
        @JvmStatic
        fun populateFromUid(uid: String?): Media? {
            if (uid == null) return null
            try {
                return Media(Tasks.await(FirebaseFirestore.getInstance().collection("media").document(uid).get()))
            } catch (e: ExecutionException) {
                Log.e(TAG, "populateFromUid: error getting user from uid", e)
            } catch (e: InterruptedException) {
                Log.e(TAG, "populateFromUid: error getting user from uid", e)
            }
            return null
        }
    }

}