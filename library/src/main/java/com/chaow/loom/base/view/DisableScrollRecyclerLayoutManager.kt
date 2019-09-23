package com.chaow.loom.base.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

/**
 * 不可滚动的GridLayoutManager
 * @author Char
 * @date 2019/9/23
 */
class DisableScrollRecyclerLayoutManager : GridLayoutManager {
    var isVerticallyScrollEnabled = true
    var isHorizontalScrollEnabled = true

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
    }

    constructor(context: Context, spanCount: Int) : super(context, spanCount) {}

    constructor(context: Context, spanCount: Int, orientation: Int, reverseLayout: Boolean) : super(
        context,
        spanCount,
        orientation,
        reverseLayout
    ) {
    }

    override fun canScrollVertically(): Boolean {
        return isVerticallyScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return isHorizontalScrollEnabled && super.canScrollHorizontally()
    }
}