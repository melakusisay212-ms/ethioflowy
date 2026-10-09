package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.data.entity.TxType
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.components.WhiteCard
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinanceScreen(repo: Repository, onBack: () -> Unit) {
    val txs by repo.observeTransactions().collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf("Other") }
    val scope = rememberCoroutineScope()
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }

    val income = txs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
    val expense = txs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense

    Column(Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(title = "Finance", subtitle = "Track income & expenses", onBack = onBack, actions = {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null, tint = TextOnDark) }
        })

        Column(Modifier.fillMaxWidth().background(NavyDark).padding(20.dp)) {
            Text("Balance", color = TextOnDarkSecondary, fontSize = 13.sp)
            Text("ETB ${fmt.format(balance)}", color = TextOnDark, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f).background(NavySoft, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column {
                        Text("Income", color = TextOnDarkSecondary, fontSize = 12.sp)
                        Text("+${fmt.format(income)}", color = AccentGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Box(Modifier.weight(1f).background(NavySoft, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column {
                        Text("Expense", color = TextOnDarkSecondary, fontSize = 12.sp)
                        Text("-${fmt.format(expense)}", color = AccentRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        LazyColumn(
            Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(SurfaceLight).padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }
            if (txs.isEmpty()) {
                item { WhiteCard { Text("No transactions yet.", color = TextSecondary, modifier = Modifier.padding(24.dp)) } }
            }
            items(txs, key = { it.id }) { tx ->
                WhiteCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tx.title, fontWeight = FontWeight.Medium)
                            Text("${tx.category} · ${tx.ethDate}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Text(
                            (if (tx.type == TxType.INCOME) "+" else "-") + fmt.format(tx.amount),
                            color = if (tx.type == TxType.INCOME) AccentGreen else AccentRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Transaction") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (ETB)") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        FilterChip(selected = isExpense, onClick = { isExpense = true }, label = { Text("Expense") })
                        Spacer(Modifier.width(8.dp))
                        FilterChip(selected = !isExpense, onClick = { isExpense = false }, label = { Text("Income") })
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (title.isNotBlank() && amt != null && amt > 0) scope.launch {
                        repo.addTransaction(title.trim(), amt, if (isExpense) TxType.EXPENSE else TxType.INCOME, category.trim())
                        title = ""; amount = ""; showAdd = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
