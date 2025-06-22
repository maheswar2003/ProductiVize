package com.productivize.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.productivize.data.model.DailySummary
import com.productivize.data.model.HourLog
import com.productivize.data.model.JournalEntry
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class DataExporter(private val context: Context) {
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    // Legacy method - keeping for backward compatibility
    fun exportToCsv(logs: List<HourLog>): File {
        val file = File(context.filesDir, "ProductiVize_${System.currentTimeMillis()}.csv")
        file.bufferedWriter().use { writer ->
            writer.write("Date,Hour,Rating,Tags,Notes\n")
            logs.forEach { log ->
                writer.write("${log.dateTime.toLocalDate()},${log.hour},${log.rating?:0},\"${log.tags.joinToString()}\",\"${log.notes?:""}\n")
            }
        }
        return file
    }
    
    // Enhanced export methods
    fun exportToCSV(
        hourLogs: List<HourLog>,
        dailySummaries: List<DailySummary>,
        journalEntries: List<JournalEntry>
    ): Intent? {
        return try {
            val timestamp = dateFormat.format(Date())
            val fileName = "ProductiVize_Export_$timestamp.csv"
            val file = File(context.cacheDir, fileName)
            
            FileWriter(file).use { writer ->
                // Hour Logs CSV
                writer.append("=== HOUR LOGS ===\n")
                writer.append("Date,Hour,Rating,Tags,Notes\n")
                hourLogs.forEach { log ->
                    writer.append("${log.dateTime},${log.hour},${log.rating ?: ""},\"${log.tags.joinToString(";")}\",\"${log.notes ?: ""}\"\n")
                }
                
                writer.append("\n=== DAILY SUMMARIES ===\n")
                writer.append("Date,Total Hours,Achievement %,Average Rating,Peak Hours,Low Hours,Top Tags\n")
                dailySummaries.forEach { summary ->
                    writer.append("${summary.date},${summary.totalHoursRated},${summary.achievementPercentage},${summary.averageRating},\"${summary.peakHours.joinToString(";")}\",\"${summary.lowHours.joinToString(";")}\",\"${summary.topTags.joinToString(";")}\"\n")
                }
                
                writer.append("\n=== JOURNAL ENTRIES ===\n")
                writer.append("Date,Wins,Challenges,Goals,Mood,Created Time\n")
                journalEntries.forEach { entry ->
                    writer.append("${entry.date},\"${entry.wins}\",\"${entry.challenges}\",\"${entry.goalsTomorrow}\",${entry.moodEmoji},${entry.createdTime}\n")
                }
            }
            
            createShareIntent(file, "text/csv")
        } catch (e: Exception) {
            null
        }
    }
    
    fun exportToJSON(
        hourLogs: List<HourLog>,
        dailySummaries: List<DailySummary>,
        journalEntries: List<JournalEntry>
    ): Intent? {
        return try {
            val timestamp = dateFormat.format(Date())
            val fileName = "ProductiVize_Export_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            
            val exportData = mapOf(
                "exportDate" to timestamp,
                "hourLogs" to hourLogs,
                "dailySummaries" to dailySummaries,
                "journalEntries" to journalEntries
            )
            
            FileWriter(file).use { writer ->
                gson.toJson(exportData, writer)
            }
            
            createShareIntent(file, "application/json")
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createShareIntent(file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ProductiVize Data Export")
            putExtra(Intent.EXTRA_TEXT, "Your productivity data export from ProductiVize")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    fun getExportSummary(
        hourLogs: List<HourLog>,
        dailySummaries: List<DailySummary>,
        journalEntries: List<JournalEntry>
    ): String {
        return """
            Export Summary:
            • ${hourLogs.size} hour logs
            • ${dailySummaries.size} daily summaries  
            • ${journalEntries.size} journal entries
            • Date range: ${hourLogs.minByOrNull { it.dateTime }?.dateTime} to ${hourLogs.maxByOrNull { it.dateTime }?.dateTime}
        """.trimIndent()
    }

    fun importFromJson(json: String): List<HourLog> {
        return Gson().fromJson(json, object : TypeToken<List<HourLog>>() {}.type)
    }
} 