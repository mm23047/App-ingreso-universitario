package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    @Inject
    private TemaDAO temaDAO;

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
     * NUEVO ENDPOINT: GET /pruebas_admision/activas
     * Retorna únicamente las pruebas vigentes.
     * ¡Este es el que llamará tu Frontend para mostrar en la vista del aspirante!
     */
    @GET
    @Path("activas")
    public Response getPruebasActivas() {
        try {
            List<PruebasAdmision> activas = pruebasAdmisionDAO.findActivas();
            return Response.ok(activas).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al recuperar las pruebas de admisión activas.")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /pruebas_admision/{idPrueba}/areas
     * Retorna las áreas de conocimiento con sus temas que participan en la prueba indicada.
     */
    @GET
    @Path("{idPrueba}/areas")
    public Response getAreasByPrueba(@PathParam("idPrueba") String idPruebaStr) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            List<Tema> temas = temaDAO.findByPrueba(idPrueba);

            LinkedHashMap<UUID, AreaConTemasDTO> mapa = new LinkedHashMap<>();
            for (Tema t : temas) {
                UUID areaId = t.getAreaConocimiento().getIdAreaConocimiento();
                mapa.computeIfAbsent(areaId, k -> new AreaConTemasDTO(t.getAreaConocimiento()))
                        .addTema(t.getIdTema(), t.getNombreTema());
            }

            List<AreaConTemasDTO> resultado = new ArrayList<>(mapa.values());
            return Response.ok(resultado)
                    .header(RestHeaders.TOTAL_RECORDS, resultado.size())
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
     * NUEVO ENDPOINT: PUT /pruebas_admision/{idPrueba}/activar
     * Ejecuta la regla de negocio exclusiva: Enciende esta prueba y apaga todas las demás.
     */
    @PUT
    @Path("{idPrueba}/activar")
    public Response activarPruebaExclusiva(@PathParam("idPrueba") String idPruebaStr) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);

            // Ejecutamos el método transaccional de tu DAO
            pruebasAdmisionDAO.setPruebaActivaExclusiva(idPrueba);

            return Response.ok()
                    .entity("La prueba ha sido activada de forma exclusiva con éxito.")
                    .build();
        } catch (IllegalArgumentException e) {
            // Captura si el UUID es inválido o si el idPrueba no existía en la consulta em.find del DAO
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    public static class AreaConTemasDTO {
        public UUID idAreaConocimiento;
        public String nombreArea;
        public List<TemaResumenDTO> temas = new ArrayList<>();

        public AreaConTemasDTO(AreasConocimiento area) {
            this.idAreaConocimiento = area.getIdAreaConocimiento();
            this.nombreArea = area.getNombreArea();
        }

        public void addTema(UUID idTema, String nombreTema) {
            temas.add(new TemaResumenDTO(idTema, nombreTema));
        }
    }

    public static class TemaResumenDTO {
        public UUID idTema;
        public String nombreTema;

        public TemaResumenDTO(UUID idTema, String nombreTema) {
            this.idTema = idTema;
            this.nombreTema = nombreTema;
        }
    }
}
