package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CarrerasElegidaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar las carreras elegidas por un aspirante en su inscripción.
 * * Base: /resources/v1/inscripciones/{idInscripcion}/carreras
 */
@Path("inscripciones/{idInscripcion}/carreras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarrerasElegidaResource extends AbstractResource<CarrerasElegida> {

    @Inject
    private CarrerasElegidaDAO carrerasElegidaDAO;

    // Inyectamos los DAOs padres para validar existencia antes de crear/modificar
    @Inject
    private InscripcionesPruebaDAO inscripcionesDAO;

    @Inject
    private CatalogoCarreraDAO catalogoCarreraDAO;

    @Override
    protected CarrerasElegidaDAO getDAO() {
        return carrerasElegidaDAO;
    }

    /**
     * GET /inscripciones/{idInscripcion}/carreras
     * Retorna la "Lista de Deseos" del aspirante ordenada por prioridad.
     */
    @GET
    public Response getCarrerasElegidas(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // Validamos que la inscripción exista
            InscripcionesPrueba inscripcion = inscripcionesDAO.leer(idInscripcion);
            if (inscripcion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Inscripción no encontrada.")
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }

            // Usamos tu método del DAO que ya trae el JOIN FETCH y el ORDER BY
            List<CarrerasElegida> lista = carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion);
            return Response.ok(lista).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID de inscripción inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /inscripciones/{idInscripcion}/carreras
     * Agrega una nueva carrera a la lista de opciones del aspirante.
     */
    @POST
    public Response addCarreraElegida(
            @PathParam("idInscripcion") String idInscripcionStr,
            CarrerasElegida nuevaEleccion,
            @Context UriInfo uriInfo) {

        // 1. Validaciones básicas de entrada
        if (nuevaEleccion == null || nuevaEleccion.getCatalogoCarrera() == null || nuevaEleccion.getCatalogoCarrera().getIdCarrera() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe especificar el ID de la carrera a elegir.")
                    .build();
        }
        if (nuevaEleccion.getPrioridad() == null || nuevaEleccion.getPrioridad() < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe asignar un nivel de prioridad válido (mayor a 0).")
                    .build();
        }

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            String idCarrera = nuevaEleccion.getCatalogoCarrera().getIdCarrera();

            // 2. Verificar existencia de los padres (Inscripción y Carrera)
            InscripcionesPrueba inscripcion = inscripcionesDAO.leer(idInscripcion);
            if (inscripcion == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Inscripción no encontrada.").build();
            }

            CatalogoCarrera carrera = catalogoCarreraDAO.leer(idCarrera);
            if (carrera == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("La carrera seleccionada no existe en el catálogo.").build();
            }

            // 3. REGLAS DE NEGOCIO (Usando los métodos de tu DAO)
            if (carrerasElegidaDAO.existsByInscripcionAndCarrera(idInscripcion, idCarrera)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Violación de regla: El aspirante ya ha seleccionado esta carrera en sus opciones.")
                        .header(RestHeaders.CONFLICT_REASON, "Carrera duplicada")
                        .build();
            }

            if (carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, nuevaEleccion.getPrioridad())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Violación de regla: El nivel de prioridad " + nuevaEleccion.getPrioridad() + " ya está ocupado por otra carrera.")
                        .header(RestHeaders.CONFLICT_REASON, "Prioridad ocupada")
                        .build();
            }

            // 4. Forzamos las relaciones de forma segura y guardamos
            nuevaEleccion.setInscripcionesPrueba(inscripcion);
            nuevaEleccion.setCatalogoCarrera(carrera);
            carrerasElegidaDAO.crear(nuevaEleccion);

            // 5. Retornar 201 Created con URI del nuevo recurso
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(carrera.getIdCarrera())
                    .build();

            return Response.created(location).entity(nuevaEleccion).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("UUID inválido.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /inscripciones/{idInscripcion}/carreras/{idCarrera}
     * Actualiza la prioridad de una carrera ya elegida.
     */
    @PUT
    @Path("{idCarrera}")
    public Response updatePrioridad(
            @PathParam("idInscripcion") String idInscripcionStr,
            @PathParam("idCarrera") String idCarrera,
            CarrerasElegida datosActualizacion) {

        if (datosActualizacion == null || datosActualizacion.getPrioridad() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Debe enviar la nueva prioridad.").build();
        }

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // 1. Buscar el registro existente
            CarrerasElegida existente = carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("El aspirante no tiene registrada la carrera especificada en sus opciones.")
                        .build();
            }

            // 2. REGLA DE NEGOCIO: Si está cambiando a una prioridad distinta, verificar que esté libre
            if (!existente.getPrioridad().equals(datosActualizacion.getPrioridad())) {
                if (carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, datosActualizacion.getPrioridad())) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity("No se puede cambiar la prioridad: El nivel " + datosActualizacion.getPrioridad() + " ya está ocupado.")
                            .header(RestHeaders.CONFLICT_REASON, "Colisión de prioridades al actualizar")
                            .build();
                }
            }

            // 3. Actualizamos y guardamos
            existente.setPrioridad(datosActualizacion.getPrioridad());
            CarrerasElegida actualizada = carrerasElegidaDAO.actualizar(existente);

            return Response.ok(actualizada).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parámetros de ruta inválidos.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /inscripciones/{idInscripcion}/carreras/{idCarrera}
     * Elimina una carrera de la lista de opciones del aspirante.
     */
    @DELETE
    @Path("{idCarrera}")
    public Response deleteCarreraElegida(
            @PathParam("idInscripcion") String idInscripcionStr,
            @PathParam("idCarrera") String idCarrera) {

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // Buscar la entidad exacta
            CarrerasElegida existente = carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Opción de carrera no encontrada para eliminar.")
                        .build();
            }

            // Eliminar mediante el DAO
            carrerasElegidaDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parámetros de ruta inválidos.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}