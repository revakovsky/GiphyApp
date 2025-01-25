package com.revakovskyi.giphy.app.main

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revakovskyi.giphy.app.main.components.NoInternetScreen
import com.revakovskyi.giphy.app.navigation.AppNavigation
import com.revakovskyi.giphy.core.presentation.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.uitls.SingleEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

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

        setContent {
            GiphyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val state = viewModel.state.collectAsStateWithLifecycle().value

                        SingleEvent(flow = viewModel.event) { event ->
                            when (event) {
                                MainEvent.ShowInternetNotification -> {
                                    /*TODO: show a snackbar message*/
                                }
                            }
                        }

                        Crossfade(
                            label = "",
                            targetState = state.canOpenGifs
                        ) { canOpenGifs ->
                            when (canOpenGifs) {
                                true -> AppNavigation()
                                false -> NoInternetScreen()
                                null -> Unit
                            }
                        }

                    }
                }
            }
        }

    }

}
