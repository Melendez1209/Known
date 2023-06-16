package com.melendez.known.main.inners

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.melendez.known.R

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_7_pro", showBackground = true)
@Composable
fun History(paddingValues: PaddingValues? = null) {
    Surface {
        Column {

            var text by remember { mutableStateOf("") }
            var active by remember { mutableStateOf(false) }
            val searchbarPadding by animateDpAsState(
                targetValue = if (active) 0.dp else 12.dp,
                label = "SearchBar spacing to expand or not"
            )

            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = searchbarPadding),
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
                    IconButton(enabled = if (text.isNotBlank()) true else active,
                        onClick = {
                            if (text.isNotBlank()) {
                                text = ""
                            } else {
                                active = false
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = if (text.isNotBlank()) stringResource(R.string.clear) else stringResource(
                                R.string.close_bar
                            )
                        )
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

                        val resultText = stringResource(R.string.exam) + index

                        ListItem(modifier = Modifier.clickable {
                            text = resultText
                            active = false
                        },
                            headlineContent = { Text(resultText) },
                            supportingContent = { Text(text = stringResource(R.string.classification) + index) },
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.LocalLibrary, contentDescription = null
                                )
                            })
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.exam),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.time),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.classification),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            val _isRefreshing: MutableLiveData<Boolean> = MutableLiveData(false)
            val isRefreshing by _isRefreshing.observeAsState(false) //TODO:Replace the LiveData with the Room

            SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                onRefresh = {
                    //TODO:Refresh
                }) {
                LazyColumn(modifier = if (paddingValues != null) Modifier.padding(bottom = paddingValues.calculateBottomPadding()) else Modifier) {
                    items(30) { count ->
                        Card(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                            onClick = {
                                //TODO:Jump to the details page
                            }) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                            ) {
                                Text(
                                    modifier = Modifier.padding(start = 12.dp),
                                    text = stringResource(R.string.exam) + count.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.time) + count.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    modifier = Modifier.padding(end = 12.dp),
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}