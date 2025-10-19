package com.example.personalfinancetrackerapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Profile : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var fileHelper: FileHelper
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Initialize singletons
        sharedPrefs = SharedPrefsHelper.getInstance(this)
        fileHelper = FileHelper(this)
        notificationHelper = NotificationHelper(this)

        // Load user data and update currency display
        loadUserData()
        updateCurrencyDisplay()

        // Setup Edit Profile button
        val editProfileButton: Button = findViewById(R.id.edit_profile_button)
        editProfileButton.setOnClickListener {
            android.widget.Toast.makeText(this, "Edit Profile clicked", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Setup Currency click
        val currencyRow: LinearLayout = findViewById(R.id.currency_row)
        currencyRow.setOnClickListener {
            showCurrencySelectionDialog()
        }

        // Setup Dark Mode switch
        val darkModeSwitch: Switch = findViewById(R.id.dark_mode_switch)
        darkModeSwitch.isChecked = sharedPrefs.isDarkModeEnabled()
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.setDarkModeEnabled(isChecked)
            android.widget.Toast.makeText(this, "Dark Mode: $isChecked", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Setup Notifications switch
        val notificationsSwitch: Switch = findViewById(R.id.notifications_switch)
        notificationsSwitch.isChecked = sharedPrefs.areNotificationsEnabled()
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.setNotificationsEnabled(isChecked)
            if (isChecked) {
                notificationHelper.enableNotifications()
            } else {
                notificationHelper.disableNotifications()
            }
            android.widget.Toast.makeText(this, "Notifications: $isChecked", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Setup Backup Data click
        val backupRow: LinearLayout = findViewById(R.id.backup_row)
        backupRow.setOnClickListener {
            val transactions = sharedPrefs.getTransactions() ?: emptyList()
            if (fileHelper.backupTransactions(transactions)) {
                android.widget.Toast.makeText(this, "Backup successful", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "Backup failed", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Restore Data click
        val restoreRow: LinearLayout = findViewById(R.id.restore_row)
        restoreRow.setOnClickListener {
            val restored = fileHelper.restoreTransactions()
            if (restored != null) {
                sharedPrefs.saveTransactions(restored)
                android.widget.Toast.makeText(this, "Restore successful", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "Restore failed", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Logout click
        val logoutRow: LinearLayout = findViewById(R.id.logout_row)
        logoutRow.setOnClickListener {
            sharedPrefs.clearUserData()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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
                    startActivity(Intent(this, BudgetActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_profile // Highlight "Settings" item
    }

    private fun loadUserData() {
        val userName: TextView = findViewById(R.id.user_name)
        val userEmail: TextView = findViewById(R.id.user_email)
        val userImage: ImageView = findViewById(R.id.user_image)

        userName.text = sharedPrefs.getUserName() ?: "User Name"
        userEmail.text = sharedPrefs.getUserEmail() ?: "email@example.com"

    }

    private fun updateCurrencyDisplay() {
        val selectedCurrencyText: TextView = findViewById(R.id.selected_currency)
        val currency = sharedPrefs.getCurrency()
        val displayText = when (currency) {
            "LKR " -> "Sri Lankan Rupee LKR"
            "$" -> "US Dollar (USD)"
            "€" -> "Euro (EUR)"
            else -> "Sri Lankan Rupee LKR"
        }
        selectedCurrencyText.text = displayText
    }

    private fun showCurrencySelectionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_currency_selection)

        // Initialize RadioGroup and RadioButtons
        val currencyRadioGroup: RadioGroup = dialog.findViewById(R.id.currency_radio_group)
        val radioLkr: RadioButton = dialog.findViewById(R.id.radio_lkr)
        val radioUsd: RadioButton = dialog.findViewById(R.id.radio_usd)
        val radioEur: RadioButton = dialog.findViewById(R.id.radio_eur)

        // Set the currently selected currency
        when (sharedPrefs.getCurrency()) {
            "LKR " -> radioLkr.isChecked = true
            "$" -> radioUsd.isChecked = true
            "€" -> radioEur.isChecked = true
        }

        // Handle currency selection
        currencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedCurrency = when (checkedId) {
                R.id.radio_lkr -> "LKR "
                R.id.radio_usd -> "$"
                R.id.radio_eur -> "€"
                else -> "LKR " // Default
            }
            sharedPrefs.saveCurrency(selectedCurrency)
            updateCurrencyDisplay()
            dialog.dismiss()
        }

        // Setup Cancel button
        val cancelButton: Button = dialog.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}