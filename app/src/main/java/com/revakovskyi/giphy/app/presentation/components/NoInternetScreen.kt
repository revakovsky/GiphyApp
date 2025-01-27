package com.revakovskyi.giphy.app.presentation.components

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.app.R
import com.revakovskyi.giphy.core.presentation.components.GiphyButton
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.theme.icons

@Composable
fun NoInternetScreen(settingsLauncher: ActivityResultLauncher<Intent>) {

    GradientBackground {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {

            Icon(
                imageVector = MaterialTheme.icons.wiFi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(100.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {

                Text(
                    text = stringResource(id = R.string.oops),
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.no_internet_connection),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

            }

            GiphyButton(
                buttonText = stringResource(id = R.string.open_settings),
                onClick = {
                    settingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            )

        }

    }

}


@PreviewScreenSizes
@Composable
private fun PreviewSizesNoInternetScreen() {
    GiphyAppTheme {
        NoInternetScreen(
            settingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { _ -> }
        )
    }
}