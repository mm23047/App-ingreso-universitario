package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar los datos de los Aspirantes.
 * * Base: /resources/v1/aspirantes
 */
@Path("aspirantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AspirantesDatoResource extends AbstractResource<AspirantesDato> {

    @Inject
    private AspirantesDatoDAO aspirantesDAO;

    //Para Saber si existen pruebas activas
    @Inject
    private PruebasAdmisionDAO pruebasDAO;

    // TODO: Inyectar InscripcionesPruebaDAO cuando se implemente la validación de borrado
     @Inject
     private InscripcionesPruebaDAO inscripcionesDAO;

    @Override
    protected AspirantesDatoDAO getDAO() {
        return aspirantesDAO;
    }

    /**
     * GET /aspirantes
     * Retorna lista paginada de aspirantes o filtra por requerimiento de accesibilidad
     * Ejemplo: /aspirantes?usaSilla=true
     */
    @GET
    public Response listAspirantes(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max,
            @QueryParam("usaSilla") Boolean usaSilla) { // Nuevo parámetro opcional

        try {
            // Si el cliente envía ?usaSilla=true en la URL
            if (usaSilla != null && usaSilla) {
                // El DAO intercepta y trae solo a los que requieren silla de ruedas
                return Response.ok(aspirantesDAO.findByRequiereSillaRuedas()).build();
            }

            // Si no hay filtro, se comporta de forma estándar (paginación)
            return findRange(first, max);

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener la lista de aspirantes")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /aspirantes
     * Crea un nuevo aspirante.
     */
    @POST
    public Response createAspirante(AspirantesDato aspirante, @Context UriInfo uriInfo) {
        // Validaciones básicas de entrada
        if (aspirante == null || aspirante.getDui() == null || aspirante.getDui().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El DUI del aspirante es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "dui")
                    .build();
        }
        if (aspirante.getCorreo() == null || aspirante.getCorreo().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El correo electrónico es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "correo")
                    .build();
        }

        try {
            // El DAO validará unicidad de DUI/Correo y que sea mayor
            aspirantesDAO.crear(aspirante);

            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(aspirante.getId().toString())
                    .build();

            return Response.created(location)
                    .entity(aspirante)
                    .build();
        } catch (IllegalArgumentException e) {
            // Captura los errores de reglas de negocio del DAO (DUI repetido, edad menor, etc.)
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear el aspirante")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /aspirantes/{idAspirante}
     * Obtiene un aspirante específico por ID.
     */
    @GET
    @Path("{idAspirante}")
    public Response getAspirante(@PathParam("idAspirante") String idAspiranteStr) {
        try {
            UUID idAspirante = UUID.fromString(idAspiranteStr);
            AspirantesDato aspirante = aspirantesDAO.leer(idAspirante);

            if (aspirante == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Aspirante no encontrado")
                        .header(RestHeaders.NOT_FOUND_ID, idAspiranteStr)
                        .build();
            }
            return Response.ok(aspirante).build();
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
     * PUT /aspirantes/{idAspirante}
     * Actualiza un aspirante existente.
     */
    @PUT
    @Path("{idAspirante}")
    public Response updateAspirante(@PathParam("idAspirante") String idAspiranteStr, AspirantesDato aspirante) {
        // Validaciones básicas de entrada
        if (aspirante == null || aspirante.getDui() == null || aspirante.getDui().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El DUI del aspirante es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "dui")
                    .build();
        }
        if (aspirante.getCorreo() == null || aspirante.getCorreo().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El correo electrónico es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "correo")
                    .build();
        }
        try {
            UUID idAspirante = UUID.fromString(idAspiranteStr);
            AspirantesDato existente = aspirantesDAO.leer(idAspirante);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAspiranteStr)
                        .build();
            }

            // Aseguramos que el ID de la URL sea el que se actualiza
            aspirante.setId(idAspirante);
            AspirantesDato actualizado = aspirantesDAO.actualizar(aspirante);

            return Response.ok(actualizado).build();
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
     * DELETE /aspirantes/{idAspirante}
     * Elimina un aspirante.
     */
    @DELETE
    @Path("{idAspirante}")
    public Response deleteAspirante(@PathParam("idAspirante") String idAspiranteStr) {
        try {
            UUID idAspirante = UUID.fromString(idAspiranteStr);
            AspirantesDato existente = aspirantesDAO.leer(idAspirante);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAspiranteStr)
                        .build();
            }

            // TODO: Según el plan, se debe validar que no tenga inscripciones antes de borrar.
            // Validación de integridad: No borrar si tiene inscripciones
            List<InscripcionesPrueba> inscripciones = inscripcionesDAO.findByAspiranteId(idAspirante);
            if (inscripciones != null && !inscripciones.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar: El aspirante tiene inscripciones registradas.")
                        .header(RestHeaders.CONFLICT_REASON, "Tiene inscripciones")
                        .build();
            }

            aspirantesDAO.eliminar(existente);
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
     * GET /aspirantes/{idAspirante}/inscripciones
     * Lista las inscripciones de un aspirante específico.
     */
    @GET
    @Path("{idAspirante}/inscripciones")
    public Response getInscripcionesPorAspirante(@PathParam("idAspirante") String idAspiranteStr) {
        try {
            UUID idAspirante = UUID.fromString(idAspiranteStr);

            // 1. Verificamos que el aspirante exista (regla de oro del anidamiento)
            AspirantesDato aspirante = aspirantesDAO.leer(idAspirante);
            if (aspirante == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Aspirante no encontrado")
                        .build();
            }

            // 2. Buscamos sus inscripciones usando el DAO
            List<InscripcionesPrueba> lista = inscripcionesDAO.findByAspiranteId(idAspirante);
            return Response.ok(lista).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("UUID inválido").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * POST /aspirantes/{idAspirante}/inscripciones
     * Inscribe a un aspirante en una prueba (FLUJO A: Inscripción Inmediata).
     */
    @POST
    @Path("{idAspirante}/inscripciones")
    public Response crearInscripcionAspirante(
            @PathParam("idAspirante") String idAspiranteStr,
            InscripcionesPrueba nuevaInscripcion,
            @Context UriInfo uriInfo) {

        try {
            UUID idAspirante = UUID.fromString(idAspiranteStr);

            // 1. Verificamos que el Aspirante exista
            AspirantesDato aspirante = aspirantesDAO.leer(idAspirante);
            if (aspirante == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No se puede inscribir: Aspirante no encontrado.")
                        .build();
            }

            // 2. Verificamos que manden el ID de la prueba en el JSON y que exista en BD
            if (nuevaInscripcion.getPruebaAdmision() == null || nuevaInscripcion.getPruebaAdmision().getIdPruebaAdmision() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Debe especificar a qué prueba de admisión desea inscribirse.")
                        .build();
            }

            PruebasAdmision prueba = pruebasDAO.leer(nuevaInscripcion.getPruebaAdmision().getIdPruebaAdmision());
            if (prueba == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La prueba de admisión especificada no existe en el sistema.")
                        .build();
            }

            // 3. Ya seguro, forzamos los datos para evitar manipulación y mandamos a guardar
            nuevaInscripcion.setAspiranteDato(aspirante);
            nuevaInscripcion.setPruebaAdmision(prueba);
            inscripcionesDAO.crear(nuevaInscripcion);

            // 4. Retornamos 201 Created y la Location
            URI location = uriInfo.getBaseUriBuilder()
                    .path("inscripciones_prueba")
                    .path(nuevaInscripcion.getIdInscripcionPrueba().toString())
                    .build();

            return Response.created(location).entity(nuevaInscripcion).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
}