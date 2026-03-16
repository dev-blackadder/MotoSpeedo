package com.example.motospeedo.di

import android.content.Context
import android.content.SharedPreferences
import com.example.motospeedo.data.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("motospeedo_prefs")
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("motospeedo_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideSettingsRepository(@Named("motospeedo_prefs") prefs: SharedPreferences): SettingsRepository =
        SettingsRepository(prefs)
}
