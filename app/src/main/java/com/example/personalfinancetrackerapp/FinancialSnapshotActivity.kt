package com.example.personalfinancetrackerapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetrackerapp.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinancialSnapshotActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var transactions: List<Transaction>
    private lateinit var filteredTransactions: List<Transaction>
    private var filterMode: String = "month" // Default to "This Month"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_financial_snapshot)

        sharedPrefs = SharedPrefsHelper(this)
        transactions = sharedPrefs.getTransactions() ?: emptyList()

        // Update date
        val dateText: TextView = findViewById(R.id.date_snapshot)
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val currentCalendar = Calendar.getInstance()
        dateText.text = dateFormat.format(currentCalendar.time)

        // Filter transactions based on the default filter (This Month)
        updateFilteredTransactions(currentCalendar)

        // Calculate totals and update UI
        updateTotals()

        // Setup tabs
        setupTabs(currentCalendar)

        // Setup Pie Chart for Spending Breakdown
        val pieChart: PieChart = findViewById(R.id.pie_chart)
        setupExpensePieChart(pieChart)

        // Setup Pie Chart for Income Breakdown
        val incomePieChart: PieChart = findViewById(R.id.pie_chart_income)
        setupIncomePieChart(incomePieChart)

        // Setup Bar Chart
        val barChart: BarChart = findViewById(R.id.bar_chart)
        setupBarChart(barChart)

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
                    // Already on this screen
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
        bottomNavigation.selectedItemId = R.id.nav_analysis
    }

    private fun updateFilteredTransactions(currentCalendar: Calendar) {
        filteredTransactions = when (filterMode) {
            "week" -> {
                val weekStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val weekEnd = Calendar.getInstance().apply {
                    time = weekStart.time
                    add(Calendar.DAY_OF_WEEK, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                transactions.filter {
                    val transactionCal = Calendar.getInstance().apply { time = it.date }
                    transactionCal.timeInMillis >= weekStart.timeInMillis &&
                            transactionCal.timeInMillis <= weekEnd.timeInMillis
                }
            }
            "month" -> {
                transactions.filter {
                    val transactionCal = Calendar.getInstance().apply { time = it.date }
                    transactionCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            transactionCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                }
            }
            "year" -> {
                transactions.filter {
                    val transactionCal = Calendar.getInstance().apply { time = it.date }
                    transactionCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                }
            }
            else -> emptyList()
        }
    }

    private fun updateTotals() {
        // Calculate totals for the filtered transactions
        val totalSpent = filteredTransactions.filter { it.isExpense }.sumOf { it.amount }
        val totalEarned = filteredTransactions.filter { !it.isExpense }.sumOf { it.amount }
        val netIncome = totalEarned - totalSpent

        // Update total spent and earned
        val totalSpentText: TextView = findViewById(R.id.value_total_spent)
        val totalEarnedText: TextView = findViewById(R.id.value_total_earned)
        val totalExpenseBreakdownText: TextView = findViewById(R.id.value_total_expense_breakdown)
        val totalIncomeBreakdownText: TextView = findViewById(R.id.value_total_income_breakdown)
        val netIncomeText: TextView = findViewById(R.id.value_net_income)

        totalSpentText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", totalSpent)}"
        totalEarnedText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", totalEarned)}"
        totalExpenseBreakdownText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", totalSpent)}"
        totalIncomeBreakdownText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", totalEarned)}"
        netIncomeText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", netIncome)}"
    }

    private fun setupTabs(currentCalendar: Calendar) {
        val tabThisWeek: TextView = findViewById(R.id.tab_this_week)
        val tabThisMonth: TextView = findViewById(R.id.tab_this_month)
        val tabThisYear: TextView = findViewById(R.id.tab_this_year)

        tabThisWeek.setOnClickListener {
            filterMode = "week"
            tabThisWeek.background = getDrawable(R.drawable.tab_background_selected)
            tabThisWeek.setTextColor(getColor(android.R.color.black))
            tabThisMonth.background = getDrawable(R.drawable.tab_background_unselected)
            tabThisMonth.setTextColor(getColor(R.color.grey))
            tabThisYear.background = getDrawable(R.drawable.tab_background_unselected)
            tabThisYear.setTextColor(getColor(R.color.grey))
            updateFilteredTransactions(currentCalendar)
            updateTotals()
            setupExpensePieChart(findViewById(R.id.pie_chart))
            setupIncomePieChart(findViewById(R.id.pie_chart_income))
            setupBarChart(findViewById(R.id.bar_chart))
        }

        tabThisMonth.setOnClickListener {
            filterMode = "month"
            tabThisWeek.background = getDrawable(R.drawable.tab_background_unselected)
            tabThisWeek.setTextColor(getColor(R.color.grey))
            tabThisMonth.background = getDrawable(R.drawable.tab_background_selected)
            tabThisMonth.setTextColor(getColor(android.R.color.black))
            tabThisYear.background = getDrawable(R.drawable.tab_background_unselected)
            tabThisYear.setTextColor(getColor(R.color.grey))
            updateFilteredTransactions(currentCalendar)
            updateTotals()
            setupExpensePieChart(findViewById(R.id.pie_chart))
            setupIncomePieChart(findViewById(R.id.pie_chart_income))
            setupBarChart(findViewById(R.id.bar_chart))
        }

        tabThisYear.setOnClickListener {
            filterMode = "year"
            tabThisWeek.background = getDrawable(R.drawable.tab_background_unselected)
            tabThisWeek.setTextColor(getColor(R.color.grey))
            tabThisMonth.background = getDrawable(R.drawable.tab_background_unselected)
            tabThisMonth.setTextColor(getColor(R.color.grey))
            tabThisYear.background = getDrawable(R.drawable.tab_background_selected)
            tabThisYear.setTextColor(getColor(android.R.color.black))
            updateFilteredTransactions(currentCalendar)
            updateTotals()
            setupExpensePieChart(findViewById(R.id.pie_chart))
            setupIncomePieChart(findViewById(R.id.pie_chart_income))
            setupBarChart(findViewById(R.id.bar_chart))
        }
    }

    private fun setupExpensePieChart(pieChart: PieChart) {
        // Group expenses by category
        val expenseCategories = filteredTransactions.filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

        // Data for the pie chart
        val entries = ArrayList<PieEntry>()
        expenseCategories.forEach { (category, amount) ->
            if (amount > 0) entries.add(PieEntry(amount, category))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            getColor(R.color.category_food),
            getColor(R.color.category_transport),
            getColor(R.color.category_entertainment),
            getColor(R.color.category_bills),
            getColor(R.color.category_others)
        )
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        pieChart.data = data

        // Customize the pie chart
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.setHoleRadius(50f)
        pieChart.setTransparentCircleRadius(55f)
        pieChart.setCenterTextSize(12f)
        pieChart.setCenterTextColor(getColor(R.color.grey))
        pieChart.invalidate()

        // Update category list
        updateExpenseCategoryList(expenseCategories)
    }

    private fun setupIncomePieChart(pieChart: PieChart) {
        // Group income by category
        val incomeCategories = filteredTransactions.filter { !it.isExpense }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

        // Data for the pie chart
        val entries = ArrayList<PieEntry>()
        incomeCategories.forEach { (category, amount) ->
            if (amount > 0) entries.add(PieEntry(amount, category))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            getColor(R.color.category_salary),
            getColor(R.color.category_freelance),
            getColor(R.color.category_interest_money),
            getColor(R.color.category_gifts),
            getColor(R.color.category_others)
        )
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        pieChart.data = data

        // Customize the pie chart
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.setHoleRadius(50f)
        pieChart.setTransparentCircleRadius(55f)
        pieChart.setCenterTextSize(12f)
        pieChart.setCenterTextColor(getColor(R.color.grey))
        pieChart.invalidate()

        // Update income category list
        updateIncomeCategoryList(incomeCategories)
    }

    private fun updateExpenseCategoryList(expenseCategories: Map<String, Float>) {
        val categoryList: LinearLayout = findViewById(R.id.category_list)

        val totalExpenses = expenseCategories.values.sum()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Updated categories from XML
        val expectedCategories = listOf("Food", "Transport", "Entertainment", "Bills", "Others")

        // Map category to their respective TextView IDs
        val amountIds = mapOf(
            "Food" to R.id.category_amount_food,
            "Transport" to R.id.category_amount_transport,
            "Entertainment" to R.id.category_amount_entertainment,
            "Bills" to R.id.category_amount_bills,
            "Others" to R.id.category_amount_others_expense
        )

        val percentageIds = mapOf(
            "Food" to R.id.category_percentage_food,
            "Transport" to R.id.category_percentage_transport,
            "Entertainment" to R.id.category_percentage_entertainment,
            "Bills" to R.id.category_percentage_bills,
            "Others" to R.id.category_percentage_others_expense
        )

        val dateIds = mapOf(
            "Food" to R.id.category_date_food,
            "Transport" to R.id.category_date_transport,
            "Entertainment" to R.id.category_date_entertainment,
            "Bills" to R.id.category_date_bills,
            "Others" to R.id.category_date_others_expense
        )

        expectedCategories.forEach { category ->
            val amount = expenseCategories[category] ?: 0f
            val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100).toInt() else 0

            // Find the latest transaction date for this category
            val latestTransaction = filteredTransactions
                .filter { it.isExpense && it.category == category }
                .maxByOrNull { it.date.time }

            val dateText = latestTransaction?.let { dateFormat.format(it.date) } ?: "N/A"

            // Find the category row and update its TextViews
            val categoryRow = categoryList.findViewWithTag<LinearLayout>(category)
            if (categoryRow != null) {
                val amountText = categoryRow.findViewById<TextView>(amountIds[category]!!)
                val percentageText = categoryRow.findViewById<TextView>(percentageIds[category]!!)
                val dateTextView = categoryRow.findViewById<TextView>(dateIds[category]!!)

                amountText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", amount)}"
                percentageText.text = "$percentage%"
                dateTextView.text = dateText
            }
        }
    }

    private fun updateIncomeCategoryList(incomeCategories: Map<String, Float>) {
        val incomeCategoryList: LinearLayout = findViewById(R.id.income_category_list)

        val totalIncome = incomeCategories.values.sum()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // Updated categories from XML
        val expectedCategories = listOf("Salary", "Freelance", "Interest Money", "Gifts", "Others")

        // Map category to their respective TextView IDs
        val amountIds = mapOf(
            "Salary" to R.id.category_amount_salary,
            "Freelance" to R.id.category_amount_freelance,
            "Interest Money" to R.id.category_amount_interest,
            "Gifts" to R.id.category_amount_gift,
            "Others" to R.id.category_amount_others
        )

        val percentageIds = mapOf(
            "Salary" to R.id.category_percentage_salary,
            "Freelance" to R.id.category_percentage_freelance,
            "Interest Money" to R.id.category_percentage_interest,
            "Gifts" to R.id.category_percentage_gift,
            "Others" to R.id.category_percentage_others
        )

        val dateIds = mapOf(
            "Salary" to R.id.category_date_salary,
            "Freelance" to R.id.category_date_freelance,
            "Interest Money" to R.id.category_date_interest,
            "Gifts" to R.id.category_date_gifts,
            "Others" to R.id.category_date_others
        )

        expectedCategories.forEach { category ->
            val amount = incomeCategories[category] ?: 0f
            val percentage = if (totalIncome > 0) (amount / totalIncome * 100).toInt() else 0

            // Find the latest transaction date for this category
            val latestTransaction = filteredTransactions
                .filter { !it.isExpense && it.category == category }
                .maxByOrNull { it.date.time }

            val dateText = latestTransaction?.let { dateFormat.format(it.date) } ?: "N/A"

            // Find the category row and update its TextViews
            val categoryRow = incomeCategoryList.findViewWithTag<LinearLayout>(category)
            if (categoryRow != null) {
                val amountText = categoryRow.findViewById<TextView>(amountIds[category]!!)
                val percentageText = categoryRow.findViewById<TextView>(percentageIds[category]!!)
                val dateTextView = categoryRow.findViewById<TextView>(dateIds[category]!!)

                amountText.text = "${sharedPrefs.getCurrency()}${String.format("%.2f", amount)}"
                percentageText.text = "$percentage%"
                dateTextView.text = dateText
            }
        }
    }

    private fun setupBarChart(barChart: BarChart) {
        // Calculate weekly data for the last 4 weeks
        val calendar = Calendar.getInstance()
        val weeks = ArrayList<String>()
        val expenses = ArrayList<BarEntry>()
        val incomes = ArrayList<BarEntry>()

        // Get the last 4 weeks
        for (i in 3 downTo 0) {
            val weekStart = Calendar.getInstance().apply {
                add(Calendar.WEEK_OF_YEAR, -i)
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val weekEnd = Calendar.getInstance().apply {
                time = weekStart.time
                add(Calendar.DAY_OF_WEEK, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            // Filter transactions for this week
            val weekTransactions = transactions.filter {
                val transactionDate = Calendar.getInstance().apply { time = it.date }
                transactionDate.timeInMillis >= weekStart.timeInMillis &&
                        transactionDate.timeInMillis <= weekEnd.timeInMillis
            }

            // Calculate expenses and income for this week
            val weekExpenses = weekTransactions.filter { it.isExpense }.sumOf { it.amount }.toFloat()
            val weekIncome = weekTransactions.filter { !it.isExpense }.sumOf { it.amount }.toFloat()

            expenses.add(BarEntry((3 - i).toFloat(), weekExpenses))
            incomes.add(BarEntry((3 - i).toFloat(), weekIncome))
            weeks.add("Week ${4 - i}")
        }

        // Create datasets
        val expenseDataSet = BarDataSet(expenses, "Expenses")
        expenseDataSet.color = getColor(R.color.red)
        val incomeDataSet = BarDataSet(incomes, "Income")
        incomeDataSet.color = getColor(R.color.green)

        // Group the datasets
        val barData = BarData(expenseDataSet, incomeDataSet)
        barData.barWidth = 0.4f // Width of each bar
        barChart.data = barData

        // Customize the bar chart
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.groupBars(0f, 0.2f, 0.02f) // Group bars with spacing

        // Customize X-axis (Week 1 to Week 4) with a custom ValueFormatter
        barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < weeks.size) weeks[index] else ""
            }
        }
        barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)

        // Customize Y-axis
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false

        barChart.invalidate() // Refresh the chart
    }

    companion object {
        const val BUDGET_REQUEST = 3
    }
}