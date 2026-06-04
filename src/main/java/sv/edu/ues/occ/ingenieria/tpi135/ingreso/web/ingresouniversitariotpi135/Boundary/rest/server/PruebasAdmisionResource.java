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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * GET /pruebas_admision?first=0&max=50&buscar=texto
     * Retorna lista paginada de pruebas, ordenada cronológicamente (año desc).
     * Si se proporciona ?buscar=, filtra por nombre (LIKE) o año (exacto).
     */
    @GET
    public Response listPruebas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max,
            @QueryParam("buscar") String buscar) {
        try {
            List<PruebasAdmision> resultado;
            if (buscar != null && !buscar.isBlank()) {
                resultado = pruebasAdmisionDAO.buscarPorTermino(buscar.trim(), first, max);
            } else {
                resultado = pruebasAdmisionDAO.findAllOrdenado(first, max);
            }
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
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
     * Retorna las áreas de conocimiento con la jerarquía real de temas (padre → hijo → nieto…)
     * que participan en la prueba indicada.
     *
     * Algoritmo:
     * 1. Obtiene los temas con preguntas en la prueba (hoja o cualquier nivel).
     * 2. Carga iterativamente los ancestros que no estén en el conjunto inicial,
     *    para que el árbol no tenga huecos de profundidad arbitraria.
     * 3. Construye DTOs y conecta cada nodo con su padre, formando el árbol.
     * 4. Agrupa los nodos raíz bajo su área de conocimiento.
     */
    @GET
    @Path("{idPrueba}/areas")
    public Response getAreasByPrueba(@PathParam("idPrueba") String idPruebaStr) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);

            // Paso 1: temas con preguntas (idTemaPadre ya pre-cargado por LEFT JOIN FETCH en la query)
            List<Tema> temasConPreguntas = temaDAO.findByPrueba(idPrueba);

            // Paso 2: mapa completo (temas con preguntas + sus ancestros)
            Map<UUID, Tema> temaMap = new LinkedHashMap<>();
            for (Tema t : temasConPreguntas) {
                temaMap.put(t.getIdTema(), t);
            }

            // Carga iterativa de ancestros faltantes para soportar N niveles
            boolean hayNuevos;
            do {
                hayNuevos = false;
                Set<UUID> pendientes = new LinkedHashSet<>();
                for (Tema t : new ArrayList<>(temaMap.values())) {
                    Tema padre = t.getIdTemaPadre();
                    if (padre != null && !temaMap.containsKey(padre.getIdTema())) {
                        pendientes.add(padre.getIdTema());
                    }
                }
                for (UUID padreId : pendientes) {
                    Tema ancestro = temaDAO.leer(padreId);
                    if (ancestro != null) {
                        temaMap.put(ancestro.getIdTema(), ancestro);
                        hayNuevos = true;
                    }
                }
            } while (hayNuevos);

            // Paso 3: construir un DTO por cada tema del mapa completo
            Map<UUID, TemaResumenDTO> dtosById = new LinkedHashMap<>();
            for (Tema t : temaMap.values()) {
                dtosById.put(t.getIdTema(), new TemaResumenDTO(t.getIdTema(), t.getNombreTema()));
            }

            // Paso 4: agrupar áreas y conectar nodos padre-hijo
            Map<UUID, AreaConTemasDTO> mapaAreas = new LinkedHashMap<>();
            for (Tema t : temaMap.values()) {
                UUID areaId = t.getAreaConocimiento().getIdAreaConocimiento();
                mapaAreas.computeIfAbsent(areaId, k -> new AreaConTemasDTO(t.getAreaConocimiento()));
            }

            for (Tema t : temaMap.values()) {
                UUID areaId = t.getAreaConocimiento().getIdAreaConocimiento();
                TemaResumenDTO dto = dtosById.get(t.getIdTema());
                Tema padre = t.getIdTemaPadre();
                if (padre == null || !dtosById.containsKey(padre.getIdTema())) {
                    // Nodo raíz dentro del área
                    mapaAreas.get(areaId).temas.add(dto);
                } else {
                    // Nodo hijo: se anida bajo su padre
                    dtosById.get(padre.getIdTema()).subtemas.add(dto);
                }
            }

            List<AreaConTemasDTO> resultado = new ArrayList<>(mapaAreas.values());
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
    }

    public static class TemaResumenDTO {
        public UUID idTema;
        public String nombreTema;
        public List<TemaResumenDTO> subtemas = new ArrayList<>();

        public TemaResumenDTO(UUID idTema, String nombreTema) {
            this.idTema = idTema;
            this.nombreTema = nombreTema;
        }
    }
}
