package com.garon.gmdb.movieDetails.trailer.bl

import com.garon.gmdb.utils.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class GetMovieTrailerUseCase @Inject constructor(
    private val movieVideoApi: GetMovieVideosApi,
    @IoScheduler val scheduler: Scheduler
) {

    private val inProgress: AtomicBoolean = AtomicBoolean(false)

    fun execute(movieId: Int): Observable<MovieVideoResult> {
        if (inProgress.getAndSet(true)) return Observable.empty()

        return movieVideoApi.getMovieVideos(movieId)
            .subscribeOn(scheduler)
            .doOnNext { inProgress.set(false) }
            .compose(ApiCallTransformer("Failed to load trailer."))
            .startWith(Observable.just(ApiInProgressState))
            .publish { shared ->
                Observable.merge(
                    onInProgress(shared),
                    onError(shared),
                    onSuccess(shared)
                )
            }
    }

    private fun onError(it: Observable<ApiCallStateResult>): Observable<MovieVideoResult> =
        it.ofType(ApiErrorState::class.java)
            .map {
                Timber.e(
                    "Api call failed. Error code: ${it.statusCode}\n${it.statusMessage}"
                )
                MovieVideoResult.Error(it.statusMessage)
            }

    private fun onSuccess(it: Observable<ApiCallStateResult>): Observable<MovieVideoResult> =
        it.ofType(ApiSuccessState::class.java)
            .map { it.content as MovieVideoResponse }
            .map { response ->
                response.results.let {
                    if (it.isNotEmpty()) {
                        val firstTrailer = it.first { it.site == "YouTube" && it.type == "Trailer" }
                        MovieVideoResult.Success(
                            MovieVideoResult.MovieTrailer(
                                videoId = firstTrailer.key,
                                youtubeUrl = "https://www.youtube.com/watch?v=${firstTrailer.key}",
                                name = firstTrailer.name
                            )
                        )
                    } else MovieVideoResult.Error("No video found.")
                }
            }

    private fun onInProgress(it: Observable<ApiCallStateResult>): Observable<MovieVideoResult> =
        it.ofType(ApiSuccessState::class.java)
            .map { MovieVideoResult.InProgress }
}

sealed class MovieVideoResult {

    object InProgress : MovieVideoResult()

    data class Error(val message: String) : MovieVideoResult()

    data class Success(val trailer: MovieTrailer) : MovieVideoResult()

    data class MovieTrailer(
        val videoId: String,
        val youtubeUrl: String,
        val name: String
    )
}
