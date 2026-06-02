package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar Inscripciones de manera global.
 * Base: /resources/v1/inscripciones_prueba
 */
@Path("inscripciones_prueba")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InscripcionesPruebaResource extends AbstractResource<InscripcionesPrueba> {

    @Inject
    private InscripcionesPruebaDAO inscripcionesPruebaDAO;

    // INYECCIONES PARA LA FASE DE GENERACIÓN DE EXAMEN
    @Inject
    private ClavesExamanDAO clavesExamanDAO;

    @Inject
    private ExamenRealizadoDAO examenRealizadoDAO;

    @Override
    protected IngresoDefaultDataAccess<InscripcionesPrueba> getDAO() {
        return inscripcionesPruebaDAO;
    }

    /**
     * GET /inscripciones
     * Lista las inscripciones. Permite filtrar por idPrueba y estado,
     * aprovechando el método de negocio de tu DAO (FASE 1).
     */
    @GET
    public Response listInscripciones(
            @QueryParam("idPrueba") String idPruebaStr,
            @QueryParam("estado") String estado,
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {

        try {
            // Si el Frontend envía idPrueba y estado, usamos la consulta especializada
            if (idPruebaStr != null && !idPruebaStr.isBlank() && estado != null && !estado.isBlank()) {
                UUID idPrueba = UUID.fromString(idPruebaStr);
                List<InscripcionesPrueba> filtradas = inscripcionesPruebaDAO.findByPruebaAndEstado(idPrueba, estado);

                // Aplicamos paginación manual en memoria para los resultados filtrados
                int fromIndex = Math.min(first, filtradas.size());
                int toIndex = Math.min(first + max, filtradas.size());
                List<InscripcionesPrueba> paginadas = filtradas.subList(fromIndex, toIndex);

                return Response.ok(paginadas)
                        .header(RestHeaders.TOTAL_RECORDS, filtradas.size())
                        .build();
            }

            // Si no hay filtros, retorna todas paginadas usando el método heredado
            return findRange(first, max);

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parámetros de filtrado inválidos: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /inscripciones/{idInscripcion}
     * Obtiene una inscripción. Utiliza el método leer() sobrescrito en el DAO
     * que trae las relaciones con JOIN FETCH.
     */
    @GET
    @Path("{idInscripcion}")
    public Response getInscripcion(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            InscripcionesPrueba inscripcion = inscripcionesPruebaDAO.leer(idInscripcion);

            if (inscripcion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }
            return Response.ok(inscripcion).build();

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
     * PUT /inscripciones/{idInscripcion}
     * Actualiza una inscripción (ej. cambio de estado a CANCELADO, APROBADO, etc.).
     */
    @PUT
    @Path("{idInscripcion}")
    public Response updateInscripcion(
            @PathParam("idInscripcion") String idInscripcionStr,
            InscripcionesPrueba inscripcionActualizada) {

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // 1. Verificar que exista
            InscripcionesPrueba existente = inscripcionesPruebaDAO.leer(idInscripcion);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }

            // 2. Asegurar que el ID del payload sea el mismo de la URL
            inscripcionActualizada.setIdInscripcionPrueba(idInscripcion);

            // 3. Evitar que se modifiquen los IDs de las relaciones vitales accidentalmente
            if(inscripcionActualizada.getAspiranteDato() == null) {
                inscripcionActualizada.setAspiranteDato(existente.getAspiranteDato());
            }
            if(inscripcionActualizada.getPruebaAdmision() == null) {
                inscripcionActualizada.setPruebaAdmision(existente.getPruebaAdmision());
            }

            // 4. Actualizar. Si viola la regla de negocio, el DAO lanzará IllegalArgumentException
            InscripcionesPrueba guardada = inscripcionesPruebaDAO.actualizar(inscripcionActualizada);

            return Response.ok(guardada).build();

        } catch (IllegalArgumentException e) {
            // Captura las reglas de negocio de tu DAO (Colisiones, nulos, etc.)
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
     * DELETE /inscripciones/{idInscripcion}
     * Cancela/Elimina la inscripción.
     */
    @DELETE
    @Path("{idInscripcion}")
    public Response deleteInscripcion(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            InscripcionesPrueba existente = inscripcionesPruebaDAO.leer(idInscripcion);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }

            // Eliminación física (Si tu negocio exige eliminación lógica,
            // aquí deberías hacer un setEstado("CANCELADO") y un actualizar).
            inscripcionesPruebaDAO.eliminar(existente);

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
     * POST /inscripciones/{idInscripcion}/examen/generar
     * Genera el Examen Realizado para una inscripción asignando una clave de forma secuencial.
     */
    @POST
    @Path("{idInscripcion}/examen/generar")
    public Response generarExamen(
            @PathParam("idInscripcion") String idInscripcionStr,
            @QueryParam("idEtapa") String idEtapaStr) { // Requerimos la etapa como query param

        if (idEtapaStr == null || idEtapaStr.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El id de la Etapa es obligatorio para generar el examen.")
                    .build();
        }

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            UUID idEtapa = UUID.fromString(idEtapaStr);

            // 1. Validar que la inscripción existe
            InscripcionesPrueba inscripcion = inscripcionesPruebaDAO.leer(idInscripcion);
            if (inscripcion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Inscripción no encontrada.")
                        .build();
            }

            // Validar que no se le haya generado un examen ya
            if ("EXAMEN_GENERADO".equalsIgnoreCase(inscripcion.getEstado()) || "CALIFICADO".equalsIgnoreCase(inscripcion.getEstado())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Esta inscripción ya tiene un examen generado o procesado.")
                        .build();
            }

            UUID idPrueba = inscripcion.getPruebaAdmision().getIdPruebaAdmision();

            // 2. Obtener las claves disponibles para esta prueba
            List<ClavesExamen> clavesDisponibles = clavesExamanDAO.findByPrueba(idPrueba);
            if (clavesDisponibles == null || clavesDisponibles.isEmpty()) {
                return Response.status(Response.Status.PRECONDITION_FAILED)
                        .entity("No hay claves de examen registradas para esta prueba.")
                        .build();
            }

            // 3. LÓGICA SECUENCIAL: Contar cuántos exámenes ya existen para esta prueba
            // Nota: Para sistemas de muy alto tráfico se recomienda un Query COUNT directo en BD.
            List<ExamenRealizado> examenesPrevios = examenRealizadoDAO.findByPruebaId(idPrueba);
            int totalExamenes = examenesPrevios != null ? examenesPrevios.size() : 0;

            // Operación Módulo para asignación secuencial
            int indiceAsignado = totalExamenes % clavesDisponibles.size();
            ClavesExamen claveSeleccionada = clavesDisponibles.get(indiceAsignado);

            // 4. Crear el cascarón de la Etapa (Solo necesitamos el ID para la FK)
            EtapasAdmision etapaRef = new EtapasAdmision();
            etapaRef.setIdEtapaAdmision(idEtapa);

            // 5. Construir y guardar el Examen Realizado
            ExamenRealizado nuevoExamen = new ExamenRealizado();
            nuevoExamen.setInscripcionesPrueba(inscripcion);
            nuevoExamen.setClaveExamen(claveSeleccionada);
            nuevoExamen.setEtapaAdmision(etapaRef);
            nuevoExamen.setFechaRealizacion(OffsetDateTime.now());
            // Puntaje final inicia nulo hasta la calificación

            examenRealizadoDAO.crear(nuevoExamen);

            // 6. Actualizar el estado de la inscripción
            inscripcion.setEstado("EXAMEN_GENERADO");
            inscripcionesPruebaDAO.actualizar(inscripcion);

            return Response.status(Response.Status.CREATED).entity(nuevoExamen).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formatos de UUID inválidos.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }


}