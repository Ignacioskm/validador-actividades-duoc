package com.validador_actividades_duoc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.validador_actividades_duoc.data.local.dao.InventarioDao
import com.validador_actividades_duoc.data.local.dao.ReservaDao
import com.validador_actividades_duoc.data.local.dao.UsuarioDao
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.RequerimientoEntity
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        ReservaEntity::class,
        RecursoInventarioEntity::class,
        RequerimientoEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun reservaDao(): ReservaDao
    abstract fun inventarioDao(): InventarioDao
}
