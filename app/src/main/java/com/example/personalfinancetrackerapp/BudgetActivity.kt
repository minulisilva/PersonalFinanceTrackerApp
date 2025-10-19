package com.example.personalfinancetrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)

        sharedPrefs = SharedPrefsHelper.getInstance(this)

        // Update date
        val dateText: TextView = findViewById(R.id.date_budget)
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val currentCalendar = Calendar.getInstance()
        dateText.text = dateFormat.format(currentCalendar.time)

        // Get current month transactions
        val transactions = sharedPrefs.getTransactions() ?: emptyList()
        val currentMonthTransactions = transactions.filter {
            val transactionCal = Calendar.getInstance().apply { time = it.date }
            transactionCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    transactionCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
        }

        // Calculate total expenses for the current month
        val totalExpenses = currentMonthTransactions.filter { it.isExpense }.sumOf { it.amount }

        // Get saved budget or default to 0
        val monthlyBudget = sharedPrefs.getBudget() ?: 0.0

        // Set up the budget input field
        val editBudgetAmount: EditText = findViewById(R.id.et_budget_amount)
        editBudgetAmount.setText(if (monthlyBudget > 0) "${sharedPrefs.getCurrency()}${String.format("%.2f", monthlyBudget)}" else "")

        // Save budget button
        val saveButton: Button = findViewById(R.id.btn_save_budget)
        saveButton.setOnClickListener {
            val budgetInput = editBudgetAmount.text.toString().replace(sharedPrefs.getCurrency(), "").toDoubleOrNull()
            if (budgetInput != null && budgetInput > 0) {
                sharedPrefs.saveBudget(budgetInput)
                updateBudgetUI(budgetInput, totalExpenses)
            } else {
                editBudgetAmount.error = "Please enter a valid budget amount"
            }
        }

        // Update budget UI initially
        updateBudgetUI(monthlyBudget, totalExpenses)

        // Setup Bottom Navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_transaction -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, FinancialSnapshotActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_budget -> {
                    // Already on this screen
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_budget
    }

    private fun updateBudgetUI(monthlyBudget: Double, totalExpenses: Double) {
        // Calculate remaining budget and percentage used
        val remainingBudget = monthlyBudget - totalExpenses
        val percentageUsed = if (monthlyBudget > 0) {
            ((totalExpenses / monthlyBudget) * 100).toInt()
        } else 0

        // Update Progress Bar and Percentage
        val progressBar: ProgressBar = findViewById(R.id.progress_budget_usage)
        val textPercentage: TextView = findViewById(R.id.text_budget_percentage)
        val textUsageMessage: TextView = findViewById(R.id.text_budget_usage_message)

        progressBar.progress = percentageUsed
        textPercentage.text = "$percentageUsed%"
        textUsageMessage.text = "You've used $percentageUsed% of your budget"

        // Update Budget Breakdown
        val textMonthlyBudget: TextView = findViewById(R.id.text_monthly_budget)
        val textTotalExpenses: TextView = findViewById(R.id.text_total_expenses)
        val textRemainingBudget: TextView = findViewById(R.id.text_remaining_budget)
        val textBudgetMessage: TextView = findViewById(R.id.text_budget_message)

        textMonthlyBudget.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", monthlyBudget)}"
        textTotalExpenses.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", totalExpenses)}"
        textRemainingBudget.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", remainingBudget)}"

        // Update message based on usage
        if (percentageUsed > 100) {
            textBudgetMessage.text = "Warning: You've exceeded your budget!"
            textBudgetMessage.setTextColor(getColor(android.R.color.holo_red_dark))
            textBudgetMessage.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_dialog_alert, 0, 0, 0)
        } else if (percentageUsed > 80) {
            textBudgetMessage.text = "Caution: You're nearing your budget limit"
            textBudgetMessage.setTextColor(getColor(android.R.color.holo_orange_dark))
            textBudgetMessage.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_dialog_alert, 0, 0, 0)
        } else {
            textBudgetMessage.text = "Great job! You're spending wisely"
            textBudgetMessage.setTextColor(getColor(android.R.color.holo_green_dark))
            textBudgetMessage.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_dialog_info, 0, 0, 0)
        }
    }
}