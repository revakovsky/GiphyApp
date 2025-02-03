package com.revakovskyi.giphy.gifs.presentation.gifs.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revakovskyi.giphy.gifs.presentation.R

@Composable
fun UserHint() {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp)
    ) {
        Text(
            text = stringResource(R.string.input_a_searching_query_to_get_new_gifs),
            style = MaterialTheme.typography.labelLarge,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        )
    }

}
