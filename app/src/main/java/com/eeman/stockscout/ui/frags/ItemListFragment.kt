package com.eeman.stockscout.ui.frags

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eeman.stockscout.R
import com.eeman.stockscout.app.StockScoutApp
import com.eeman.stockscout.data.models.Item
import com.eeman.stockscout.databinding.FragmentItemListBinding
import com.eeman.stockscout.databinding.ItemRowBinding
import com.eeman.stockscout.ui.vms.ItemListUiState
import com.eeman.stockscout.ui.vms.ItemListViewModel
import com.eeman.stockscout.ui.vms.SearchState
import kotlinx.coroutines.launch

class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private val vm: ItemListViewModel by viewModels {
        val container = (requireActivity().application as StockScoutApp).container
        ItemListViewModel.Factory(container.itemRepository, container.resolveItemUseCase)
    }

    private val adapter = ItemAdapter { item ->
        findNavController().navigate(
            ItemListFragmentDirections.actionItemListToDetail(item.itemCode)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Search bar — keyboard "Search" action or scan button
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                vm.search(binding.searchInput.text.toString())
                true
            } else false
        }

        binding.btnScan.setOnClickListener {
            findNavController().navigate(R.id.scanFragment)
        }

        binding.btnSearch.setOnClickListener {
            vm.search(binding.searchInput.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.uiState.collect(::renderList) }
                launch { vm.searchState.collect(::renderSearch) }
            }
        }
    }

    private fun renderList(state: ItemListUiState) {
        binding.progressBar.isVisible = state is ItemListUiState.Loading
        binding.tvError.isVisible = state is ItemListUiState.Error
        binding.recyclerView.isVisible = state is ItemListUiState.Success

        when (state) {
            is ItemListUiState.Success -> adapter.submitList(state.items)
            is ItemListUiState.Error   -> binding.tvError.text = state.message
            else -> Unit
        }
    }

    private fun renderSearch(state: SearchState) {
        when (state) {
            is SearchState.Found -> {
                findNavController().navigate(
                    ItemListFragmentDirections.actionItemListToDetail(state.item.itemCode)
                )
                vm.clearSearch()
            }
            is SearchState.NotFound -> {
                Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                vm.clearSearch()
            }
            SearchState.Idle -> Unit
        }
    }

    /** Called from ScannerFragment via NavController result */
    override fun onResume() {
        super.onResume()
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("scan_result")
            ?.let { barcode ->
                binding.searchInput.setText(barcode)
                vm.search(barcode)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle?.remove<String>("scan_result")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ── Adapter ──────────────────────────────────────────────────────────────────

private class ItemAdapter(
    private val onClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Item) {
            binding.tvCode.text  = item.itemCode
            binding.tvName.text  = item.name
            binding.tvQty.text   = "Qty: ${item.onHandQty} ${item.uom}"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(a: Item, b: Item) = a.itemCode == b.itemCode
            override fun areContentsTheSame(a: Item, b: Item) = a == b
        }
    }
}