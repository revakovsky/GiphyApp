package com.revakovskyi.giphy.app.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revakovskyi.giphy.app.R
import com.revakovskyi.giphy.app.navigation.AppNavigation
import com.revakovskyi.giphy.app.presentation.components.NoInternetScreen
import com.revakovskyi.giphy.core.presentation.components.DefaultSnackBarHost
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent
import com.revakovskyi.giphy.core.presentation.ui.uitls.SnackBarController
import com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarAction
import com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarEvent
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.value.canOpenGifs == null
            }
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        settingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }

        setContent {
            com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme {
                MainContent(
                    viewModel = viewModel,
                    settingsLauncher = settingsLauncher
                )
            }
        }

    }

}


@Composable
private fun MainContent(
    viewModel: MainViewModel,
    settingsLauncher: ActivityResultLauncher<Intent>,
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }


    com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent(flow = viewModel.event) { event ->
        when (event) {
            MainEvent.ShowInternetNotification -> {
                coroutineScope.launch {
                    com.revakovskyi.giphy.core.presentation.ui.uitls.SnackBarController.sendEvent(
                        com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarEvent(
                            message = context.getString(R.string.no_internet_connection),
                            action = com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarAction(
                                name = context.getString(R.string.settings),
                                action = { settingsLauncher?.launch(Intent(Settings.ACTION_WIFI_SETTINGS)) }
                            )
                        )
                    )
                }
            }
        }
    }


    Scaffold(
        snackbarHost = {
            com.revakovskyi.giphy.core.presentation.components.DefaultSnackBarHost(
                snackBarHostState
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { _ ->

        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            val state = viewModel.state.collectAsStateWithLifecycle().value

            Crossfade(
                label = "",
                targetState = state.canOpenGifs
            ) { canOpenGifs ->
                when (canOpenGifs) {
                    true -> AppNavigation()
                    false -> NoInternetScreen(settingsLauncher)
                    null -> Unit
                }
            }

        }

    }

}