package com.loop.app.ui

enum class Screen {
    Today,
    Tasks,
    Habits,
    Insights,
    More,
    Goals,
    Journal,
    Calendar,
    Deen,
    Search,
    Reviews,
    Settings,
}

enum class SheetType {
    NewTask,
    EditTask,
    NewHabit,
    EditHabit,
    NewJournal,
    EditJournal,
    NewGoal,
    EditGoal,
}

enum class TaskTab {
    Today,
    Inbox,
    Upcoming,
}

enum class InsightsRange {
    Week,
    Month,
}

enum class ThemeOption {
    System,
    Light,
    Dark,
}

enum class LanguageOption {
    English,
    Arabic,
}

data class Task(
    val id: String,
    val title: String,
    val done: Boolean,
    val today: Boolean,
    val details: String? = null,
    val scheduledDate: String,
)

data class Habit(
    val id: String,
    val title: String,
    val done: Boolean,
)

data class Goal(
    val id: String,
    val title: String,
    val progress: Int,
    val target: Int,
    val unit: String,
)

data class JournalEntry(
    val id: String,
    val date: String,
    val content: String,
)

data class Prayer(
    val name: String,
    val arabicName: String,
    val time: String,
    val done: Boolean,
)

data class Review(
    val id: String,
    val date: String,
    val wins: String,
    val challenges: String,
    val nextFocus: String,
)

data class SettingsState(
    val firstRunComplete: Boolean = false,
    val profileName: String = "",
    val theme: ThemeOption = ThemeOption.System,
    val language: LanguageOption = LanguageOption.English,
)

data class AppState(
    val tasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val journal: List<JournalEntry> = emptyList(),
    val prayers: List<Prayer> = defaultPrayers(),
    val reviews: List<Review> = emptyList(),
    val settings: SettingsState = SettingsState(),
)

fun defaultPrayers(): List<Prayer> = listOf(
    Prayer("Fajr", "الفجر", "4:47 AM", false),
    Prayer("Dhuhr", "الظهر", "12:15 PM", false),
    Prayer("Asr", "العصر", "3:38 PM", false),
    Prayer("Maghrib", "المغرب", "6:52 PM", false),
    Prayer("Isha", "العشاء", "8:18 PM", false),
)
