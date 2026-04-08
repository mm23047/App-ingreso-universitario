package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.util.List;
import java.util.UUID;

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

    @QueryParam("aspiranteId")
    String aspiranteIdParam;

    @QueryParam("pruebaId")
    String pruebaIdParam;

    @Override
    protected IngresoDefaultDataAccess<ExamenesRealizado> getDAO() {
        return examenesRealizadoDAO;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0")  @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max")   int max) {

        try {

            if (aspiranteIdParam != null && !aspiranteIdParam.isBlank()) {
                try {
                    UUID aspiranteId = UUID.fromString(aspiranteIdParam);
                    List<ExamenesRealizado> list = examenesRealizadoDAO.findByAspiranteId(aspiranteId);
                    return Response.ok(list).build();
                } catch (IllegalArgumentException e) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "aspiranteId must be a valid UUID")
                            .build();
                }
            }

            if (pruebaIdParam != null && !pruebaIdParam.isBlank()) {
                try {
                    UUID pruebaId = UUID.fromString(pruebaIdParam);
                    List<ExamenesRealizado> list = examenesRealizadoDAO.findByPruebaId(pruebaId);
                    return Response.ok(list).build();
                } catch (IllegalArgumentException e) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "pruebaId must be a valid UUID")
                            .build();
                }
            }

            // Sin filtros: comportamiento paginado normal heredado
            return super.findRange(first, max);

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(SERVER_EXCEPTION, "Cannot access db")
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
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
