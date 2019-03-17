package com.garon.gmdb

import android.app.Application
import com.garon.gmdb.di.ApplicationComponent
import com.garon.gmdb.di.DaggerApplicationComponent
import timber.log.Timber

class GmdbApplication : Application() {

    companion object {
        private lateinit var application: GmdbApplication

        fun getInstance(): GmdbApplication {
            return application
        }
    }

    lateinit var appComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()
        application = this
        Timber.plant(Timber.DebugTree())

        appComponent = DaggerApplicationComponent
            .builder()
            .application(this)
            .build()
        appComponent.inject(this)
    }
}