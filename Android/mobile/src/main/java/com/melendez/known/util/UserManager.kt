package com.melendez.known.util

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * User Manager for managing user-related information
 */
object UserManager {
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow(value = auth.currentUser)

    private val _userAvatar = MutableStateFlow(auth.currentUser?.photoUrl)
    val userAvatar: StateFlow<Uri?> = _userAvatar.asStateFlow()

    private val _userName = MutableStateFlow(auth.currentUser?.displayName)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(auth.currentUser?.email)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // Authentication Status Listener
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            _userAvatar.value = user?.photoUrl
            _userName.value = user?.displayName
            _userEmail.value = user?.email
            _isLoggedIn.value = user != null
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        auth.signOut()
    }
}