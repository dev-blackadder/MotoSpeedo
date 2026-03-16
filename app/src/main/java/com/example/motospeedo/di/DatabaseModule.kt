package com.example.motospeedo.di

import android.content.Context
import androidx.room.Room
import com.example.motospeedo.data.AppDatabase
import com.example.motospeedo.data.RideDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "motospeedo.db")
            .build()

    @Provides
    fun provideRideDao(db: AppDatabase): RideDao = db.rideDao()
}
