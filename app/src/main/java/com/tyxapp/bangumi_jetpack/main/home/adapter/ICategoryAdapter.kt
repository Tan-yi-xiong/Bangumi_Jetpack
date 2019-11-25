package com.tyxapp.bangumi_jetpack.main.home.adapter

import com.tyxapp.bangumi_jetpack.data.CategoryBangumi

/**
 * [CategoryBangumisFragment]RecyclerView适配器必须实现该接口
 *
 */
interface ICategoryAdapter {
    /**
     * [PlayerActivity]追番可能状态改变, 当返回该[CategoryBangumisFragment]时应该把该条目的追番状态改成和修改的一样,
     * onActivityResult()是触发此函数
     *
     */
    fun onFollowStateChange(position: Int, isFollow: Boolean)

    /**
     * 根据索引获取[CategoryBangumi]
     */
    fun getBagnumi(position: Int): CategoryBangumi?
}