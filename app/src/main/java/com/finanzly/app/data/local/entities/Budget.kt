package com.finanzly.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finanzly.app.domain.model.CategoryType

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey
    val category: CategoryType,
    val monthlyLimit: Double
)
