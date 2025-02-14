package com.melendez.known.ui.screens.feedback

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.melendez.known.R
import com.melendez.known.ui.components.SharedTopBar
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Bug(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        SharedTopBar(
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            title = stringResource(R.string.bug),
            scrollBehavior = scrollBehavior
        )
        Bug_Content(
            widthSizeClass = widthSizeClass,
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }
}

@Composable
fun Bug_Content(widthSizeClass: WindowWidthSizeClass, modifier: Modifier) {

    val focusManager = LocalFocusManager.current
    var showingDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    val steps = remember { mutableStateListOf("") }

    val context = LocalContext.current

    val pickMultipleMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(steps.size + 1)
    ) {
        Log.d("Melendez", "Me: Url = $it")
        // TODO: Save it
    }
    val videoPicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
            if (it != null) {
                Log.d("Melendez", "Me: Url = $it")
                // TODO: Save it
            } else {
                Log.d("Melendez", "Me: No media selected")
            }
        }

    val failure = stringResource(R.string.failure)
    val toastTitle = stringResource(R.string.input_title)

    if (showingDialog) {
        AlertDialog(
            onDismissRequest = {
                showingDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showingDialog = false
                        videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                ) {
                    Text(stringResource(R.string.screen_record))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showingDialog = false
                        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Text(text = stringResource(R.string.screenshots))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.UploadFile,
                    contentDescription = stringResource(id = R.string.method)
                )
            },
            title = {
                Text(text = stringResource(R.string.method))
            }
        )
    }

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
                    placeholder = { Text(text = stringResource(R.string.bug_about)) },
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
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    singleLine = true
                )
            }
            item {
                Text(
                    text = stringResource(R.string.reproduce_bug),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            steps.forEachIndexed { index, step ->
                item {
                    OutlinedTextField(
                        value = step,
                        onValueChange = { text: String -> steps[index] = text },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp, horizontal = 12.dp),
                        label = { Text(text = "${index + 1}.") },
                        trailingIcon = {
                            IconButton(
                                onClick = { steps[index] = "" },
                                enabled = steps[index].isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.clear)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (steps.all { it.isNotEmpty() } && steps.size < 10) {
                                    steps.add("")
                                }
                                focusManager.moveFocus(FocusDirection.Next)
                            }
                        )
                    )
                }
            }
            item {
                if (widthSizeClass == WindowWidthSizeClass.Compact) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp, start = 12.dp, end = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { steps.add("") },
                            enabled = steps.all { it.isNotEmpty() } && steps.size < 10
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(id = R.string.add)
                            )
                            Text(text = stringResource(id = R.string.add))
                        }
                        Button(
                            onClick = { showingDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Upload,
                                contentDescription = stringResource(R.string.upload_feedback)
                            )
                            Text(
                                text = stringResource(R.string.upload_feedback),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp, start = 12.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { steps.add("") },
                            modifier = Modifier.align(Alignment.CenterVertically),
                            enabled = steps.all { it.isNotEmpty() } && steps.size < 10
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(id = R.string.add)
                            )
                            Text(text = stringResource(id = R.string.add))
                        }
                        Button(
                            onClick = { showingDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Upload,
                                contentDescription = stringResource(R.string.upload_feedback)
                            )
                            Text(
                                text = stringResource(R.string.upload_feedback),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = actual,
                    onValueChange = { actual = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp, horizontal = 12.dp)
                        .height(128.dp),
                    label = { Text(text = stringResource(R.string.actual)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    )
                )
            }
            item {
                OutlinedTextField(
                    value = expected,
                    onValueChange = { expected = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp, horizontal = 12.dp)
                        .height(128.dp),
                    label = { Text(text = stringResource(R.string.expected)) },
                    placeholder = { Text(text = stringResource(R.string.forward)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (title.isNotEmpty() && actual.isNotEmpty()) {
                                feedbackBug(
                                    title = title,
                                    steps = steps,
                                    context = context,
                                    failure = failure
                                )
                            } else {
                                Toast.makeText(context, toastTitle, Toast.LENGTH_SHORT).show()
                                Log.e("Melendez", "Bug-withoutTitle: ")
                            }
                        }
                    )
                )
            }
            item {
                Button(
                    onClick = {
                        feedbackBug(
                            title = title,
                            steps = steps,
                            context = context,
                            failure = failure
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    enabled = title.isNotEmpty() && actual.isNotEmpty()
                ) {
                    Text(text = stringResource(R.string.preview))
                }
            }
        }
    }
}

fun feedbackBug(title: String, steps: MutableList<String>, context: Context, failure: String) {

    val params = HashMap<String, String>()
    params["title"] = title
    params["body"] = steps.mapIndexed { index, item -> "${index + 1}. $item" }
        .joinToString(separator = "\n")

    val queue = Volley.newRequestQueue(context)

    val jsonObjectRequest = object : JsonObjectRequest(
        Method.POST,
        "https://api.github.com/repos/Melendez1209/Known/issues",
        JSONObject(params as Map<*, *>),
        Response.Listener { response ->
            Log.d("Melendez", "Bug-response: $response")
        },
        Response.ErrorListener { error ->
            Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
            Log.e("Melendez", "Bug-error: $error")
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

@Preview(device = "id:pixel_9_pro")
@Composable
fun Bug_Preview() {
    Bug(widthSizeClass = WindowWidthSizeClass.Compact, navTotalController = rememberNavController())
}