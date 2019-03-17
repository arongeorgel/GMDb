package com.garon.gmdb.movies.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.garon.gmdb.GmdbApplication
import com.garon.gmdb.R
import com.garon.gmdb.movieDetails.MovieDetailsPresenter
import com.garon.gmdb.movieDetails.MovieDetailsView
import com.garon.gmdb.movieDetails.bl.MovieDetailsResult
import com.garon.gmdb.movieDetails.trailer.bl.MovieVideoResult
import com.garon.gmdb.utils.*
import com.garon.gmdb.utils.kotterknife.bindView
import javax.inject.Inject

// Note: currently there is no possibility to detect when the view is invisible from the recycler view, thus
// unwanted behaviour is happening while fast scrolling because the presenter is not detached - to be improved by
// adding a listener for knowing when a view has been recycled and detach the presenter
class MovieItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    ItemRenderer<Int>, MovieDetailsView {

    init {
        selfInflate(R.layout.item_movie_details)
    }

    private val poster: ImageView by bindView(R.id.movie_item_details_poster)
    private val title: TextView by bindView(R.id.movie_item_details_title)
    private val loader: View by bindView(R.id.movie_item_details_loader)
    private val retry: View by bindView(R.id.movie_item_details_retry)

    var movieId: Int = 0

    @Inject
    lateinit var presenter: MovieDetailsPresenter

    override fun render(data: Int) {
        movieId = data
        inject()
        bindViews()
    }

    private fun inject() {
        GmdbApplication.getInstance().appComponent
            .movieDetailsBuilder()
            .build()
            .inject(this)

        presenter.attach(this)
    }

    private fun bindViews() {
        presenter.loadMovieDetails(movieId)

        retry.setOnClickListener { presenter.loadMovieDetails(movieId) }
    }

    fun setOnItemClickListener(onMovieClick: OnMovieClick) {
        poster.setOnClickListener { onMovieClick.invoke(movieId) }
    }

    override fun showProgress() {
        loader.show()
        retry.hide()
        poster.hide()
        title.hide()
    }

    override fun error(message: String) {
        loader.hide()
        retry.show()
        poster.hide()
        title.hide()
    }

    override fun displayDetails(movieDetails: MovieDetailsResult.MovieDetails) {
        loader.hide()
        retry.hide()
        poster.show()
        title.show()

        title.text = movieDetails.title
        Glide.with(this).load(movieDetails.posterPath).into(poster)
    }

    override fun displayYoutubeTrailer(trailer: MovieVideoResult.MovieTrailer) {
        // no-op
    }

    override fun hideYoutubeTrailer() {
        // no-op
    }

}
