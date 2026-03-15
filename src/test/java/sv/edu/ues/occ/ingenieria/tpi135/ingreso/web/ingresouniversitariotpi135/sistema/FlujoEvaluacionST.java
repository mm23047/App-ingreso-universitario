package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Esqueleto ST para el integrante 3.
 * Feature: flujo de evaluacion y resultado de admision.
 */
@Disabled("Esqueleto base para implementar pruebas del flujo de evaluacion")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoEvaluacionST extends BaseSistemaST {

    @Test
    @Order(1)
    void listarClavesExamenPaginado_debeRetornar200YTotalRecords() {
        // TODO integrante 3: GET /claves_examen?first=0&max=50
    }

    @Test
    @Order(2)
    void crearClaveExamenValida_debeRetornar201YSerConsultable() {
        // TODO integrante 3: POST /claves_examen y luego GET por id
    }

    @Test
    @Order(3)
    void asociarPreguntasPorClave_debeRetornar201YSerConsultable() {
        // TODO integrante 3: POST /preguntas_por_clave y luego GET por PK compuesta
    }

    @Test
    @Order(4)
    void registrarExamenRealizado_debeRetornar201YSerConsultable() {
        // TODO integrante 3: POST /examenes_realizados y luego GET por id
    }

    @Test
    @Order(5)
    void actualizarProcesoAdmisionSegunResultado_debeRetornar200() {
        // TODO integrante 3: PUT /proceso_admision_aspirante/{idInscripcion}
    }

    @Test
    @Order(6)
    void registrarExamenConAsignacionInexistente_debeRetornarError() {
        // TODO integrante 3: escenario negativo por FK invalida
    }

    @Test
    @Order(7)
    void consultarPreguntaPorClaveInexistente_debeRetornar404() {
        // TODO integrante 3: GET /preguntas_por_clave/{idClave}/{idPregunta}
    }
}