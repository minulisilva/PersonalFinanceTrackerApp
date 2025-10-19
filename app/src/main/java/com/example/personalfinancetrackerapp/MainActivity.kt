package com.example.personalfinancetrackerapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var fileHelper: FileHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var adapter: TransactionAdapter
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var mainContent: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101)
            }
        }

        sharedPrefs = SharedPrefsHelper.getInstance(this)
        fileHelper = FileHelper(this)
        notificationHelper = NotificationHelper(this)
        transactions = sharedPrefs.getTransactions()?.toMutableList() ?: mutableListOf()

        // Initialize views
        mainContent = findViewById(R.id.main_content)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Setup greeting and date
        val greetingText: TextView = findViewById(R.id.greeting_text)
        val dateText: TextView = findViewById(R.id.date_text)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        greetingText.text = "$greeting, Minuli"
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        dateText.text = dateFormat.format(calendar.time)

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.transaction_list)
        adapter = TransactionAdapter(transactions, sharedPrefs.getCurrency(), { position ->
            // Edit transaction
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("transaction", transactions[position])
            intent.putExtra("position", position)
            startActivityForResult(intent, EDIT_REQUEST)
        }, { position ->
            // Delete transaction
            transactions.removeAt(position)
            sharedPrefs.saveTransactions(transactions)
            adapter.notifyDataSetChanged()
            updateBudgetStatus()
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup buttons
        findViewById<Button>(R.id.btn_add_income).setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("isIncome", true)
            startActivityForResult(intent, ADD_REQUEST)
        }
        findViewById<Button>(R.id.btn_add_expense).setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)
            intent.putExtra("isIncome", false)
            startActivityForResult(intent, ADD_REQUEST)
        }
        findViewById<TextView>(R.id.view_all).setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }

        // Setup Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Show the main content (Home page)
                    mainContent.visibility = View.VISIBLE
                    true
                }
                R.id.nav_transaction -> {
                    // Navigate to AnalysisActivity (All Transactions)
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, FinancialSnapshotActivity::class.java))
                    true
                }
                R.id.nav_budget -> {
                    startActivityForResult(Intent(this, BudgetActivity::class.java), BUDGET_REQUEST)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                else -> false
            }
        }

        // Set default selection to Home
        bottomNavigation.selectedItemId = R.id.nav_home

        updateBudgetStatus()
    }

    private fun updateBudgetStatus() {
        val budget = sharedPrefs.getBudget()
        val totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }
        val remainingBudget = budget - totalExpenses
        val progress = if (budget > 0) ((totalExpenses / budget) * 100).toInt() else 0


        if (budget > 0) {
            if (totalExpenses >= budget) {
                notificationHelper.sendBudgetNotification("You have exceeded your budget!")
            } else if (totalExpenses >= budget * 0.9) {
                notificationHelper.sendBudgetNotification("You are nearing your budget limit!")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_REQUEST -> {
                    val transaction = data?.getSerializableExtra("transaction") as? Transaction
                    if (transaction != null) {
                        transactions.add(transaction)
                        sharedPrefs.saveTransactions(transactions)
                        adapter.notifyDataSetChanged()
                        updateBudgetStatus()
                    }
                }
                EDIT_REQUEST -> {
                    val transaction = data?.getSerializableExtra("transaction") as? Transaction
                    val position = data?.getIntExtra("position", -1) ?: -1
                    if (transaction != null && position != -1) {
                        transactions[position] = transaction
                        sharedPrefs.saveTransactions(transactions)
                        adapter.notifyItemChanged(position)
                        updateBudgetStatus()
                    }
                }
                BUDGET_REQUEST -> {
                    updateBudgetStatus()
                }
            }
        }
    }

    companion object {
        const val ADD_REQUEST = 1
        const val EDIT_REQUEST = 2
        const val BUDGET_REQUEST = 3
    }
}