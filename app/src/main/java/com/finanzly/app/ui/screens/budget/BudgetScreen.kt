package com.finanzly.app.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finanzly.app.data.local.entities.Budget
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.ui.theme.Crimson500
import com.finanzly.app.ui.theme.Emerald500
import com.finanzly.app.ui.theme.Indigo500
import com.finanzly.app.ui.viewmodel.FinanzlyViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: FinanzlyViewModel) {
    val budgets by viewModel.budgets.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(CategoryType.FOOD) }
    var limitText by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    var limitError by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; limitText = ""; limitError = false },
            title = { Text("Nuevo Presupuesto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = showDropdown,
                        onExpandedChange = { showDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            CategoryType.values().forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.displayName) },
                                    onClick = { selectedCategory = category; showDropdown = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it.filter { c -> c.isDigit() || c == '.' }; limitError = false },
                        label = { Text("Límite mensual") },
                        prefix = { Text("$") },
                        isError = limitError,
                        supportingText = { if (limitError) Text("Ingresa un monto válido") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (limit == null || limit <= 0) {
                        limitError = true
                    } else {
                        viewModel.upsertBudget(selectedCategory, limit)
                        showAddDialog = false
                        limitText = ""
                        limitError = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; limitText = ""; limitError = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Indigo500
            ) {
                Icon(Icons.Filled.Add, "Agregar presupuesto", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Presupuestos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Text(
                    text = "Define límites de gasto mensuales por categoría",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (budgets.isEmpty()) {
                item {
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
                                text = "Sin presupuestos.\nToca + para agregar uno.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(budgets) { budget ->
                    BudgetItem(
                        budget = budget,
                        formatter = formatter,
                        onDelete = { viewModel.deleteBudget(budget) }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetItem(
    budget: Budget,
    formatter: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Límite: ${formatter.format(budget.monthlyLimit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Indigo500
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar presupuesto",
                    tint = Crimson500
                )
            }
        }
    }
}
