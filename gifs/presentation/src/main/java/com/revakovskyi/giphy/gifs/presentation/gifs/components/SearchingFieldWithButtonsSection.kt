package com.revakovskyi.giphy.gifs.presentation.gifs.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.components.GiphyButton
import com.revakovskyi.giphy.core.presentation.components.GiphyTextField
import com.revakovskyi.giphy.core.presentation.ui.uitls.rememberImeState
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsAction
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsState
import kotlinx.coroutines.launch

@Composable
fun SearchingFieldWithButtonsSection(
    state: GifsState,
    gridState: LazyGridState,
    onAction: (action: GifsAction) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()
    val imeStateOpen by rememberImeState()

    LaunchedEffect(imeStateOpen) {
        if (!imeStateOpen) localFocusManager.clearFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {

        GiphyTextField(
            text = state.searchingQuery,
            title = stringResource(R.string.input_a_searching_query),
            hint = stringResource(R.string.your_query),
            error = state.errorMessage?.asString(),
            onTextChange = { input -> onAction(GifsAction.ValidateQuery(input)) },
            onClearClick = { onAction(GifsAction.ClearQuery) },
            onDoneClick = {
                scope.launch { gridState.animateScrollToItem(1) }
                onAction(GifsAction.Search)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )

        AnimatedVisibility(
            label = "",
            visible = imeStateOpen,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {

                GiphyButton(
                    buttonText = stringResource(R.string.cancel),
                    onClick = {
                        keyboardController?.hide()
                        localFocusManager.clearFocus()
                        onAction(GifsAction.ClearQuery)
                    }
                )

                GiphyButton(
                    buttonText = stringResource(R.string.search),
                    buttonDefaultColor = MaterialTheme.colorScheme.onBackground,
                    enabled = state.errorMessage == null && state.searchingQuery.isNotEmpty(),
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(1) }
                        keyboardController?.hide()
                        localFocusManager.clearFocus()
                        onAction(GifsAction.Search)
                    }
                )

            }

        }

    }

}
