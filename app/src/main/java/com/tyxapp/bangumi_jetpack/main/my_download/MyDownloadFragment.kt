package com.tyxapp.bangumi_jetpack.main.my_download

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.tyxapp.bangumi_jetpack.R

class MyDownloadFragment : Fragment() {

    companion object {
        fun newInstance() = MyDownloadFragment()
    }

    private lateinit var viewModel: MyDownloadViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.my_download_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MyDownloadViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
