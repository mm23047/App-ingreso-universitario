package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AreasConocimientoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar Áreas de Conocimiento.
 * 
 * Base: /resources/v1/areas
 */
@Path("areas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AreasConocimientoResource extends AbstractResource<AreasConocimiento> {

    @Inject
    private AreasConocimientoDAO areasDAO;

    @Inject
    private TemaDAO temaDAO;

    @Override
    protected AreasConocimientoDAO getDAO() {
        return areasDAO;
    }

    /**
     * GET /areas
     * Retorna lista paginada de áreas de conocimiento.
     */
    @GET
    public Response listAreas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /areas
     * Crea una nueva área de conocimiento.
     */
    @POST
    public Response createArea(AreasConocimiento area, @Context UriInfo uriInfo) {
        if (area == null || area.getNombreArea() == null || area.getNombreArea().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre del área es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "nombreArea")
                    .build();
        }

        try {
            areasDAO.crear(area);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(area.getIdAreaConocimiento().toString())
                    .build();
            return Response.created(location)
                    .entity(area)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el área")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /areas/{idArea}
     * Obtiene una área específica por ID.
     */
    @GET
    @Path("{idArea}")
    public Response getArea(@PathParam("idArea") String idAreaStr) {
        try {
            UUID idArea = UUID.fromString(idAreaStr);
            AreasConocimiento area = areasDAO.leer(idArea);
            if (area == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Área no encontrada")
                        .header(RestHeaders.NOT_FOUND_ID, idAreaStr)
                        .build();
            }
            return Response.ok(area).build();
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
     * PUT /areas/{idArea}
     * Actualiza una área existente.
     */
    @PUT
    @Path("{idArea}")
    public Response updateArea(@PathParam("idArea") String idAreaStr, AreasConocimiento area) {
        try {
            UUID idArea = UUID.fromString(idAreaStr);
            AreasConocimiento existente = areasDAO.leer(idArea);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAreaStr)
                        .build();
            }

            area.setIdAreaConocimiento(idArea);
            AreasConocimiento actualizada = areasDAO.actualizar(area);
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
     * DELETE /areas/{idArea}
     * Elimina una área. Solo permite si no tiene temas asociados.
     */
    @DELETE
    @Path("{idArea}")
    public Response deleteArea(@PathParam("idArea") String idAreaStr) {
        try {
            UUID idArea = UUID.fromString(idAreaStr);
            AreasConocimiento existente = areasDAO.leer(idArea);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAreaStr)
                        .build();
            }

            // Validación: No permitir eliminar si tiene temas asociados
            List<Tema> temas = temaDAO.findByArea(idArea);
            if (!temas.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar un área con temas asociados")
                        .header(RestHeaders.CONFLICT_REASON, "Area tiene " + temas.size() + " temas")
                        .build();
            }

            areasDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /areas/{idArea}/temas
     * Retorna todos los temas de un área.
     */
    @GET
    @Path("{idArea}/temas")
    public Response getTemasByArea(
            @PathParam("idArea") String idAreaStr,
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        try {
            UUID idArea = UUID.fromString(idAreaStr);
            AreasConocimiento existente = areasDAO.leer(idArea);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAreaStr)
                        .build();
            }

            List<Tema> temas = temaDAO.findByArea(idArea);
            // Aplicar paginación manual
            int fromIndex = Math.min(first, temas.size());
            int toIndex = Math.min(first + max, temas.size());
            List<Tema> paginados = temas.subList(fromIndex, toIndex);

            return Response.ok(paginados)
                    .header(RestHeaders.TOTAL_RECORDS, temas.size())
                    .build();
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
     * POST /areas/{idArea}/temas
     * Crea un nuevo tema dentro de un área.
     * El payload NO debe incluir idArea (se obtiene de la URI).
     */
    @POST
    @Path("{idArea}/temas")
    public Response createTemaInArea(@PathParam("idArea") String idAreaStr, Tema tema, @Context UriInfo uriInfo) {
        if (tema == null || tema.getNombreTema() == null || tema.getNombreTema().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre del tema es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "nombreTema")
                    .build();
        }

        try {
            UUID idArea = UUID.fromString(idAreaStr);
            AreasConocimiento area = areasDAO.leer(idArea);
            if (area == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAreaStr)
                        .build();
            }

            // Asignar área al tema (inyectada desde la URI)
            tema.setAreaConocimiento(area);
            // El idTemaPadre puede venir en el payload o ser null
            temaDAO.crear(tema);

            URI location = uriInfo.getBaseUriBuilder()
                    .path("temas/{idTema}")
                    .build(tema.getIdTema());
            return Response.created(location)
                    .entity(tema)
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
}
