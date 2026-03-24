package com.finanzly.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finanzly.app.data.local.entities.Transaction
import com.finanzly.app.domain.model.TransactionType
import com.finanzly.app.ui.theme.*
import com.finanzly.app.ui.viewmodel.BudgetProgress
import com.finanzly.app.ui.viewmodel.FinanzlyViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: FinanzlyViewModel) {
    val state by viewModel.dashboardUiState.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Finanzly",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Balance Hero Card
        item {
            BalanceCard(
                balance = state.totalBalance,
                income = state.monthlyIncome,
                expenses = state.monthlyExpenses,
                formatter = currencyFormatter
            )
        }

        // Budget Progress Section
        if (state.budgetProgresses.isNotEmpty()) {
            item {
                Text(
                    text = "Presupuestos del mes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(state.budgetProgresses) { progress ->
                BudgetProgressCard(progress = progress, formatter = currencyFormatter)
            }
        }

        // Recent Transactions
        item {
            Text(
                text = "Últimos movimientos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (state.recentTransactions.isEmpty()) {
            item {
                EmptyStateCard("Sin movimientos aún.\n¡Agrega tu primera transacción!")
            }
        } else {
            items(state.recentTransactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    formatter = currencyFormatter,
                    onDelete = { viewModel.deleteTransaction(transaction) }
                )
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expenses: Double,
    formatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Indigo600, Indigo400)),
                    RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Balance del mes",
                    style = MaterialTheme.typography.labelLarge,
                    color = Indigo100
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatter.format(balance),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryChip(
                        label = "Ingresos",
                        amount = formatter.format(income),
                        icon = Icons.Filled.ArrowDownward,
                        color = Emerald400,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryChip(
                        label = "Gastos",
                        amount = formatter.format(expenses),
                        icon = Icons.Filled.ArrowUpward,
                        color = Crimson400,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryChip(
    label: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, color = Indigo50)
            Text(amount, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BudgetProgressCard(progress: BudgetProgress, formatter: NumberFormat) {
    val progressColor = if (progress.isOverBudget) Crimson500 else Indigo500
    val progressBg = if (progress.isOverBudget) Crimson500.copy(alpha = 0.15f)
                     else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progress.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (progress.isOverBudget) {
                    Badge(containerColor = Crimson500) { Text("Excedido") }
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressBg
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatter.format(progress.spent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (progress.isOverBudget) Crimson500
                            else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Límite: ${formatter.format(progress.limit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    formatter: NumberFormat,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) Emerald500 else Crimson500
    val amountPrefix = if (isIncome) "+" else "-"
    val icon = if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward
    val iconBg = if (isIncome) Emerald500.copy(alpha = 0.15f) else Crimson500.copy(alpha = 0.15f)
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es"))

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar transacción") },
            text = { Text("¿Deseas eliminar \"${transaction.description}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Eliminar", color = Crimson500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { showDeleteDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = amountColor, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${transaction.category.displayName} · ${transaction.date.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "$amountPrefix${formatter.format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
