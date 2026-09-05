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

        // Display nicely formatted date
        if (dsrDate.isNotEmpty()) {
            runCatching {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dsrDate)
                val label = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(parsed!!)
                binding.tvDashboardDate.text = label
            }
        }

        // Observe counts
        vm.getRetailerCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountRetailers.text = "Retailers: $it entries"
        }
        vm.getCreditCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountCredit.text = "Credit: $it entries"
        }
        vm.getDebitCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountDebit.text = "Debit: $it entries"
        }
        vm.getRemarkCount(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCountRemarks.text = "Remarks: $it entries"
        }

        // Observe totals
        vm.getCreditSum(dsrDate).observe(viewLifecycleOwner) {
            binding.tvCreditTotal.text = "₹%.2f".format(it ?: 0.0)
        }
        vm.getDebitSum(dsrDate).observe(viewLifecycleOwner) {
            binding.tvDebitTotal.text = "₹%.2f".format(it ?: 0.0)
        }

        // Quick action cards
        binding.cardRetailer.setOnClickListener {
            findNavController().navigate(R.id.nav_retailers)
        }
        binding.cardCredit.setOnClickListener {
            findNavController().navigate(R.id.nav_credit)
        }
        binding.cardDebit.setOnClickListener {
            findNavController().navigate(R.id.nav_debit)
        }
        binding.cardRemark.setOnClickListener {
            findNavController().navigate(R.id.nav_remarks)
        }

        // Submit button
        binding.btnReviewSubmit.setOnClickListener {
            showSubmitDialog(dsrDate)
        }
    }

    private fun showSubmitDialog(dsrDate: String) {
        val creditTotal = binding.tvCreditTotal.text
        val debitTotal = binding.tvDebitTotal.text
        AlertDialog.Builder(requireContext())
            .setTitle("Review & Submit")
            .setMessage(
                "DSR Date: $dsrDate\n\n" +
                        "Credit Total: $creditTotal\n" +
                        "Debit Total: $debitTotal\n\n" +
                        "Submit all entries to the server and clear today's data?"
            )
            .setPositiveButton("SUBMIT") { _, _ ->
                submitData(dsrDate)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun submitData(dsrDate: String) {
        binding.btnReviewSubmit.isEnabled = false
        lifecycleScope.launch {
            val result = vm.submitAndClear(dsrDate)
            binding.btnReviewSubmit.isEnabled = true
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "DSR submitted successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Submit failed: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
