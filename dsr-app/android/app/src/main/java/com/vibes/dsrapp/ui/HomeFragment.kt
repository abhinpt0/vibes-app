package com.vibes.dsrapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibes.dsrapp.adapter.DsrListAdapter
import com.vibes.dsrapp.databinding.FragmentHomeBinding
import com.vibes.dsrapp.viewmodel.DsrViewModel
import com.vibes.dsrapp.viewmodel.UiState

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DsrViewModel by activityViewModels()
    private val adapter = DsrListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadDSRList() }

        viewModel.dsrList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
                    binding.tvEmpty.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.swipeRefresh.isRefreshing = false
                    if (state.data.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        adapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.tvEmpty.text = "Error: ${state.message}"
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }
        }

        viewModel.loadDSRList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
