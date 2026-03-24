package com.finanzly.app.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.finanzly.app.data.local.entities.Transaction
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CsvExporter {

    fun exportToCsv(context: Context, transactions: List<Transaction>): Boolean {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "finanzly_export_$timestamp.csv"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportWithMediaStore(context, fileName, transactions)
            } else {
                exportLegacy(fileName, transactions)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun exportWithMediaStore(
        context: Context,
        fileName: String,
        transactions: List<Transaction>
    ) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("No se pudo crear el archivo")

        resolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writeCsvContent(writer, transactions)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun exportLegacy(fileName: String, transactions: List<Transaction>) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = java.io.File(downloadsDir, fileName)
        OutputStreamWriter(file.outputStream(), Charsets.UTF_8).use { writer ->
            writeCsvContent(writer, transactions)
        }
    }

    private fun writeCsvContent(
        writer: OutputStreamWriter,
        transactions: List<Transaction>
    ) {
        // BOM for Excel UTF-8 compatibility
        writer.write("\uFEFF")
        // Header
        writer.write("ID,Descripción,Monto,Tipo,Categoría,Fecha\n")
        // Rows
        transactions.forEach { t ->
            val type = if (t.type.name == "INCOME") "Ingreso" else "Gasto"
            val escapedDescription = t.description.replace("\"", "\"\"")
            writer.write(
                "${t.id},\"$escapedDescription\",${t.amount},$type,${t.category.displayName},${t.date}\n"
            )
        }
    }
}
