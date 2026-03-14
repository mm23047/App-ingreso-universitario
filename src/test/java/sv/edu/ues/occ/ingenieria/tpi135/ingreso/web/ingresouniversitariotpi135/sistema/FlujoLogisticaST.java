package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Esqueleto ST para el integrante 2.
 * Feature: flujo logistico de aplicacion de prueba.
 */
@Disabled("Esqueleto base para implementar pruebas del flujo logistico")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoLogisticaST extends BaseSistemaST {

    @Test
    @Order(1)
    void listarTurnosPaginado_debeRetornar200YTotalRecords() {
        // TODO integrante 2: GET /turnos_examen?first=0&max=50
    }

    @Test
    @Order(2)
    void crearTurnoValido_debeRetornar201YSerConsultable() {
        // TODO integrante 2: POST /turnos_examen y luego GET por id
    }

    @Test
    @Order(3)
    void crearAulaValidaParaTurno_debeRetornar201YSerConsultable() {
        // TODO integrante 2: POST /aulas_examen y luego GET por id
    }

    @Test
    @Order(4)
    void asignarPupitreAInscripcionValida_debeRetornar201YSerConsultable() {
        // TODO integrante 2: POST /asignaciones_aula_pupitre y luego GET por id
    }

    @Test
    @Order(5)
    void crearAulaConTurnoInexistente_debeRetornarError() {
        // TODO integrante 2: escenario negativo por FK invalida
    }

    @Test
    @Order(6)
    void asignarPupitreConInscripcionInexistente_debeRetornarError() {
        // TODO integrante 2: escenario negativo por FK invalida
    }
}
