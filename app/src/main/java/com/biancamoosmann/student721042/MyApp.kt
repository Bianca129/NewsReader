package com.biancamoosmann.student721042

import android.app.Application
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://inhollandbackend.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        RetrofitInstance.retrofit = retrofit

        appContext = this
    }

    companion object RetrofitInstance {
        lateinit var retrofit: Retrofit

         lateinit var appContext: Context


        fun provideContext(): Context {
            return appContext
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appContext = base
    }
}
