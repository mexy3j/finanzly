package com.finanzly.app.data.local

import androidx.room.*
import com.finanzly.app.data.local.entities.Transaction
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    fun getRecentTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE type = :type AND date >= :startDate AND date <= :endDate
    """)
    fun getSumByTypeAndDateRange(
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE type = 'EXPENSE' AND category = :category 
        AND date >= :startDate AND date <= :endDate
    """)
    fun getExpensesByCategory(
        category: CategoryType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsOnce(): List<Transaction>
}
