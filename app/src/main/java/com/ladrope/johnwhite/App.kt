package com.ladrope.johnwhite

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class App : Application() {


    override fun onCreate() {
        super.onCreate()


        //Firebase persist memory
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)


    }

}