package com.garon.gmdb.movies

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garon.gmdb.GmdbApplication
import com.garon.gmdb.R
import com.garon.gmdb.movieDetails.MovieDetailsActivity
import com.garon.gmdb.movies.adapter.MoviesAdapter
import com.garon.gmdb.utils.hide
import com.garon.gmdb.utils.kotterknife.bindView
import com.garon.gmdb.utils.setScreenOrientation
import com.garon.gmdb.utils.show
import javax.inject.Inject

class MoviesActivity : AppCompatActivity(), MoviesView {

    private val moviesRecycler: RecyclerView by bindView(R.id.movies_recycler)
    private val retry: View by bindView(R.id.movies_retry)
    private val loader: View by bindView(R.id.movies_loader)

    private val adapter = MoviesAdapter {
        startActivity(
            Intent(this, MovieDetailsActivity::class.java)
                .apply { putExtra(MovieDetailsActivity.MOVIE_ID, it) }
        )
    }

    @Inject
    lateinit var presenter: MoviesPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenOrientation()
        setContentView(R.layout.activity_movies)

        bindViews()
        inject()
    }

    private fun bindViews() {
        val layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.grid_movie_span))
        moviesRecycler.layoutManager = layoutManager
        moviesRecycler.adapter = adapter

        // TODO implement picker for date ranges and pass it to the presenter
        retry.setOnClickListener { presenter.loadMovieChanges() }
    }

    private fun inject() {
        GmdbApplication.getInstance().appComponent
            .moviesBuilder()
            .build()
            .inject(this)

        presenter.attach(this)
        // TODO implement picker for date ranges and pass it to the presenter
        presenter.loadMovieChanges()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            presenter.detach()
        }
    }

    override fun showProgress() {
        loader.show()
        moviesRecycler.hide()
        retry.hide()
    }

    override fun error(message: String) {
        loader.hide()
        moviesRecycler.hide()
        retry.show()
    }

    override fun movieList(list: List<Int>) {
        loader.hide()
        moviesRecycler.show()
        retry.hide()

        adapter.updateDataSet(list)
    }
}
