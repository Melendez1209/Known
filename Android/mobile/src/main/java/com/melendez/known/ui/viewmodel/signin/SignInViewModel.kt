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
                    _errorMessage.value = "邮箱和密码不能为空"
                    return@launch
                }

                auth.signInWithEmailAndPassword(email.value, password.value).await()
                _isSuccessful.value = true
            } catch (e: Exception) {
                Log.e("SignInViewModel", "登录失败", e)
                _errorMessage.value = e.localizedMessage ?: "登录失败，请稍后再试"
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
                    _errorMessage.value = "邮箱和密码不能为空"
                    return@launch
                }

                auth.createUserWithEmailAndPassword(email.value, password.value).await()
                _isSuccessful.value = true
            } catch (e: Exception) {
                Log.e("SignInViewModel", "注册失败", e)
                _errorMessage.value = e.localizedMessage ?: "注册失败，请稍后再试"
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
                    _errorMessage.value = "Google登录失败，请稍后再试"
                }
            } catch (e: Exception) {
                Log.e("SignInViewModel", "Google登录失败", e)
                _errorMessage.value = e.localizedMessage ?: "Google登录失败，请稍后再试"
            } finally {
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