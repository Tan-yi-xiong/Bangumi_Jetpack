package com.tyxapp.bangumi_jetpack.player.bottomsheet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.LayoutDanmuSettingBottomsheetBinding
import org.jetbrains.anko.defaultSharedPreferences

class DanmuSettingBottomSheet : BaseBottomSheet() {
    private lateinit var binding: LayoutDanmuSettingBottomsheetBinding
    private lateinit var mDanmuTextSizeSeekBar: SeekBar
    private lateinit var mDanmuMaxRawSeekBar: SeekBar
    private lateinit var mDanmuSwitch: Switch

    private var mOnCheckedChangeListener: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutDanmuSettingBottomsheetBinding.inflate(
            inflater, container, false
        ).apply {
            mDanmuSwitch = danmuSwitch
            mDanmuMaxRawSeekBar = danmuRowsSeekBar
            mDanmuTextSizeSeekBar = danmuTextSeekBar

            root.background = requireActivity().window.decorView.background
        }
        initView()
        return binding.root
    }

    private fun initView() {
        val isCheck = arguments!!.getBoolean(BOOLEAN_ARG)
        mDanmuSwitch.isChecked = isCheck
        mDanmuSwitch.setOnCheckedChangeListener { _, isChecked ->
            mOnCheckedChangeListener?.invoke(isChecked)
        }

        val danmuTextSizt = requireActivity().defaultSharedPreferences.getInt(
            getString(R.string.key_danmaku_textsize),
            70
        )
        val danmuMaxRaw = requireActivity().defaultSharedPreferences.getInt(
            getString(R.string.key_danmaku_maxRaw),
            3
        )

        mDanmuMaxRawSeekBar.max = 8
        mDanmuTextSizeSeekBar.max = 200
        mDanmuMaxRawSeekBar.progress = danmuMaxRaw
        mDanmuTextSizeSeekBar.progress = danmuTextSizt
        binding.danmuTextsize.text = "$danmuTextSizt%"
        binding.maxRawText.text = danmuMaxRaw.toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDanmuMaxRawSeekBar.min = 3
            mDanmuTextSizeSeekBar.min = 30
        }
        mDanmuMaxRawSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && progress < 3) {
                    seekBar.progress = 3
                }
                binding.maxRawText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                requireActivity().defaultSharedPreferences.edit(commit = true) {
                    putInt(getString(R.string.key_danmaku_maxRaw), seekBar.progress)
                }
            }

        })

        mDanmuTextSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && progress < 30) {
                    seekBar.progress = 30
                }
                binding.danmuTextsize.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                requireActivity().defaultSharedPreferences.edit(commit = true) {
                    putInt(getString(R.string.key_danmaku_textsize), seekBar.progress)
                }
            }

        })
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: (Boolean) -> Unit) {
        mOnCheckedChangeListener = onCheckedChangeListener
    }

    companion object{
        private const val BOOLEAN_ARG = "BOOLEAN_ARG"

        fun getInstance(isSwitchCheck: Boolean): DanmuSettingBottomSheet {
            return DanmuSettingBottomSheet().apply {
                arguments = bundleOf(BOOLEAN_ARG to isSwitchCheck)
            }
        }
    }
}