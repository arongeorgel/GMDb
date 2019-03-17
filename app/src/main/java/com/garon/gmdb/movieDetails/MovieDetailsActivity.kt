package com.garon.gmdb.movieDetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.garon.gmdb.GmdbApplication
import com.garon.gmdb.R
import com.garon.gmdb.movieDetails.bl.MovieDetailsResult
import com.garon.gmdb.movieDetails.trailer.bl.MovieVideoResult
import com.garon.gmdb.utils.hide
import com.garon.gmdb.utils.kotterknife.bindView
import com.garon.gmdb.utils.show
import com.google.android.youtube.player.YouTubeIntents
import javax.inject.Inject


class MovieDetailsActivity : AppCompatActivity(), MovieDetailsView {

    private val title: TextView by bindView(R.id.movie_details_title)
    private val poster: ImageView by bindView(R.id.movie_details_poster)
    private val retry: View by bindView(R.id.movie_details_retry)
    private val loader: View by bindView(R.id.movie_details_loading)
    private val overview: TextView by bindView(R.id.movie_details_overview)
    private val language: TextView by bindView(R.id.movie_details_language)
    private val releaseDate: TextView by bindView(R.id.movie_details_release_date)
    private val genre: TextView by bindView(R.id.movie_details_genre)
    private val youtubeTrailer: View by bindView(R.id.movie_details_trailer)

    private var movieId: Int = 0

    @Inject
    lateinit var presenter: MovieDetailsPresenter

    companion object {
        const val MOVIE_ID = "movie.id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        movieId = intent.getIntExtra(MOVIE_ID, 0)

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
        retry.setOnClickListener { presenter.loadMovieDetails(movieId) }
        presenter.loadMovieDetails(movieId)
        presenter.loadYoutubeTrailer(movieId)

        title.setOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing)
            presenter.detach()
    }

    override fun showProgress() {
        loader.show()
        retry.hide()
    }

    override fun error(message: String) {
        loader.hide()
        retry.show()
    }

    override fun displayDetails(movieDetails: MovieDetailsResult.MovieDetails) {
        title.text = movieDetails.title
        overview.text = movieDetails.overview
        language.text = getString(R.string.movie_details_language, movieDetails.originalLanguage.displayLanguage)
        releaseDate.text = getString(R.string.movie_details_release_date, movieDetails.releaseDate)

        if (movieDetails.genres.isNotEmpty()) {
            genre.show()
            genre.text = getString(com.garon.gmdb.R.string.movie_details_genre, movieDetails.genres)
        } else {
            genre.hide()
        }

        Glide.with(this).load(movieDetails.posterPath).into(poster)

        loader.hide()
        retry.hide()
    }

    override fun displayYoutubeTrailer(trailer: MovieVideoResult.MovieTrailer) {
        youtubeTrailer.show()
        youtubeTrailer.setOnClickListener {
            if (YouTubeIntents.canResolvePlayVideoIntent(this)) {
                startActivity(YouTubeIntents.createPlayVideoIntent(this, trailer.videoId))
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(trailer.youtubeUrl))
                startActivity(webIntent)
            }
        }
    }

    override fun hideYoutubeTrailer() {
        youtubeTrailer.hide()
    }
}
