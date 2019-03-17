package com.garon.gmdb.movieDetails.bl

import com.garon.gmdb.movieDetails.di.MovieDetailsScope
import com.garon.gmdb.utils.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import timber.log.Timber
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.ArrayList

@MovieDetailsScope
class GetMovieDetailsUseCase @Inject constructor(
    private val movieDetailsApi: GetMovieDetailsApi,
    @IoScheduler val scheduler: Scheduler
) {

    private val inProgress: AtomicBoolean = AtomicBoolean(false)

    fun execute(movieId: Int): Observable<MovieDetailsResult> {
        if (inProgress.getAndSet(true)) return Observable.empty()

        return movieDetailsApi.getMovieDetails(movieId)
            .subscribeOn(scheduler)
            .doOnNext { inProgress.set(false) }
            .compose(ApiCallTransformer("Failed to load title"))
            .startWith(Observable.just(ApiInProgressState))
            .publish { shared ->
                Observable.merge(
                    onInProgress(shared),
                    onError(shared),
                    onSuccess(shared)
                )
            }
    }

    private fun onError(it: Observable<ApiCallStateResult>): Observable<MovieDetailsResult> =
        it.ofType(ApiErrorState::class.java)
            .map {
                Timber.e("Api call failed. Error code: ${it.statusCode}\n${it.statusMessage}")
                MovieDetailsResult.Error(it.statusMessage)
            }

    private fun onSuccess(it: Observable<ApiCallStateResult>): Observable<MovieDetailsResult> =
        it.ofType(ApiSuccessState::class.java)
            .map { it.content as MovieDetailsResponse }
            .map { response ->

                return@map MovieDetailsResult.Success(
                    MovieDetailsResult.MovieDetails(
                        movieId = response.id,
                        title = response.title,
                        overview = response.overview,
                        posterPath = "https://image.tmdb.org/t/p/w500${response.posterPath}",
                        originalLanguage = Locale(response.originalLanguage, ""),
                        releaseDate = response.releaseDate,
                        genres = StringBuilder().apply {
                            response.genres.map {
                                append(it.name)
                                append(", ")
                            }
                        }.toString().run {
                            if (length > 2) substring(0, length - 2) else this
                        }
                    )
                )
            }

    private fun onInProgress(it: Observable<ApiCallStateResult>): Observable<MovieDetailsResult> =
        it.ofType(ApiSuccessState::class.java)
            .map { MovieDetailsResult.InProgress }

}

sealed class MovieDetailsResult {

    object InProgress : MovieDetailsResult()

    data class Error(val message: String) : MovieDetailsResult()

    data class Success(val movieDetails: MovieDetails) : MovieDetailsResult()

    data class MovieDetails(
        val movieId: Int,
        val title: String,
        val overview: String,
        val originalLanguage: Locale,
        val posterPath: String,
        val releaseDate: String,
        val genres: String
    ) {
        data class Genre(
            val id: Int,
            val name: String
        )
    }
}
