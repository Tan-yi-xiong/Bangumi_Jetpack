package com.tyxapp.bangumi_jetpack.main.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.adapter.SearchHelperAdapter
import com.tyxapp.bangumi_jetpack.main.viewmodels.SearchHelperViewModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.SearchViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils

class SearchHelperFragment : ListFragment() {

    private val parentViewModel by viewModels<SearchViewModel>(ownerProducer = { parentFragment!! })
    private val viewModel: SearchHelperViewModel by viewModels {
        InjectorUtils.provideSearchHelperViewModelFactory()
    }

    private val editText: EditText by lazy {
        parentFragment!!.view!!.findViewById<EditText>(R.id.editText)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            initView()
            addObserver()
        }
    }

    private fun initView() {
        mRecyclerView.adapter = SearchHelperAdapter(viewModel)

        with(editText) {

            addTextChangedListener(onTextChanged = { s: CharSequence?, _, _, _ ->
                viewModel.onTextChange(s.toString())
            })

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val word = editText.text.toString()
                    viewModel.saveSearchWord(word)//保存搜索到历史搜索
                    parentViewModel.searchWord.value = word
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun addObserver() {
        viewModel.searchWords.observe {
            (mRecyclerView.adapter as SearchHelperAdapter).submitList(it)
            if (bind.showContent != true) {
                showContent()
            }
        }

        viewModel.clickSearchWord.observe {
            with(editText) {
                setText(it.word)
                setSelection(it.word.length)
                clearFocus()
            }
            parentViewModel.searchWord.value = it.word
        }
    }

    override fun onResume() {
        super.onResume()
        if (!editText.isFocused) {
            editText.requestFocus()
        }
        showKeyBoard()
    }

    override fun onPause() {
        super.onPause()
        if (editText.isFocused) {
            editText.clearFocus()
        }
        hideKeyBoard()
    }

    private fun hideKeyBoard() {
        val inputService = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputService.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun showKeyBoard() {
        val inputService = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputService.showSoftInput(editText, 0)
    }

    companion object {
        fun newInstance(): SearchHelperFragment = SearchHelperFragment()
    }
}