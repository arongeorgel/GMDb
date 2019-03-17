package com.garon.gmdb.movies.bl

import com.garon.gmdb.movies.MoviesView
import com.garon.gmdb.movies.di.MoviesScope
import com.garon.gmdb.utils.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@MoviesScope
class GetMoviesUseCase @Inject constructor(
    private val moviesApi: GetMoviesApi,
    @IoScheduler val scheduler: Scheduler
) {

    private val inProgress: AtomicBoolean = AtomicBoolean(false)

    /**
     * Return a list of Int representing the movie IDs which are not adult
     */
    fun execute(dateRange: MoviesView.DaysDateRange): Observable<MoviesResult> {
        if (inProgress.getAndSet(true)) return Observable.empty()

        val period = getDateRange(dateRange)

        return moviesApi.getChanges(period.first, period.second)
            .subscribeOn(scheduler)
            .doOnNext { inProgress.set(false) }
            .compose(ApiCallTransformer("Failed to retrieve the movie changes"))
            .startWith(Observable.just(ApiInProgressState))
            .publish { shared ->
                Observable.merge(
                    onInProgress(shared),
                    onError(shared),
                    onSuccess(shared)
                )
            }
    }

    /**
     * Based on the give range return a pair with start date and end date
     */
    internal fun getDateRange(daysDateRange: MoviesView.DaysDateRange): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = dateFormatter.format(calendar.time)

        val startDate = when (daysDateRange) {
            MoviesView.DaysDateRange.LAST_3 -> {
                calendar.add(Calendar.DATE, -3)
                dateFormatter.format(calendar.time)
            }
            MoviesView.DaysDateRange.LAST_5 -> {
                calendar.add(Calendar.DATE, -5)
                dateFormatter.format(calendar.time)
            }
            MoviesView.DaysDateRange.LAST_10 -> {
                calendar.add(Calendar.DATE, -10)
                dateFormatter.format(calendar.time)
            }
            MoviesView.DaysDateRange.LAST_15 -> {
                calendar.add(Calendar.DATE, -15)
                dateFormatter.format(calendar.time)
            }
        }

        return Pair(startDate, endDate)
    }

    private fun onError(it: Observable<ApiCallStateResult>): Observable<MoviesResult> =
        it.ofType(ApiErrorState::class.java)
            .map {
                Timber.e(
                    "Api call failed. Error code: ${it.statusCode}\n${it.statusMessage}"
                )
                MoviesResult.Error(it.statusMessage)
            }

    private fun onSuccess(it: Observable<ApiCallStateResult>): Observable<MoviesResult> =
        it.ofType(ApiSuccessState::class.java)
            .map { it.content as MovieChangesResponse }
            .map { response ->
                val filteredList = ArrayList<Int>().apply {
                    response.results
                        .filter { !it.adult }
                        .map { this.add(it.id) }
                }
                return@map MoviesResult.Success(filteredList)
            }

    private fun onInProgress(it: Observable<ApiCallStateResult>): Observable<MoviesResult> =
        it.ofType(ApiSuccessState::class.java)
            .map { MoviesResult.InProgress }
}

sealed class MoviesResult {

    object InProgress : MoviesResult()

    data class Error(val message: String) : MoviesResult()

    data class Success(val list: List<Int>) : MoviesResult()
}
