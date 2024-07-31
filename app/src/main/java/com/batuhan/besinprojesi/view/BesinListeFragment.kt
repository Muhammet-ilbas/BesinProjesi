package com.batuhan.besinprojesi.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.batuhan.besinprojesi.adapter.BesinRecyclerAdapter
import com.batuhan.besinprojesi.databinding.FragmentBesinListeBinding
import com.batuhan.besinprojesi.viewModel.BesinListesiViewModel

class BesinListeFragment : Fragment() {
    private var _binding: FragmentBesinListeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : BesinListesiViewModel
    private val besinRecyclerAdapter = BesinRecyclerAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBesinListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =ViewModelProvider(this)[BesinListesiViewModel::class.java]
        viewModel.refreshData()

        binding.besinListRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.besinListRecycler.adapter = besinRecyclerAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.besinHataMesaji.visibility =View.GONE
            binding.besinListRecycler.visibility =View.GONE
            binding.besinYukleniyor.visibility =View.VISIBLE
            viewModel.refreshDataFromInternet()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        obsorveLiveData()
    }
    private fun obsorveLiveData(){
        viewModel.besinler.observe(viewLifecycleOwner){
            //adapter
            besinRecyclerAdapter.besinListesiGuncelle(it)
            binding.besinListRecycler.visibility = View.VISIBLE
        }
        viewModel.besinHataMesaji.observe(viewLifecycleOwner){
            if (it){
                binding.besinHataMesaji.visibility = View.VISIBLE
                binding.besinListRecycler.visibility = View.GONE
            }else{
                binding.besinHataMesaji.visibility = View.GONE
            }
        }
        viewModel.besinYukleniyor.observe(viewLifecycleOwner){
            if (it){
            binding.besinHataMesaji.visibility =View.GONE
            binding.besinListRecycler.visibility =View.GONE
            binding.besinYukleniyor.visibility =View.VISIBLE
            }else{
                binding.besinYukleniyor.visibility = View.GONE
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
