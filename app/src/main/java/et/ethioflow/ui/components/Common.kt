package et.ethioflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.data.entity.Priority
import et.ethioflow.data.entity.Task
import et.ethioflow.data.entity.TaskStatus
import et.ethioflow.ui.theme.*

@Composable
fun DarkHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDark)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = TextOnDark)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                if (subtitle != null) {
                    Text(subtitle, color = TextOnDarkSecondary, fontSize = 13.sp)
                }
                Text(title, color = TextOnDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            actions()
        }
    }
}

@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val done = task.status == TaskStatus.DONE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
            Icon(
                if (done) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) AccentGreen else TextSecondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (done) TextSecondary else TextPrimary
            )
            if (task.dueEthDate != null) {
                Text(
                    task.dueEthDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
        PriorityChip(task.priority)
    }
}

@Composable
fun PriorityChip(priority: Priority) {
    val (bg, fg) = when (priority) {
        Priority.LOW -> Color(0xFFE8F5E9) to AccentGreen
        Priority.MEDIUM -> Color(0xFFE3F2FD) to AccentBlue
        Priority.HIGH -> Color(0xFFFFF3E0) to AccentOrange
        Priority.URGENT -> Color(0xFFFFEBEE) to AccentRed
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(priority.name.take(1), color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FabAdd(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = NavyDark,
        contentColor = TextOnDark,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) NavyDark else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            fontSize = 11.sp,
            color = if (selected) NavyDark else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
