package com.chaow.loom.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
    private var mEmptyLayout: FrameLayout? = null
    //header
    private var isHeaderEnable: Boolean = false
    //footer
    private var isFooterEnable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        var viewHolder: VH = when (viewType) {
            ITEM_TYPE_EMPTY -> mEmptyLayout?.let { createBaseViewHolder(it) } as VH
            else -> createBaseViewHolder(createContentView(parent))
        }
        return viewHolder
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun isEmptyNow(): Boolean {
        return isEmptyEnable && data.isNotEmpty()
    }

    private fun getHeaderCount(): Int {
        return if (isHeaderEnable) 1 else 0
    }

    private fun getFooterCount(): Int {
        return if (isFooterEnable) 1 else 0
    }

    fun getContentItemCount(): Int {
        return data.size
    }

    private fun createContentView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(contentLayoutId, parent, false)
    }

    public fun setEmptyView(emptyLayoutId: Int) {
        if (emptyLayoutId < 0) {
            return
        }
    }

    public fun setEmptyView(emptyView: View?) {
        if (emptyView == null) {
            isEmptyEnable = false
            mEmptyLayout = null
            return
        }
        if (mEmptyLayout == null) {
            mEmptyLayout = FrameLayout(emptyView.context)
            val layoutParams =
                RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT
                )
            val lp = emptyView.layoutParams
            if (lp != null) {
                layoutParams.width = lp.width
                layoutParams.height = lp.height
            }
            mEmptyLayout!!.layoutParams = layoutParams
        }
        mEmptyLayout!!.removeAllViews()
        mEmptyLayout!!.addView(emptyView)
        isEmptyEnable = true
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
}