package com.example.solicitudes.model

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasEvidenciaTest {

    @Test
    fun aceptaFormatosPermitidosDentroDelLimite() {
        assertNull(ReglasEvidencia.validar("image/jpeg", 200_000))
        assertNull(ReglasEvidencia.validar("image/png", 1_000))
        assertNull(ReglasEvidencia.validar("application/pdf", ReglasEvidencia.TAMANO_MAXIMO_BYTES))
    }

    @Test
    fun rechazaFormatoNoPermitido() {
        assertNotNull(ReglasEvidencia.validar("video/mp4", 1_000))
        assertNotNull(ReglasEvidencia.validar(null, 1_000))
    }

    @Test
    fun rechazaArchivoVacioODemasiadoGrande() {
        assertNotNull(ReglasEvidencia.validar("image/png", 0))
        assertNotNull(ReglasEvidencia.validar("image/png", ReglasEvidencia.TAMANO_MAXIMO_BYTES + 1))
    }
}
