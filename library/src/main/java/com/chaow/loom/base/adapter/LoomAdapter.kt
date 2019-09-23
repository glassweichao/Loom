package com.chaow.loom.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.chaow.loom.base.viewholder.BasicViewHolder
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * @author Char
 * @date 2019/9/19
 */
private val ITEM_TYPE_HEADER = 0x0001
private val ITEM_TYPE_LOADING = 0x002
private val ITEM_TYPE_FOOTER = 0x0003
private val ITEM_TYPE_EMPTY = 0x0005
private val ITEM_TYPE_CONTENT = 0x0006

abstract class LoomAdapter<T, VH : BasicViewHolder>(
    val data: MutableList<T>,
    val contentLayoutId: Int = -1
) : RecyclerView.Adapter<VH>() {

    //empty
    /**whether show empty view*/
    private var isEmptyEnable: Boolean = false
    private var mEmptyLayoutId: Int = -1
    //header
    private var isHeaderEnable: Boolean = false
    private var mHeaderLayoutId: Int = -1
    //footer
    private var isFooterEnable: Boolean = false
    private var mFooterLayoutId: Int = -1
    //listener
    private var mOnItemClickListener: OnItemClickListener? = null
    private var mOnItemLongClickListener: OnItemLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when (viewType) {
            ITEM_TYPE_EMPTY -> createEmptyViewHolder(parent)
            ITEM_TYPE_HEADER -> createHeaderViewHolder(parent)
            ITEM_TYPE_FOOTER -> createFooterViewHolder(parent)
            ITEM_TYPE_CONTENT -> createContentViewHolder(parent)
            else -> createContentViewHolder(parent)
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        if (isEmptyNow()) {
            count++
            if (isHeaderEnable && getHeaderCount() != 0) count++
            if (isFooterEnable && getFooterCount() != 0) count++
        } else {
            count = getHeaderCount() + getContentItemCount() + getFooterCount()
        }
        return count
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_EMPTY -> converEmpty(holder)
            ITEM_TYPE_HEADER -> converHeader(holder)
            ITEM_TYPE_FOOTER -> converFooter(holder)
            ITEM_TYPE_CONTENT -> {
                bindItemClickListener(holder)
                conver(holder, getContentItem(position - getHeaderCount()))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isEmptyNow()) {
            val emptyPosition = getHeaderCount() - 1
            if (position < emptyPosition && isHeaderEnable) {
                ITEM_TYPE_HEADER
            } else if (position > emptyPosition && isFooterEnable) {
                ITEM_TYPE_FOOTER
            } else {
                ITEM_TYPE_EMPTY
            }
        } else if (isHeaderEnable && position < getHeaderCount()) {
            ITEM_TYPE_HEADER
        } else if (isFooterEnable && position >= getHeaderCount() + getContentItemCount()) {
            ITEM_TYPE_FOOTER
        } else {
            ITEM_TYPE_CONTENT
        }
    }

    fun getContentItem(@IntRange(from = 0) position: Int): T? {
        return if (position > 0 && position < data.size) {
            data[position]
        } else {
            null
        }
    }

    private fun isEmptyNow(): Boolean {
        return isEmptyEnable && data.isNotEmpty()
    }

    private fun getHeaderCount(): Int {
        return if (isHeaderEnable && mHeaderLayoutId > 0) 1 else 0
    }

    private fun getFooterCount(): Int {
        return if (isFooterEnable && mFooterLayoutId > 0) 1 else 0
    }

    fun getContentItemCount(): Int {
        return data.size
    }

    private fun createContentView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(contentLayoutId, parent, false)
    }

    fun setEmptyView(emptyLayoutId: Int) {
        if (emptyLayoutId < 0) {
            isEmptyEnable = false
            return
        }
        isEmptyEnable = true
        mEmptyLayoutId = emptyLayoutId
    }

    fun setHeaderView(headerLayoutId: Int) {
        if (headerLayoutId < 0) {
            isHeaderEnable = false
            return
        }
        isHeaderEnable = true
        mHeaderLayoutId = headerLayoutId
    }

    fun setFooterView(footerLayoutId: Int) {
        if (footerLayoutId < 0) {
            isFooterEnable = false
            return
        }
        isFooterEnable = true
        mFooterLayoutId = footerLayoutId
    }

    private fun createEmptyViewHolder(parent: ViewGroup): VH {
        val emptyView =
            LayoutInflater.from(parent.context).inflate(mEmptyLayoutId, parent, false)
        return createBaseViewHolder(emptyView)
    }

    private fun createHeaderViewHolder(parent: ViewGroup): VH {
        val headerView = LayoutInflater.from(parent.context).inflate(mHeaderLayoutId, parent, false)
        return createBaseViewHolder(headerView)
    }

    private fun createFooterViewHolder(parent: ViewGroup): VH {
        val footerView = LayoutInflater.from(parent.context).inflate(mFooterLayoutId, parent, false)
        return createBaseViewHolder(parent)
    }

    private fun createContentViewHolder(parent: ViewGroup): VH {
        val contentView =
            LayoutInflater.from(parent.context).inflate(contentLayoutId, parent, false)
        return createBaseViewHolder(contentView)
    }

    private fun bindItemClickListener(viewHolder: VH) {
        val view = viewHolder.itemView
        if (mOnItemClickListener != null) {
            view.setOnClickListener {
                var position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }
                position -= getHeaderCount()
                mOnItemClickListener?.onItemClick(it, position)
            }
        }
        if (mOnItemLongClickListener != null) {
            view.setOnLongClickListener {
                var position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnLongClickListener false
                }
                position -= getHeaderCount()
                mOnItemLongClickListener?.onItemLongClick(it, position)
                return@setOnLongClickListener true
            }
        }
    }

    protected fun createBaseViewHolder(view: View): VH {
        var temp: Class<*> = javaClass
        var z: Class<*>? = null
        while (z == null) {
            z = getInstancedGenericKClass(temp)
            temp = temp.javaClass.superclass
        }
        val viewHolder: VH?
        viewHolder = createGenericKInstance(z, view)
        return viewHolder ?: BasicViewHolder(view) as VH
    }

    private fun getInstancedGenericKClass(z: Class<*>): Class<*>? {
        val type = z.genericSuperclass
        if (type is ParameterizedType) {
            val types = type.actualTypeArguments
            for (temp in types) {
                if (temp is Class<*>) {
                    if (BasicViewHolder::class.java.isAssignableFrom(temp)) {
                        return temp
                    }
                } else if (temp is ParameterizedType) {
                    val rawType = temp.rawType
                    if (rawType is Class<*> && BasicViewHolder::class.java.isAssignableFrom(
                            rawType
                        )
                    ) {
                        return rawType
                    }
                }
            }
        }
        return null
    }

    @SuppressWarnings("unchecked")
    private fun createGenericKInstance(z: Class<*>, view: View): VH? {
        try {
            val constructor: Constructor<*>
            return if (z.isMemberClass && !Modifier.isStatic(z.modifiers)) {
                constructor = z.getDeclaredConstructor(javaClass, View::class.java)
                constructor.isAccessible = true
                constructor.newInstance(this, view) as VH
            } else {
                constructor = z.getDeclaredConstructor(View::class.java)
                constructor.isAccessible = true
                constructor.newInstance(view) as VH
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    abstract fun conver(viewHolder: VH, item: T?)
    fun converHeader(viewHolder: VH) {}
    fun converFooter(viewHolder: VH) {}
    fun converEmpty(viewHolder: VH) {}

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }
}