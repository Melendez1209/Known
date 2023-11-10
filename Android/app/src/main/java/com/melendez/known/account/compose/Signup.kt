package com.melendez.known.account.compose

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens


@Composable
fun Signup(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Signup_Compat(navTotalController = navTotalController)
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> Signup_MediumExpanded(
            navTotalController = navTotalController
        )

        else -> Signup_Compat(navTotalController = navTotalController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Signup_Compat(navTotalController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text(text = stringResource(R.string.sign_up)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 12.dp)
            )
            Signup_Content(navTotalController = navTotalController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Signup_MediumExpanded(navTotalController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.sign_in)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(vertical = 12.dp)
                )
                Signup_Content(navTotalController = navTotalController)
            }
        }
    }
}

@Composable
fun Signup_Content(navTotalController: NavHostController) {

    val focusManager = LocalFocusManager.current
    val previousDestinationRoute = navTotalController.previousBackStackEntry?.destination?.route

    var userName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 6.dp),
            label = { Text(text = stringResource(R.string.user_name)) },
            trailingIcon = {
                IconButton(onClick = { userName = "" }, enabled = userName.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.clear)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            label = { Text(text = stringResource(R.string.password)) },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = password.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = stringResource(R.string.clear)
                    )
                }
            },
            visualTransformation = if (!passwordVisible) PasswordVisualTransformation('*') else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    // TODO: Sign in
                }
            ),
            singleLine = true
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { /*TODO: Sign up*/ }) {
                Text(text = stringResource(id = R.string.sign_up))
            }
            TextButton(onClick = {
                Log.d("Melendez", "Signin_Content: last page:$previousDestinationRoute")
                if (previousDestinationRoute != "Signin") {
                    navTotalController.navigate(Screens.Signin.router)
                } else {
                    navTotalController.popBackStack()
                }
            }) {
                Text(stringResource(R.string.sign_in))
            }
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Signup_Preview() {
    Signup(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}
