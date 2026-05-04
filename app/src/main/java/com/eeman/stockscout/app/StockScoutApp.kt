package com.eeman.stockscout.app

import android.app.Application
import com.eeman.stockscout.data.manualDI.AppContainer

class StockScoutApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}