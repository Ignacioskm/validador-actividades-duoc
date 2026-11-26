package com.validador_actividades_duoc.data.remote

import com.validador_actividades_duoc.domain.model.Departamento
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InventarioRemoteTest : StringSpec({

    val deptoValido = Departamento.entries.first()

    afterSpec {
        RetrofitInstance.setBaseUrl("http://52.1.226.180:3000/")
    }

    "RecursoDto.toEntity mapea todos los campos correctamente" {
        val dto = RecursoDto(
            id = 42L,
            departamento = deptoValido.name,
            nombreItem = "Proyector HDMI",
            stockTotal = 6,
            activo = false,
            stockDisponible = 4
        )

        val entity = dto.toEntity()

        entity.id shouldBe 42L
        entity.departamento shouldBe deptoValido
        entity.nombreItem shouldBe "Proyector HDMI"
        entity.stockTotal shouldBe 6
        entity.activo shouldBe false
    }

    "RecursoDto.toEntity lanza excepción si el departamento no existe" {
        val dtoInvalido = RecursoDto(
            id = 1L,
            departamento = "DEPARTAMENTO_INEXISTENTE",
            nombreItem = "Mesa",
            stockTotal = 2
        )

        shouldThrow<IllegalArgumentException> {
            dtoInvalido.toEntity()
        }
    }

    "modelos de request conservan sus valores esperados" {
        val crearReq = CrearRecursoRequest(
            departamento = deptoValido.name,
            nombreItem = "Parlante",
            stockTotal = 3
        )
        val stockReq = ActualizarStockRequest(stockTotal = 8)
        val itemReq = ItemCompromisoRequest(recursoId = 10L, cantidad = 2)
        val compromisoReq = CompromisoRequest(
            reservaRef = "RES-100",
            fecha = "2026-12-15",
            bloqueInicio = 1,
            bloqueFin = 3,
            items = listOf(itemReq)
        )

        crearReq.departamento shouldBe deptoValido.name
        crearReq.nombreItem shouldBe "Parlante"
        crearReq.stockTotal shouldBe 3
        stockReq.stockTotal shouldBe 8
        itemReq.recursoId shouldBe 10L
        itemReq.cantidad shouldBe 2
        compromisoReq.reservaRef shouldBe "RES-100"
        compromisoReq.fecha shouldBe "2026-12-15"
        compromisoReq.bloqueInicio shouldBe 1
        compromisoReq.bloqueFin shouldBe 3
        compromisoReq.items.size shouldBe 1
    }

    "RetrofitInstance formatea baseUrl con barra final y expone inventarioApi" {
        RetrofitInstance.setBaseUrl("http://192.168.1.50:3000")
        RetrofitInstance.getBaseUrl() shouldBe "http://192.168.1.50:3000/"
        RetrofitInstance.inventarioApi.shouldNotBeNull()

        RetrofitInstance.setBaseUrl("http://127.0.0.1:3000/")
        RetrofitInstance.getBaseUrl() shouldBe "http://127.0.0.1:3000/"
        RetrofitInstance.inventarioApi.shouldNotBeNull()
    }
})