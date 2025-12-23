package com.example.pethub.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for Firebase Authentication and Storage operations
 */
@Singleton
class FirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
    private val firestoreHelper: FirestoreHelper,
    @ApplicationContext private val context: Context
) {

    private val client = OkHttpClient()
    // TODO: Replace with your actual API Key or fetch it from BuildConfig
    private val IMGBB_API_KEY = "01c298d1456e56a6e95ba961002c73b0"

    // ============================================
    // AUTHENTICATION
    // ============================================

    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    /**
     * Register new user with email and password
     */
    suspend fun registerUser(
        email: String,
        password: String//,
        //displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {

                Result.success(user)
            } else {
                Result.failure(Exception("User registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                // Update FCM Token on login
                updateFCMToken()
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user email
     */
    suspend fun updateEmail(newEmail: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.updateEmail(newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user password
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reauthenticate user (required before sensitive operations)
     */
    suspend fun reauthenticate(email: String, password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get ID token for current user
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val tokenResult = user.getIdToken(forceRefresh).await()
            val token = tokenResult.token ?: return Result.failure(Exception("Token is null"))
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if user has admin claim
     */
    suspend fun isAdmin(): Boolean {
        return try {
            val user = auth.currentUser ?: return false
            val tokenResult = user.getIdToken(true).await()
            tokenResult.claims["admin"] == true
        } catch (e: Exception) {
            false
        }
    }

    // ============================================
    // FIREBASE CLOUD MESSAGING (FCM)
    // ============================================

    suspend fun getFCMToken(): Result<String> {
        return try {
            val token = messaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFCMToken(specificToken: String? = null) {
        try {
            val token = specificToken ?: messaging.token.await()
            val userId = getCurrentUserId()
            if (userId != null) {
                // Update token in Firestore
                // We attempt to update the customer document.
                firestoreHelper.updateDocument(
                    FirestoreHelper.COLLECTION_CUSTOMER,
                    userId,
                    mapOf("fcmToken" to token)
                )
            }
        } catch (e: Exception) {
            // Log error or ignore if user document doesn't exist yet
            e.printStackTrace()
        }
    }

    suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            messaging.subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            messaging.unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // IMGBB UPLOAD IMPLEMENTATION
    // ============================================

    /**
     * Upload image to ImgBB
     */
    suspend fun uploadImage(
        uri: Uri,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val base64Image = encodeImageToBase64(uri) ?: return@withContext Result.failure(Exception("Failed to encode image"))
                uploadBase64Image(base64Image, onProgress)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Upload Bitmap to ImgBB
     */
    suspend fun uploadBitmap(
        bitmap: Bitmap,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                
                uploadBase64Image(base64Image, onProgress)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun uploadBase64Image(
        base64Image: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        return try {
            onProgress?.invoke(10f)

            val formBody = FormBody.Builder()
                .add("key", IMGBB_API_KEY)
                .add("image", base64Image)
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(formBody)
                .build()

            onProgress?.invoke(50f)

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return Result.failure(Exception("ImgBB Upload failed: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)

            if (jsonResponse.has("error")) {
                val errorMsg = jsonResponse.getJSONObject("error").getString("message")
                return Result.failure(Exception("ImgBB Error: $errorMsg"))
            }

            val imageUrl = jsonResponse.getJSONObject("data").getString("url")

            onProgress?.invoke(100f)
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to convert Uri to Base64 String
     */
    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            // Compress to JPEG, 80% quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Wrapper functions
    suspend fun uploadProfileImage(userId: String, imageUri: Uri, onProgress: ((Float) -> Unit)? = null): Result<String> {
        return uploadImage(imageUri, onProgress)
    }

    suspend fun uploadPetImage(userId: String, petId: String, imageUri: Uri, onProgress: ((Float) -> Unit)? = null): Result<String> {
        return uploadImage(imageUri, onProgress)
    }

    suspend fun uploadServiceImage(serviceId: String, imageUri: Uri, onProgress: ((Float) -> Unit)? = null): Result<String> {
        return uploadImage(imageUri, onProgress)
    }

    suspend fun uploadPetQrCode(userId: String, petId: String, bitmap: Bitmap): Result<String> {
        return uploadBitmap(bitmap)
    }


    // ============================================
    // HELPER FUNCTIONS
    // ============================================

    /**
     * Get user email
     */
    fun getUserEmail(): String? = auth.currentUser?.email

    /**
     * Get user display name
     */
    fun getUserDisplayName(): String? = auth.currentUser?.displayName

    /**
     * Get user photo URL
     */
    fun getUserPhotoUrl(): Uri? = auth.currentUser?.photoUrl

    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        displayName: String? = null,
        photoUri: Uri? = null
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val profileUpdates = UserProfileChangeRequest.Builder().apply {
                if (displayName != null) setDisplayName(displayName)
                if (photoUri != null) setPhotoUri(photoUri)
            }.build()

            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
