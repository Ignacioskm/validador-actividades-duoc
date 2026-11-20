package com.validador_actividades_duoc.data.local

import android.content.Context
import androidx.room.Room
import com.validador_actividades_duoc.data.local.entity.RecursoInventarioEntity
import com.validador_actividades_duoc.data.local.entity.ReservaEntity
import com.validador_actividades_duoc.data.local.entity.UsuarioEntity
import com.validador_actividades_duoc.data.repository.InventarioRepository
import com.validador_actividades_duoc.data.repository.ReservaRepository
import com.validador_actividades_duoc.domain.model.Departamento
import com.validador_actividades_duoc.domain.model.RolUsuario
import java.time.LocalDate
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppContainer {

    @Volatile
    private var dbInstance: AppDatabase? = null


    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE usuarios ADD COLUMN firebaseUid TEXT"
            )

            db.execSQL(
                """
            CREATE UNIQUE INDEX IF NOT EXISTS
            index_usuarios_firebaseUid
            ON usuarios(firebaseUid)
            """.trimIndent()
            )
        }
    }


    fun provideDatabase(context: Context): AppDatabase =
        dbInstance ?: synchronized(this) {
            dbInstance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "validador_duoc_db"
            )
                .addMigrations(MIGRATION_3_4)
                .build()
                .also { dbInstance = it }
        }

    fun provideReservaRepository(context: Context) = ReservaRepository(provideDatabase(context))

    fun provideInventarioRepository(context: Context) =
        InventarioRepository(provideDatabase(context).inventarioDao())

    /**
     * Datos de prueba. Solo se insertan si la base está vacía.
     * Correos de acceso rápido: admin@duoc.cl, profesor@duoc.cl,
     * porteria@duoc.cl, digitales@duoc.cl, auxiliares@duoc.cl, comunicacion@duoc.cl
     */
    suspend fun sembrarDatosIniciales(context: Context) {
        val db = provideDatabase(context)
        val usuarios = db.usuarioDao()
        if (usuarios.contar() > 0) return

        usuarios.insertarUsuario(
            UsuarioEntity(correo = "admin@duoc.cl", nombre = "Administrador", rol = RolUsuario.ADMIN)
        )
        Departamento.entries.forEach { depto ->
            usuarios.insertarUsuario(
                UsuarioEntity(
                    correo = "${depto.name.lowercase()}@duoc.cl",
                    nombre = "Validador ${depto.etiqueta}",
                    rol = RolUsuario.VALIDADOR,
                    departamento = depto
                )
            )
        }
        val profesorId = usuarios.insertarUsuario(
            UsuarioEntity(correo = "profesor@duoc.cl", nombre = "Profesor Demo", rol = RolUsuario.SOLICITANTE)
        )

        val inventario = db.inventarioDao()
        suspend fun recurso(depto: Departamento, nombre: String, stock: Int): Long =
            inventario.insertarOActualizarRecurso(
                RecursoInventarioEntity(departamento = depto, nombreItem = nombre, stockTotal = stock)
            )

        val guardias = recurso(Departamento.PORTERIA, "Guardia de seguridad", 6)
        recurso(Departamento.PORTERIA, "Estacionamiento para proveedor", 5)
        val micros = recurso(Departamento.DIGITALES, "Micrófono inalámbrico", 6)
        recurso(Departamento.DIGITALES, "Proyector", 4)
        recurso(Departamento.DIGITALES, "Computador", 3)
        val mesas = recurso(Departamento.AUXILIARES, "Mesa plegable", 20)
        recurso(Departamento.AUXILIARES, "Toldo", 3)
        val fotos = recurso(Departamento.COMUNICACION, "Registro de fotos y video", 2)
        recurso(Departamento.COMUNICACION, "Difusión en RSS", 5)

        // Solicitud de ejemplo para probar los paneles sin esperar al formulario de Persona 2
        ReservaRepository(db).crearReserva(
            ReservaEntity(
                solicitanteId = profesorId,
                nombreActividad = "Encuentro de Innovación (DEMO)",
                fecha = LocalDate.now().plusDays(10).toString(),
                bloqueInicio = 4,
                bloqueFin = 6
            ),
            mapOf(guardias to 4, micros to 2, mesas to 10, fotos to 1)
        )
    }
}
