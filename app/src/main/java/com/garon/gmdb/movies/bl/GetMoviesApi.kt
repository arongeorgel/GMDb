package com.garon.gmdb.movies.bl

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.themoviedb.org/3/movie/changes
// ?api_key=f1a2917f7077195ab6b66b661e8299e8
// &end_date=2019-03-02
// &start_date=2019-03-01


interface GetMoviesApi {

    @GET("movie/changes")
    fun getChanges(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ) : Observable<Response<MovieChangesResponse>>
}

data class MovieChangesResponse(
    val results: List<MovieChanges>
) {
    data class MovieChanges(
        val id: Int,
        val adult: Boolean
    )
}
