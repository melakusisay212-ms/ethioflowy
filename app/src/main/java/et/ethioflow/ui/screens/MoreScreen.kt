package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.theme.*

data class MoreItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@Composable
fun MoreScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        MoreItem("Projects", "Group your work", Icons.Outlined.Folder, "projects"),
        MoreItem("Notes", "PARA second brain", Icons.Outlined.Description, "notes"),
        MoreItem("Goals", "Link tasks & habits", Icons.Outlined.EmojiEvents, "goals"),
        MoreItem("Finance", "Income & expenses", Icons.Outlined.Payments, "finance"),
        MoreItem("Journal", "Daily + weekly review", Icons.Outlined.MenuBook, "journal"),
    )

    Column(Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(title = "More", subtitle = "All modules", onBack = onBack)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(item.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.Start) {
                        Icon(item.icon, null, tint = NavyDark, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(item.subtitle, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
