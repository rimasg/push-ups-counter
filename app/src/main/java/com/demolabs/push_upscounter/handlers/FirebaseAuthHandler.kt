package com.demolabs.push_upscounter.handlers

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Created by rgaina on 16/12/2017.
 */
object FirebaseAuthHandler {
    const val RC_SIGN_IN = 9001
    private val TAG = FirebaseAuthHandler::class.java.simpleName

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseAuthListener: FirebaseAuthListener? = null

    val user: FirebaseUser?
        get() = auth.currentUser

    fun onStart(firebaseAuthListener: FirebaseAuthListener?) {
        this.firebaseAuthListener = firebaseAuthListener
        this.firebaseAuthListener?.updateUI(user)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                firebaseAuthListener?.updateUI(null)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct?.getId())
        val credential = GoogleAuthProvider.getCredential(acct?.getIdToken(), null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        val user = auth.currentUser
                        firebaseAuthListener?.updateUI(user)
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        firebaseAuthListener?.notify("Authentication failed.")
                        firebaseAuthListener?.updateUI(null)
                    }
                }
    }

    fun signOut() {
        auth.signOut()
    }

    interface FirebaseAuthListener {
        fun updateUI(user: FirebaseUser?)
        fun notify(msg: String)
    }
}