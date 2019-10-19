package com.tyxapp.bangumi_jetpack.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.SearchFragmentBinding
import com.tyxapp.bangumi_jetpack.main.observe
import com.tyxapp.bangumi_jetpack.main.viewmodels.SearchViewModel
import com.tyxapp.bangumi_jetpack.utilities.SEARCHFRAGMENT_STACK_NAME

const val SEARCH_BACK_STACK_NAME = "SEARCH_BACK_STACK_NAME"

/**
 * 控制SearchResultFragment和SearchHelperFragment的显示
 *
 */

class SearchFragment : Fragment() {

    private lateinit var bind: SearchFragmentBinding
    private lateinit var mEditText: EditText
    private val viewModel: SearchViewModel by viewModels()

    companion object {
        fun newInstance() = SearchFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            //处理完自己的返回栈后再把自己pop
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack(
                    SEARCH_BACK_STACK_NAME,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                requireActivity().supportFragmentManager.popBackStack(SEARCHFRAGMENT_STACK_NAME,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = SearchFragmentBinding.inflate(inflater, container, false)
        mEditText = bind.editText

        initToolBar()
        initEditView()
        addObservse()

        return bind.root
    }

    private fun addObservse() {
        viewModel.searchWord.observe(this) {
            childFragmentManager.commit {
                val searchResultFragment =
                    childFragmentManager.findFragmentByTag(SearchResultFragment::class.java.simpleName)

                val searchHelperFragment =
                    childFragmentManager.findFragmentByTag(SearchHelperFragment::class.java.simpleName)!!

                if (searchResultFragment == null) {
                    this.add(
                        R.id.searchContent,
                        SearchResultFragment.newInstance(),
                        SearchResultFragment::class.java.simpleName
                    )
                    this.remove(searchHelperFragment)
                } else {
                    childFragmentManager.popBackStack(
                        SEARCH_BACK_STACK_NAME,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }
        }
    }

    private fun initToolBar() {
        with(bind.toolBar) {
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun initEditView() {
        mEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                childFragmentManager.commit {
                    this.add(
                        R.id.searchContent,
                        SearchHelperFragment.newInstance(),
                        SearchHelperFragment::class.java.simpleName
                    )

                    val searchResultFragent =
                        childFragmentManager.findFragmentByTag(SearchResultFragment::class.java.simpleName)

                    if (searchResultFragent != null) {
                        hide(searchResultFragent)
                        addToBackStack(SEARCH_BACK_STACK_NAME)
                        setCustomAnimations(
                            R.anim.nav_default_enter_anim,
                            R.anim.nav_default_exit_anim,
                            R.anim.nav_default_pop_enter_anim,
                            R.anim.nav_default_pop_exit_anim
                        )
                    }

                    this.setReorderingAllowed(true)//优化事务
                }
            }
        }

        mEditText.requestFocus()

        childFragmentManager.addOnBackStackChangedListener {
            //有返回栈证明searchResultFragment被覆盖, 生命周期控制到onPause, 否则控制到onResume
            val searchResultFragment =
                childFragmentManager.findFragmentByTag(SearchResultFragment::class.java.simpleName)!!

            if (childFragmentManager.backStackEntryCount > 0) {
                setMaxLifecycle(searchResultFragment, Lifecycle.State.STARTED)
            } else {
                setMaxLifecycle(searchResultFragment, Lifecycle.State.RESUMED)
            }
        }
    }

    private fun setMaxLifecycle(fragment: Fragment, state: Lifecycle.State) {
        childFragmentManager.commit { setMaxLifecycle(fragment, state) }
    }

}
