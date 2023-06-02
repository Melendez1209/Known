package com.melendez.known.add.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
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
    var full by rememberSaveable { mutableStateOf("") }

    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp),
            text = subject,
            style = MaterialTheme.typography.titleLarge
        )
        Row {
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                value = full,
                singleLine = true,
                label = { Text(stringResource(R.string.full_mark)) },
                placeholder = { Text(text = "150") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Right
                    )
                }),
                trailingIcon = {
                    IconButton(onClick = { full = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                },
                onValueChange = { full = it })
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                value = mark,
                singleLine = true,
                label = { Text(stringResource(R.string.mark)) },
                placeholder = { Text(text = "150") },
                isError = if (mark.isEmpty() || full.isEmpty()) false else mark.toFloat() > full.toFloat(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                trailingIcon = {
                    IconButton(onClick = { mark = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                },
                onValueChange = { mark = it })
        }
    }
}

@Preview
@Composable
fun Subject_Card_Preview() {
    Subject_Card(subject = "Subject")
}