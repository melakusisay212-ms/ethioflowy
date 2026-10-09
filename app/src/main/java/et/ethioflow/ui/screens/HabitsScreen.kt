package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import et.ethioflow.data.entity.Habit
import et.ethioflow.data.entity.HabitFrequency
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.components.WhiteCard
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HabitsScreen(
    repo: Repository,
    onBack: () -> Unit
) {
    val habits by repo.observeHabits().collectAsState(initial = emptyList())
    val today = remember { EthiopianDate.now() }
    val logs by repo.observeHabitLogsForDate(today.toString()).collectAsState(initial = emptyList())
    val doneIds = remember(logs) { logs.map { it.habitId }.toSet() }
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = "Habits",
            subtitle = "Build consistency · ${today.monthName()} ${today.day}",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, null, tint = TextOnDark)
                }
            }
        )

        // Summary strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                value = habits.size.toString(),
                label = "Active"
            )
            SummaryItem(
                value = doneIds.size.toString(),
                label = "Done today"
            )
            SummaryItem(
                value = (habits.maxOfOrNull { it.streak } ?: 0).toString(),
                label = "Best streak"
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(SurfaceLight)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (habits.isEmpty()) {
                item {
                    WhiteCard {
                        Text(
                            "No habits yet.\nTap + to start building one.",
                            color = TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }

            items(habits, key = { it.id }) { habit ->
                val done = habit.id in doneIds
                HabitCard(
                    habit = habit,
                    isDoneToday = done,
                    onToggle = {
                        scope.launch {
                            repo.toggleHabitForDate(habit.id, today)
                        }
                    }
                )
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New Habit") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Habit name") },
                        singleLine = true,
                        placeholder = { Text("e.g. Morning prayer, Read 10 pages") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tracked on the Ethiopian calendar. Streak resets if you miss a day.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        scope.launch {
                            repo.addHabit(newTitle.trim())
                            newTitle = ""
                            showAdd = false
                        }
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextOnDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextOnDarkSecondary, fontSize = 12.sp)
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    isDoneToday: Boolean,
    onToggle: () -> Unit
) {
    WhiteCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color dot
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(habit.colorHex)).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(habit.colorHex)))
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (habit.streak > 0) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            null,
                            tint = AccentOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "${habit.streak} day streak",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (isDoneToday) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (isDoneToday) "Undo" else "Complete",
                    tint = if (isDoneToday) AccentGreen else TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
