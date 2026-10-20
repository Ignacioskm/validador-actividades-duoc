package com.validador_actividades_duoc.data.local

import android.content.Context
import androidx.room.Room
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.data.repository.InventarioRepository

object AppContainer {
    private var dbInstance: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return dbInstance ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "validador_duoc_db"
            )
                .fallbackToDestructiveMigration()
                .build()
            dbInstance = instance
            instance
        }
    }
}
