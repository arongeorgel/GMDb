package com.garon.gmdb.movieDetails

import com.garon.gmdb.movieDetails.bl.GetMovieDetailsUseCase
import com.garon.gmdb.movieDetails.bl.MovieDetailsResult
import com.garon.gmdb.movieDetails.di.MovieDetailsScope
import com.garon.gmdb.movieDetails.trailer.bl.GetMovieTrailerUseCase
import com.garon.gmdb.movieDetails.trailer.bl.MovieVideoResult
import com.garon.gmdb.utils.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

interface MovieDetailsView {
    fun showProgress()

    fun error(message: String)

    fun displayDetails(movieDetails: MovieDetailsResult.MovieDetails)

    fun displayYoutubeTrailer(trailer: MovieVideoResult.MovieTrailer)

    fun hideYoutubeTrailer()
}

/**
 * Note from the author - right now the experience is not that good because the results are not being cached.
 */
@MovieDetailsScope
class MovieDetailsPresenter @Inject constructor(
    private val movieDetailsUseCase: GetMovieDetailsUseCase,
    private val movieTrailerUseCase: GetMovieTrailerUseCase
) {
    private lateinit var view: MovieDetailsView

    private val disposable = CompositeDisposable()

    fun attach(v: MovieDetailsView) {
        view = v
    }

    fun detach() {
        disposable.clear()
    }

    fun loadMovieDetails(movieId: Int) {
        disposable += movieDetailsUseCase.execute(movieId)
            .observeOn(AndroidSchedulers.mainThread())
            .publish {
                Observable.merge(
                    success(it),
                    error(it),
                    inProgress(it)
                )
            }
            .subscribe()
    }

    fun loadYoutubeTrailer(movieId: Int) {
        disposable += movieTrailerUseCase.execute(movieId)
            .observeOn(AndroidSchedulers.mainThread())
            .publish {
                Observable.merge(
                    trailerSuccess(it),
                    trailerError(it)
                )
            }
            .subscribe()
    }


    private fun trailerSuccess(it: Observable<MovieVideoResult>) =
        it.ofType(MovieVideoResult.Success::class.java)
            .map { view.displayYoutubeTrailer(it.trailer) }

    private fun trailerError(it: Observable<MovieVideoResult>) =
        it.ofType(MovieVideoResult.Error::class.java)
            .map { view.hideYoutubeTrailer() }

    private fun success(it: Observable<MovieDetailsResult>) =
        it.ofType(MovieDetailsResult.InProgress::class.java)
            .map { view.showProgress() }

    private fun error(it: Observable<MovieDetailsResult>) = it.ofType(MovieDetailsResult.Error::class.java)
        .map { view.error(it.message) }

    private fun inProgress(it: Observable<MovieDetailsResult>) = it.ofType(MovieDetailsResult.Success::class.java)
        .map { view.displayDetails(it.movieDetails) }
}
