package com.example.personalfinancetrackerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AnalysisActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var adapter: TransactionAdapter
    private lateinit var transactions: MutableList<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        sharedPrefs = SharedPrefsHelper.getInstance(this)
        transactions = sharedPrefs.getTransactions()?.toMutableList() ?: mutableListOf()

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.all_transactions_list)
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
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup FAB
        findViewById<FloatingActionButton>(R.id.fab_add_transaction).setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("isIncome", false)
            startActivityForResult(intent, ADD_REQUEST)
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
                    // Already on this screen
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, FinancialSnapshotActivity::class.java))
                    finish()
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
        bottomNavigation.selectedItemId = R.id.nav_transaction
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
                    }
                }
                EDIT_REQUEST -> {
                    val transaction = data?.getSerializableExtra("transaction") as? Transaction
                    val position = data?.getIntExtra("position", -1) ?: -1
                    if (transaction != null && position != -1) {
                        transactions[position] = transaction
                        sharedPrefs.saveTransactions(transactions)
                        adapter.notifyItemChanged(position)
                    }
                }
                BUDGET_REQUEST -> {

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