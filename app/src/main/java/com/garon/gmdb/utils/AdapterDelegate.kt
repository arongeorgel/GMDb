package com.garon.gmdb.utils

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class DelegatedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val delegates: SparseArray<AdapterDelegate> = SparseArray()

    abstract val items: List<Any>

    fun addDelegate(viewType: Int, delegate: AdapterDelegate) {
        delegates.put(viewType, delegate)
    }

    override fun getItemCount() = items.size

    abstract override fun getItemViewType(position: Int): Int

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterDelegate = delegates[viewType] ?: throw IllegalStateException("No delegate for $viewType viewType")
        return adapterDelegate.onCreateViewHolder(parent)
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegates[getItemViewType(position)].onBindViewHolder(holder, position, items)
    }
}

interface AdapterDelegate {

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, items: List<Any>)
}

class TypedAdapterDelegate<in Data>(
    private val creator: (ViewGroup) -> ViewHolder<Data>
) : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder<Data> {
        return creator(parent)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, items: List<Any>) {
        val data = items[position] as? Data
            ?: throw IllegalStateException("Incorrect data for position $position. Was ${items[position]}. Check your items list.")
        (holder as ViewHolder<Data>).bind(data)
    }
}

interface ItemRenderer<in D> {
    fun render(data: D)
}

abstract class ViewHolder<in D>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(data: D)
}

class ViewHolderRenderer<in Data, out Layout>(private val layout: Layout) : ViewHolder<Data>(layout)
        where  Layout : android.view.View, Layout : ItemRenderer<Data> {

    override fun bind(data: Data) {
        layout.render(data)
    }
}
