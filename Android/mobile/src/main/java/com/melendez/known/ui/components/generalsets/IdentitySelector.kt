package com.melendez.known.ui.components.generalsets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.melendez.known.R
import com.melendez.known.ui.components.PreferenceSingleChoiceItem
import com.melendez.known.util.Identity

@Composable
fun IdentitySelector(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val identityOptions = listOf(
        Identity.STUDENT to R.string.student,
        Identity.TEACHER to R.string.teacher,
        Identity.PARENT to R.string.parent,
    )

    Column(modifier = modifier) {
        identityOptions.forEach { (constant, textRes) ->
            PreferenceSingleChoiceItem(
                text = stringResource(textRes),
                selected = selected == constant,
                onClick = { onSelect(constant) },
            )
        }
    }
}