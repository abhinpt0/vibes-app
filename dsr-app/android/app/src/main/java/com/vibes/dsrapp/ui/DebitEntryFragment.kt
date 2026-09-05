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
import com.vibes.dsrapp.databinding.FragmentDebitEntryBinding
import com.vibes.dsrapp.databinding.ItemDebitTxnBinding
import com.vibes.dsrapp.model.DebitTxn
import com.vibes.dsrapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DebitEntryFragment : Fragment() {

    private var _binding: FragmentDebitEntryBinding? = null
    private val binding get() = _binding!!
    private val vm: EntryViewModel by activityViewModels()
    private lateinit var dsrDate: String

    private val particulars = listOf(
        "Reverse From Retailer",
        "UPI Transfer",
        "Vibes Deposit – BOB",
        "Vibes Deposit – SBI 5859",
        "Vibes Deposit – Canara",
        "PayBingo SBI Deposit (SBI 3696)",
        "PayBingo Deposit – PNB",
        "PayBingo SBI Deposit – HDFC",
        "PayBingo Deposit – Online Stock",
        "PayBingo Deposit – Other",
        "Cash Paid Against Reverse",
        "UPI Against Reverse",
        "PG Stock Adjust – Vibes",
        "Other"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebitEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dsrDate = requireActivity()
            .getSharedPreferences("dsr_prefs", android.content.Context.MODE_PRIVATE)
            .getString("dsr_date", "") ?: ""

        binding.tvDebitDate.text = dsrDate

        val spinAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            particulars
        )
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDebitParticular.adapter = spinAdapter

        binding.spinnerDebitParticular.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long
                ) {
                    binding.tilDebitCustomParticular.visibility =
                        if (particulars[pos] == "Other") View.VISIBLE else View.GONE
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        val listAdapter = DebitTxnAdapter(
            onEdit = { txn -> showEditDialog(txn) },
            onDelete = { txn -> confirmDelete(txn) }
        )
        binding.rvDebitTxns.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDebitTxns.adapter = listAdapter

        vm.getDebitTxns(dsrDate).observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
            binding.tvDebitEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.rvDebitTxns.visibility  = if (it.isEmpty()) View.GONE  else View.VISIBLE
        }

        binding.btnAddDebit.setOnClickListener {
            val selectedItem = binding.spinnerDebitParticular.selectedItem.toString()
            val particular = if (selectedItem == "Other") {
                val custom = binding.etDebitCustomParticular.text.toString().trim()
                if (custom.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter custom particular", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                custom
            } else {
                selectedItem
            }
            val amount = binding.etDebitAmount.text.toString().toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(requireContext(), "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            vm.insertDebit(
                DebitTxn(
                    dsrDate = dsrDate,
                    time = time,
                    createdAt = System.currentTimeMillis(),
                    particular = particular,
                    amount = amount
                )
            )
            binding.etDebitAmount.text?.clear()
            binding.etDebitCustomParticular.text?.clear()
            Toast.makeText(requireContext(), "Debit added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(txn: DebitTxn) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(com.vibes.dsrapp.R.layout.fragment_debit_entry, null)
        val db = FragmentDebitEntryBinding.bind(dialogView)

        val spinAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            particulars
        )
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        db.spinnerDebitParticular.adapter = spinAdapter

        val pos = particulars.indexOf(txn.particular)
        if (pos >= 0) {
            db.spinnerDebitParticular.setSelection(pos)
        } else {
            db.spinnerDebitParticular.setSelection(particulars.size - 1)
            db.tilDebitCustomParticular.visibility = View.VISIBLE
            db.etDebitCustomParticular.setText(txn.particular)
        }
        db.etDebitAmount.setText(txn.amount.toString())

        db.tvDebitDate.visibility = View.GONE
        db.tvDebitHeader.visibility = View.GONE
        db.tvDebitSubtitle.visibility = View.GONE
        db.rvDebitTxns.visibility = View.GONE
        db.btnAddDebit.visibility = View.GONE

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Debit")
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val selectedItem = db.spinnerDebitParticular.selectedItem.toString()
                val particular = if (selectedItem == "Other") {
                    db.etDebitCustomParticular.text.toString().trim().ifEmpty { selectedItem }
                } else selectedItem
                val amount = db.etDebitAmount.text.toString().toDoubleOrNull() ?: txn.amount
                vm.updateDebit(txn.copy(particular = particular, amount = amount))
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private fun confirmDelete(txn: DebitTxn) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Delete debit entry: ${txn.particular} — ₹${txn.amount.toLong()}?")
            .setPositiveButton("DELETE") { _, _ -> vm.deleteDebit(txn) }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    inner class DebitTxnAdapter(
        private val onEdit: (DebitTxn) -> Unit,
        private val onDelete: (DebitTxn) -> Unit
    ) : RecyclerView.Adapter<DebitTxnAdapter.VH>() {

        private var list: List<DebitTxn> = emptyList()

        fun submitList(newList: List<DebitTxn>) {
            list = newList
            notifyDataSetChanged()
        }

        inner class VH(val binding: ItemDebitTxnBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemDebitTxnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val txn = list[position]
            val timeFmt = runCatching {
                val parsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(txn.time)
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed!!)
            }.getOrDefault(txn.time)

            holder.binding.tvDebitTime.text = timeFmt
            holder.binding.tvDebitParticular.text = txn.particular
            holder.binding.tvDebitAmount.text = "₹%,.0f".format(txn.amount)
            holder.binding.btnEditDebit.setOnClickListener { onEdit(txn) }
            holder.binding.btnDeleteDebit.setOnClickListener { onDelete(txn) }
        }

        override fun getItemCount() = list.size
    }
}
