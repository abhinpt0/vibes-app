package com.vibes.dsrapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputEditText
import com.vibes.dsrapp.R
import com.vibes.dsrapp.databinding.FragmentEntryBinding
import com.vibes.dsrapp.databinding.ItemRetailerTransactionBinding
import com.vibes.dsrapp.model.DsrEntry
import com.vibes.dsrapp.model.RetailerTransaction
import com.vibes.dsrapp.viewmodel.DsrViewModel
import com.vibes.dsrapp.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

class EntryFragment : Fragment() {

    private var _binding: FragmentEntryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DsrViewModel by activityViewModels()
    private var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val retailers = listOf(
        "Prompt", "Fantasy", "Falcon", "Cell city", "Fixit",
        "Iswarya statio", "Smartech", "SM online", "Mobile mart",
        "Vibes (Mobicare)", "Sbi Service", "Fono", "Tk Store", "Mobi time", "KKM"
    )

    // Holds a reference to each retailer row's binding so we can read values on submit
    private val retailerRowBindings = mutableListOf<ItemRetailerTransactionBinding>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Date picker
        binding.btnPickDate.text = selectedDate
        binding.btnPickDate.setOnClickListener { showDatePicker() }

        // Add retailer row
        binding.btnAddRetailerRow.setOnClickListener { addRetailerRow() }

        // Submit
        binding.btnSubmit.setOnClickListener { submitForm() }

        // Observe submit result
        viewModel.submitResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.btnSubmit.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "✅ DSR saved successfully!", Toast.LENGTH_LONG).show()
                    clearForm()
                }
                is UiState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "❌ ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addRetailerRow() {
        val rowBinding = ItemRetailerTransactionBinding.inflate(
            LayoutInflater.from(requireContext()), binding.retailerRowsContainer, false
        )

        // Populate spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, retailers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rowBinding.spinnerRetailer.adapter = adapter

        // Remove button
        rowBinding.btnRemoveRow.setOnClickListener {
            binding.retailerRowsContainer.removeView(rowBinding.root)
            retailerRowBindings.remove(rowBinding)
        }

        binding.retailerRowsContainer.addView(rowBinding.root)
        retailerRowBindings.add(rowBinding)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
            binding.btnPickDate.text = selectedDate
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun d(text: String?) = text?.toDoubleOrNull() ?: 0.0
    private fun i(text: String?) = text?.toIntOrNull() ?: 0

    private fun collectRetailerRows(): List<RetailerTransaction> =
        retailerRowBindings.map { r ->
            RetailerTransaction(
                date      = selectedDate,
                retailer  = r.spinnerRetailer.selectedItem?.toString() ?: "",
                forward   = d(r.etForward.text.toString()),
                reverse   = d(r.etReverse.text.toString()),
                pgStock   = d(r.etPgStock.text.toString()),
                credit    = d(r.etCredit.text.toString())
            )
        }

    private fun submitForm() {
        val b = binding
        val entry = DsrEntry(
            date                       = selectedDate,
            openingCash                = d(b.etOpeningCash.text.toString()),
            jioCollectionAswanth       = d(b.etJioAswanth.text.toString()),
            jioCollectionTmkCk         = d(b.etJioTmk.text.toString()),
            paybingoCollection         = d(b.etPaybingoCollection.text.toString()),
            pgCharge                   = d(b.etPgCharge.text.toString()),
            paybingoIdSale             = d(b.etPaybingoIdSale.text.toString()),
            paybingoOnlineTransfer     = d(b.etPaybingoOnline.text.toString()),
            selfDeposit                = d(b.etSelfDeposit.text.toString()),
            reverseFromRetailer        = d(b.etReverseFromRetailer.text.toString()),
            upiTransfer                = d(b.etUpiTransfer.text.toString()),
            vibesDepositBob            = d(b.etVibesBob.text.toString()),
            vibesDepositSbi            = d(b.etVibesSbi.text.toString()),
            vibesDepositCanara         = d(b.etVibesCanara.text.toString()),
            paybingoSbiDepositSbi3696  = d(b.etPbSbi3696.text.toString()),
            paybingoDepositPnb         = d(b.etPbPnb.text.toString()),
            paybingoSbiDepositHdfc     = d(b.etPbHdfc.text.toString()),
            paybingoDepositOnlineStock = d(b.etPbOnlineStock.text.toString()),
            paybingoDepositOther       = d(b.etPbOther.text.toString()),
            cashPaidAgainstReverse     = d(b.etCashPaidReverse.text.toString()),
            upiAgainstReverse          = d(b.etUpiReverse.text.toString()),
            pgStockAdjustVibes         = d(b.etPgStockVibes.text.toString()),
            notes500                   = i(b.etNotes500.text.toString()),
            notes200                   = i(b.etNotes200.text.toString()),
            notes100                   = i(b.etNotes100.text.toString()),
            notes50                    = i(b.etNotes50.text.toString()),
            notes20                    = i(b.etNotes20.text.toString()),
            notes10                    = i(b.etNotes10.text.toString()),
            notes5                     = i(b.etNotes5.text.toString()),
            odReceived                 = d(b.etOdReceived.text.toString()),
            valueReceived              = d(b.etValueReceived.text.toString()),
            odSettlement               = d(b.etOdSettlement.text.toString()),
            remarks                    = b.etRemarks.text.toString(),
            retailerTransactions       = collectRetailerRows()
        )
        viewModel.submitDSR(entry)
    }

    private fun clearForm() {
        val fields = listOf(
            binding.etOpeningCash, binding.etJioAswanth, binding.etJioTmk,
            binding.etPaybingoCollection, binding.etPgCharge, binding.etPaybingoIdSale,
            binding.etPaybingoOnline, binding.etSelfDeposit, binding.etReverseFromRetailer,
            binding.etUpiTransfer, binding.etVibesBob, binding.etVibesSbi,
            binding.etVibesCanara, binding.etPbSbi3696, binding.etPbPnb, binding.etPbHdfc,
            binding.etPbOnlineStock, binding.etPbOther, binding.etCashPaidReverse,
            binding.etUpiReverse, binding.etPgStockVibes,
            binding.etNotes500, binding.etNotes200, binding.etNotes100,
            binding.etNotes50, binding.etNotes20, binding.etNotes10, binding.etNotes5,
            binding.etOdReceived, binding.etValueReceived, binding.etOdSettlement,
            binding.etRemarks
        )
        fields.forEach { it.text?.clear() }
        // Clear retailer rows
        binding.retailerRowsContainer.removeAllViews()
        retailerRowBindings.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
