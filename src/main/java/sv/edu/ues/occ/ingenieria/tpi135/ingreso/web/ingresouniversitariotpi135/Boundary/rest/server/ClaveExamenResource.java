package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar las Claves de Examen directamente.
 * Base: /resources/v1/claves
 */
@Path("claves")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClaveExamenResource extends AbstractResource<ClavesExamen> {

    @Inject
    private ClavesExamanDAO clavesExamanDAO;

    @Inject
    private ExamenRealizadoDAO examenRealizadoDAO;

    @Override
    protected ClavesExamanDAO getDAO() {
        return clavesExamanDAO;
    }

    /**
     * GET /claves/{idClave}
     * Obtiene una clave específica por su ID.
     */
    @GET
    @Path("{idClave}")
    public Response getClave(@PathParam("idClave") String idClaveStr) {
        try {
            UUID idClave = UUID.fromString(idClaveStr);
            ClavesExamen clave = clavesExamanDAO.leer(idClave);

            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Clave de examen no encontrada.")
                        .header(RestHeaders.NOT_FOUND_ID, idClaveStr)
                        .build();
            }
            return Response.ok(clave).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de UUID inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /claves
     * Permite la creación de nuevas variantes (Claves).
     */
    @POST
    public Response crearClave(ClavesExamen nuevaClave, @Context UriInfo uriInfo) {
        if (nuevaClave == null || nuevaClave.getNombreClave() == null || nuevaClave.getNombreClave().isBlank() || nuevaClave.getPruebaAdmision() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre de la clave y la prueba asociada son campos mandatorios.")
                    .build();
        }
        try {
            clavesExamanDAO.crear(nuevaClave);
            URI uriCreada = uriInfo.getAbsolutePathBuilder().path(nuevaClave.getIdClaveExaman().toString()).build();
            return Response.created(uriCreada).entity(nuevaClave).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("X-Server-Exception", e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /claves/{idClave}
     * Actualiza el nombre de una clave mutando el estado del objeto gestionado por JPA.
     */
    @PUT
    @Path("{idClave}")
    public Response updateClave(@PathParam("idClave") String idClaveStr, ClavesExamen datosEntrantes) {
        if (datosEntrantes == null || datosEntrantes.getNombreClave() == null || datosEntrantes.getNombreClave().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nuevo nombre de la clave es obligatorio.")
                    .build();
        }

        try {
            UUID idClave = UUID.fromString(idClaveStr);
            ClavesExamen existente = clavesExamanDAO.leer(idClave);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Modificamos el registro de la BD, conservando sus otras relaciones intactas
            existente.setNombreClave(datosEntrantes.getNombreClave());

            ClavesExamen guardada = clavesExamanDAO.actualizar(existente);
            return Response.ok(guardada).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("X-Server-Exception", e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /claves/{idClave}
     * Elimina una clave (Solo si ningún aspirante ha generado un examen con ella).
     */
    @DELETE
    @Path("{idClave}")
    public Response deleteClave(@PathParam("idClave") String idClaveStr) {
        try {
            UUID idClave = UUID.fromString(idClaveStr);
            ClavesExamen existente = clavesExamanDAO.leer(idClave);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Consumo limpio desde el DAO para respetar integridad referencial
            long examenesAsociados = examenRealizadoDAO.countByClaveExamen(idClave);

            if (examenesAsociados > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar la clave porque ya pertenece a uno o más exámenes de aspirantes.")
                        .build();
            }

            clavesExamanDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de UUID inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("X-Server-Exception", e.getMessage())
                    .build();
        }
    }
}