package com.demolabs.push_upscounter

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.demolabs.push_upscounter.handlers.FirebaseAuthHandler
import com.demolabs.push_upscounter.handlers.FirebaseAuthHandler.FirebaseAuthListener
import com.demolabs.push_upscounter.handlers.FirebaseDatabaseHandler
import com.demolabs.push_upscounter.handlers.FirebaseDatabaseHandler.FirebaseDatabaseListener
import com.demolabs.push_upscounter.handlers.SensorHandler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener, FirebaseAuthListener, FirebaseDatabaseListener {
    private var sensorEventVal: Float = 0f
    private lateinit var sensorHandler: SensorHandler
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorHandler = SensorHandler(this)

        initSignIn()
        initControls()
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuthHandler.onStart(this)
        FirebaseDatabaseHandler.onStart(this)
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.onPause(this)
    }

    private fun initSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initControls() {
        btnSensor.setOnClickListener { toggleSensor() }
        btnAddPushUps.setOnClickListener {
            var pushUpsCount = edtPushUpsCount.text.toString().toInt()
            edtPushUpsCount.setText("")
            pushUpsCount = Math.min(pushUpsCount, 500)
            if (pushUpsCount > 0) {
                addPushUps(FirebaseAuthHandler.user?.displayName ?: "no-name", pushUpsCount, Date().time)
            }
        }
        sign_in_button.setOnClickListener {
            signIn()
        }
        sign_out_button.setOnClickListener {
            signOut()
        }
        disconnect_button.setOnClickListener {
            revokeAccess()
        }
    }

    private fun toggleSensor() {
        Toast.makeText(this, "Sensor enabled / disabled", Toast.LENGTH_LONG).show()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, FirebaseAuthHandler.RC_SIGN_IN)
    }

    private fun signOut() {
        FirebaseAuthHandler.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    private fun revokeAccess() {
        FirebaseAuthHandler.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {
        // no-op
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEventVal = sensorEvent?.values?.get(0) ?: 0f
        proximityValue.text = sensorEventVal.toString()
    }

    override fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            status.text = getString(R.string.google_status_fmt, user.displayName)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

            sign_in_button.visibility = View.GONE
            sign_out_and_disconnect.visibility = View.VISIBLE
        } else {
            status.text = getString(R.string.signed_out)
            detail.text = null

            sign_in_button.visibility = View.VISIBLE
            sign_out_and_disconnect.visibility = View.GONE
        }
    }

    override fun notify(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FirebaseAuthHandler.onActivityResult(requestCode, resultCode, data)
    }

    fun addPushUps(name: String, count: Int, date: Long) {
        FirebaseDatabaseHandler.addPushups(name, count, date)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
