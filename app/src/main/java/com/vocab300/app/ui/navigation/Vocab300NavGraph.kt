package com.vocab300.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vocab300.app.VocabApp
import com.vocab300.app.domain.Language
import com.vocab300.app.ui.screens.favorites.FavoritesScreen
import com.vocab300.app.ui.screens.home.HomeScreen
import com.vocab300.app.ui.screens.lesson.LessonScreen
import com.vocab300.app.ui.screens.lessonlist.LessonListScreen
import com.vocab300.app.ui.screens.onboarding.OnboardingScreen
import com.vocab300.app.ui.screens.result.ResultScreen
import com.vocab300.app.ui.screens.review.ReviewScreen
import com.vocab300.app.ui.screens.search.SearchScreen
import com.vocab300.app.ui.screens.settings.SettingsScreen
import com.vocab300.app.ui.screens.splash.SplashScreen
import com.vocab300.app.ui.screens.stats.StatsScreen
import com.vocab300.app.ui.screens.worddetail.WordDetailScreen

@Composable
fun Vocab300NavGraph(app: VocabApp) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                app = app,
                onGoToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onGoToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                app = app,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                app = app,
                onOpenLessonList = { language ->
                    navController.navigate(Screen.LessonList.createRoute(language.name))
                },
                onOpenReview = { navController.navigate(Screen.Review.route) },
                onOpenStats = { navController.navigate(Screen.Stats.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenSearch = { navController.navigate(Screen.Search.route) },
                onOpenFavorites = { navController.navigate(Screen.Favorites.route) }
            )
        }

        composable(
            Screen.LessonList.route,
            arguments = listOf(navArgument("language") { type = NavType.StringType })
        ) { backStackEntry ->
            val languageStr = backStackEntry.arguments?.getString("language") ?: "EN"
            val language = runCatching { Language.valueOf(languageStr) }.getOrDefault(Language.EN)
            LessonListScreen(
                app = app,
                language = language,
                onBack = { navController.popBackStack() },
                onOpenLesson = { lessonIndex ->
                    navController.navigate(Screen.Lesson.createRoute(language.name, lessonIndex))
                }
            )
        }

        composable(
            Screen.Lesson.route,
            arguments = listOf(
                navArgument("language") { type = NavType.StringType },
                navArgument("lessonIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val languageStr = backStackEntry.arguments?.getString("language") ?: "EN"
            val language = runCatching { Language.valueOf(languageStr) }.getOrDefault(Language.EN)
            val lessonIndex = backStackEntry.arguments?.getInt("lessonIndex") ?: 0

            LessonScreen(
                app = app,
                language = language,
                lessonIndex = lessonIndex,
                onExit = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onFinished = { total, correct, xp, timeMs, passed ->
                    navController.navigate(
                        Screen.Result.createRoute(language.name, lessonIndex, total, correct, xp, timeMs, passed)
                    ) {
                        popUpTo(Screen.Lesson.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Screen.Result.route,
            arguments = listOf(
                navArgument("language") { type = NavType.StringType },
                navArgument("lessonIndex") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("xp") { type = NavType.IntType },
                navArgument("timeMs") { type = NavType.LongType },
                navArgument("passed") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val languageStr = args?.getString("language") ?: "EN"
            val language = runCatching { Language.valueOf(languageStr) }.getOrDefault(Language.EN)
            val lessonIndex = args?.getInt("lessonIndex") ?: 0

            ResultScreen(
                total = args?.getInt("total") ?: 0,
                correct = args?.getInt("correct") ?: 0,
                xp = args?.getInt("xp") ?: 0,
                timeMs = args?.getLong("timeMs") ?: 0L,
                passed = args?.getBoolean("passed") ?: false,
                onGoHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = {
                    navController.navigate(Screen.Lesson.createRoute(language.name, lessonIndex)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(app = app, onBack = { navController.popBackStack() })
        }

        composable(Screen.Stats.route) {
            StatsScreen(app = app, onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(app = app, onBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onOpenWord = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onOpenWord = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) }
            )
        }

        composable(
            Screen.WordDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getString("wordId") ?: ""
            WordDetailScreen(app = app, wordId = wordId, onBack = { navController.popBackStack() })
        }
    }
}
