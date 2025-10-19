package com.example.personalfinancetrackerapp

import android.content.Context
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.gson.Gson
import java.io.IOException

class FileHelper(private val context: Context) {
    private val gson = Gson()
    private val fileName = "transactions_backup.json"

    fun backupTransactions(transactions: List<Transaction>): Boolean {
        return try {
            val json = gson.toJson(transactions)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    fun restoreTransactions(): List<Transaction>? {
        return try {
            val file = java.io.File(context.filesDir, fileName)
            if (!file.exists()) return null
            context.openFileInput(fileName).use { input ->
                val json = input.bufferedReader().readText()
                val type = object : com.google.gson.reflect.TypeToken<List<Transaction>>() {}.type
                gson.fromJson(json, type)
            }
        } catch (e: IOException) {
            null
        }
    }
}