package com.finanzly.app.ui.screens.history

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.finanzly.app.ui.screens.dashboard.TransactionItem
import com.finanzly.app.ui.theme.Indigo500
import com.finanzly.app.ui.viewmodel.FinanzlyViewModel
import com.finanzly.app.utils.CsvExporter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: FinanzlyViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val data = viewModel.getAllTransactionsForExport()
                        val success = CsvExporter.exportToCsv(context, data)
                        Toast.makeText(
                            context,
                            if (success) "CSV exportado a Descargas" else "Error al exportar",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                icon = { Icon(Icons.Filled.FileDownload, null) },
                text = { Text("Exportar CSV") },
                containerColor = Indigo500,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${transactions.size} movimientos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay transacciones registradas aún.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        formatter = formatter,
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}
