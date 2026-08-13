package com.melendez.known.ui.components.generalsets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.melendez.known.util.subjectKeyToStringResource
import com.melendez.known.util.subjectKeys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSelector(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        subjectKeys.chunked(3).forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowKeys.forEach { key ->
                    FilterChip(
                        selected = key in selected,
                        onClick = { onToggle(key) },
                        label = { Text(stringResource(subjectKeyToStringResource(key))) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowKeys.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}