package com.validador_actividades_duoc.data.repository

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.validador_actividades_duoc.R
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val usuarioFirebase: FirebaseUser?
        get() = auth.currentUser

    suspend fun iniciarSesionGoogle(
        activity: Activity
    ): FirebaseUser {

        val googleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = activity.getString(
                R.string.default_web_client_id
            )
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val credentialManager = CredentialManager.create(activity)

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        require(
            credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            "No se obtuvo una credencial Google válida"
        }

        val googleCredential =
            GoogleIdTokenCredential.createFrom(credential.data)

        val firebaseCredential = GoogleAuthProvider.getCredential(
            googleCredential.idToken,
            null
        )

        val authResult = auth.signInWithCredential(
            firebaseCredential
        ).await()

        return authResult.user
            ?: error("Firebase no devolvió un usuario")
    }

    suspend fun cerrarSesion(activity: Activity) {
        auth.signOut()

        CredentialManager.create(activity)
            .clearCredentialState(
                ClearCredentialStateRequest()
            )
    }
}
