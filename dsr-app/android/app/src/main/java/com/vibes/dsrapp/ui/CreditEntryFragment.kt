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
import com.vibes.dsrapp.databinding.FragmentCreditEntryBinding
import com.vibes.dsrapp.databinding.ItemCreditTxnBinding
import com.vibes.dsrapp.model.CreditTxn
import com.vibes.dsrapp.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreditEntryFragment : Fragment() {

    private var _binding: FragmentCreditEntryBinding? = null
    private val binding get() = _binding!!
    private val vm: EntryViewModel by activityViewModels()
    private lateinit var dsrDate: String

    private val particulars = listOf(
        "Opening Cash",
        "Jio Collection – Aswanth (Onchiam Market)",
        "Jio Collection – TMK + CK",
        "PayBingo Collection",
        "PG Charge",
        "PayBingo ID Sale",
        "PayBingo Online Transfer",
        "Self Deposit (CDM SBI)",
        "Other"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dsrDate = requireActivity()
            .getSharedPreferences("dsr_prefs", android.content.Context.MODE_PRIVATE)
            .getString("dsr_date", "") ?: ""

        binding.tvCreditDate.text = dsrDate

        val spinAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            particulars
        )
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCreditParticular.adapter = spinAdapter

        binding.spinnerCreditParticular.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long
                ) {
                    binding.tilCreditCustomParticular.visibility =
                        if (particulars[pos] == "Other") View.VISIBLE else View.GONE
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        val listAdapter = CreditTxnAdapter(
            onEdit = { txn -> showEditDialog(txn) },
            onDelete = { txn -> vm.deleteCredit(txn) }
        )
        binding.rvCreditTxns.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCreditTxns.adapter = listAdapter

        vm.getCreditTxns(dsrDate).observe(viewLifecycleOwner) { listAdapter.submitList(it) }

        binding.btnAddCredit.setOnClickListener {
            val selectedItem = binding.spinnerCreditParticular.selectedItem.toString()
            val particular = if (selectedItem == "Other") {
                val custom = binding.etCreditCustomParticular.text.toString().trim()
                if (custom.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter custom particular", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                custom
            } else {
                selectedItem
            }
            val amount = binding.etCreditAmount.text.toString().toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(requireContext(), "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            vm.insertCredit(
                CreditTxn(
                    dsrDate = dsrDate,
                    time = time,
                    createdAt = System.currentTimeMillis(),
                    particular = particular,
                    amount = amount
                )
            )
            binding.etCreditAmount.text?.clear()
            binding.etCreditCustomParticular.text?.clear()
            Toast.makeText(requireContext(), "Credit added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(txn: CreditTxn) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(com.vibes.dsrapp.R.layout.fragment_credit_entry, null)
        val db = FragmentCreditEntryBinding.bind(dialogView)

        val spinAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            particulars
        )
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        db.spinnerCreditParticular.adapter = spinAdapter

        val pos = particulars.indexOf(txn.particular)
        if (pos >= 0) {
            db.spinnerCreditParticular.setSelection(pos)
        } else {
            db.spinnerCreditParticular.setSelection(particulars.size - 1) // "Other"
            db.tilCreditCustomParticular.visibility = View.VISIBLE
            db.etCreditCustomParticular.setText(txn.particular)
        }
        db.etCreditAmount.setText(txn.amount.toString())

        db.tvCreditDate.visibility = View.GONE
        db.tvCreditHeader.visibility = View.GONE
        db.tvCreditSubtitle.visibility = View.GONE
        db.rvCreditTxns.visibility = View.GONE
        db.btnAddCredit.visibility = View.GONE

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Credit")
            .setView(dialogView)
            .setPositiveButton("SAVE") { _, _ ->
                val selectedItem = db.spinnerCreditParticular.selectedItem.toString()
                val particular = if (selectedItem == "Other") {
                    db.etCreditCustomParticular.text.toString().trim().ifEmpty { selectedItem }
                } else selectedItem
                val amount = db.etCreditAmount.text.toString().toDoubleOrNull() ?: txn.amount
                vm.updateCredit(txn.copy(particular = particular, amount = amount))
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    inner class CreditTxnAdapter(
        private val onEdit: (CreditTxn) -> Unit,
        private val onDelete: (CreditTxn) -> Unit
    ) : RecyclerView.Adapter<CreditTxnAdapter.VH>() {

        private var list: List<CreditTxn> = emptyList()

        fun submitList(newList: List<CreditTxn>) {
            list = newList
            notifyDataSetChanged()
        }

        inner class VH(val binding: ItemCreditTxnBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemCreditTxnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val txn = list[position]
            val timeFmt = runCatching {
                val parsed = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(txn.time)
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed!!)
            }.getOrDefault(txn.time)

            holder.binding.tvCreditTime.text = timeFmt
            holder.binding.tvCreditParticular.text = txn.particular
            holder.binding.tvCreditAmount.text = "₹%,.0f".format(txn.amount)
            holder.binding.btnEditCredit.setOnClickListener { onEdit(txn) }
            holder.binding.btnDeleteCredit.setOnClickListener { onDelete(txn) }
        }

        override fun getItemCount() = list.size
    }
}
