package com.garon.gmdb.movieDetails.di

import com.garon.gmdb.movieDetails.MovieDetailsActivity
import com.garon.gmdb.movieDetails.bl.GetMovieDetailsApi
import com.garon.gmdb.movieDetails.trailer.bl.GetMovieVideosApi
import com.garon.gmdb.movies.adapter.MovieItemLayout
import com.garon.gmdb.movies.di.MoviesComponent
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import retrofit2.Retrofit
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MovieDetailsScope


@MovieDetailsScope
@Subcomponent(modules = [MovieDetailsBindsModule::class, MovieDetailsProvidesModule::class])
interface MovieDetailsComponent {

    fun inject(layout: MovieItemLayout)

    fun inject(activity: MovieDetailsActivity)

    @Subcomponent.Builder
    interface Builder {

        fun build(): MovieDetailsComponent
    }
}

@Module
interface MovieDetailsBindsModule

@Module
class MovieDetailsProvidesModule {

    @Provides
    fun providesMovieDetailsApi(retrofit: Retrofit): GetMovieDetailsApi {
        return retrofit.create(GetMovieDetailsApi::class.java)
    }

    @Provides
    fun providesMovieVideosApi(retrofit: Retrofit): GetMovieVideosApi {
        return retrofit.create(GetMovieVideosApi::class.java)
    }
}
