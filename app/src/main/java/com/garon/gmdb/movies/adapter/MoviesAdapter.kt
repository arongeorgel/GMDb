package com.garon.gmdb.movies.adapter

import com.garon.gmdb.utils.DelegatedAdapter
import com.garon.gmdb.utils.TypedAdapterDelegate
import com.garon.gmdb.utils.ViewHolderRenderer

class MoviesAdapter(
    private var itemClickListener: OnMovieClick
) : DelegatedAdapter() {

    companion object {
        // TODO - add type loader when infinite scrolling will be implemented
        const val TYPE_MOVIE = 0
    }

    init {
        addDelegate(TYPE_MOVIE, TypedAdapterDelegate { parent ->
            ViewHolderRenderer(MovieItemLayout(parent.context).apply {
                setOnItemClickListener(itemClickListener)
            })
        })
    }

    override val items: MutableList<Int> = mutableListOf()

    override fun getItemViewType(position: Int): Int = TYPE_MOVIE

    fun updateDataSet(updatedList: List<Int>) {
        items.clear()
        items.addAll(updatedList)
        notifyDataSetChanged()
    }
}

typealias OnMovieClick = (movieId: Int) -> Unit
