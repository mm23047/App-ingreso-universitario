package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

/**
 * Recurso REST de SOLO LECTURA para Exámenes Realizados.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone únicamente operaciones de consulta bajo /resources/v1/examenes_realizados
 * <p>
 * Los exámenes realizados son datos históricos generados por el sistema;
 * no se permiten POST, PUT ni DELETE desde la API para preservar la integridad de los resultados.
 * </p>
 */
@Path("examenes_realizados")
public class ExamenesRealizadoResource extends AbstractResource<ExamenesRealizado> {

    @Inject
    ExamenesRealizadoDAO examenesRealizadoDAO;

    @Override
    protected IngresoDefaultDataAccess<ExamenesRealizado> getDAO() {
        return examenesRealizadoDAO;
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") Integer id) {
        if (id != null) {
            try {
                ExamenesRealizado resp = examenesRealizadoDAO.leer(id);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }
}
