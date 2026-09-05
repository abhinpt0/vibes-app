package com.vibes.dsrapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vibes.dsrapp.databinding.FragmentRemarksEntryBinding
import com.vibes.dsrapp.databinding.ItemRemarkTxnBinding
import com.vibes.dsrapp.model.RemarkTxn
import com.vibes.dsrapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RemarksEntryFragment : Fragment() {

    private var _binding: FragmentRemarksEntryBinding? = null
    private val binding get() = _binding!!
    private val vm: EntryViewModel by activityViewModels()
    private lateinit var dsrDate: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemarksEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dsrDate = requireActivity()
            .getSharedPreferences("dsr_prefs", android.content.Context.MODE_PRIVATE)
            .getString("dsr_date", "") ?: ""

        binding.tvRemarksDate.text = dsrDate

        val listAdapter = RemarkTxnAdapter(
            onDelete = { txn -> vm.deleteRemark(txn) }
        )
        binding.rvRemarkTxns.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRemarkTxns.adapter = listAdapter

        vm.getRemarkTxns(dsrDate).observe(viewLifecycleOwner) { listAdapter.submitList(it) }

        binding.btnAddRemark.setOnClickListener {
            val remark = binding.etRemark.text.toString().trim()
            if (remark.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a remark", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = binding.etRemarkAmount.text.toString().toDoubleOrNull() ?: 0.0
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            vm.insertRemark(
                RemarkTxn(
                    dsrDate = dsrDate,
                    time = time,
                    createdAt = System.currentTimeMillis(),
                    remark = remark,
                    amount = amount
                )
            )
            binding.etRemark.text?.clear()
            binding.etRemarkAmount.text?.clear()
            Toast.makeText(requireContext(), "Remark added", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    inner class RemarkTxnAdapter(
        private val onDelete: (RemarkTxn) -> Unit
    ) : RecyclerView.Adapter<RemarkTxnAdapter.VH>() {

        private var list: List<RemarkTxn> = emptyList()

        fun submitList(newList: List<RemarkTxn>) {
            list = newList
            notifyDataSetChanged()
        }

        inner class VH(val binding: ItemRemarkTxnBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemRemarkTxnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val txn = list[position]
            val timeFmt = runCatching {
                val parsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(txn.time)
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed!!)
            }.getOrDefault(txn.time)

            holder.binding.tvRemarkTime.text = timeFmt
            holder.binding.tvRemarkText.text = txn.remark
            holder.binding.tvRemarkAmount.text = if (txn.amount != 0.0) "₹%,.0f".format(txn.amount) else ""
            holder.binding.btnDeleteRemark.setOnClickListener { onDelete(txn) }
        }

        override fun getItemCount() = list.size
    }
}
