package com.finanzly.app.data.local

import androidx.room.*
import com.finanzly.app.data.local.entities.Budget
import com.finanzly.app.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun getBudgetByCategory(category: CategoryType): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)
}
