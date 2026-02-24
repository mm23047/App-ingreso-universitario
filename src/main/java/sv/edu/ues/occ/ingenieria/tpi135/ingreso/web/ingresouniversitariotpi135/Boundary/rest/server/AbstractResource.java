package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;

import java.util.List;

/**
 * Clase abstracta base para recursos REST del sistema de Ingreso Universitario.
 * Provee la operación GET paginada común a todos los recursos.
 *
 * @param <T> Tipo de entidad manejada por el recurso
 */
public abstract class AbstractResource<T> {

    /**
     * Método abstracto que cada recurso concreto debe implementar
     * para proveer acceso a su DAO específico.
     */
    protected abstract IngresoDefaultDataAccess<T> getDAO();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0")  @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max")   int max) {

        if (first >= 0 && max > 0 && max <= 100) {
            try {
                IngresoDefaultDataAccess<T> dao = getDAO();
                int total = dao.count();
                List<T> entities = dao.findRange(first, max);
                return Response.ok(entities)
                        .header(TOTAL_RECORDS, total)
                        .build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "first:" + first + ", max:" + max)
                .build();
    }
}
