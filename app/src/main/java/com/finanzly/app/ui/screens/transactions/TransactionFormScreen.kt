package com.finanzly.app.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.domain.model.TransactionType
import com.finanzly.app.ui.theme.Crimson500
import com.finanzly.app.ui.theme.Emerald500
import com.finanzly.app.ui.viewmodel.FinanzlyViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: FinanzlyViewModel,
    onTransactionAdded: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(CategoryType.OTHER) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("es"))
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nueva Transacción",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Type Selector
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeButton(
                    label = "Gasto",
                    selected = selectedType == TransactionType.EXPENSE,
                    color = Crimson500,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = TransactionType.EXPENSE }
                )
                TypeButton(
                    label = "Ingreso",
                    selected = selectedType == TransactionType.INCOME,
                    color = Emerald500,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = TransactionType.INCOME }
                )
            }
        }

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; descriptionError = false },
            label = { Text("Descripción") },
            placeholder = { Text("Ej. Supermercado, salario...") },
            isError = descriptionError,
            supportingText = { if (descriptionError) Text("Campo requerido") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Amount
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; amountError = false },
            label = { Text("Monto") },
            placeholder = { Text("0.00") },
            isError = amountError,
            supportingText = { if (amountError) Text("Ingresa un monto válido") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("$") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = showCategoryDropdown,
            onExpandedChange = { showCategoryDropdown = it }
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
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false }
            ) {
                CategoryType.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = { selectedCategory = category; showCategoryDropdown = false }
                    )
                }
            }
        }

        // Date Picker
        OutlinedTextField(
            value = selectedDate.format(dateFormatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, "Seleccionar fecha")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = {
                descriptionError = description.isBlank()
                amountError = amountText.isBlank() || amountText.toDoubleOrNull() == null || amountText.toDouble() <= 0

                if (!descriptionError && !amountError) {
                    viewModel.addTransaction(
                        description = description.trim(),
                        amount = amountText.toDouble(),
                        type = selectedType,
                        category = selectedCategory,
                        date = selectedDate
                    )
                    onTransactionAdded()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType == TransactionType.INCOME) Emerald500 else Crimson500
            )
        ) {
            Text(
                text = if (selectedType == TransactionType.INCOME) "Agregar Ingreso" else "Agregar Gasto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TypeButton(
    label: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else color.copy(alpha = 0.1f),
            contentColor = if (selected) androidx.compose.ui.graphics.Color.White else color
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
