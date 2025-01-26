package com.revakovskyi.giphy.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.uitls.SingleEvent
import com.revakovskyi.giphy.core.presentation.uitls.SnackBarController
import com.revakovskyi.giphy.core.presentation.uitls.snack_bar_models.SnackBarEvent
import kotlinx.coroutines.launch

@Composable
fun DefaultSnackBarHost(
    snackBarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()

    var snackBarEvent by remember { mutableStateOf<SnackBarEvent?>(null) }


    SingleEvent(flow = SnackBarController.events, snackBarHostState) { event ->
        coroutineScope.launch {
            snackBarEvent = event

            snackBarHostState.currentSnackbarData?.dismiss()

            snackBarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.name,
                duration = SnackbarDuration.Short
            )
        }
    }


    snackBarEvent?.let { event ->

        SnackbarHost(snackBarHostState) { data: SnackbarData ->

            Snackbar(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                action = {
                    if (event.action?.name != null) {
                        Text(
                            text = event.action.name,
                            style = MaterialTheme.typography.labelMedium,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .padding(end = 16.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        event.action.action.invoke()
                                        data.dismiss()
                                    }
                                }
                        )
                    }
                },
                content = {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.background,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 12.dp)
            )

        }

    }

}
