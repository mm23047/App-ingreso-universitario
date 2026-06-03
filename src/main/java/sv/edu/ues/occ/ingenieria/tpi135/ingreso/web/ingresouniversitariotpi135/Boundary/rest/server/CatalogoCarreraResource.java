package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.net.URI;

/**
 * Recurso REST para gestionar Carreras (Catálogo).
 * 
 * Base: /resources/v1/carreras
 */
@Path("carreras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CatalogoCarreraResource extends AbstractResource<CatalogoCarrera> {

    @Inject
    private CatalogoCarreraDAO catalogoCarreraDAO;

    @Override
    protected CatalogoCarreraDAO getDAO() {
        return catalogoCarreraDAO;
    }

    /**
     * GET /carreras
     * Retorna lista paginada de carreras.
     */
    @GET
    public Response listCarreras(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /carreras
     * Crea una nueva carrera.
     */
    @POST
    public Response createCarrera(CatalogoCarrera carrera, @Context UriInfo uriInfo) {
        if (carrera == null || carrera.getIdCarrera() == null || carrera.getIdCarrera().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID de la carrera es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "idCarrera")
                    .build();
        }

        try {
            catalogoCarreraDAO.crear(carrera);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(carrera.getIdCarrera())
                    .build();
            return Response.created(location)
                    .entity(carrera)
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
     * GET /carreras/{idCarrera}
     * Obtiene una carrera específica.
     */
    @GET
    @Path("{idCarrera}")
    public Response getCarrera(@PathParam("idCarrera") String idCarrera) {
        try {
            CatalogoCarrera carrera = catalogoCarreraDAO.leer(idCarrera);
            if (carrera == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idCarrera)
                        .build();
            }
            return Response.ok(carrera).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /carreras/{idCarrera}
     * Actualiza una carrera.
     */
    @PUT
    @Path("{idCarrera}")
    public Response updateCarrera(@PathParam("idCarrera") String idCarrera, CatalogoCarrera carrera) {
        try {
            CatalogoCarrera existente = catalogoCarreraDAO.leer(idCarrera);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idCarrera)
                        .build();
            }

            carrera.setIdCarrera(idCarrera);
            CatalogoCarrera actualizada = catalogoCarreraDAO.actualizar(carrera);
            return Response.ok(actualizada).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /carreras/{idCarrera}
     * Elimina una carrera.
     */
    @DELETE
    @Path("{idCarrera}")
    public Response deleteCarrera(@PathParam("idCarrera") String idCarrera) {
        try {
            CatalogoCarrera existente = catalogoCarreraDAO.leer(idCarrera);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idCarrera)
                        .build();
            }

            catalogoCarreraDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
