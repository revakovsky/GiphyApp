package com.revakovskyi.giphy.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.theme.icons

@Composable
fun GiphyTextField(
    modifier: Modifier = Modifier,
    text: String,
    title: String = "",
    error: String? = null,
    hint: String = "",
    onDoneClick: () -> Unit,
    onClearClick: () -> Unit,
    onTextChange: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { onTextChange(it) },
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
        },
        placeholder = {
            Text(
                text = hint,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        textStyle = MaterialTheme.typography.labelLarge,
        supportingText = {
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        },
        isError = error != null,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()

                if (error == null) onDoneClick()
            },
        ),
        singleLine = true,
        maxLines = 1,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSecondary
        ),
        leadingIcon = {
            Icon(
                imageVector = if (isFocused) {
                    MaterialTheme.icons.inputBold
                } else MaterialTheme.icons.inputLight,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.onSecondary else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                Icon(
                    imageVector = MaterialTheme.icons.clear,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClearClick() }
                )
            }
        },
        modifier = modifier
            .onFocusEvent { event -> isFocused = event.isFocused },
    )

}

@Preview
@Composable
private fun PreviewSizesGiphyTextField() {
    GiphyAppTheme {
        GiphyTextField(
            text = "Input text",
            title = "Input your query",
            error = "Error",
            onTextChange = { input -> },
            onDoneClick = {},
            onClearClick = {}
        )
    }
}