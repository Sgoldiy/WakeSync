package com.social.wakesync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.content.Intent
import com.social.wakesync.feature.home.AlarmService
import com.social.wakesync.app.App
import com.social.wakesync.feature.permission.getPermissionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val activityScope = CoroutineScope(Dispatchers.Main)
    private var pendingAuthCallback: ((String?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge with a dark style (light icons) to match VoidBg
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
        )

        // Ensure status bar icons are white
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        com.social.wakesync.feature.permission.initializePermissionHandler(this)
        com.social.wakesync.feature.home.initializeAlarmScheduler(this)
        com.social.wakesync.feature.home.initializeSoundDownloader(this)
        com.social.wakesync.feature.home.initSoundPlayer(this)

        val viewModel = com.social.wakesync.app.MainViewModel()

        setContent {
            val handler = getPermissionHandler()
            val allPermissionsGranted = handler.isAlarmPermissionGranted() &&
                    handler.isNotificationPermissionGranted() &&
                    handler.isCameraPermissionGranted()

            App(
                viewModel = viewModel,
                initiallyAuthenticated = firebaseAuth.currentUser != null,
                isPermissionsGranted = allPermissionsGranted,
                onGoogleSignInRequested = { callback ->
                    startGoogleSignIn(callback)
                },
                onDismissAlarm = {
                    val dismissIntent = Intent(this@MainActivity, AlarmService::class.java).apply {
                        action = AlarmService.ACTION_DISMISS
                    }
                    startService(dismissIntent)
                }
            )
        }
    }

    private fun startGoogleSignIn(callback: (String?) -> Unit) {
        pendingAuthCallback = callback

        val googleIdOption = GetSignInWithGoogleOption.Builder(
            serverClientId = getString(R.string.default_web_client_id)
        )
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        activityScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("WakeSync", "Google sign-in failed", e)
                callbackAndClear("Sign-in failed: ${e.message}")
            } catch (e: Exception) {
                Log.e("WakeSync", "Unexpected Google sign-in error", e)
                callbackAndClear("An unexpected error occurred: ${e.message}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    signInToFirebaseWithGoogleToken(googleCredential.idToken)
                } else {
                    callbackAndClear("Unexpected Google credential type: ${credential.type}")
                }
            }

            else -> {
                callbackAndClear("Unexpected credential type: ${credential::class.java.simpleName}")
            }
        }
    }

    private fun signInToFirebaseWithGoogleToken(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        saveBasicUserInfoToFirestore(user)
                    }
                    callbackAndClear(null)
                } else {
                    callbackAndClear(task.exception?.message ?: "Authentication failed.")
                }
            }
    }

    private fun saveBasicUserInfoToFirestore(user: com.google.firebase.auth.FirebaseUser) {
        val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
        val data = hashMapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "authDisplayName" to (user.displayName ?: ""),
            "profilePictureUrl" to (user.photoUrl?.toString() ?: ""),
            "lastLogin" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("WakeSync", "Saved basic Google profile for ${user.uid}")
                } else {
                    Log.e("WakeSync", "Failed to save basic user profile", task.exception)
                }
            }
    }

    private fun callbackAndClear(message: String?) {
        val callback = pendingAuthCallback
        pendingAuthCallback = null
        callback?.invoke(message)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        viewModel = com.social.wakesync.app.MainViewModel(),
        initiallyAuthenticated = false,
        isPermissionsGranted = false,
        onGoogleSignInRequested = {}
    )
}
