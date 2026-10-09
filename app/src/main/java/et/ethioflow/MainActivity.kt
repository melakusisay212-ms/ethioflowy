package et.ethioflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.entity.Task
import et.ethioflow.ui.components.BottomNavItem
import et.ethioflow.ui.components.FabAdd
import et.ethioflow.ui.screens.*
import et.ethioflow.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as EthioFlowApp).repository

        setContent {
            EthioFlowTheme {
                var currentTab by remember { mutableIntStateOf(0) }
                var showAddTask by remember { mutableStateOf(false) }
                var addTaskDate by remember { mutableStateOf<EthiopianDate?>(null) }
                var selectedTask by remember { mutableStateOf<Task?>(null) }
                // Secondary routes from More: projects, notes, goals, finance, journal
                var secondaryRoute by remember { mutableStateOf<String?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        showAddTask -> {
                            AddTaskScreen(
                                repo = repo,
                                initialDate = addTaskDate,
                                onDone = { showAddTask = false; addTaskDate = null }
                            )
                        }
                        secondaryRoute != null -> {
                            when (secondaryRoute) {
                                "projects" -> ProjectsScreen(repo) { secondaryRoute = null }
                                "notes" -> NotesScreen(repo) { secondaryRoute = null }
                                "goals" -> GoalsScreen(repo) { secondaryRoute = null }
                                "finance" -> FinanceScreen(repo) { secondaryRoute = null }
                                "journal" -> JournalScreen(repo) { secondaryRoute = null }
                            }
                        }
                        else -> {
                            when (currentTab) {
                                0 -> TodayScreen(
                                    repo = repo,
                                    onOpenCalendar = { currentTab = 1 },
                                    onOpenProjects = { secondaryRoute = "projects" },
                                    onOpenHabits = { currentTab = 2 },
                                    onAddTask = { showAddTask = true },
                                    onTaskClick = { selectedTask = it }
                                )
                                1 -> CalendarScreen(
                                    repo = repo,
                                    onBack = { currentTab = 0 },
                                    onAddTaskForDate = { date -> addTaskDate = date; showAddTask = true },
                                    onTaskClick = { selectedTask = it }
                                )
                                2 -> HabitsScreen(repo) { currentTab = 0 }
                                3 -> MoreScreen(
                                    onBack = { currentTab = 0 },
                                    onNavigate = { secondaryRoute = it }
                                )
                            }

                            // Bottom bar
                            Box(Modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                                Surface(color = CardWhite, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                                            .padding(vertical = 8.dp, horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BottomNavItem(
                                            icon = if (currentTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                            label = "Today", selected = currentTab == 0,
                                            onClick = { currentTab = 0 }
                                        )
                                        BottomNavItem(
                                            icon = if (currentTab == 1) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                            label = "Calendar", selected = currentTab == 1,
                                            onClick = { currentTab = 1 }
                                        )
                                        Spacer(Modifier.width(40.dp))
                                        BottomNavItem(
                                            icon = if (currentTab == 2) Icons.Filled.Loop else Icons.Outlined.Loop,
                                            label = "Habits", selected = currentTab == 2,
                                            onClick = { currentTab = 2 }
                                        )
                                        BottomNavItem(
                                            icon = if (currentTab == 3) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                                            label = "More", selected = currentTab == 3,
                                            onClick = { currentTab = 3 }
                                        )
                                    }
                                }
                                Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-28).dp)) {
                                    FabAdd(onClick = {
                                        addTaskDate = EthiopianDate.now()
                                        showAddTask = true
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
