package edu.ucsb.cs.cs184.urth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    companion object {
        private val TAG = AuthActivity::class.simpleName
        private const val RC_SIGN_IN = 32
        const val LOGOUT_ERROR = "LOGOUT_ERROR"
        const val FETCH_PERMISSIONS = "FETCH_PERMISSIONS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().uid != null && !intent.getBooleanExtra(LOGOUT_ERROR, false)) {
            // Skip authentication if we're already signed in
            startMapActivity()
        } else {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTheme(R.style.AuthTheme)
                    .setIsSmartLockEnabled(false) // Disabled for testing
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Authentication success!")
                startMapActivity()
            } else {
                val response = IdpResponse.fromResultIntent(data)
                val error = response?.error?.errorCode
                Log.w(TAG, "Authentication failed. Error code: $error")
            }
        }
    }

    private fun Context.startMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_ANIMATION
        intent.putExtra(FETCH_PERMISSIONS, true)
        startActivity(intent)
    }
}
