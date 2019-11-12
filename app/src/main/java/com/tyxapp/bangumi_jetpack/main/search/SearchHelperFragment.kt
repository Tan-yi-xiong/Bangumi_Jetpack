package com.tyxapp.bangumi_jetpack.main.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.AppBarLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.search.adapter.SearchHelperAdapter
import com.tyxapp.bangumi_jetpack.main.search.viewmodels.SearchHelperViewModel
import com.tyxapp.bangumi_jetpack.main.search.viewmodels.SearchViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils

class SearchHelperFragment : ListFragment() {

    private val parentViewModel by viewModels<SearchViewModel>(ownerProducer = { parentFragment!! })
    private val viewModel: SearchHelperViewModel by viewModels {
        InjectorUtils.provideSearchHelperViewModelFactory()
    }

    private val editText: EditText by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.view!!.findViewById<EditText>(R.id.editText)
    }

    private val clearButton: ImageButton by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.view!!.findViewById<ImageButton>(R.id.clear_text)
    }

    private val appBarLayout: AppBarLayout by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.view!!.findViewById<AppBarLayout>(R.id.appBarLayout)
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
        if (appbarElevation == 0f) {
            appBarLayout.post {
                appbarElevation =  appBarLayout.elevation
            }
        }

        mRecyclerView.adapter =
            SearchHelperAdapter(viewModel)
        clearButton.setOnClickListener {
            editText.setText("")
            if (!editText.isFocused) {
                editText.requestFocus()
            }
        }

        with(editText) {

            addTextChangedListener(onTextChanged = { s: CharSequence?, _, _, _ ->
                viewModel.onTextChange(s.toString())
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            })

            setOnEditorActionListener { _, actionId, _ ->
                val text = editText.text.toString()
                if (actionId == EditorInfo.IME_ACTION_SEARCH && text.isNotEmpty()) {
                    viewModel.saveSearchWord(text)//保存搜索到历史搜索
                    parentViewModel.searchWord.value = text
                }
                true//true为不隐藏键盘
            }
        }
    }

    private fun addObserver() {
        viewModel.searchWords.observe {
            (mRecyclerView.adapter as SearchHelperAdapter).submitList(it)
            showContent()
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
        if (appBarLayout.elevation == 0f) {
            appBarLayout.elevation =
                appbarElevation
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

    override fun onDestroy() {
        super.onDestroy()
        appBarLayout.elevation = 0f
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
        private var appbarElevation = 0f
        fun newInstance(): SearchHelperFragment =
            SearchHelperFragment()
    }
}