package com.example.personalfinancetrackerapp.model

import java.io.Serializable
import java.util.Date

data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val isExpense: Boolean = true
) : Serializable