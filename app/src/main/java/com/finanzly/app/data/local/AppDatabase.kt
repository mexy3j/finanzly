package com.finanzly.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.finanzly.app.data.local.entities.Budget
import com.finanzly.app.data.local.entities.Transaction
import com.finanzly.app.domain.model.CategoryType
import com.finanzly.app.domain.model.TransactionType
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(category: CategoryType): String = category.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType =
        CategoryType.valueOf(value)
}

@Database(
    entities = [Transaction::class, Budget::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}
