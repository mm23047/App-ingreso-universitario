package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion REST para el recurso ProcesoAdmisionAspiranteResource.
 *
 * Estas pruebas ejercitan el avance de admision de un aspirante a traves de:
 * - Consulta de un proceso existente (estado y etapa actual).
 * - Manejo de id inexistente.
 * - Creacion de un nuevo proceso de admision asociado a una inscripcion real.
 * - Validacion de reglas basicas de integridad (422 en payload invalido).
 * - Cambio de estado y etapa del proceso, verificando que persista en BD.
 */
public class ProcesoAdmisionAspiranteResourceST extends AbstractResourceST {

    // UUIDs tomados del init.sql (mismos que en ProcesoAdmisionAspiranteDAOIT)
    private static final UUID ID_PROCESO_1   = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_ASPIRANTE_1 = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2025 = UUID.fromString("d1000000-0000-0000-0000-000000000002");
    private static final UUID ID_ETAPA_1     = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_FINAL = UUID.fromString("c1000000-0000-0000-0000-000000000003");

    /**
     * GET /proceso_admision_aspirante/{id} con un id existente debe devolver 200
     * y reflejar el estado y la etapa inicial configurados en init.sql.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("proceso_admision_aspirante/" + ID_PROCESO_1);

        assertEquals(200, response.getStatus());

        ProcesoAdmisionAspirante entidad = response.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(entidad);
        assertEquals(ID_PROCESO_1, entidad.getId());
        assertNotNull(entidad.getInscripcionesPrueba());
        assertEquals(ID_PROCESO_1, entidad.getInscripcionesPrueba().getId());
        assertNotNull(entidad.getIdEtapaActual());
        assertEquals(ID_ETAPA_1, entidad.getIdEtapaActual().getId());
        assertEquals("EN_PROCESO", entidad.getEstado());
    }

    /**
     * GET /proceso_admision_aspirante/{id} con un id inexistente debe devolver 404 y header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("proceso_admision_aspirante/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * POST /proceso_admision_aspirante con una entidad valida debe devolver 201 y
     * permitir consultar luego el recurso creado. Se crea previamente una InscripcionesPrueba
     * real via el recurso /inscripciones_prueba para respetar la restriccion @MapsId.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        // 1) Crear una inscripcion real para el aspirante1 y la prueba 2025
        UUID idInscripcionCreada = crearInscripcionReal(ID_ASPIRANTE_1, ID_PRUEBA_2025, "INSCRITO");

        // 2) Crear el proceso de admision asociado a esa inscripcion
        ProcesoAdmisionAspirante nuevoProceso = crearProcesoAdmision(idInscripcionCreada, ID_ETAPA_1, "EN_PROCESO");

        Response responseProceso = post("proceso_admision_aspirante", nuevoProceso);

        assertEquals(201, responseProceso.getStatus());
        String locationProceso = responseProceso.getHeaderString("Location");
        assertNotNull(locationProceso);

        String idProcesoStr = locationProceso.substring(locationProceso.lastIndexOf('/') + 1);
        UUID idProcesoCreado = UUID.fromString(idProcesoStr);

        // Por la relacion @MapsId, el id del proceso debe coincidir con el id de la inscripcion
        assertEquals(idInscripcionCreada, idProcesoCreado);

        // 3) Consultar el proceso creado y validar su estado y etapa
        Response responseConsulta = get("proceso_admision_aspirante/" + idProcesoCreado);
        assertEquals(200, responseConsulta.getStatus());

        ProcesoAdmisionAspirante creado = responseConsulta.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(creado);
        assertEquals(idProcesoCreado, creado.getId());
        assertNotNull(creado.getInscripcionesPrueba());
        assertEquals(idInscripcionCreada, creado.getInscripcionesPrueba().getId());
        assertNotNull(creado.getIdEtapaActual());
        assertEquals(ID_ETAPA_1, creado.getIdEtapaActual().getId());
        assertEquals("EN_PROCESO", creado.getEstado());
    }

    /**
     * POST /proceso_admision_aspirante con una entidad invalida (sin inscripcion asociada)
     * debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void create_ConEntidadInvalida_SinInscripcion_DebeRetornar422() {
        ProcesoAdmisionAspirante nuevo = new ProcesoAdmisionAspirante();

        EtapasAdmision etapa1 = new EtapasAdmision();
        etapa1.setId(ID_ETAPA_1);
        nuevo.setIdEtapaActual(etapa1);

        nuevo.setEstado("EN_PROCESO");

        Response response = post("proceso_admision_aspirante", nuevo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * Validacion de cambio de estado del proceso de admision:
     * se crea un nuevo proceso EN_PROCESO en etapa 1 y luego se actualiza
     * a ADMITIDO en la etapa final, verificando que el cambio persista en BD.
     */
    @Test
    void update_CambioDeEstado_DeEnProcesoAAdmitido_DebePersistirse() {
        // 1) Crear una inscripcion y su proceso EN_PROCESO en etapa 1
        UUID idInscripcionCreada = crearInscripcionReal(ID_ASPIRANTE_1, ID_PRUEBA_2025, "INSCRITO");

        ProcesoAdmisionAspirante nuevoProceso = crearProcesoAdmision(idInscripcionCreada, ID_ETAPA_1, "EN_PROCESO");

        Response responseProceso = post("proceso_admision_aspirante", nuevoProceso);
        assertEquals(201, responseProceso.getStatus());
        String locationProceso = responseProceso.getHeaderString("Location");
        assertNotNull(locationProceso);

        String idProcesoStr = locationProceso.substring(locationProceso.lastIndexOf('/') + 1);
        UUID idProcesoCreado = UUID.fromString(idProcesoStr);

        // 2) Consultar el proceso, cambiar estado y etapa, y actualizar via PUT
        Response responseConsulta = get("proceso_admision_aspirante/" + idProcesoCreado);
        assertEquals(200, responseConsulta.getStatus());

        ProcesoAdmisionAspirante proceso = responseConsulta.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(proceso);
        assertEquals("EN_PROCESO", proceso.getEstado());
        assertNotNull(proceso.getIdEtapaActual());
        assertEquals(ID_ETAPA_1, proceso.getIdEtapaActual().getId());

        EtapasAdmision etapaFinal = new EtapasAdmision();
        etapaFinal.setId(ID_ETAPA_FINAL);
        proceso.setIdEtapaActual(etapaFinal);
        proceso.setEstado("ADMITIDO");

        Response responseUpdate = put("proceso_admision_aspirante/" + idProcesoCreado, proceso);
        assertEquals(200, responseUpdate.getStatus());

        ProcesoAdmisionAspirante actualizado = responseUpdate.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(actualizado);
        assertEquals(idProcesoCreado, actualizado.getId());
        assertNotNull(actualizado.getIdEtapaActual());
        assertEquals(ID_ETAPA_FINAL, actualizado.getIdEtapaActual().getId());
        assertEquals("ADMITIDO", actualizado.getEstado());

        // 3) Verificar que el cambio persista consultando nuevamente
        Response responseFinal = get("proceso_admision_aspirante/" + idProcesoCreado);
        assertEquals(200, responseFinal.getStatus());

        ProcesoAdmisionAspirante desdeBd = responseFinal.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(desdeBd);
        assertEquals(idProcesoCreado, desdeBd.getId());
        assertNotNull(desdeBd.getIdEtapaActual());
        assertEquals(ID_ETAPA_FINAL, desdeBd.getIdEtapaActual().getId());
        assertEquals("ADMITIDO", desdeBd.getEstado());
    }

    /**
     * PUT /proceso_admision_aspirante/{id} con un id inexistente debe devolver 404
     * y el header Not-found-id, sin intentar crear ni modificar registros.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        ProcesoAdmisionAspirante payload = new ProcesoAdmisionAspirante();
        payload.setEstado("EN_PROCESO");

        Response response = put("proceso_admision_aspirante/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * PUT /proceso_admision_aspirante/no-es-uuid debe resultar en 404, ya que
     * el runtime JAX-RS no puede convertir el path param a UUID y no invoca al recurso.
     */
    @Test
    void update_ConIdInvalidoEnPath_DebeRetornar404() {
        ProcesoAdmisionAspirante payload = new ProcesoAdmisionAspirante();
        payload.setEstado("EN_PROCESO");

        Response response = put("proceso_admision_aspirante/no-es-uuid", payload);

        assertEquals(404, response.getStatus());
    }

    /**
     * POST /proceso_admision_aspirante/{idInscripcion}/asignar-carrera
     * Debe asignar la primera carrera (por prioridad) que tenga cupos > 0
     * en la etapa actual del proceso, decrementando el cupo exactamente en 1.
     *
     * Caso: Prioridad 1 (ICS) SIN cupo → debe elegir Prioridad 2 (ISI) CON cupo.
     *
     * Validaciones:
     * - Status 200 ✓
     * - Estado = ADMITIDO ✓
     * - Carrera asignada = ISI ✓
     * - Cupos decrementaron de 2 → 1 ✓
     */
    @Test
    void asignarCarreraFinal_ConPrioridadYCupos_DebeAsignarYDecrementar() {
        UUID idInscripcionCreada = crearInscripcionReal(ID_ASPIRANTE_1, ID_PRUEBA_2025, "INSCRITO");

        UUID idEtapaAsignacion = crearEtapa("Etapa Asignacion Carrera ST");

        ProcesoAdmisionAspirante nuevoProceso = crearProcesoAdmision(idInscripcionCreada, idEtapaAsignacion, "EN_PROCESO");
        Response responseProceso = post("proceso_admision_aspirante", nuevoProceso);
        assertEquals(201, responseProceso.getStatus());

        crearCarreraElegida(idInscripcionCreada, "ICS", (short) 1);
        crearCarreraElegida(idInscripcionCreada, "ISI", (short) 2);

        crearCuposCarrera(ID_PRUEBA_2025, "ICS", idEtapaAsignacion, 0);
        crearCuposCarrera(ID_PRUEBA_2025, "ISI", idEtapaAsignacion, 2);

        // Validar cupos ANTES de la asignacion
        Response responseCuposAntes = get("cupos_carrera/" + ID_PRUEBA_2025 + "/ISI/" + idEtapaAsignacion);
        assertEquals(200, responseCuposAntes.getStatus());
        CuposCarrera cuposAntes = responseCuposAntes.readEntity(CuposCarrera.class);
        assertNotNull(cuposAntes);
        int cuposDisponiblesAntes = cuposAntes.getCupos();
        assertEquals(2, cuposDisponiblesAntes, "ISI debe tener 2 cupos disponibles antes");

        Response responseAsignacion = post(
                "proceso_admision_aspirante/" + idInscripcionCreada + "/asignar-carrera",
            "{}"
        );

        assertEquals(200, responseAsignacion.getStatus());
        ProcesoAdmisionAspirante resultado = responseAsignacion.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(resultado);
        assertEquals(idInscripcionCreada, resultado.getId());
        assertEquals("ADMITIDO", resultado.getEstado(), "El estado debe ser ADMITIDO tras asignacion exitosa");
        assertNotNull(resultado.getCarreraAsignada(), "Debe haber carrera asignada");
        assertEquals("ISI", resultado.getCarreraAsignada().getIdCarrera(), "Debe asignar ISI (prioridad 2, porque ICS sin cupo)");

        // Validar que persiste en BD via GET
        Response responseProcesoFinal = get("proceso_admision_aspirante/" + idInscripcionCreada);
        assertEquals(200, responseProcesoFinal.getStatus());
        ProcesoAdmisionAspirante procesoFinal = responseProcesoFinal.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(procesoFinal);
        assertEquals("ADMITIDO", procesoFinal.getEstado());
        assertNotNull(procesoFinal.getCarreraAsignada());
        assertEquals("ISI", procesoFinal.getCarreraAsignada().getIdCarrera());

        // Validar que los CUPOS decrementaron exactamente en 1
        Response responseCuposDespues = get("cupos_carrera/" + ID_PRUEBA_2025 + "/ISI/" + idEtapaAsignacion);
        assertEquals(200, responseCuposDespues.getStatus());
        CuposCarrera cuposDespues = responseCuposDespues.readEntity(CuposCarrera.class);
        assertNotNull(cuposDespues);
        int cuposDisponiblesDespues = cuposDespues.getCupos();
        assertEquals(cuposDisponiblesAntes - 1, cuposDisponiblesDespues, 
            "Los cupos deben decrementar exactamente en 1 (de " + cuposDisponiblesAntes + " a " + (cuposDisponiblesAntes - 1) + ")");
    }

    /**
     * POST /proceso_admision_aspirante/{idInscripcion}/asignar-carrera sin cupos disponibles
     * Debe cambiar el estado a NO_ADMITIDO y NO asignar carrera.
     *
     * Validaciones:
     * - Status 200 ✓
     * - Estado = NO_ADMITIDO ✓
     * - Carrera asignada = null ✓
     * - Cupos se mantienen en 0 ✓
     */
    @Test
    void asignarCarreraFinal_SinCuposDisponibles_DebeNoAdmitirYNoAsignarCarrera() {
        UUID idInscripcionCreada = crearInscripcionReal(ID_ASPIRANTE_1, ID_PRUEBA_2025, "INSCRITO");

        UUID idEtapaAsignacion = crearEtapa("Etapa Sin Cupos ST");

        ProcesoAdmisionAspirante nuevoProceso = crearProcesoAdmision(idInscripcionCreada, idEtapaAsignacion, "EN_PROCESO");
        Response responseProceso = post("proceso_admision_aspirante", nuevoProceso);
        assertEquals(201, responseProceso.getStatus());

        // Crear 2 carreras elegidas, ambas sin cupos
        crearCarreraElegida(idInscripcionCreada, "ICS", (short) 1);
        crearCarreraElegida(idInscripcionCreada, "ISI", (short) 2);

        crearCuposCarrera(ID_PRUEBA_2025, "ICS", idEtapaAsignacion, 0);
        crearCuposCarrera(ID_PRUEBA_2025, "ISI", idEtapaAsignacion, 0);

        Response responseAsignacion = post(
                "proceso_admision_aspirante/" + idInscripcionCreada + "/asignar-carrera",
            "{}"
        );

        assertEquals(200, responseAsignacion.getStatus());
        ProcesoAdmisionAspirante resultado = responseAsignacion.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(resultado);
        assertEquals("NO_ADMITIDO", resultado.getEstado(), "Sin cupos disponibles debe ser NO_ADMITIDO");
        assertTrue(resultado.getCarreraAsignada() == null, "No debe haber carrera asignada");

        // Validar que persista
        Response responseProcesoFinal = get("proceso_admision_aspirante/" + idInscripcionCreada);
        assertEquals(200, responseProcesoFinal.getStatus());
        ProcesoAdmisionAspirante procesoFinal = responseProcesoFinal.readEntity(ProcesoAdmisionAspirante.class);
        assertNotNull(procesoFinal);
        assertEquals("NO_ADMITIDO", procesoFinal.getEstado());
        assertTrue(procesoFinal.getCarreraAsignada() == null);

        // Validar que los cupos se mantienen en 0
        Response responseCuposICS = get("cupos_carrera/" + ID_PRUEBA_2025 + "/ICS/" + idEtapaAsignacion);
        assertEquals(200, responseCuposICS.getStatus());
        CuposCarrera cuposICS = responseCuposICS.readEntity(CuposCarrera.class);
        assertEquals(0, cuposICS.getCupos());

        Response responseCuposISI = get("cupos_carrera/" + ID_PRUEBA_2025 + "/ISI/" + idEtapaAsignacion);
        assertEquals(200, responseCuposISI.getStatus());
        CuposCarrera cuposISI = responseCuposISI.readEntity(CuposCarrera.class);
        assertEquals(0, cuposISI.getCupos());
    }

    /**
     * Usado para crear una inscripcion real via el recurso REST de inscripciones,
     * reutilizado por varios tests. NOTA: El parámetro idAspirante se ignora
     * porque siempre creamos aspirantes dinámicos para evitar conflictos con init.sql.
     */
    private UUID crearInscripcionReal(UUID idAspirante, UUID idPrueba, String estado) {
        InscripcionesPrueba nuevaInscripcion = new InscripcionesPrueba();

        // Siempre crear aspirante dinámico para evitar duplicidad
        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setId(crearAspiranteDinamico());
        nuevaInscripcion.setIdAspirante(aspirante);

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(idPrueba);
        nuevaInscripcion.setIdPrueba(prueba);

        nuevaInscripcion.setEstado(estado);

        Response responseInscripcion = post("inscripciones_prueba", nuevaInscripcion);
        assertEquals(201, responseInscripcion.getStatus());
        String locationInscripcion = responseInscripcion.getHeaderString("Location");
        assertNotNull(locationInscripcion);

        String idInscripcionStr = locationInscripcion.substring(locationInscripcion.lastIndexOf('/') + 1);
        return UUID.fromString(idInscripcionStr);
    }

    /**
     * Usado para crear un ProcesoAdmisionAspirante coherente a partir del id
     * de inscripcion, la etapa actual y el estado deseado.
     */
    private ProcesoAdmisionAspirante crearProcesoAdmision(UUID idInscripcion, UUID idEtapa, String estado) {
        ProcesoAdmisionAspirante proceso = new ProcesoAdmisionAspirante();

        InscripcionesPrueba refInscripcion = new InscripcionesPrueba();
        refInscripcion.setId(idInscripcion);
        proceso.setInscripcionesPrueba(refInscripcion);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setId(idEtapa);
        proceso.setIdEtapaActual(etapa);

        proceso.setEstado(estado);
        return proceso;
    }

    private UUID crearEtapa(String nombreEtapa) {
        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setNombre(nombreEtapa);

        Response responseEtapa = post("etapas_admision", etapa);
        assertEquals(201, responseEtapa.getStatus());
        String locationEtapa = responseEtapa.getHeaderString("Location");
        assertNotNull(locationEtapa);

        String idEtapaStr = locationEtapa.substring(locationEtapa.lastIndexOf('/') + 1);
        return UUID.fromString(idEtapaStr);
    }

    private void crearCarreraElegida(UUID idInscripcion, String idCarrera, short prioridad) {
        CarrerasElegidaId pk = new CarrerasElegidaId();
        pk.setIdInscripcion(idInscripcion);
        pk.setIdCarrera(idCarrera);

        CarrerasElegida ce = new CarrerasElegida();
        ce.setId(pk);

        InscripcionesPrueba insRef = new InscripcionesPrueba();
        insRef.setId(idInscripcion);
        ce.setIdInscripcion(insRef);

        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(idCarrera);
        ce.setIdCarrera(carreraRef);

        ce.setPrioridad(prioridad);

        Response responseCarrera = post("carreras_elegidas", ce);
        assertEquals(201, responseCarrera.getStatus());
    }

    private void crearCuposCarrera(UUID idPrueba, String idCarrera, UUID idEtapa, int cupos) {
        CuposCarreraId pk = new CuposCarreraId();
        pk.setIdPrueba(idPrueba);
        pk.setIdCarrera(idCarrera);
        pk.setIdEtapa(idEtapa);

        CuposCarrera entidad = new CuposCarrera();
        entidad.setId(pk);

        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(idPrueba);
        entidad.setIdPrueba(pruebaRef);

        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(idCarrera);
        entidad.setIdCarrera(carreraRef);

        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setId(idEtapa);
        entidad.setIdEtapa(etapaRef);

        entidad.setCupos(cupos);

        Response responseCupos = post("cupos_carrera", entidad);
        assertEquals(201, responseCupos.getStatus());
    }

    /**
     * Crea un aspirante nuevo de forma dinámica para evitar choques de duplicidad
     * entre los diferentes tests.
     */
    private UUID crearAspiranteDinamico() {
        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setNombres("Aspirante IT");
        nuevoAspirante.setApellidos("Dinamico");
        // Generamos un DUI aleatorio de 9 dígitos
        nuevoAspirante.setDui(String.valueOf(System.currentTimeMillis()).substring(4));
        nuevoAspirante.setUsaSillaRuedas(false);

        // Usamos una semilla de usuario que ya existe en tu DB
        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(UUID.fromString("b1000000-0000-0000-0000-000000000001"));
        nuevoAspirante.setIdUsuario(usuario);

        Response response = post("aspirantes_datos", nuevoAspirante);
        assertEquals(201, response.getStatus(), "Fallo al crear el aspirante dinamico");
        String location = response.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

}
