package com.garon.gmdb.movieDetails.trailer.bl

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface GetMovieVideosApi {

    @GET("movie/{movieId}/videos")
    fun getMovieVideos(@Path("movieId") movieId: Int): Observable<Response<MovieVideoResponse>>
}

data class MovieVideoResponse(
    val id: Int,
    val results: List<MovieVideo>
) {
    data class MovieVideo(
        val name: String,
        val site: String,
        val key: String,
        val type: String
    )
}
