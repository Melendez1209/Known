package com.melendez.known.add.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melendez.known.R

@Preview(device = "id:pixel_7_pro")
@Composable
fun Inputting() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Subject_Card("Chinese")
        }
    }
}

@Composable
fun Subject_Card(subject: String) {

    val focusManager = LocalFocusManager.current
    var mark by rememberSaveable { mutableStateOf("") }

    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp),
            text = subject
        )
        OutlinedTextField(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp),
            label = { Text(stringResource(R.string.mark)) },
            value = mark,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(
                    FocusDirection.Down
                )
            }),
            trailingIcon = {
                IconButton(onClick = { mark = "" }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = if (mark.isNotBlank()) stringResource(R.string.clear) else stringResource(
                            R.string.close_input
                        )
                    )
                }
            },
            onValueChange = { mark = it })

        var full by rememberSaveable { mutableStateOf("") }

        OutlinedTextField(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp),
            label = { Text(stringResource(R.string.full_mark)) },
            value = full,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = null),
            trailingIcon = {
                IconButton(onClick = { full = "" }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = if (mark.isNotBlank()) stringResource(R.string.clear) else stringResource(
                            R.string.close_input
                        )
                    )
                }
            },
            onValueChange = { full = it })
    }
}

@Preview
@Composable
fun Subject_Card_Preview() {
    Subject_Card(subject = "Subject")
}