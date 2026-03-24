package com.finanzly.app.data.repository

import com.finanzly.app.data.local.BudgetDao
import com.finanzly.app.data.local.TransactionDao
import com.finanzly.app.data.local.entities.Budget
import com.finanzly.app.data.local.entities.Transaction
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanzlyRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    fun getRecentTransactions(): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions()

    fun getMonthlyIncome(start: LocalDate, end: LocalDate): Flow<Double> =
        transactionDao.getSumByTypeAndDateRange(TransactionType.INCOME, start, end)

    fun getMonthlyExpenses(start: LocalDate, end: LocalDate): Flow<Double> =
        transactionDao.getSumByTypeAndDateRange(TransactionType.EXPENSE, start, end)

    fun getCategoryExpenses(
        category: CategoryType,
        start: LocalDate,
        end: LocalDate
    ): Flow<Double> = transactionDao.getExpensesByCategory(category, start, end)

    suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    suspend fun getAllTransactionsOnce(): List<Transaction> =
        transactionDao.getAllTransactionsOnce()

    // Budgets
    fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAllBudgets()

    suspend fun upsertBudget(budget: Budget) =
        budgetDao.upsertBudget(budget)

    suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget)
}
