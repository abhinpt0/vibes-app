package com.vibes.dsrapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vibes.dsrapp.databinding.ItemDsrBinding
import com.vibes.dsrapp.model.DsrEntry

class DsrListAdapter : ListAdapter<DsrEntry, DsrListAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemDsrBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(entry: DsrEntry) {
            b.tvDate.text = entry.date
            b.tvTotalCredit.text  = "Credit:  ₹%.2f".format(entry.totalCredit)
            b.tvTotalDebit.text   = "Debit:   ₹%.2f".format(entry.totalDebit)
            b.tvClosingBal.text   = "Closing: ₹%.2f".format(entry.closingBalance)
            b.tvCashDiff.text     = "Cash Diff: ₹%.2f".format(entry.cashDifference)
            b.tvRemarks.text      = entry.remarks.ifBlank { "—" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDsrBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DsrEntry>() {
            override fun areItemsTheSame(a: DsrEntry, b: DsrEntry) = a.date == b.date
            override fun areContentsTheSame(a: DsrEntry, b: DsrEntry) = a == b
        }
    }
}
