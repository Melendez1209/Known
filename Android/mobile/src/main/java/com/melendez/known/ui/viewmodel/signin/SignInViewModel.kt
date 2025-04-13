@file:Suppress("DEPRECATION")

package com.melendez.known.ui.viewmodel.signin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSuccessful = MutableStateFlow(false)
    val isSuccessful = _isSuccessful.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun signInWithEmailPassword() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (email.value.isBlank() || password.value.isBlank()) {
                    _errorMessage.value = "Email and password cannot be empty"
                    return@launch
                }

                auth.signInWithEmailAndPassword(email.value, password.value).await()
                _isSuccessful.value = true
            } catch (e: Exception) {
                Log.e("SignInViewModel", "Login Failed", e)
                _errorMessage.value = e.localizedMessage ?: "Login failed, please try again later"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmailPassword() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (email.value.isBlank() || password.value.isBlank()) {
                    _errorMessage.value = "Email and password cannot be empty"
                    return@launch
                }

                auth.createUserWithEmailAndPassword(email.value, password.value).await()
                _isSuccessful.value = true
            } catch (e: Exception) {
                Log.e("SignInViewModel", "Registration Failed", e)
                _errorMessage.value =
                    e.localizedMessage ?: "Registration failed, please try again later"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential).await()
                    _isSuccessful.value = true
                } else {
                    _errorMessage.value = "Google login failed. Please try again later."
                }
            } catch (e: Exception) {
                Log.e("SignInViewModel", "Google login failed.", e)
                _errorMessage.value =
                    e.localizedMessage ?: "Google login failed. Please try again later."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGitHub(context: Context) {
        val provider = OAuthProvider.newBuilder("github.com")
        provider.addCustomParameter("login", "")

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val pendingResultTask = auth.pendingAuthResult
                if (pendingResultTask != null) {
                    pendingResultTask.await()
                    _isSuccessful.value = true
                } else {
                    auth.startActivityForSignInWithProvider(
                        context as android.app.Activity,
                        provider.build()
                    )
                        .addOnSuccessListener {
                            _isSuccessful.value = true
                            _isLoading.value = false
                        }
                        .addOnFailureListener { e ->
                            Log.e("SignInViewModel", "GitHub login failed", e)
                            _errorMessage.value =
                                e.localizedMessage ?: "GitHub login failed. Please try again later."
                            _isLoading.value = false
                        }
                }
            } catch (e: Exception) {
                Log.e("SignInViewModel", "GitHub login failed", e)
                _errorMessage.value =
                    e.localizedMessage ?: "GitHub login failed. Please try again later."
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("884248348428-jj48bma61m76cq028tru1kffnqcii266.apps.googleusercontent.com")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }
}