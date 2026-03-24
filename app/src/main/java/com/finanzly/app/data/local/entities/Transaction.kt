package com.finanzly.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.domain.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: CategoryType,
    val date: LocalDate = LocalDate.now()
)
