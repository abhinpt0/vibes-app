package com.vibes.dsrapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vibes.dsrapp.databinding.FragmentRetailerEntryBinding
import com.vibes.dsrapp.databinding.ItemRetailerTxnBinding
import com.vibes.dsrapp.model.RetailerTxn
import com.vibes.dsrapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RetailerEntryFragment : Fragment() {

    private var _binding: FragmentRetailerEntryBinding? = null
    private val binding get() = _binding!!
    private val vm: EntryViewModel by activityViewModels()
    private lateinit var dsrDate: String

    private val retailers = listOf(
        "Prompt", "Fantasy", "Falcon", "Cell city", "Fixit",
        "Iswarya statio", "Smartech", "SM online", "Mobile mart",
        "Vibes (Mobicare)", "Sbi Service", "Fono", "Tk Store", "Mobi time", "KKM"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dsrDate = requireActivity()
            .getSharedPreferences("dsr_prefs", android.content.Context.MODE_PRIVATE)
            .getString("dsr_date", "") ?: ""

        // Date chip
        binding.tvRetailerDate.text = dsrDate

        // Retailer spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            retailers
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRetailer.adapter = adapter

        // RecyclerView
        val listAdapter = RetailerTxnAdapter(
            onEdit = { txn -> showEditDialog(txn) },
            onDelete = { txn -> confirmDelete(txn) }
        )
        binding.rvRetailerTxns.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRetailerTxns.adapter = listAdapter

        vm.getRetailerTxns(dsrDate).observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
            binding.tvRetailerEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRetailerTxns.visibility  = if (it.isEmpty()) View.GONE  else View.VISIBLE
        }

        // Add Entry
        binding.btnAddRetailer.setOnClickListener {
            val retailer = binding.spinnerRetailer.selectedItem.toString()
            val forward = binding.etForward.text.toString().toDoubleOrNull() ?: 0.0
            val reverse = binding.etReverse.text.toString().toDoubleOrNull() ?: 0.0
            val pgStock = binding.etPgStock.text.toString().toDoubleOrNull() ?: 0.0
            val credit = binding.etCredit.text.toString().toDoubleOrNull() ?: 0.0

            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            vm.insertRetailer(
                RetailerTxn(
                    dsrDate = dsrDate,
                    time = time,
                    createdAt = System.currentTimeMillis(),
                    retailer = retailer,
                    forward = forward,
                    reverse = reverse,
                    pgStock = pgStock,
                    credit = credit
                )
            )
            clearForm()
            Toast.makeText(requireContext(), "Entry added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        binding.etForward.text?.clear()
        binding.etReverse.text?.clear()
        binding.etPgStock.text?.clear()
        binding.etCredit.text?.clear()
    }

    private fun showEditDialog(txn: RetailerTxn) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(com.vibes.dsrapp.R.layout.fragment_retailer_entry, null)
        val dialogBinding = FragmentRetailerEntryBinding.bind(dialogView)

        val spinAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            retailers
        )
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRetailer.adapter = spinAdapter
        val pos = retailers.indexOf(txn.retailer).coerceAtLeast(0)
        dialogBinding.spinnerRetailer.setSelection(pos)
        dialogBinding.etForward.setText(if (txn.forward != 0.0) txn.forward.toString() else "")
        dialogBinding.etReverse.setText(if (txn.reverse != 0.0) txn.reverse.toString() else "")
        dialogBinding.etPgStock.setText(if (txn.pgStock != 0.0) txn.pgStock.toString() else "")
        dialogBinding.etCredit.setText(if (txn.credit != 0.0) txn.credit.toString() else "")
        dialogBinding.tvRetailerDate.visibility = View.GONE
        dialogBinding.tvRetailerHeader.visibility = View.GONE
        dialogBinding.rvRetailerTxns.visibility = View.GONE
        dialogBinding.btnAddRetailer.visibility = View.GONE

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Entry")
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val updated = txn.copy(
                    retailer = dialogBinding.spinnerRetailer.selectedItem.toString(),
                    forward = dialogBinding.etForward.text.toString().toDoubleOrNull() ?: 0.0,
                    reverse = dialogBinding.etReverse.text.toString().toDoubleOrNull() ?: 0.0,
                    pgStock = dialogBinding.etPgStock.text.toString().toDoubleOrNull() ?: 0.0,
                    credit = dialogBinding.etCredit.text.toString().toDoubleOrNull() ?: 0.0
                )
                vm.updateRetailer(updated)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private fun confirmDelete(txn: RetailerTxn) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Delete the entry for ${txn.retailer}?")
            .setPositiveButton("DELETE") { _, _ -> vm.deleteRetailer(txn) }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    inner class RetailerTxnAdapter(
        private val onEdit: (RetailerTxn) -> Unit,
        private val onDelete: (RetailerTxn) -> Unit
    ) : RecyclerView.Adapter<RetailerTxnAdapter.VH>() {

        private var list: List<RetailerTxn> = emptyList()

        fun submitList(newList: List<RetailerTxn>) {
            list = newList
            notifyDataSetChanged()
        }

        inner class VH(val binding: ItemRetailerTxnBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemRetailerTxnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val txn = list[position]
            val timeFmt = runCatching {
                val parsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(txn.time)
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed!!)
            }.getOrDefault(txn.time)

            holder.binding.tvRetailerName.text = txn.retailer
            holder.binding.tvRetailerTime.text = timeFmt
            val parts = mutableListOf<String>()
            if (txn.forward != 0.0) parts.add("Forward ₹${txn.forward.toLong()}")
            if (txn.reverse != 0.0) parts.add("Reverse ₹${txn.reverse.toLong()}")
            if (txn.pgStock != 0.0) parts.add("PG ₹${txn.pgStock.toLong()}")
            if (txn.credit != 0.0) parts.add("Credit ₹${txn.credit.toLong()}")
            holder.binding.tvRetailerDetails.text = parts.joinToString(" · ")
            holder.binding.btnEditRetailer.setOnClickListener { onEdit(txn) }
            holder.binding.btnDeleteRetailer.setOnClickListener { onDelete(txn) }
        }

        override fun getItemCount() = list.size
    }
}
