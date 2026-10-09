package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.entity.Task
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.*
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    repo: Repository,
    onBack: () -> Unit,
    onAddTaskForDate: (EthiopianDate) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    var currentMonth by remember {
        val n = EthiopianDate.now()
        mutableStateOf(n.year to n.month)
    }
    var selectedDate by remember { mutableStateOf(EthiopianDate.now()) }
    val tasks by repo.observeTasksOn(selectedDate).collectAsState(initial = emptyList())
    val allTasks by repo.observeTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val (year, month) = currentMonth
    val daysInMonth = EthiopianDate.daysInMonth(year, month)
    val firstDay = EthiopianDate(year, month, 1)
    val startOffset = firstDay.toGregorian().dayOfWeek.value % 7   // 0=Sun

    Column(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = firstDay.monthName() + " " + year,
            subtitle = "Ethiopian Calendar",
            onBack = onBack,
            actions = {
                IconButton(onClick = {
                    val prev = if (month == 1) (year - 1) to 13 else year to (month - 1)
                    currentMonth = prev
                }) {
                    Icon(Icons.Default.ChevronLeft, null, tint = TextOnDark)
                }
                IconButton(onClick = {
                    val next = if (month == 13) (year + 1) to 1 else year to (month + 1)
                    currentMonth = next
                }) {
                    Icon(Icons.Default.ChevronRight, null, tint = TextOnDark)
                }
            }
        )

        // Calendar grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(NavyDark)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Weekday headers
            Row(Modifier.fillMaxWidth()) {
                listOf("እ", "ሰ", "ማ", "ረ", "ሐ", "አ", "ቅ").forEach { d ->
                    Text(
                        d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = TextOnDarkSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cell = row * 7 + col
                        val dayNum = cell - startOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                val date = EthiopianDate(year, month, dayNum)
                                val isSelected = date == selectedDate
                                val isToday = date == EthiopianDate.now()
                                val hasTasks = allTasks.any { it.dueEthDate == date.toString() }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> AccentBlue
                                                isToday -> NavySoft
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        dayNum.toString(),
                                        color = if (isSelected) TextOnDark else TextOnDark,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                                if (hasTasks && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 2.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(AccentGreen)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Tasks for selected day
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedDate.dayOfWeekName() + ", " +
                                selectedDate.monthName() + " " + selectedDate.day,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { onAddTaskForDate(selectedDate) }) {
                        Text("+ Add", color = AccentBlue)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (tasks.isEmpty()) {
                item {
                    WhiteCard {
                        Text("No tasks on this day", color = TextSecondary, modifier = Modifier.padding(16.dp))
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    WhiteCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        TaskRow(
                            task = task,
                            onToggle = {
                                scope.launch {
                                    if (task.status == et.ethioflow.data.entity.TaskStatus.DONE)
                                        repo.updateTask(task.copy(status = et.ethioflow.data.entity.TaskStatus.TODO))
                                    else repo.completeTask(task)
                                }
                            },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }
}
