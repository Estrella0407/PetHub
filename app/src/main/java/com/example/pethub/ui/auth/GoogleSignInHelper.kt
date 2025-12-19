package com.example.pethub.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

suspend fun signInWithGoogle(
    context: Context,
    webClientId: String,
    onTokenReceived: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(
        context = context,
        request = request
    )

    val credential = result.credential
    if (credential is GoogleIdTokenCredential) {
        onTokenReceived(credential.idToken)
    }
}

fun firebaseSignInWithGoogle(
    idToken: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    FirebaseAuth.getInstance()
        .signInWithCredential(credential)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onError(it) }
}