@file:Suppress("DEPRECATION")

package com.melendez.known.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.melendez.known.R
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.viewmodel.signin.SignInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Signin(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    val viewModel: SignInViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccessful by viewModel.isSuccessful.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Whether the registration mode is active
    var isRegisterMode by remember { mutableStateOf(false) }

    // Google sign-in launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.handleGoogleSignInResult(task)
        } else {
            // Login cancelled or failed
            Toast.makeText(context, "Google login cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Process after successful login
    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            // Login successful, return to the previous screen or home
            Toast.makeText(
                context,
                if (isRegisterMode) "Registration successful" else "Login successful",
                Toast.LENGTH_SHORT
            ).show()
            navTotalController.popBackStack()
        }
    }

    // Display error information
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SharedTopBar(
            title = stringResource(if (isRegisterMode) R.string.sign_up else R.string.sign_in),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )

        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.sign_to),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
                        )
                    }

                    item {
                        // Email input field
                        LoginForm(
                            viewModel = viewModel,
                            isRegisterMode = isRegisterMode,
                            onSubmit = {
                                if (isRegisterMode) {
                                    viewModel.signUpWithEmailPassword()
                                } else {
                                    viewModel.signInWithEmailPassword()
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Forgot password button
                        if (!isRegisterMode) {
                            TextButton(onClick = { /* Forgot password processing */ }) {
                                Text(text = stringResource(R.string.forgot_password))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Switch between login/registration mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(if (isRegisterMode) R.string.has_account else R.string.no_account),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                Text(
                                    text = stringResource(if (isRegisterMode) R.string.sign_in else R.string.sign_up),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    item {
                        // Separator and "or" text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = stringResource(R.string.or_continue_with),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        // Google sign-in button
                        ElevatedButton(
                            onClick = {
                                val googleSignInClient = viewModel.getGoogleSignInClient(context)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_account_circle_24),
                                contentDescription = "Google",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                            )
                            Text(text = stringResource(R.string.google_sign_in))
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Loading indicator
                if (isLoading) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    viewModel: SignInViewModel,
    isRegisterMode: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(stringResource(R.string.email)) },
            placeholder = { Text(stringResource(R.string.email_hint)) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateEmail("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true
        )

        // Password input field
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(stringResource(R.string.password)) },
            placeholder = { Text(stringResource(R.string.password_hint)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }
            ),
            singleLine = true
        )

        // Login/registration button
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = stringResource(if (isRegisterMode) R.string.register_with_email else R.string.email_sign_in),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun SigninPreview() {
    Signin(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}