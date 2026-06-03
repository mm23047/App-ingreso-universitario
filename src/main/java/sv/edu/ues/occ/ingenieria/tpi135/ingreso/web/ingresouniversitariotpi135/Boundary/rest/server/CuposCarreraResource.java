package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CuposCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar la parametrización de cupos por carrera, prueba y etapa.
 * Base: /resources/v1/cupos_carrera
 */
@Path("cupos_carrera")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CuposCarreraResource extends AbstractResource<CuposCarrera> {

    @Inject
    private CuposCarreraDAO cuposCarreraDAO;

    @Override
    protected CuposCarreraDAO getDAO() {
        return cuposCarreraDAO;
    }

    /**
     * GET /cupos_carrera
     * Retorna lista paginada de cupos por carrera.
     */
    @GET
    public Response listCupos(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /cupos_carrera
     * Crea una nueva parametrización de cupos.
     */
    @POST
    public Response createCupo(CuposCarrera cupo, @Context UriInfo uriInfo) {
        if (cupo == null
                || cupo.getPruebaAdmision() == null
                || cupo.getCatalogoCarrera() == null
                || cupo.getEtapaAdmision() == null
                || cupo.getCupos() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Prueba, carrera, etapa y cupos son campos obligatorios.")
                    .header(RestHeaders.MISSING_PARAMETER, "pruebaAdmision, catalogoCarrera, etapaAdmision, cupos")
                    .build();
        }

        try {
            cuposCarreraDAO.crear(cupo);
            UUID idPrueba = cupo.getIdCupoCarrera().getIdPrueba();
            String idCarrera = cupo.getIdCupoCarrera().getIdCarrera();
            UUID idEtapa = cupo.getIdCupoCarrera().getIdEtapa();
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(idPrueba.toString())
                    .path(idCarrera)
                    .path(idEtapa.toString())
                    .build();
            return Response.created(location).entity(cupo).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .header(RestHeaders.MISSING_PARAMETER, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /cupos_carrera/{idPrueba}/{idCarrera}/{idEtapa}
     * Obtiene la parametrización de cupos por clave compuesta.
     */
    @GET
    @Path("{idPrueba}/{idCarrera}/{idEtapa}")
    public Response getCupo(
            @PathParam("idPrueba") String idPruebaStr,
            @PathParam("idCarrera") String idCarrera,
            @PathParam("idEtapa") String idEtapaStr) {
        try {
            CuposCarreraId id = buildId(idPruebaStr, idCarrera, idEtapaStr);
            CuposCarrera cupo = cuposCarreraDAO.leer(id);
            if (cupo == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPruebaStr + "/" + idCarrera + "/" + idEtapaStr)
                        .build();
            }
            return Response.ok(cupo).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parámetros de ruta inválidos: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /cupos_carrera/{idPrueba}/{idCarrera}/{idEtapa}
     * Actualiza la cantidad de cupos para una combinación existente.
     */
    @PUT
    @Path("{idPrueba}/{idCarrera}/{idEtapa}")
    public Response updateCupo(
            @PathParam("idPrueba") String idPruebaStr,
            @PathParam("idCarrera") String idCarrera,
            @PathParam("idEtapa") String idEtapaStr,
            CuposCarrera datosActualizacion) {
        try {
            CuposCarreraId id = buildId(idPruebaStr, idCarrera, idEtapaStr);
            CuposCarrera existente = cuposCarreraDAO.leer(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPruebaStr + "/" + idCarrera + "/" + idEtapaStr)
                        .build();
            }
            if (datosActualizacion == null || datosActualizacion.getCupos() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La cantidad de cupos es obligatoria para actualizar.")
                        .header(RestHeaders.MISSING_PARAMETER, "cupos")
                        .build();
            }
            existente.setCupos(datosActualizacion.getCupos());
            CuposCarrera actualizado = cuposCarreraDAO.actualizar(existente);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /cupos_carrera/{idPrueba}/{idCarrera}/{idEtapa}
     * Elimina una parametrización de cupos.
     */
    @DELETE
    @Path("{idPrueba}/{idCarrera}/{idEtapa}")
    public Response deleteCupo(
            @PathParam("idPrueba") String idPruebaStr,
            @PathParam("idCarrera") String idCarrera,
            @PathParam("idEtapa") String idEtapaStr) {
        try {
            CuposCarreraId id = buildId(idPruebaStr, idCarrera, idEtapaStr);
            CuposCarrera existente = cuposCarreraDAO.leer(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPruebaStr + "/" + idCarrera + "/" + idEtapaStr)
                        .build();
            }
            cuposCarreraDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    private CuposCarreraId buildId(String idPruebaStr, String idCarrera, String idEtapaStr) {
        UUID idPrueba = UUID.fromString(idPruebaStr);
        UUID idEtapa = UUID.fromString(idEtapaStr);
        CuposCarreraId id = new CuposCarreraId();
        id.setIdPrueba(idPrueba);
        id.setIdCarrera(idCarrera);
        id.setIdEtapa(idEtapa);
        return id;
    }
}
