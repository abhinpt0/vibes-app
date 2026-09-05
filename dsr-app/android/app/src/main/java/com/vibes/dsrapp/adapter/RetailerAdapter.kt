package com.vibes.dsrapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vibes.dsrapp.databinding.ItemRetailerBinding
import com.vibes.dsrapp.model.RetailerEntry

class RetailerAdapter : ListAdapter<RetailerEntry, RetailerAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemRetailerBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: RetailerEntry) {
            b.tvRetName.text   = r.retName
            b.tvOpening.text   = "Opening: ₹%.0f".format(r.opening)
            b.tvForward.text   = "Forward: ₹%.0f".format(r.forward)
            b.tvReverse.text   = "Reverse: ₹%.0f".format(r.reverse)
            b.tvPgSock.text    = "PG Sock/Cash: ₹%.0f".format(r.pgSockFwdCashPaid)
            b.tvCredit.text    = "Credit: ₹%.0f".format(r.credit)

            val balance = r.balance
            b.tvBalance.text   = "Balance: ₹%.0f".format(balance)
            b.tvBalance.setTextColor(
                if (balance >= 0) 0xFF1B7A3F.toInt() else 0xFFB00020.toInt()
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRetailerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<RetailerEntry>() {
            override fun areItemsTheSame(a: RetailerEntry, b: RetailerEntry) = a.retName == b.retName
            override fun areContentsTheSame(a: RetailerEntry, b: RetailerEntry) = a == b
        }
    }
}
