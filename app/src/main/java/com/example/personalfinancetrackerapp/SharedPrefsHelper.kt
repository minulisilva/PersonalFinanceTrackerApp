package com.example.personalfinancetrackerapp

import android.content.Context
import android.content.SharedPreferences
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsHelper (context: Context) {
    companion object {
        @Volatile
        private var instance: SharedPrefsHelper? = null

        fun getInstance(context: Context): SharedPrefsHelper {
            return instance ?: synchronized(this) {
                instance ?: SharedPrefsHelper(context).also { instance = it }
            }
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PersonalFinancePrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save and retrieve budget
    fun saveBudget(budget: Double) {
        sharedPreferences.edit().putFloat("budget", budget.toFloat()).apply()
    }

    fun getBudget(): Double {
        return sharedPreferences.getFloat("budget", 0.0f).toDouble()
    }

    // Save and retrieve currency
    fun saveCurrency(currency: String) {
        sharedPreferences.edit().putString("currency", currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString("currency", "LKR ") ?: "LKR "
    }

    // Save and retrieve transactions
    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions()?.toMutableList() ?: mutableListOf()
        transactions.add(transaction)
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString("transactions", json).apply()
    }

    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString("transactions", json).apply()
    }

    fun getTransactions(): List<Transaction>? {
        val json = sharedPreferences.getString("transactions", null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // Save and retrieve users
    fun saveUser(user: User) {
        val users = getUsers()?.toMutableList() ?: mutableListOf()
        users.add(user)
        val json = gson.toJson(users)
        sharedPreferences.edit().putString("users", json).apply()
    }

    fun getUsers(): List<User>? {
        val json = sharedPreferences.getString("users", null)
        return if (json != null) {
            val type = object : TypeToken<List<User>>() {}.type
            try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                null // Handle deserialization errors gracefully
            }
        } else {
            null
        }
    }

    fun getUser(email: String): User? {
        val users = getUsers() ?: return null
        return users.find { it.email == email }
    }

    // Legacy method for email (for backward compatibility)
    fun getLastLoggedInEmailLegacy(): String? {
        return getLastLoggedInEmail()
    }

    // Login state management
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    // Remember Me functionality
    fun setRememberMe(rememberMe: Boolean) {
        sharedPreferences.edit().putBoolean("rememberMe", rememberMe).apply()
    }

    fun getRememberMe(): Boolean {
        return sharedPreferences.getBoolean("rememberMe", false)
    }

    // Last logged-in user credentials
    fun setLastLoggedInEmail(email: String) {
        sharedPreferences.edit().putString("lastLoggedInEmail", email).apply()
    }

    fun getLastLoggedInEmail(): String? {
        return sharedPreferences.getString("lastLoggedInEmail", null)
    }

    fun setLastLoggedInPassword(password: String) {
        sharedPreferences.edit().putString("lastLoggedInPassword", password).apply()
    }

    fun getLastLoggedInPassword(): String? {
        return sharedPreferences.getString("lastLoggedInPassword", null)
    }

    // Username management (for display purposes, e.g., in MainActivity greeting)
    fun setUserName(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("username", null)
    }

    // Email management (for ProfileActivity)
    fun setUserEmail(email: String) {
        sharedPreferences.edit().putString("userEmail", email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("userEmail", null)
    }

    // Password management for signup
    fun setUserPassword(password: String) {
        sharedPreferences.edit().putString("userPassword", password).apply()
    }

    fun getUserPassword(): String? {
        return sharedPreferences.getString("userPassword", null)
    }

    // User image management (for ProfileActivity)
    fun setUserImageUrl(imageUrl: String?) {
        sharedPreferences.edit().putString("userImageUrl", imageUrl).apply()
    }

    fun getUserImageUrl(): String? {
        return sharedPreferences.getString("userImageUrl", null)
    }

    // Dark mode
    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }

    // Notifications
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications", true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications", enabled).apply()
    }

    // Clear all user data (for logout)
    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}