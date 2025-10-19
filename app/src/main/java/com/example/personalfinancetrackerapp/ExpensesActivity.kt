package com.example.personalfinancetrackerapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpensesActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDate: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var prefs: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expenses)

        prefs = SharedPrefsHelper.getInstance(this)

        // Initialize views
        etTitle = findViewById(R.id.et_title)
        etAmount = findViewById(R.id.et_amount)
        spinnerCategory = findViewById(R.id.spinner_category)
        etDate = findViewById(R.id.et_date)
        btnSave = findViewById(R.id.btn_save)

        // Populate category spinner
        val categories = arrayOf("Food", "Transport", "Entertainment", "Bills", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Set up date picker
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(selectedDate.time))
            }, year, month, day)
            datePickerDialog.show()
        }

        // Save button click listener
        btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun saveExpense() {
        val title = etTitle.text.toString().trim()
        val amount = etAmount.text.toString().trim().toDoubleOrNull()
        val category = spinnerCategory.selectedItem.toString()
        val dateString = etDate.text.toString().trim()

        // Validate inputs
        if (title.isEmpty()) {
            etTitle.error = "Please enter a title"
            return
        }
        if (amount == null || amount <= 0) {
            etAmount.error = "Please enter a valid amount"
            return
        }
        if (dateString.isEmpty()) {
            etDate.error = "Please select a date"
            return
        }

        // Parse date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date: Date = try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        // Create expense (isExpense = true)
        val expense = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = date,
            isExpense = true
        )

        // Save expense using PreferenceManager
        prefs.saveTransaction(expense)

        Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()

        // Finish activity
        finish()
    }
}