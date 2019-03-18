package com.garon.gmdb.movies

import com.garon.gmdb.movies.bl.GetMoviesUseCase
import com.garon.gmdb.movies.bl.MoviesResult
import com.garon.gmdb.movies.di.MoviesScope
import com.garon.gmdb.utils.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

interface MoviesView {

    fun showProgress()

    fun error(message: String)

    fun movieList(list: List<Int>)

    enum class DaysDateRange {
        LAST_3, LAST_5, LAST_10, LAST_14
    }
}

/**
 * Note from the author:
 * Due to the fact that the list is quite large and there is no pagination from the API, local pagination
 * is suggested to be implemented.
 */
@MoviesScope
class MoviesPresenter @Inject constructor(
    private val moviesUseCase: GetMoviesUseCase
) {

    private lateinit var view: MoviesView

    private val disposable = CompositeDisposable()

    fun attach(v: MoviesView) {
        view = v
    }

    fun detach() {
        disposable.clear()
    }

    fun loadMovieChanges(dateRange: MoviesView.DaysDateRange = MoviesView.DaysDateRange.LAST_3) {
        disposable += moviesUseCase.execute(dateRange)
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

    private fun success(it: Observable<MoviesResult>): Observable<Unit> = it.ofType(MoviesResult.InProgress::class.java)
        .map { view.showProgress() }

    private fun error(it: Observable<MoviesResult>) = it.ofType(MoviesResult.Error::class.java)
        .map { view.error(it.message) }

    private fun inProgress(it: Observable<MoviesResult>) = it.ofType(MoviesResult.Success::class.java)
        .map { view.movieList(it.list) }
}
