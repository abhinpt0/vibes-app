package com.vibes.dsrapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibes.dsrapp.adapter.RetailerAdapter
import com.vibes.dsrapp.databinding.FragmentRetailersBinding
import com.vibes.dsrapp.viewmodel.DsrViewModel
import com.vibes.dsrapp.viewmodel.UiState

class RetailersFragment : Fragment() {

    private var _binding: FragmentRetailersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DsrViewModel by activityViewModels()
    private val adapter = RetailerAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRetailersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadRetailers() }

        viewModel.retailers.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> binding.swipeRefresh.isRefreshing = true
                is UiState.Success -> {
                    binding.swipeRefresh.isRefreshing = false
                    adapter.submitList(state.data)
                }
                is UiState.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        viewModel.loadRetailers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
