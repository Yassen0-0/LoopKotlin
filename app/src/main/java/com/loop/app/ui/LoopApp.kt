package com.loop.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loop.app.ui.components.AddRow
import com.loop.app.ui.components.FullScreenSurface
import com.loop.app.ui.components.LoopBottomNavigation
import com.loop.app.ui.components.LoopHabitRow
import com.loop.app.ui.components.LoopPrimaryButton
import com.loop.app.ui.components.LoopProgressBar
import com.loop.app.ui.components.LoopSectionLabel
import com.loop.app.ui.components.LoopTaskCard
import com.loop.app.ui.components.LoopToggle
import com.loop.app.ui.components.LoopTopAppBar
import com.loop.app.ui.theme.LoopColor
import com.loop.app.ui.theme.LoopRadius
import com.loop.app.ui.theme.LoopTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class SheetRequest(val type: SheetType, val id: String? = null)
private data class DeleteRequest(val kind: String, val id: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoopApp(userId: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val store = remember(userId) { LoopLocalStore(context, userId) }
    val initial = remember(userId) { store.load() }
    var appState by remember { mutableStateOf(initial.getOrDefault(AppState())) }
    var storageError by remember { mutableStateOf(initial.exceptionOrNull()?.message) }
    var routeStack by remember { mutableStateOf(listOf(Screen.Today)) }
    var selectedTaskTab by remember { mutableStateOf(TaskTab.Today) }
    var insightsRange by remember { mutableStateOf(InsightsRange.Week) }
    var calendarDate by remember { mutableStateOf(todayIsoDate()) }
    var activeSheet by remember { mutableStateOf<SheetRequest?>(null) }
    var reviewSheetOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<DeleteRequest?>(null) }

    fun persist(next: AppState) {
        appState = next
        storageError = store.save(next).exceptionOrNull()?.message
    }

    fun updateState(block: (AppState) -> AppState) = persist(block(appState))

    fun navigate(screen: Screen) {
        routeStack = if (screen in rootScreens) listOf(screen) else routeStack + screen
    }

    fun goBack() {
        routeStack = if (routeStack.size > 1) routeStack.dropLast(1) else listOf(Screen.More)
    }

    val currentScreen = routeStack.last()
    val darkTheme = when (appState.settings.theme) {
        ThemeOption.System -> isSystemInDarkTheme()
        ThemeOption.Light -> false
        ThemeOption.Dark -> true
    }
    val isArabic = appState.settings.language == LanguageOption.Arabic
    val t = remember(isArabic) { LoopStrings(isArabic) }

    CompositionLocalProvider(LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        LoopTheme(darkTheme = darkTheme) {
            FullScreenSurface {
                if (!appState.settings.firstRunComplete) {
                    OnboardingScreen(
                        t = t,
                        onFinish = { name ->
                            updateState { state ->
                                state.copy(settings = state.settings.copy(firstRunComplete = true, profileName = name.trim()))
                            }
                        },
                    )
                } else {
                    BackHandler(enabled = routeStack.size > 1, onBack = ::goBack)
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (currentScreen in rootScreens) {
                                LoopBottomNavigation(currentScreen = currentScreen, onNavigate = { navigate(it) })
                            }
                        },
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        ) {
                            when (currentScreen) {
                                Screen.Today -> TodayScreen(
                                    t = t,
                                    state = appState,
                                    onToggleTask = { id -> updateState { it.copy(tasks = it.tasks.toggleTask(id)) } },
                                    onToggleHabit = { id -> updateState { it.copy(habits = it.habits.toggleHabit(id)) } },
                                    onOpenSheet = { type, id -> activeSheet = SheetRequest(type, id) },
                                    onNavigate = ::navigate,
                                )
                                Screen.Tasks -> TasksScreen(
                                    t = t,
                                    tasks = appState.tasks,
                                    selectedTab = selectedTaskTab,
                                    onTabChange = { selectedTaskTab = it },
                                    onToggle = { id -> updateState { it.copy(tasks = it.tasks.toggleTask(id)) } },
                                    onOpenSheet = { type, id -> activeSheet = SheetRequest(type, id) },
                                )
                                Screen.Habits -> HabitsScreen(
                                    t = t,
                                    habits = appState.habits,
                                    onToggle = { id -> updateState { it.copy(habits = it.habits.toggleHabit(id)) } },
                                    onOpenSheet = { type, id -> activeSheet = SheetRequest(type, id) },
                                )
                                Screen.Insights -> InsightsScreen(
                                    t = t,
                                    state = appState,
                                    range = insightsRange,
                                    onRangeChange = { insightsRange = it },
                                )
                                Screen.More -> MoreScreen(t = t, onNavigate = ::navigate)
                                Screen.Goals -> GoalsScreen(t = t, goals = appState.goals, onBack = ::goBack, onOpenSheet = { type, id -> activeSheet = SheetRequest(type, id) })
                                Screen.Journal -> JournalScreen(t = t, journal = appState.journal, onBack = ::goBack, onOpenSheet = { type, id -> activeSheet = SheetRequest(type, id) })
                                Screen.Calendar -> CalendarScreen(t = t, tasks = appState.tasks, selectedDate = calendarDate, onDateChange = { calendarDate = it }, onBack = ::goBack)
                                Screen.Deen -> DeenScreen(t = t, prayers = appState.prayers, onBack = ::goBack) { name ->
                                    updateState { it.copy(prayers = it.prayers.map { prayer -> if (prayer.name == name) prayer.copy(done = !prayer.done) else prayer }) }
                                }
                                Screen.Search -> SearchScreen(t = t, state = appState, onBack = ::goBack)
                                Screen.Reviews -> ReviewsScreen(t = t, state = appState, onBack = ::goBack, onStart = { reviewSheetOpen = true })
                                Screen.Settings -> SettingsScreen(
                                    t = t,
                                    state = appState,
                                    storageError = storageError,
                                    onBack = ::goBack,
                                    onUpdateSettings = { settings -> updateState { it.copy(settings = settings) } },
                                    onResetData = {
                                        val resetState = store.reset().getOrDefault(AppState(settings = SettingsState(firstRunComplete = true)))
                                        appState = resetState
                                        storageError = store.save(resetState).exceptionOrNull()?.message
                                    },
                                    onLogout = onLogout,
                                )
                            }
                        }
                    }
                }

                if (storageError != null) {
                    ErrorBanner(t.storageProblem, storageError.orEmpty()) { storageError = null }
                }

                val sheet = activeSheet
                if (sheet != null) {
                    ModalBottomSheet(
                        onDismissRequest = { activeSheet = null },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        CoreObjectSheet(
                            t = t,
                            sheet = sheet,
                            state = appState,
                            onDismiss = { activeSheet = null },
                            onDelete = { request -> pendingDelete = request },
                            onSaveTask = { task ->
                                updateState { state ->
                                    state.copy(tasks = state.tasks.upsert(task).sortedBy { it.scheduledDate })
                                }
                            },
                            onSaveHabit = { habit -> updateState { it.copy(habits = it.habits.upsert(habit)) } },
                            onSaveGoal = { goal -> updateState { it.copy(goals = it.goals.upsert(goal)) } },
                            onSaveJournal = { entry -> updateState { it.copy(journal = it.journal.upsert(entry).sortedByDescending { item -> item.date }) } },
                        )
                    }
                }

                if (reviewSheetOpen) {
                    ModalBottomSheet(
                        onDismissRequest = { reviewSheetOpen = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        ReviewSheet(
                            t = t,
                            onCancel = { reviewSheetOpen = false },
                            onSave = { review ->
                                updateState { it.copy(reviews = listOf(review) + it.reviews) }
                                reviewSheetOpen = false
                            },
                        )
                    }
                }

                pendingDelete?.let { request ->
                    AlertDialog(
                        onDismissRequest = { pendingDelete = null },
                        title = { Text(t.deleteTitle) },
                        text = { Text(t.deleteBody(request.label)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    updateState { state -> state.delete(request) }
                                    pendingDelete = null
                                    activeSheet = null
                                },
                            ) { Text(t.delete) }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingDelete = null }) { Text(t.cancel) }
                        },
                    )
                }
            }
        }
    }
}

private val rootScreens = setOf(Screen.Today, Screen.Tasks, Screen.Habits, Screen.Insights, Screen.More)

@Composable
private fun OnboardingScreen(t: LoopStrings, onFinish: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            Text(t.welcomeTitle, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Text(t.welcomeBody, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            LoopInput(value = name, onValueChange = { name = it.take(40) }, placeholder = t.nameOptional)
            Spacer(Modifier.height(14.dp))
            LoopPrimaryButton(t.startLoop, onClick = { onFinish(name) })
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { onFinish("") }, modifier = Modifier.fillMaxWidth()) { Text(t.skip) }
        }
    }
}

@Composable
private fun TodayScreen(
    t: LoopStrings,
    state: AppState,
    onToggleTask: (String) -> Unit,
    onToggleHabit: (String) -> Unit,
    onOpenSheet: (SheetType, String?) -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> t.goodNight
        hour < 12 -> t.goodMorning
        hour < 17 -> t.goodAfternoon
        else -> t.goodEvening
    }
    val today = todayIsoDate()
    val todayTasks = state.tasks.filter { it.scheduledDate == today }
    val doneCount = todayTasks.count { it.done }
    val focusTask = todayTasks.firstOrNull { !it.done }
    val habitsDone = state.habits.count { it.done }
    val displayName = state.settings.profileName.ifBlank { t.you }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
    ) {
        item {
            Text(greeting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(displayName, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text(t.date(today), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
        if (focusTask != null) {
            item {
                SurfaceCard {
                    LoopSectionLabel(t.focus)
                    Spacer(Modifier.height(6.dp))
                    Text(focusTask.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    focusTask.details?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        item {
            SectionHeader(title = t.today, count = "$doneCount / ${todayTasks.size}")
            Spacer(Modifier.height(8.dp))
            LoopProgressBar(progress = if (todayTasks.isEmpty()) 0f else doneCount.toFloat() / todayTasks.size)
            Spacer(Modifier.height(12.dp))
        }
        if (todayTasks.isEmpty()) {
            item { EmptyState(t.noTodayTasks, t.addTodayTask, onAction = { onOpenSheet(SheetType.NewTask, null) }) }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                LoopTaskCard(
                    title = task.title,
                    done = task.done,
                    details = task.details,
                    onToggle = { onToggleTask(task.id) },
                    onEdit = { onOpenSheet(SheetType.EditTask, task.id) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                LoopSectionLabel(t.habits)
                TextButton(onClick = { onNavigate(Screen.Habits) }, contentPadding = PaddingValues(0.dp)) {
                    Text("$habitsDone / ${state.habits.size}", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            SurfaceCard {
                if (state.habits.isEmpty()) {
                    EmptyState(t.noHabits, t.addHabit, onAction = { onOpenSheet(SheetType.NewHabit, null) })
                } else {
                    LoopProgressBar(progress = habitsDone.toFloat() / state.habits.size)
                    Spacer(Modifier.height(12.dp))
                    state.habits.take(4).forEach { habit ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LoopToggle(done = habit.done, label = t.toggleHabit(habit.title), onToggle = { onToggleHabit(habit.id) })
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (habit.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                textDecoration = if (habit.done) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            LoopSectionLabel(t.quickAdd)
            Spacer(Modifier.height(10.dp))
            QuickAddRows(t = t, onOpenSheet = onOpenSheet)
        }
    }
}

@Composable
private fun QuickAddRows(t: LoopStrings, onOpenSheet: (SheetType, String?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(
            SheetType.NewTask to t.task,
            SheetType.NewHabit to t.habit,
            SheetType.NewJournal to t.journal,
        ).forEach { (type, label) ->
            Surface(
                shape = RoundedCornerShape(LoopRadius.pill),
                color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f).height(44.dp).clickable { onOpenSheet(type, null) },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+ $label", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun TasksScreen(
    t: LoopStrings,
    tasks: List<Task>,
    selectedTab: TaskTab,
    onTabChange: (TaskTab) -> Unit,
    onToggle: (String) -> Unit,
    onOpenSheet: (SheetType, String?) -> Unit,
) {
    val today = todayIsoDate()
    val filtered = when (selectedTab) {
        TaskTab.Today -> tasks.filter { it.scheduledDate == today }
        TaskTab.Inbox -> tasks.filter { !it.done && it.scheduledDate <= today }
        TaskTab.Upcoming -> tasks.filter { !it.done && it.scheduledDate > today }
    }

    Column(Modifier.fillMaxSize()) {
        TabHeader(
            tabs = listOf(TaskTab.Today to t.today, TaskTab.Inbox to t.inbox, TaskTab.Upcoming to t.upcoming),
            selected = selectedTab,
            onSelect = onTabChange,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (filtered.isEmpty()) {
                item { EmptyState(t.noTasksHere, t.addTask, onAction = { onOpenSheet(SheetType.NewTask, null) }) }
            } else {
                items(filtered, key = { it.id }) { task ->
                    LoopTaskCard(
                        title = task.title,
                        done = task.done,
                        details = listOfNotNull(task.details, t.date(task.scheduledDate)).joinToString(" • "),
                        onToggle = { onToggle(task.id) },
                        onEdit = { onOpenSheet(SheetType.EditTask, task.id) },
                    )
                }
            }
            item { AddRow(t.addTask, onClick = { onOpenSheet(SheetType.NewTask, null) }) }
        }
    }
}

@Composable
private fun HabitsScreen(t: LoopStrings, habits: List<Habit>, onToggle: (String) -> Unit, onOpenSheet: (SheetType, String?) -> Unit) {
    val done = habits.count { it.done }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 16.dp)) {
            SectionHeader(t.today, "$done / ${habits.size}")
            Spacer(Modifier.height(8.dp))
            LoopProgressBar(if (habits.isEmpty()) 0f else done.toFloat() / habits.size)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (habits.isEmpty()) {
                item { EmptyState(t.noHabits, t.addHabit, onAction = { onOpenSheet(SheetType.NewHabit, null) }) }
            } else {
                items(habits, key = { it.id }) { habit ->
                    LoopHabitRow(title = habit.title, done = habit.done, onToggle = { onToggle(habit.id) }, onEdit = { onOpenSheet(SheetType.EditHabit, habit.id) })
                }
            }
            item { AddRow(t.addHabit, onClick = { onOpenSheet(SheetType.NewHabit, null) }) }
        }
    }
}

@Composable
private fun InsightsScreen(t: LoopStrings, state: AppState, range: InsightsRange, onRangeChange: (InsightsRange) -> Unit) {
    val today = todayIsoDate()
    val days = if (range == InsightsRange.Week) 7 else 30
    val dates = (0 until days).map { shiftIsoDate(today, -it) }.toSet()
    val rangeTasks = state.tasks.filter { it.scheduledDate in dates }
    val rangeJournal = state.journal.filter { it.date in dates }
    val doneTasks = rangeTasks.count { it.done }
    val habitPct = if (state.habits.isEmpty()) 0 else ((state.habits.count { it.done }.toFloat() / state.habits.size) * 100).toInt()
    val activeGoals = state.goals.count { it.progress < it.target }

    Column(Modifier.fillMaxSize()) {
        TabHeader(tabs = listOf(InsightsRange.Week to t.week, InsightsRange.Month to t.month), selected = range, onSelect = onRangeChange)
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(t.tasksDone, "$doneTasks", if (range == InsightsRange.Week) t.thisWeek else t.thisMonth, Modifier.weight(1f))
                    MetricCard(t.habitRate, "$habitPct%", t.consistency, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(t.journal, "${rangeJournal.size}", t.entries, Modifier.weight(1f))
                    MetricCard(t.goalsActive, "$activeGoals", t.inProgress, Modifier.weight(1f))
                }
            }
            item { CompletionTrendCard(t = t, tasks = rangeTasks, days = if (range == InsightsRange.Week) 7 else 10) }
            item {
                SurfaceCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Column {
                            Text(t.dataOnDevice, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(2.dp))
                            Text(t.dataOnDeviceBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreScreen(t: LoopStrings, onNavigate: (Screen) -> Unit) {
    data class MoreItem(val screen: Screen, val label: String, val icon: ImageVector, val desc: String)
    val items = listOf(
        MoreItem(Screen.Goals, t.goals, Icons.Rounded.Flag, t.goalsDesc),
        MoreItem(Screen.Journal, t.journal, Icons.Rounded.Book, t.journalDesc),
        MoreItem(Screen.Calendar, t.calendar, Icons.Rounded.CalendarToday, t.calendarDesc),
        MoreItem(Screen.Deen, t.deen, Icons.AutoMirrored.Rounded.MenuBook, t.deenDesc),
        MoreItem(Screen.Search, t.search, Icons.Rounded.Search, t.searchDesc),
        MoreItem(Screen.Reviews, t.reviews, Icons.Rounded.RateReview, t.reviewsDesc),
        MoreItem(Screen.Settings, t.settings, Icons.Rounded.Settings, t.settingsDesc),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(t.more, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(18.dp))
        }
        items(items) { item -> MoreRow(label = item.label, desc = item.desc, icon = item.icon, onClick = { onNavigate(item.screen) }) }
    }
}

@Composable
private fun GoalsScreen(t: LoopStrings, goals: List<Goal>, onBack: () -> Unit, onOpenSheet: (SheetType, String?) -> Unit) {
    SecondaryScaffold(t.goals, onBack) {
        item { AddRow(t.addGoal, onClick = { onOpenSheet(SheetType.NewGoal, null) }) }
        if (goals.isEmpty()) {
            item { EmptyState(t.noGoals, t.addGoal, onAction = { onOpenSheet(SheetType.NewGoal, null) }) }
        } else {
            items(goals, key = { it.id }) { goal ->
                SurfaceCard(Modifier.clickable { onOpenSheet(SheetType.EditGoal, goal.id) }) {
                    Text(goal.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(10.dp))
                    LoopProgressBar(progress = goal.progress.toFloat() / goal.target.coerceAtLeast(1), color = LoopColor.Blue)
                    Spacer(Modifier.height(8.dp))
                    Text("${goal.progress} / ${goal.target} ${goal.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun JournalScreen(t: LoopStrings, journal: List<JournalEntry>, onBack: () -> Unit, onOpenSheet: (SheetType, String?) -> Unit) {
    SecondaryScaffold(t.journal, onBack) {
        item { AddRow(t.newJournalEntry, onClick = { onOpenSheet(SheetType.NewJournal, null) }) }
        if (journal.isEmpty()) {
            item { EmptyState(t.noJournal, t.newJournalEntry, onAction = { onOpenSheet(SheetType.NewJournal, null) }) }
        } else {
            items(journal, key = { it.id }) { entry ->
                SurfaceCard(Modifier.clickable { onOpenSheet(SheetType.EditJournal, entry.id) }) {
                    Text(t.date(entry.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(entry.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun CalendarScreen(t: LoopStrings, tasks: List<Task>, selectedDate: String, onDateChange: (String) -> Unit, onBack: () -> Unit) {
    val dayTasks = tasks.filter { it.scheduledDate == selectedDate }
    SecondaryScaffold(t.calendar, onBack) {
        item {
            SurfaceCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onDateChange(shiftIsoDate(selectedDate, -1)) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = t.previousDay)
                    }
                    Text(t.date(selectedDate), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = { onDateChange(shiftIsoDate(selectedDate, 1)) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = t.nextDay)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (dayTasks.isEmpty()) {
                    EmptyState(t.noCalendarItems, null)
                } else {
                    dayTasks.forEach { task ->
                        Text("• ${task.title}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeenScreen(t: LoopStrings, prayers: List<Prayer>, onBack: () -> Unit, onToggle: (String) -> Unit) {
    SecondaryScaffold(t.deen, onBack) {
        item {
            SurfaceCard {
                LoopSectionLabel(t.todayPrayers)
                Spacer(Modifier.height(12.dp))
                prayers.forEach { prayer ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LoopToggle(done = prayer.done, label = t.togglePrayer(prayer.name), onToggle = { onToggle(prayer.name) })
                        Column(Modifier.weight(1f)) {
                            Text("${prayer.name}  ${prayer.arabicName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
                            Text(prayer.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(t: LoopStrings, state: AppState, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val normalized = query.trim()
    val taskMatches = state.tasks.filter { normalized.isNotBlank() && (it.title.contains(normalized, true) || it.details.orEmpty().contains(normalized, true) || it.scheduledDate.contains(normalized, true)) }
    val habitMatches = state.habits.filter { normalized.isNotBlank() && it.title.contains(normalized, true) }
    val journalMatches = state.journal.filter { normalized.isNotBlank() && (it.content.contains(normalized, true) || it.date.contains(normalized, true)) }
    val goalMatches = state.goals.filter { normalized.isNotBlank() && (it.title.contains(normalized, true) || it.unit.contains(normalized, true)) }
    val reviewMatches = state.reviews.filter { normalized.isNotBlank() && (it.wins.contains(normalized, true) || it.challenges.contains(normalized, true) || it.nextFocus.contains(normalized, true)) }
    val total = taskMatches.size + habitMatches.size + journalMatches.size + goalMatches.size + reviewMatches.size

    Column(Modifier.fillMaxSize()) {
        LoopTopAppBar(t.search, onBack)
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                LoopInput(value = query, onValueChange = { query = it.take(80) }, placeholder = t.searchPlaceholder)
                Spacer(Modifier.height(12.dp))
                if (normalized.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(t.results(total), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { query = "" }) { Text(t.clear) }
                    }
                }
            }
            if (normalized.isBlank()) {
                item { EmptyState(t.typeToSearch, null) }
            } else if (total == 0) {
                item { EmptyState(t.noSearchResults, t.clear, onAction = { query = "" }) }
            } else {
                searchGroup(t.tasks, taskMatches.map { "${it.title} • ${t.date(it.scheduledDate)}" })
                searchGroup(t.habits, habitMatches.map { it.title })
                searchGroup(t.journal, journalMatches.map { "${t.date(it.date)} • ${it.content}" })
                searchGroup(t.goals, goalMatches.map { it.title })
                searchGroup(t.reviews, reviewMatches.map { "${t.date(it.date)} • ${it.nextFocus}" })
            }
        }
    }
}

private fun LazyListScope.searchGroup(title: String, rows: List<String>) {
    if (rows.isEmpty()) return
    item { LoopSectionLabel(title) }
    items(rows) { row ->
        SurfaceCard { Text(row, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 3, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ReviewsScreen(t: LoopStrings, state: AppState, onBack: () -> Unit, onStart: () -> Unit) {
    val latest = state.reviews.firstOrNull()
    SecondaryScaffold(t.reviews, onBack) {
        item {
            SurfaceCard {
                LoopSectionLabel(t.weeklyReview)
                Spacer(Modifier.height(10.dp))
                Text(t.reviewSummary(state.tasks.count { it.done }, state.habits.count { it.done }, state.journal.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                latest?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(t.lastReview(t.date(it.date)), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    Text(it.nextFocus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(14.dp))
                LoopPrimaryButton(t.startReview, onClick = onStart)
            }
        }
        if (state.reviews.isNotEmpty()) {
            item { LoopSectionLabel(t.savedReviews) }
            items(state.reviews, key = { it.id }) { review ->
                SurfaceCard {
                    Text(t.date(review.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(review.wins, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(review.nextFocus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    t: LoopStrings,
    state: AppState,
    storageError: String?,
    onBack: () -> Unit,
    onUpdateSettings: (SettingsState) -> Unit,
    onResetData: () -> Unit,
    onLogout: () -> Unit,
) {
    SecondaryScaffold(t.settings, onBack) {
        item {
            SurfaceCard {
                Text(t.profile, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(10.dp))
                SettingTextField(t.nameOptional, state.settings.profileName) {
                    onUpdateSettings(state.settings.copy(profileName = it.take(40)))
                }
            }
        }
        item {
            SurfaceCard {
                Text(t.theme, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(10.dp))
                SegmentedOptions(
                    options = listOf(ThemeOption.System to t.system, ThemeOption.Light to t.light, ThemeOption.Dark to t.dark),
                    selected = state.settings.theme,
                    onSelect = { onUpdateSettings(state.settings.copy(theme = it)) },
                )
            }
        }
        item {
            SurfaceCard {
                Text(t.language, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(10.dp))
                SegmentedOptions(
                    options = listOf(LanguageOption.English to "English", LanguageOption.Arabic to "العربية"),
                    selected = state.settings.language,
                    onSelect = { onUpdateSettings(state.settings.copy(language = it)) },
                )
            }
        }
        item {
            SurfaceCard {
                Text(t.dataManagement, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(t.exportSummary(state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                storageError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("${t.storageProblem}: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onResetData, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(t.resetData)
                }
            }
        }
        item {
            SurfaceCard {
                Text(t.appInfo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text("Loop 0.2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SurfaceCard {
                Text(t.account, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(t.accountBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(t.logout)
                }
            }
        }
    }
}

@Composable
private fun CoreObjectSheet(
    t: LoopStrings,
    sheet: SheetRequest,
    state: AppState,
    onDismiss: () -> Unit,
    onDelete: (DeleteRequest) -> Unit,
    onSaveTask: (Task) -> Unit,
    onSaveHabit: (Habit) -> Unit,
    onSaveGoal: (Goal) -> Unit,
    onSaveJournal: (JournalEntry) -> Unit,
) {
    val task = state.tasks.firstOrNull { it.id == sheet.id }
    val habit = state.habits.firstOrNull { it.id == sheet.id }
    val goal = state.goals.firstOrNull { it.id == sheet.id }
    val entry = state.journal.firstOrNull { it.id == sheet.id }
    val isEdit = sheet.id != null
    var title by remember(sheet) { mutableStateOf(task?.title ?: habit?.title ?: goal?.title.orEmpty()) }
    var details by remember(sheet) { mutableStateOf(task?.details.orEmpty()) }
    var date by remember(sheet) { mutableStateOf(task?.scheduledDate ?: entry?.date ?: todayIsoDate()) }
    var content by remember(sheet) { mutableStateOf(entry?.content.orEmpty()) }
    var progress by remember(sheet) { mutableStateOf(goal?.progress?.toString().orEmpty()) }
    var target by remember(sheet) { mutableStateOf(goal?.target?.toString().orEmpty()) }
    var unit by remember(sheet) { mutableStateOf(goal?.unit.orEmpty()) }
    var error by remember(sheet) { mutableStateOf<String?>(null) }
    var confirmDiscard by remember(sheet) { mutableStateOf(false) }

    fun closeWithProtection() {
        val dirty = title.isNotBlank() || details.isNotBlank() || content.isNotBlank() || progress.isNotBlank() || target.isNotBlank() || unit.isNotBlank()
        if (!isEdit && dirty) confirmDiscard = true else onDismiss()
    }

    val heading = when (sheet.type) {
        SheetType.NewTask -> t.newTask
        SheetType.EditTask -> t.editTask
        SheetType.NewHabit -> t.newHabit
        SheetType.EditHabit -> t.editHabit
        SheetType.NewJournal -> t.newJournalEntry
        SheetType.EditJournal -> t.editJournal
        SheetType.NewGoal -> t.newGoal
        SheetType.EditGoal -> t.editGoal
    }

    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(heading, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            IconButton(onClick = ::closeWithProtection, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = t.close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        when (sheet.type) {
            SheetType.NewTask, SheetType.EditTask -> {
                LoopInput(title, { title = it.take(80) }, t.title)
                Spacer(Modifier.height(10.dp))
                LoopInput(details, { details = it.take(200) }, t.detailsOptional)
                Spacer(Modifier.height(10.dp))
                LoopInput(date, { date = it.take(10) }, t.datePlaceholder)
            }
            SheetType.NewHabit, SheetType.EditHabit -> LoopInput(title, { title = it.take(80) }, t.title)
            SheetType.NewJournal, SheetType.EditJournal -> {
                LoopInput(date, { date = it.take(10) }, t.datePlaceholder)
                Spacer(Modifier.height(10.dp))
                LoopInput(content, { content = it.take(1000) }, t.journalPrompt, singleLine = false, minLines = 4)
            }
            SheetType.NewGoal, SheetType.EditGoal -> {
                LoopInput(title, { title = it.take(80) }, t.title)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoopInput(progress, { progress = it.filter(Char::isDigit).take(8) }, t.progress, modifier = Modifier.weight(1f))
                    LoopInput(target, { target = it.filter(Char::isDigit).take(8) }, t.target, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                LoopInput(unit, { unit = it.take(20) }, t.unit)
            }
        }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(18.dp))
        LoopPrimaryButton(
            label = t.save,
            enabled = true,
            onClick = {
                val trimmedTitle = title.trim()
                val duplicateTask = state.tasks.any { it.id != sheet.id && it.title.equals(trimmedTitle, true) }
                val duplicateHabit = state.habits.any { it.id != sheet.id && it.title.equals(trimmedTitle, true) }
                when (sheet.type) {
                    SheetType.NewTask, SheetType.EditTask -> when {
                        trimmedTitle.isBlank() -> error = t.titleRequired
                        duplicateTask -> error = t.duplicateTitle
                        date.length != 10 -> error = t.dateInvalid
                        else -> {
                            onSaveTask(Task(sheet.id ?: newId("t"), trimmedTitle, task?.done ?: false, date == todayIsoDate(), details.trim().ifBlank { null }, date))
                            onDismiss()
                        }
                    }
                    SheetType.NewHabit, SheetType.EditHabit -> when {
                        trimmedTitle.isBlank() -> error = t.titleRequired
                        duplicateHabit -> error = t.duplicateTitle
                        else -> {
                            onSaveHabit(Habit(sheet.id ?: newId("h"), trimmedTitle, habit?.done ?: false))
                            onDismiss()
                        }
                    }
                    SheetType.NewJournal, SheetType.EditJournal -> when {
                        content.trim().isBlank() -> error = t.contentRequired
                        date.length != 10 -> error = t.dateInvalid
                        else -> {
                            onSaveJournal(JournalEntry(sheet.id ?: newId("j"), date, content.trim()))
                            onDismiss()
                        }
                    }
                    SheetType.NewGoal, SheetType.EditGoal -> when {
                        trimmedTitle.isBlank() -> error = t.titleRequired
                        target.toIntOrNull() == null || target.toInt() <= 0 -> error = t.targetRequired
                        else -> {
                            onSaveGoal(Goal(sheet.id ?: newId("g"), trimmedTitle, progress.toIntOrNull() ?: 0, target.toInt(), unit.trim().ifBlank { t.units }))
                            onDismiss()
                        }
                    }
                }
            },
        )
        if (isEdit) {
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    val kind = when (sheet.type) {
                        SheetType.EditTask -> "task"
                        SheetType.EditHabit -> "habit"
                        SheetType.EditJournal -> "journal"
                        SheetType.EditGoal -> "goal"
                        else -> ""
                    }
                    if (kind.isNotBlank()) onDelete(DeleteRequest(kind, sheet.id.orEmpty(), title.ifBlank { content.take(30) }))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(t.delete)
            }
        }
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = ::closeWithProtection, modifier = Modifier.fillMaxWidth()) { Text(t.cancel) }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(t.discardChanges) },
            text = { Text(t.discardBody) },
            confirmButton = { TextButton(onClick = onDismiss) { Text(t.discard) } },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text(t.keepEditing) } },
        )
    }
}

@Composable
private fun ReviewSheet(t: LoopStrings, onCancel: () -> Unit, onSave: (Review) -> Unit) {
    var wins by remember { mutableStateOf("") }
    var challenges by remember { mutableStateOf("") }
    var nextFocus by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
        Text(t.weeklyReview, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        LoopInput(wins, { wins = it.take(500) }, t.reviewWins, singleLine = false, minLines = 3)
        Spacer(Modifier.height(10.dp))
        LoopInput(challenges, { challenges = it.take(500) }, t.reviewChallenges, singleLine = false, minLines = 3)
        Spacer(Modifier.height(10.dp))
        LoopInput(nextFocus, { nextFocus = it.take(300) }, t.reviewNext, singleLine = false, minLines = 2)
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(18.dp))
        LoopPrimaryButton(t.saveReview, onClick = {
            if (wins.isBlank() || nextFocus.isBlank()) {
                error = t.reviewRequired
            } else {
                onSave(Review(newId("r"), todayIsoDate(), wins.trim(), challenges.trim(), nextFocus.trim()))
            }
        })
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text(t.cancel) }
    }
}

@Composable
private fun LoopInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(LoopRadius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun <T> SegmentedOptions(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (value, label) ->
            val active = selected == value
            Button(
                onClick = { onSelect(value) },
                shape = RoundedCornerShape(LoopRadius.pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).height(44.dp),
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), maxLines = 1)
            }
        }
    }
}

@Composable
private fun SettingTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    LoopInput(value = value, onValueChange = onValueChange, placeholder = label)
}

@Composable
private fun SectionHeader(title: String, count: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        LoopSectionLabel(title)
        Text(count, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun <T> TabHeader(tabs: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEach { (value, label) ->
                val active = selected == value
                Button(
                    onClick = { onSelect(value) },
                    shape = RoundedCornerShape(LoopRadius.pill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp),
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 0.sp, fontWeight = FontWeight.SemiBold))
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun MetricCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(LoopRadius.xxl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(2.dp))
            Text(sub, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CompletionTrendCard(t: LoopStrings, tasks: List<Task>, days: Int) {
    SurfaceCard {
        LoopSectionLabel(t.completionTrend)
        Spacer(Modifier.height(14.dp))
        val today = todayIsoDate()
        (days - 1 downTo 0).forEach { offset ->
            val date = shiftIsoDate(today, -offset)
            val dayTasks = tasks.filter { it.scheduledDate == date }
            val pct = if (dayTasks.isEmpty()) 0f else dayTasks.count { it.done }.toFloat() / dayTasks.size
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(shortDate(date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(width = 42.dp, height = 18.dp))
                LoopProgressBar(progress = pct, modifier = Modifier.weight(1f))
                Text("${(pct * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MoreRow(label: String, desc: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(LoopRadius.xl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).semantics {
            role = Role.Button
            contentDescription = "$label, $desc"
        },
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(LoopRadius.xxl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SecondaryScaffold(title: String, onBack: () -> Unit, content: LazyListScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        LoopTopAppBar(title = title, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun EmptyState(text: String, actionLabel: String?, onAction: (() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun ErrorBanner(title: String, body: String, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
        Surface(shape = RoundedCornerShape(LoopRadius.lg), color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                    Text(body, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                }
                    TextButton(onClick = onDismiss) { Text("OK", color = Color.White) }
            }
        }
    }
}

private fun List<Task>.toggleTask(id: String) = map { if (it.id == id) it.copy(done = !it.done) else it }
private fun List<Habit>.toggleHabit(id: String) = map { if (it.id == id) it.copy(done = !it.done) else it }
private fun List<Task>.upsert(task: Task) = filterNot { it.id == task.id } + task
private fun List<Habit>.upsert(habit: Habit) = filterNot { it.id == habit.id } + habit
private fun List<Goal>.upsert(goal: Goal) = filterNot { it.id == goal.id } + goal
private fun List<JournalEntry>.upsert(entry: JournalEntry) = filterNot { it.id == entry.id } + entry

private fun AppState.delete(request: DeleteRequest): AppState = when (request.kind) {
    "task" -> copy(tasks = tasks.filterNot { it.id == request.id })
    "habit" -> copy(habits = habits.filterNot { it.id == request.id })
    "journal" -> copy(journal = journal.filterNot { it.id == request.id })
    "goal" -> copy(goals = goals.filterNot { it.id == request.id })
    else -> this
}

private fun shortDate(isoDate: String): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate) ?: return@runCatching isoDate
    SimpleDateFormat("d MMM", Locale.US).format(parsed)
}.getOrElse { isoDate }

private class LoopStrings(private val ar: Boolean) {
    private fun s(en: String, arabic: String) = if (ar) arabic else en
    val welcomeTitle = s("Set up your Loop", "ابدأ إعداد Loop")
    val welcomeBody = s("Start with an empty, private workspace. Add only your own tasks, habits, goals, and journal entries.", "ابدأ بمساحة خاصة وفارغة. أضف مهامك وعاداتك وأهدافك ومذكراتك فقط.")
    val nameOptional = s("Name optional", "الاسم اختياري")
    val startLoop = s("Start Loop", "ابدأ Loop")
    val skip = s("Skip", "تخطي")
    val you = s("You", "أنت")
    val goodNight = s("Good night", "تصبح على خير")
    val goodMorning = s("Good morning", "صباح الخير")
    val goodAfternoon = s("Good afternoon", "مساء الخير")
    val goodEvening = s("Good evening", "مساء الخير")
    val focus = s("Focus", "التركيز")
    val today = s("Today", "اليوم")
    val habits = s("Habits", "العادات")
    val quickAdd = s("Quick add", "إضافة سريعة")
    val tasks = s("Tasks", "المهام")
    val task = s("Task", "مهمة")
    val habit = s("Habit", "عادة")
    val journal = s("Journal", "المذكرات")
    val noTodayTasks = s("No tasks scheduled for today. Add one when you are ready.", "لا توجد مهام مجدولة اليوم. أضف مهمة عندما تكون جاهزا.")
    val addTodayTask = s("Add today task", "إضافة مهمة اليوم")
    val noHabits = s("No habits yet. Add a small daily habit to track.", "لا توجد عادات بعد. أضف عادة يومية صغيرة لتتبعها.")
    val addHabit = s("Add habit", "إضافة عادة")
    val inbox = s("Inbox", "الوارد")
    val upcoming = s("Upcoming", "القادم")
    val noTasksHere = s("No tasks here. Create or schedule a task to fill this view.", "لا توجد مهام هنا. أنشئ أو جدوب مهمة لملء هذه الصفحة.")
    val addTask = s("Add task", "إضافة مهمة")
    val week = s("Week", "الأسبوع")
    val month = s("Month", "الشهر")
    val tasksDone = s("Tasks done", "المهام المكتملة")
    val habitRate = s("Habit rate", "معدل العادات")
    val goalsActive = s("Goals active", "الأهداف النشطة")
    val thisWeek = s("this week", "هذا الأسبوع")
    val thisMonth = s("this month", "هذا الشهر")
    val consistency = s("consistency", "الانتظام")
    val entries = s("entries", "إدخالات")
    val inProgress = s("in progress", "قيد التنفيذ")
    val completionTrend = s("Completion trend", "اتجاه الإنجاز")
    val dataOnDevice = s("Your data stays on device", "بياناتك تبقى على الجهاز")
    val dataOnDeviceBody = s("Loop stores your tasks, habits, journal, goals, reviews, and settings locally on this device.", "يحفظ Loop مهامك وعاداتك ومذكراتك وأهدافك ومراجعاتك وإعداداتك محليا على هذا الجهاز.")
    val more = s("More", "المزيد")
    val goals = s("Goals", "الأهداف")
    val goalsDesc = s("Long-term intentions", "نوايا طويلة المدى")
    val journalDesc = s("Daily reflections", "تأملات يومية")
    val calendar = s("Calendar", "التقويم")
    val calendarDesc = s("Agenda view", "عرض الجدول")
    val deen = s("Deen", "الدين")
    val deenDesc = s("Prayer and reflection", "الصلاة والتأمل")
    val search = s("Search", "البحث")
    val searchDesc = s("Find anything", "ابحث في كل شيء")
    val reviews = s("Reviews", "المراجعات")
    val reviewsDesc = s("Weekly review", "مراجعة أسبوعية")
    val settings = s("Settings", "الإعدادات")
    val settingsDesc = s("Theme, language, and data", "المظهر واللغة والبيانات")
    val addGoal = s("Add goal", "إضافة هدف")
    val noGoals = s("No goals yet. Add a measurable goal to track progress.", "لا توجد أهداف بعد. أضف هدفا قابلا للقياس لتتبع التقدم.")
    val newJournalEntry = s("New journal entry", "إدخال مذكرة جديد")
    val noJournal = s("No journal entries yet. Capture a short reflection.", "لا توجد مذكرات بعد. سجّل تأملا قصيرا.")
    val previousDay = s("Previous day", "اليوم السابق")
    val nextDay = s("Next day", "اليوم التالي")
    val noCalendarItems = s("No scheduled tasks for this day.", "لا توجد مهام مجدولة لهذا اليوم.")
    val todayPrayers = s("Today prayers", "صلوات اليوم")
    val searchPlaceholder = s("Search tasks, details, habits, goals, journal, reviews", "ابحث في المهام والتفاصيل والعادات والأهداف والمذكرات والمراجعات")
    val clear = s("Clear", "مسح")
    val typeToSearch = s("Type to search your Loop data.", "اكتب للبحث في بيانات Loop.")
    val noSearchResults = s("No matches. Try a task detail, date, goal, or review word.", "لا توجد نتائج. جرّب تفصيلا أو تاريخا أو هدفا أو كلمة من مراجعة.")
    val weeklyReview = s("Weekly review", "المراجعة الأسبوعية")
    val startReview = s("Start review", "بدء المراجعة")
    val savedReviews = s("Saved reviews", "المراجعات المحفوظة")
    val profile = s("Profile", "الملف الشخصي")
    val theme = s("Theme", "المظهر")
    val system = s("System", "النظام")
    val light = s("Light", "فاتح")
    val dark = s("Dark", "داكن")
    val language = s("Language", "اللغة")
    val dataManagement = s("Data management", "إدارة البيانات")
    val resetData = s("Reset local data", "إعادة ضبط البيانات المحلية")
    val account = s("Account", "الحساب")
    val accountBody = s("Sign out to return to the login screen. Your local Loop data stays separated by Firebase account on this device.", "سجل الخروج للعودة إلى شاشة الدخول. بيانات Loop المحلية تبقى منفصلة حسب حساب Firebase على هذا الجهاز.")
    val logout = s("Log out", "تسجيل الخروج")
    val appInfo = s("App info", "معلومات التطبيق")
    val storageProblem = s("Storage problem", "مشكلة في التخزين")
    val newTask = s("New task", "مهمة جديدة")
    val editTask = s("Edit task", "تعديل المهمة")
    val newHabit = s("New habit", "عادة جديدة")
    val editHabit = s("Edit habit", "تعديل العادة")
    val editJournal = s("Edit journal entry", "تعديل إدخال المذكرة")
    val newGoal = s("New goal", "هدف جديد")
    val editGoal = s("Edit goal", "تعديل الهدف")
    val title = s("Title", "العنوان")
    val detailsOptional = s("Details optional", "التفاصيل اختيارية")
    val datePlaceholder = s("Date YYYY-MM-DD", "التاريخ YYYY-MM-DD")
    val journalPrompt = s("What happened today?", "ماذا حدث اليوم؟")
    val progress = s("Progress", "التقدم")
    val target = s("Target", "الهدف")
    val unit = s("Unit", "الوحدة")
    val units = s("units", "وحدات")
    val close = s("Close", "إغلاق")
    val save = s("Save", "حفظ")
    val cancel = s("Cancel", "إلغاء")
    val delete = s("Delete", "حذف")
    val deleteTitle = s("Delete item?", "حذف العنصر؟")
    val discardChanges = s("Discard changes?", "تجاهل التغييرات؟")
    val discardBody = s("You have unsaved changes.", "لديك تغييرات غير محفوظة.")
    val discard = s("Discard", "تجاهل")
    val keepEditing = s("Keep editing", "متابعة التعديل")
    val titleRequired = s("Add a title before saving.", "أضف عنوانا قبل الحفظ.")
    val contentRequired = s("Add content before saving.", "أضف المحتوى قبل الحفظ.")
    val duplicateTitle = s("An item with this title already exists.", "يوجد عنصر بهذا العنوان بالفعل.")
    val dateInvalid = s("Use date format YYYY-MM-DD.", "استخدم صيغة التاريخ YYYY-MM-DD.")
    val targetRequired = s("Target must be greater than zero.", "يجب أن يكون الهدف أكبر من صفر.")
    val reviewWins = s("What went well?", "ما الذي سار بشكل جيد؟")
    val reviewChallenges = s("What was difficult?", "ما الذي كان صعبا؟")
    val reviewNext = s("Main focus for next week", "التركيز الأساسي للأسبوع القادم")
    val saveReview = s("Save review", "حفظ المراجعة")
    val reviewRequired = s("Add wins and a next focus before saving.", "أضف النجاحات والتركيز التالي قبل الحفظ.")
    fun deleteBody(label: String) = s("This permanently removes \"$label\" from this device.", "سيتم حذف \"$label\" نهائيا من هذا الجهاز.")
    fun results(count: Int) = s("$count results", "$count نتائج")
    fun toggleHabit(title: String) = s("Toggle habit $title", "تبديل عادة $title")
    fun togglePrayer(name: String) = s("Toggle prayer $name", "تبديل صلاة $name")
    fun date(isoDate: String) = displayDate(isoDate, if (ar) Locale.forLanguageTag("ar") else Locale.US)
    fun reviewSummary(tasks: Int, habits: Int, journal: Int) = s("You completed $tasks tasks, checked $habits habits, and wrote $journal journal entries from your stored data.", "أكملت $tasks مهام، وسجلت $habits عادات، وكتبت $journal إدخالات مذكرة من بياناتك المحفوظة.")
    fun lastReview(date: String) = s("Last review: $date", "آخر مراجعة: $date")
    fun exportSummary(state: AppState) = s(
        "Local export summary: ${state.tasks.size} tasks, ${state.habits.size} habits, ${state.goals.size} goals, ${state.journal.size} journal entries, ${state.reviews.size} reviews.",
        "ملخص التصدير المحلي: ${state.tasks.size} مهام، ${state.habits.size} عادات، ${state.goals.size} أهداف، ${state.journal.size} مذكرات، ${state.reviews.size} مراجعات.",
    )
}
