package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar Pruebas de Admisión.
 * 
 * Base: /resources/v1/pruebas_admision
 */
@Path("pruebas_admision")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PruebasAdmisionResource extends AbstractResource<PruebasAdmision> {

    @Inject
    private PruebasAdmisionDAO pruebasAdmisionDAO;

    @Override
    protected PruebasAdmisionDAO getDAO() {
        return pruebasAdmisionDAO;
    }

    /**
     * GET /pruebas_admision
     * Retorna lista paginada de pruebas.
     */
    @GET
    public Response listPruebas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /pruebas_admision
     * Crea una nueva prueba de admisión.
     */
    @POST
    public Response createPrueba(PruebasAdmision prueba, @Context UriInfo uriInfo) {
        if (prueba == null || prueba.getNombrePrueba() == null || prueba.getNombrePrueba().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre de la prueba es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "nombrePrueba")
                    .build();
        }

        try {
            pruebasAdmisionDAO.crear(prueba);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(prueba.getIdPruebaAdmision().toString())
                    .build();
            return Response.created(location)
                    .entity(prueba)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /pruebas_admision/{idPrueba}
     * Obtiene una prueba específica.
     */
    @GET
    @Path("{idPrueba}")
    public Response getPrueba(@PathParam("idPrueba") String idPruebaStr) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            PruebasAdmision prueba = pruebasAdmisionDAO.leer(idPrueba);
            if (prueba == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPruebaStr)
                        .build();
            }
            return Response.ok(prueba).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID inválido")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /pruebas_admision/{idPrueba}
     * Actualiza una prueba.
     */
    @PUT
    @Path("{idPrueba}")
    public Response updatePrueba(@PathParam("idPrueba") String idPruebaStr, PruebasAdmision prueba) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            PruebasAdmision existente = pruebasAdmisionDAO.leer(idPrueba);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPruebaStr)
                        .build();
            }

            prueba.setIdPruebaAdmision(idPrueba);
            PruebasAdmision actualizada = pruebasAdmisionDAO.actualizar(prueba);
            return Response.ok(actualizada).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /pruebas_admision/{idPrueba}
     * Elimina una prueba.
     */
    @DELETE
    @Path("{idPrueba}")
    public Response deletePrueba(@PathParam("idPrueba") String idPruebaStr) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            PruebasAdmision existente = pruebasAdmisionDAO.leer(idPrueba);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPruebaStr)
                        .build();
            }

            pruebasAdmisionDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
