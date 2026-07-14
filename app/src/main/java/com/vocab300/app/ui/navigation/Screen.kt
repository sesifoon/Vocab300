package com.vocab300.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object LessonList : Screen("lesson_list/{language}") {
        fun createRoute(language: String) = "lesson_list/$language"
    }
    object Lesson : Screen("lesson/{language}/{lessonIndex}") {
        fun createRoute(language: String, lessonIndex: Int) = "lesson/$language/$lessonIndex"
    }
    object Result : Screen(
        "result/{language}/{lessonIndex}/{total}/{correct}/{xp}/{timeMs}/{passed}"
    ) {
        fun createRoute(
            language: String,
            lessonIndex: Int,
            total: Int,
            correct: Int,
            xp: Int,
            timeMs: Long,
            passed: Boolean
        ) = "result/$language/$lessonIndex/$total/$correct/$xp/$timeMs/$passed"
    }
    object Review : Screen("review")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: String) = "word_detail/$wordId"
    }
}
