package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Esqueleto ST para el integrante 1.
 * Feature: flujo de inscripcion de aspirante.
 */
@Disabled("Esqueleto base para implementar pruebas del flujo de inscripcion")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoInscripcionST extends BaseSistemaST {

    @Test
    @Order(1)
    void listarAspirantesPaginado_debeRetornar200YTotalRecords() {
        // TODO integrante 1: GET /aspirantes_datos?first=0&max=50
    }

    @Test
    @Order(2)
    void obtenerAspiranteExistentePorId_debeRetornar200() {
        // TODO integrante 1: GET /aspirantes_datos/{id}
    }

    @Test
    @Order(3)
    void crearInscripcionValida_debeRetornar201YSerConsultable() {
        // TODO integrante 1: POST /inscripciones_prueba y luego GET por id
    }

    @Test
    @Order(4)
    void agregarCarrerasElegidas_debePersistirPrioridades() {
        // TODO integrante 1: POST /carreras_elegidas para una inscripcion creada
    }

    @Test
    @Order(5)
    void consultarProcesoAdmision_debeRetornarEstadoEsperado() {
        // TODO integrante 1: GET /proceso_admision_aspirante/{idInscripcion}
    }

    @Test
    @Order(6)
    void crearInscripcionConAspiranteInexistente_debeRetornarError() {
        // TODO integrante 1: escenario negativo por FK invalida
    }

    @Test
    @Order(7)
    void consultarInscripcionInexistente_debeRetornar404() {
        // TODO integrante 1: GET /inscripciones_prueba/{idInexistente}
    }
}
