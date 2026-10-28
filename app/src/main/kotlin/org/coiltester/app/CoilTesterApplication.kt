package org.coiltester.app

import android.app.Application
import org.coiltester.app.di.startKoin

class CoilTesterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this)
    }
}