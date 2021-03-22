package com.jackz314.keepfit.controllers

import android.content.res.Resources
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jackz314.keepfit.models.User
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.util.*

object UserControllerKt {
    @JvmStatic
    val currentUser: Single<User?>
        get() = Single.create { emitter: SingleEmitter<User?> ->
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnCompleteListener { ds: Task<DocumentSnapshot?> ->
                            if (ds.isSuccessful && ds.result?.exists() == true) {
                                emitter.onSuccess(User(Objects.requireNonNull(ds.result)))
                            } else {
                                emitter.onError(ds.exception
                                        ?: Resources.NotFoundException("User not found."))
                            }
                        }
            } else {
                emitter.onError(IllegalStateException("Firebase User isn't available"))
            }
        }

    @JvmStatic
    val currentUserDoc: DocumentReference
        get() {
            return FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser?.uid
                    ?: "non-existence-user")
        }

    @JvmStatic
    fun likeVideo(uid: String) {
        val db = FirebaseFirestore.getInstance()
        currentUserDoc.collection("liked_videos").document(uid).set(hashMapOf("exists" to true))
    }

    @JvmStatic
    fun unlikeVideo(uid: String) {
        val db = FirebaseFirestore.getInstance()
        currentUserDoc.collection("liked_videos").document(uid).delete()
    }


}