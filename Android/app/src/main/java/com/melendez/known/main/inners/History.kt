package com.melendez.known.main.inners

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melendez.known.R

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_7_pro", showBackground = true)
@Composable
fun History() {
    Column {

        var text by remember { mutableStateOf("") }
        var active by remember { mutableStateOf(false) }

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            query = text,
            onQueryChange = { text = it },
            onSearch = { active = false },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text(text = stringResource(R.string.search)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search)
                )
            },
            trailingIcon = {
                if (active) {
                    IconButton(onClick = {
                        if (text.isNotBlank()) {
                            text = ""
                        } else {
                            active = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            },
            enabled = true //TODO:Changes based on the presence or absence of a history
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(4) { index ->

                    val resultText = stringResource(R.string.exam_name) + index

                    ListItem(modifier = Modifier.clickable {
                        text = resultText
                        active = false
                    },
                        headlineContent = { Text(resultText) },
                        supportingContent = { Text(text = stringResource(R.string.classification) + index) },
                        leadingContent = {
                            Icon(
                                Icons.Rounded.LocalLibrary,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Text(text = stringResource(R.string.exam_name))
            Text(text = stringResource(R.string.time))
            Text(text = stringResource(R.string.classification))
        }

        LazyColumn {

            items(100) { count ->
                Card(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    onClick = { TODO("Jump to the details page") }) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.exam_name) + count.toString(),
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Text(text = stringResource(R.string.time) + count.toString())
                        Text(text = count.toString(), modifier = Modifier.padding(end = 12.dp))
                    }
                }
            }
        }
    }
}