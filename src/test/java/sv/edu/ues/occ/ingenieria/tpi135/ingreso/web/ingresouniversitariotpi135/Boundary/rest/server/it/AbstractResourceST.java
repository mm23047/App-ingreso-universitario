package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

/**
 * Clase base para pruebas de integracion de Resources REST.
 *
 * Reutiliza la infraestructura de BaseSistemaBDD (PostgreSQL + Liberty + WAR desplegada
 * + cliente HTTP Jersey) y expone un punto comun para que las clases *ResourceST puedan
 * ejecutarse sobre el mismo entorno contenedorizado que las pruebas de sistema BDD,
 * pero sin depender de Cucumber.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractResourceST extends BaseSistemaBDD {

    /**
     * Inicializa la infraestructura de contenedores (PostgreSQL + Liberty + cliente HTTP)
     * una sola vez para toda la suite de pruebas de integracion REST.
     *
    * Se apoya en el metodo Singleton BaseSistemaBDD.init().
     */
    @BeforeAll
    void inicializarInfraestructuraHttp() {
        BaseSistemaBDD.init();
    }
}
