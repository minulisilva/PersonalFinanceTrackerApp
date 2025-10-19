package com.example.personalfinancetrackerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetrackerapp.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val currency: String,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_transaction_title)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        val tvDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_transaction)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        holder.tvTitle.text = transaction.title.lowercase()
        holder.tvCategory.text = transaction.category.lowercase()
        holder.tvDate.text = dateFormat.format(transaction.date)
        holder.tvAmount.text = if (transaction.isExpense) {
            "-$currency${String.format("%.2f", transaction.amount)}"
        } else {
            "$currency${String.format("%.2f", transaction.amount)}"
        }
        holder.tvAmount.setTextColor(
            holder.itemView.context.getColor(
                if (transaction.isExpense) R.color.red else R.color.green
            )
        )

        holder.btnEdit.setOnClickListener { onEdit(position) }
        holder.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = transactions.size
}