package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.entity.Task
import et.ethioflow.data.entity.TaskStatus
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.*
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    repo: Repository,
    onOpenCalendar: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenHabits: () -> Unit,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val today = remember { EthiopianDate.now() }
    val tasks by repo.observeActiveTasks().collectAsState(initial = emptyList())
    val todayTasks = tasks.filter { it.dueEthDate == today.toString() || it.dueEthDate == null }
    val habits by repo.observeHabits().collectAsState(initial = emptyList())
    val logs by repo.observeHabitLogsForDate(today.toString()).collectAsState(initial = emptyList())
    val doneHabitIds = remember(logs) { logs.map { it.habitId }.toSet() }
    val scope = rememberCoroutineScope()

    val greeting = remember {
        val h = java.time.LocalTime.now().hour
        when {
            h < 12 -> "Good Morning"
            h < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
        // Dark header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Menu, null, tint = TextOnDark)
                Icon(Icons.Default.Person, null, tint = TextOnDark)
            }
            Spacer(Modifier.height(12.dp))
            Text(greeting, color = TextOnDarkSecondary, fontSize = 14.sp)
            Text(
                today.dayOfWeekName() + ", " + today.monthName() + " " + today.day,
                color = TextOnDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ethiopian ${today.year}",
                color = TextOnDarkSecondary,
                fontSize = 13.sp
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-12).dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(SurfaceLight)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            // Quick stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Tasks left",
                        value = todayTasks.count { it.status != TaskStatus.DONE }.toString(),
                        color = AccentBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Habits done",
                        value = "${doneHabitIds.size}/${habits.size}",
                        color = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // Habits due today
            if (habits.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Habits",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onOpenHabits) {
                            Text("See all", color = AccentBlue)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                items(habits.take(5), key = { "h-${it.id}" }) { habit ->
                    HabitCard(
                        habit = habit,
                        isDoneToday = habit.id in doneHabitIds,
                        onToggle = {
                            scope.launch {
                                repo.toggleHabitForDate(habit.id, today)
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(12.dp)) }
            }

            // Today's tasks
            item {
                Text(
                    "Today's Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
            }

            if (todayTasks.isEmpty()) {
                item {
                    WhiteCard {
                        Text(
                            "No tasks for today.\nTap + to add one.",
                            color = TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    WhiteCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        TaskRow(
                            task = task,
                            onToggle = {
                                scope.launch {
                                    if (task.status == TaskStatus.DONE) {
                                        repo.updateTask(task.copy(status = TaskStatus.TODO, completedAt = null))
                                    } else {
                                        repo.completeTask(task)
                                    }
                                }
                            },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }

            // Upcoming
            val upcoming = tasks.filter {
                it.dueEthDate != null && it.dueEthDate != today.toString() && it.status != TaskStatus.DONE
            }.take(5)
            if (upcoming.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Upcoming", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                }
                items(upcoming, key = { "up-${it.id}" }) { task ->
                    WhiteCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        TaskRow(task, onToggle = {}, onClick = { onTaskClick(task) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = TextSecondary, fontSize = 13.sp)
            Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}
