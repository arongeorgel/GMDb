package com.garon.gmdb.movies.di

import com.garon.gmdb.movies.MoviesActivity
import com.garon.gmdb.movies.bl.GetMoviesApi
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import retrofit2.Retrofit
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MoviesScope


@MoviesScope
@Subcomponent(modules = [MoviesBindsModule::class, MoviesProvidesModule::class])
interface MoviesComponent {

    fun inject(activity: MoviesActivity)

    @Subcomponent.Builder
    interface Builder {

        fun build(): MoviesComponent
    }
}

@Module
interface MoviesBindsModule

@Module
class MoviesProvidesModule {

    @Provides
    fun providesMovieChangesApi(retrofit: Retrofit): GetMoviesApi {
        return retrofit.create(GetMoviesApi::class.java)
    }
}
