package com.garon.gmdb.movieDetails.bl

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GetMovieDetailsApi {

    @GET("movie/{movieId}")
    fun getMovieDetails(@Path("movieId") movieId: Int): Observable<Response<MovieDetailsResponse>>
}

data class MovieDetailsResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("original_language") val originalLanguage: String,
    @SerializedName("poster_path") val posterPath: String,
    @SerializedName("release_date") val releaseDate: String,
    val genres: List<Genre>
) {
    data class Genre(
        val id: Int,
        val name: String
    )
}
