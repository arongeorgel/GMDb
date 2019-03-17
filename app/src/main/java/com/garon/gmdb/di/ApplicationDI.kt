package com.garon.gmdb.di

import android.app.Application
import android.content.Context
import com.garon.gmdb.GmdbApplication
import com.garon.gmdb.movieDetails.di.MovieDetailsComponent
import com.garon.gmdb.movies.di.MoviesComponent
import com.garon.gmdb.utils.IoScheduler
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NetworkModule::class])
interface ApplicationComponent {

    fun inject(app: GmdbApplication)

    fun moviesBuilder(): MoviesComponent.Builder

    fun movieDetailsBuilder(): MovieDetailsComponent.Builder

    @Component.Builder
    interface Builder {

        fun build(): ApplicationComponent

        @BindsInstance
        fun application(app: Application): Builder
    }
}

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext
}

@Module
class NetworkModule {

    private companion object {
        //                        https://api.themoviedb.org/3/movie/changes
        const val API_BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY = "api_key"
        const val API_KEY_VALUE = "f1a2917f7077195ab6b66b661e8299e8"
    }

    @Provides
    @Singleton
    @IoScheduler
    fun providesIoScheduler(): Scheduler = Schedulers.io()

    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                val url = original.url()
                    .newBuilder()
                    .addQueryParameter(API_KEY, API_KEY_VALUE)
                    .build()

                val request = original.newBuilder().url(url).build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build())
        .build()
}
