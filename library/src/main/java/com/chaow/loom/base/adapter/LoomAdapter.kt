package com.chaow.loom.base.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Char
 * @date 2019/9/19
 */
private val ITEM_TYPE_HEADER = 0x0001
private val ITEM_TYPE_LOADING = 0x002
private val ITEM_TYPE_FOOTER = 0x0003
private val ITEM_TYPE_EMPTY = 0x0005

abstract class LoomAdapter<T, VH : RecyclerView.ViewHolder>(
    val mData: MutableList<T>,
    val contentLayoutId: Int = -1
) : RecyclerView.Adapter<VH>() {

    //empty
    /**whether show empty view*/
    private var isEmptyEnable: Boolean = false
    //header
    private var isHeaderEnable: Boolean = false
    //footer
    private var isFooterEnable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemViewType(position: Int): Int {
        if (isEmptyNow()) {
            val emptyPosition = getHeaderCount() - 1
            return if (position < emptyPosition && isHeaderEnable) {
                ITEM_TYPE_HEADER
            } else if (position > emptyPosition + 1 && isFooterEnable) {
                ITEM_TYPE_FOOTER
            } else {
                ITEM_TYPE_EMPTY
            }
        }
        return super.getItemViewType(position)
    }

    private fun isEmptyNow(): Boolean {
        return isEmptyEnable && mData.isNotEmpty()
    }

    private fun getHeaderCount(): Int {
        TODO("添加获取header数量逻辑")
        return 0
    }

}