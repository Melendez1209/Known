package com.melendez.known.ui.screens.feedback

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.melendez.known.R
import org.json.JSONObject

@Composable
fun Feature(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Feature_CompactExpanded(navTotalController = navTotalController)
        WindowWidthSizeClass.Medium -> Feature_Medium(navTotalController = navTotalController)
        WindowWidthSizeClass.Expanded -> Feature_CompactExpanded(navTotalController = navTotalController)
        else -> Feature_CompactExpanded(navTotalController = navTotalController)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feature_CompactExpanded(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.feature)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Feature_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(top = padding.calculateTopPadding())
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feature_Medium(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = stringResource(R.string.feature)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Feature_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(top = padding.calculateTopPadding())
        )
    }
}

@Composable
fun Feature_Content(modifier: Modifier) {

    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    val failure = stringResource(R.string.failure)

    var title by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 6.dp),

                    label = { Text(text = stringResource(R.string.title)) },
                    placeholder = { Text(text = stringResource(R.string.want_functionality)) },
                    trailingIcon = {
                        IconButton(onClick = { title = "" }, enabled = title.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = expected,
                    onValueChange = { expected = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .padding(vertical = 3.dp, horizontal = 12.dp),
                    label = { Text(text = stringResource(id = R.string.expected)) },
                    placeholder = { Text(text = stringResource(id = R.string.forward)) }
                )
            }
            item {
                Button(
                    onClick = {
                        feedbackFeature(
                            title = title,
                            feature = expected,
                            context = context,
                            failure = failure
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    enabled = title.isNotEmpty() && expected.isNotEmpty(),
                ) {
                    Text(text = stringResource(R.string.preview))
                }
            }
        }
    }
}


fun feedbackFeature(title: String, feature: String, context: Context, failure: String) {

    val queue = Volley.newRequestQueue(context)

    val params = HashMap<String, String>()
    params["title"] = title
    params["body"] = feature

    val jsonObjectRequest = object : JsonObjectRequest(
        Method.POST,
        "https://api.github.com/repos/Melendez1209/Known/issues",
        JSONObject((params as Map<*, *>?)!!),
        Response.Listener { response ->
            Log.d("Melendez", "Feature-response: $response")
        },
        Response.ErrorListener { error ->
            Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
            Log.e("Melendez", "Feature-error: $error")
        }
    ) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] =
                "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"
            return headers
        }
    }
    queue.add(jsonObjectRequest)
}

@Preview(device = "id:pixel_8_pro")
@Composable
fun Feature_Preview() {
    Feature(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}
