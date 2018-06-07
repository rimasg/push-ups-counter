package com.demolabs.push_upscounter.handlers

import android.util.Log
import com.demolabs.push_upscounter.models.PushUps
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by rgaina on 09/12/2017.
 */
object FirebaseDatabaseHandler {
    private val TAG = FirebaseDatabaseHandler::class.java.simpleName
    private const val PUSH_UPS = "push-ups"

    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var firebaseDatabaseListener: FirebaseDatabaseListener? = null

    fun onStart(firebaseDatabaseListener: FirebaseDatabaseListener?) {
        this.firebaseDatabaseListener = firebaseDatabaseListener
    }

    /**
     * @param name Person name
     * @param count Number of push-ups
     * @param date Date
     */
    fun addPushups(name: String, count: Int, date: Long) {
        dbRef.child(PUSH_UPS).push().setValue(PushUps(name, count, date))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseDatabaseListener?.notify("Data uploaded successfully")
                    } else {
                        Log.w(TAG, "Error uploading data.", task.exception)
                        firebaseDatabaseListener?.notify(task.exception?.message ?: "Error uploading data.")
                    }
                }
    }

    interface FirebaseDatabaseListener {
        fun notify(msg: String)
    }
}