package com.vibes.dsrapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vibes.dsrapp.R
import com.vibes.dsrapp.databinding.FragmentDashboardBinding
import com.vibes.dsrapp.viewmodel.EntryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val vm: EntryViewModel by activityViewModels()

    private var creditTotal = 0.0
    private var debitTotal = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dsrDate = requireActivity()
            .getSharedPreferences("dsr_prefs", android.content.Context.MODE_PRIVATE)
            .getString("dsr_date", "") ?: ""

        // Nicely formatted date header
        if (dsrDate.isNotEmpty()) {
            runCatching {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dsrDate)
                val label = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(parsed!!)
                binding.tvDashboardDate.text = label
            }.onFailure { binding.tvDashboardDate.text = dsrDate }
        }

        // Progress counts — show just the number in the big card
        vm.getRetailerCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountRetailers.text = it.toString()
        }
        vm.getCreditCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountCredit.text = it.toString()
        }
        vm.getDebitCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountDebit.text = it.toString()
        }
        vm.getRemarkCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountRemarks.text = it.toString()
        }

        // Totals + net balance
        vm.getCreditSum(dsrDate).observe(viewLifecycleOwner) { sum ->
            creditTotal = sum ?: 0.0
            binding.tvCreditTotal.text = formatAmount(creditTotal)
            updateNetBalance()
        }
        vm.getDebitSum(dsrDate).observe(viewLifecycleOwner) { sum ->
            debitTotal = sum ?: 0.0
            binding.tvDebitTotal.text = formatAmount(debitTotal)
            updateNetBalance()
        }

        // Quick action cards → navigate to tab
        binding.cardRetailer.setOnClickListener { findNavController().navigate(R.id.nav_retailers) }
        binding.cardCredit.setOnClickListener   { findNavController().navigate(R.id.nav_credit) }
        binding.cardDebit.setOnClickListener    { findNavController().navigate(R.id.nav_debit) }
        binding.cardRemark.setOnClickListener   { findNavController().navigate(R.id.nav_remarks) }

        binding.btnReviewSubmit.setOnClickListener { showSubmitDialog(dsrDate) }
    }

    private fun updateNetBalance() {
        val net = creditTotal - debitTotal
        binding.tvNetBalance.text = formatAmount(net)
        val color = when {
            net > 0 -> resources.getColor(com.vibes.dsrapp.R.color.colorGreen, null)
            net < 0 -> resources.getColor(com.vibes.dsrapp.R.color.colorRed, null)
            else    -> resources.getColor(com.vibes.dsrapp.R.color.colorPrimary, null)
        }
        binding.tvNetBalance.setTextColor(color)
    }

    private fun formatAmount(value: Double): String {
        return "₹%,.0f".format(value)
    }

    private fun showSubmitDialog(dsrDate: String) {
        val net = creditTotal - debitTotal
        AlertDialog.Builder(requireContext())
            .setTitle("Review & Submit")
            .setMessage(
                "Date: $dsrDate\n\n" +
                "Credit Total : ${formatAmount(creditTotal)}\n" +
                "Debit Total  : ${formatAmount(debitTotal)}\n" +
                "Net Balance  : ${formatAmount(net)}\n\n" +
                "This will submit all entries to Google Sheets and clear today's local data.\n\nProceed?"
            )
            .setPositiveButton("SUBMIT") { _, _ -> submitData(dsrDate) }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun submitData(dsrDate: String) {
        binding.btnReviewSubmit.isEnabled = false
        binding.btnReviewSubmit.text = "SUBMITTING…"
        lifecycleScope.launch {
            val result = vm.submitAndClear(dsrDate)
            binding.btnReviewSubmit.isEnabled = true
            binding.btnReviewSubmit.text = "REVIEW & SUBMIT TO GOOGLE SHEETS"
            if (result.isSuccess) {
                val body = result.getOrNull() ?: ""
                // Check if the response itself reports an error
                if (body.contains("\"error\"")) {
                    Toast.makeText(
                        requireContext(),
                        "Server error: $body",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "✓ DSR submitted to Google Sheets!", Toast.LENGTH_LONG).show()
                }
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Unknown error"
                Toast.makeText(requireContext(), "Submit failed: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
