package com.loop.app.ui

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class LoopLocalStore(context: Context, userId: String) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "loop_local_store_${userId.toSafeStoreName()}",
        Context.MODE_PRIVATE,
    )

    fun load(): Result<AppState> = runCatching {
        val raw = prefs.getString(KEY_STATE, null) ?: return@runCatching AppState()
        val json = JSONObject(raw)
        AppState(
            tasks = json.optJSONArray("tasks").toTasks(),
            habits = json.optJSONArray("habits").toHabits(),
            goals = json.optJSONArray("goals").toGoals(),
            journal = json.optJSONArray("journal").toJournal(),
            prayers = json.optJSONArray("prayers").toPrayers().ifEmpty { defaultPrayers() },
            reviews = json.optJSONArray("reviews").toReviews(),
            settings = json.optJSONObject("settings").toSettings(),
        )
    }

    fun save(state: AppState): Result<Unit> = runCatching {
        val json = JSONObject()
            .put("tasks", state.tasks.toJsonArray { it.toJson() })
            .put("habits", state.habits.toJsonArray { it.toJson() })
            .put("goals", state.goals.toJsonArray { it.toJson() })
            .put("journal", state.journal.toJsonArray { it.toJson() })
            .put("prayers", state.prayers.toJsonArray { it.toJson() })
            .put("reviews", state.reviews.toJsonArray { it.toJson() })
            .put("settings", state.settings.toJson())
        prefs.edit { putString(KEY_STATE, json.toString()) }
    }

    fun reset(): Result<AppState> = runCatching {
        prefs.edit { clear() }
        AppState(settings = SettingsState(firstRunComplete = true))
    }

    private fun Task.toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("done", done)
        .put("today", today)
        .put("details", details)
        .put("scheduledDate", scheduledDate)

    private fun Habit.toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("done", done)

    private fun Goal.toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("progress", progress)
        .put("target", target)
        .put("unit", unit)

    private fun JournalEntry.toJson() = JSONObject()
        .put("id", id)
        .put("date", date)
        .put("content", content)

    private fun Prayer.toJson() = JSONObject()
        .put("name", name)
        .put("arabicName", arabicName)
        .put("time", time)
        .put("done", done)

    private fun Review.toJson() = JSONObject()
        .put("id", id)
        .put("date", date)
        .put("wins", wins)
        .put("challenges", challenges)
        .put("nextFocus", nextFocus)

    private fun SettingsState.toJson() = JSONObject()
        .put("firstRunComplete", firstRunComplete)
        .put("profileName", profileName)
        .put("theme", theme.name)
        .put("language", language.name)

    private fun JSONArray?.toTasks(): List<Task> = mapObjects { obj ->
        Task(
            id = obj.optString("id"),
            title = obj.optString("title"),
            done = obj.optBoolean("done"),
            today = obj.optBoolean("today"),
            details = obj.optString("details").takeIf { it.isNotBlank() && it != "null" },
            scheduledDate = obj.optString("scheduledDate").ifBlank { todayIsoDate() },
        )
    }

    private fun JSONArray?.toHabits(): List<Habit> = mapObjects { obj ->
        Habit(
            id = obj.optString("id"),
            title = obj.optString("title"),
            done = obj.optBoolean("done"),
        )
    }

    private fun JSONArray?.toGoals(): List<Goal> = mapObjects { obj ->
        Goal(
            id = obj.optString("id"),
            title = obj.optString("title"),
            progress = obj.optInt("progress"),
            target = obj.optInt("target").takeIf { it > 0 } ?: 1,
            unit = obj.optString("unit").ifBlank { "units" },
        )
    }

    private fun JSONArray?.toJournal(): List<JournalEntry> = mapObjects { obj ->
        JournalEntry(
            id = obj.optString("id"),
            date = obj.optString("date").ifBlank { todayIsoDate() },
            content = obj.optString("content"),
        )
    }

    private fun JSONArray?.toPrayers(): List<Prayer> = mapObjects { obj ->
        Prayer(
            name = obj.optString("name"),
            arabicName = obj.optString("arabicName"),
            time = obj.optString("time"),
            done = obj.optBoolean("done"),
        )
    }

    private fun JSONArray?.toReviews(): List<Review> = mapObjects { obj ->
        Review(
            id = obj.optString("id"),
            date = obj.optString("date").ifBlank { todayIsoDate() },
            wins = obj.optString("wins"),
            challenges = obj.optString("challenges"),
            nextFocus = obj.optString("nextFocus"),
        )
    }

    private fun JSONObject?.toSettings(): SettingsState {
        if (this == null) return SettingsState()
        return SettingsState(
            firstRunComplete = optBoolean("firstRunComplete"),
            profileName = optString("profileName"),
            theme = enumValueOrDefault(optString("theme"), ThemeOption.System),
            language = enumValueOrDefault(optString("language"), LanguageOption.English),
        )
    }

    private inline fun <T> JSONArray?.mapObjects(block: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { add(block(it)) }
            }
        }
    }

    private inline fun <T> List<T>.toJsonArray(block: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { array.put(block(it)) }
        return array
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(raw: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == raw } ?: fallback

    private companion object {
        const val KEY_STATE = "state"

        fun String.toSafeStoreName(): String = replace(Regex("[^A-Za-z0-9_.-]"), "_")
    }
}
