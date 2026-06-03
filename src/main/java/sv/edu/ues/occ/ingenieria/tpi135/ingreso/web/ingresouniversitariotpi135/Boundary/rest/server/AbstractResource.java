package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;

import java.util.List;

/**
 * Clase base genérica para recursos REST.
 * Proporciona métodos comunes de paginación y acceso.
 */
public abstract class AbstractResource<T> {

    /**
     * Cada recurso debe implementar y devolver su DAO correspondiente.
     */
    protected abstract IngresoDefaultDataAccess<T> getDAO();

    /**
     * GET paginado: Retorna una lista de entidades con paginación.
     * @param first Índice de inicio (default: 0)
     * @param max Cantidad máxima de registros (default: 50)
     * @return Response con JSON de entidades + header TOTAL_RECORDS
     */
    /**
     * Método auxiliar de Java (SIN ANOTACIONES JAX-RS).
     * El Resource hijo se encarga de recibir los parámetros HTTP y pasárselos a este método.
     */
    protected Response findRange(int first, int max) {
        try {
            List<T> entidades = getDAO().findRange(first, max);
            long total = getDAO().count();
            return Response.ok(entidades, MediaType.APPLICATION_JSON)
                    .header(RestHeaders.TOTAL_RECORDS, total)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al recuperar registros")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
