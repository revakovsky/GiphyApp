package com.revakovskyi.giphy.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsScreenRoot
import com.revakovskyi.giphy.gifs.presentation.original_gif.OriginalGifScreenRoot
import kotlin.reflect.typeOf

private const val TRANSITION_DURATION = 300

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Gifs,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_DURATION)
            )
        }
    ) {

        composable<Routes.Gifs> {
            GifsScreenRoot(
                openOriginalGif = { gif ->
                    navController.navigate(Routes.Original(gif))
                }
            )
        }

        composable<Routes.Original>(
            typeMap = mapOf(typeOf<Gif>() to CustomNavType.PetType)
        ) {
            OriginalGifScreenRoot(
                gif = it.toRoute<Routes.Original>().gif,
                goBack = { navController.navigateUp() }
            )
        }

    }

}