package com.tyxapp.bangumi_jetpack.main.categorydetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ListFragment

class CategoryDetailFragment : ListFragment() {

    private val categoryArgs by navArgs<CategoryDetailFragmentArgs>()
    private lateinit var viewModel: CategoryDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Toast.makeText(requireContext(), categoryArgs.categoryWord, Toast.LENGTH_SHORT).show()
        return inflater.inflate(R.layout.category_detail_fragment, container, false)
    }


}
