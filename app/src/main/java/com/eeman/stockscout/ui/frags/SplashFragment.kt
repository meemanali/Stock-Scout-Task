package com.eeman.stockscout.ui.frags

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.eeman.stockscout.R
import com.eeman.stockscout.app.StockScoutApp
import com.eeman.stockscout.databinding.FragmentSplashBinding
import com.eeman.stockscout.ui.vms.SplashUiState
import com.eeman.stockscout.ui.vms.SplashViewModel
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val vm: SplashViewModel by viewModels {
        val container = (requireActivity().application as StockScoutApp).container
        SplashViewModel.Factory(container.itemRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    when (state) {
                        SplashUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.tvError.isVisible = false
                        }

                        SplashUiState.Done -> {
                            // Navigate immediately — ItemListViewModel will just observe warm cache
                            findNavController().navigate(R.id.itemListFragment)
                        }

                        is SplashUiState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.tvError.isVisible = true
                            binding.tvError.text =
                                "Sync failed: ${state.message}\nStarting offline…"
                            // Still navigate after a short delay — Room may have cached data
                            binding.root.postDelayed({
                                if (isAdded) findNavController().navigate(R.id.itemListFragment)
                            }, 2_000)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
