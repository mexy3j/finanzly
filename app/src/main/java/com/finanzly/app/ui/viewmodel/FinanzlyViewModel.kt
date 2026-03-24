package com.finanzly.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finanzly.app.data.local.entities.Budget
import com.finanzly.app.data.local.entities.Transaction
import com.finanzly.app.data.repository.FinanzlyRepository
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class BudgetProgress(
    val category: CategoryType,
    val limit: Double,
    val spent: Double
) {
    val progress: Float get() = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget: Boolean get() = spent > limit
}

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val budgetProgresses: List<BudgetProgress> = emptyList()
)

@HiltViewModel
class FinanzlyViewModel @Inject constructor(
    private val repository: FinanzlyRepository
) : ViewModel() {

    private val currentMonth: YearMonth get() = YearMonth.now()
    private val monthStart: LocalDate get() = currentMonth.atDay(1)
    private val monthEnd: LocalDate get() = currentMonth.atEndOfMonth()

    // All transactions
    val allTransactions: StateFlow<List<Transaction>> =
        repository.getAllTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent transactions (last 5)
    val recentTransactions: StateFlow<List<Transaction>> =
        repository.getRecentTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly income
    val monthlyIncome: StateFlow<Double> =
        repository.getMonthlyIncome(monthStart, monthEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Monthly expenses
    val monthlyExpenses: StateFlow<Double> =
        repository.getMonthlyExpenses(monthStart, monthEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // All budgets
    val budgets: StateFlow<List<Budget>> =
        repository.getAllBudgets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Budget progress per category
    private val _budgetProgresses = MutableStateFlow<List<BudgetProgress>>(emptyList())
    val budgetProgresses: StateFlow<List<BudgetProgress>> = _budgetProgresses.asStateFlow()

    // Composed Dashboard state
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        monthlyIncome, monthlyExpenses, recentTransactions, _budgetProgresses
    ) { income, expenses, recent, progresses ->
        DashboardUiState(
            totalBalance = income - expenses,
            monthlyIncome = income,
            monthlyExpenses = expenses,
            recentTransactions = recent,
            budgetProgresses = progresses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        observeBudgetProgresses()
    }

    private fun observeBudgetProgresses() {
        viewModelScope.launch {
            budgets.collect { budgetList ->
                val progresses = budgetList.map { budget ->
                    // Collect current month spending for this category
                    repository.getCategoryExpenses(budget.category, monthStart, monthEnd)
                        .first()
                        .let { spent ->
                            BudgetProgress(budget.category, budget.monthlyLimit, spent)
                        }
                }
                _budgetProgresses.value = progresses
            }
        }
    }

    // Actions
    fun addTransaction(
        description: String,
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        date: LocalDate
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    description = description,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    fun upsertBudget(category: CategoryType, limit: Double) {
        viewModelScope.launch {
            repository.upsertBudget(Budget(category = category, monthlyLimit = limit))
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { repository.deleteBudget(budget) }
    }

    suspend fun getAllTransactionsForExport(): List<Transaction> =
        repository.getAllTransactionsOnce()
}
