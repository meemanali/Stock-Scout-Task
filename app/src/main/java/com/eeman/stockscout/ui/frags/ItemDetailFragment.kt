package com.eeman.stockscout.ui.frags

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.eeman.stockscout.app.StockScoutApp
import com.eeman.stockscout.databinding.FragmentItemDetailBinding
import com.eeman.stockscout.ui.vms.DetailUiState
import com.eeman.stockscout.ui.vms.ItemDetailViewModel
import kotlinx.coroutines.launch

class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ItemDetailFragmentArgs by navArgs()

    private val vm: ItemDetailViewModel by viewModels {
        val container = (requireActivity().application as StockScoutApp).container
        ItemDetailViewModel.Factory(
            args.itemCode,
            container.itemRepository,
            container.pickRepository,
            container.pickItemUseCase
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPick.setOnClickListener { vm.pick() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.uiState.collect(::render) }
                launch {
                    vm.pickDone.collect { done ->
                        if (done) Toast.makeText(requireContext(), "Pick recorded!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun render(state: DetailUiState) {
        binding.progressBar.isVisible = state is DetailUiState.Loading
        binding.groupContent.isVisible = state is DetailUiState.Success
        binding.tvNotFound.isVisible   = state is DetailUiState.NotFound

        if (state !is DetailUiState.Success) return
        val item = state.item

        binding.tvCode.text    = item.itemCode
        binding.tvName.text    = item.name
        binding.tvUom.text     = "UOM: ${item.uom}"
        binding.tvQty.text     = "On Hand: ${item.onHandQty}"

        // Aliases chip list
        binding.tvAliases.text = item.aliases.joinToString("\n") { "[${it.type}] ${it.value}" }

        // Sync badge
        val pending = state.pendingCount
        binding.tvSyncStatus.text = if (pending == 0) {
            "✓ All picks synced"
        } else {
            "⏳ $pending pick(s) pending sync"
        }
        binding.tvSyncStatus.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}