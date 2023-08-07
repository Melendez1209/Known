package com.melendez.known.main.inners

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.melendez.known.R
import com.melendez.known.Screens

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun History(paddingValues: PaddingValues? = null, navTotalController: NavHostController) {

    var checkboxes = remember { mutableStateListOf(false, false, false) }

    Surface {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (checkboxes.isNotEmpty()) {

                var visible by remember { mutableStateOf(false) }
                val rowPadding by animateDpAsState(
                    targetValue = if (visible) 36.dp else 12.dp,
                    label = "Checkbox spacing to expand or not"
                )

                var triState by remember { mutableStateOf(ToggleableState.Indeterminate) }
                val toggleTriState = {
                    triState = when (triState) {
                        ToggleableState.On -> ToggleableState.Off
                        ToggleableState.Off -> ToggleableState.On
                        else -> ToggleableState.On
                    }
                    checkboxes.indices.forEach { index ->
                        checkboxes[index] = triState == ToggleableState.On
                    }
                }

                var key by remember { mutableStateOf("") }
                var active by remember { mutableStateOf(false) }
                val searchbarPadding by animateDpAsState(
                    targetValue = if (active) 0.dp else 12.dp,
                    label = "SearchBar spacing to expand or not"
                )

                BackHandler(enabled = visible) {
                    visible = false
                    checkboxes = mutableStateListOf(false, false, false)
                }

                SearchBar(
                    query = key,
                    onQueryChange = { key = it },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = searchbarPadding),
                    placeholder = { Text(text = stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            enabled = if (key.isNotBlank()) true else active,
                            onClick = {
                                if (key.isNotBlank()) {
                                    key = ""
                                } else {
                                    active = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = if (key.isNotBlank()) stringResource(R.string.clear) else stringResource(
                                    R.string.close_bar
                                )
                            )
                        }
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(4) { index ->

                            val resultText = stringResource(R.string.exam) + index

                            ListItem(
                                headlineContent = { Text(resultText) },
                                modifier = Modifier.clickable {
                                    key = resultText
                                    active = false
                                },
                                supportingContent = { Text(text = "$index") }, //TODO: Marks
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.School,
                                        contentDescription = stringResource(R.string.exam) + index
                                    )
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(visible = visible) {
                        TriStateCheckbox(state = triState, onClick = toggleTriState)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = rowPadding, end = 12.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
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
                            text = stringResource(R.string.mark),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                val _isRefreshing: MutableLiveData<Boolean> = MutableLiveData(false)
                val isRefreshing by _isRefreshing.observeAsState(false) //TODO:Replace the LiveData with the Room

                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                    onRefresh = {
                        //TODO:Refresh
                    }
                ) {
                    LazyColumn(modifier = if (paddingValues != null) Modifier.padding(bottom = paddingValues.calculateBottomPadding()) else Modifier) {
                        checkboxes.forEachIndexed { index, it ->
                            item {
                                Row(
                                    modifier = Modifier.padding(
                                        top = 6.dp,
                                        bottom = 6.dp,
                                        start = rowPadding,
                                        end = 12.dp
                                    ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AnimatedVisibility(visible = visible) {
                                        Checkbox(
                                            checked = it,
                                            onCheckedChange = {
                                                checkboxes[index] = it
                                                triState =
                                                    if (
                                                        checkboxes.stream().allMatch { it }
                                                    ) ToggleableState.On
                                                    else if (
                                                        checkboxes.stream().allMatch { !it }
                                                    ) ToggleableState.Off
                                                    else ToggleableState.Indeterminate
                                            }
                                        )
                                    }
                                    Card(
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    //TODO:Jump to the details page
                                                }, onLongClick = {
                                                    if (!visible) {
                                                        checkboxes[index] = true
                                                        triState =
                                                            if (
                                                                checkboxes.stream().allMatch { it }
                                                            ) ToggleableState.On
                                                            else if (
                                                                checkboxes.stream().allMatch { !it }
                                                            ) ToggleableState.Off
                                                            else ToggleableState.Indeterminate
                                                    } else {
                                                        checkboxes =
                                                            mutableStateListOf(false, false, false)
                                                    }
                                                    visible = !visible
                                                }
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = stringResource(R.string.exam),
                                                modifier = Modifier.padding(start = 12.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = stringResource(R.string.time),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                text = "150",
                                                modifier = Modifier.padding(end = 12.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_history),
                    modifier = Modifier.padding(vertical = 3.dp)
                )
                TextButton(
                    onClick = { navTotalController.navigate(Screens.DRP.router) },
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Text(text = stringResource(id = R.string.add))
                }
            }
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun History_Preview() {
    History(navTotalController = rememberNavController())
}