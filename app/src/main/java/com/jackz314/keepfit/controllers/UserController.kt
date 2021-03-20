package com.jackz314.keepfit.controllers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jackz314.keepfit.models.User
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.util.*

object UserController {
    @JvmStatic
    val currentUser: Single<User?>
        get() = Single.create { emitter: SingleEmitter<User?> ->
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnCompleteListener { ds: Task<DocumentSnapshot?> ->
                            if (ds.isSuccessful) {
                                emitter.onSuccess(User(Objects.requireNonNull(ds.result)))
                            } else {
                                emitter.onError(ds.exception)
                            }
                        }
            } else {
                emitter.onError(IllegalStateException("Firebase User isn't available"))
            }
        }
}