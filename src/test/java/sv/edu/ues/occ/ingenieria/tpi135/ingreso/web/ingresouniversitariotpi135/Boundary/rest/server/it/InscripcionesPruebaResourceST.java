package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion REST para el recurso InscripcionesPruebaResource.
 *
 * Estas pruebas:
    * - Usan WAR real desplegada en Liberty (via BaseSistemaBDD / AbstractResourceST).
 * - Consumen endpoints reales via HTTP.
 * - Validan el contrato REST (codigos HTTP, headers, cuerpo JSON) y la integracion
 *   con la base de datos inicializada por ingresoTPI135_init.sql.
 */
public class InscripcionesPruebaResourceST extends AbstractResourceST {

    // UUIDs tomados del init.sql (mismos que en InscripcionesPruebaDAOIT)
    // id_inscripcion y id_aspirante son distintos en la BD
    private static final UUID ID_INSCRIPCION_1       = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_2       = UUID.fromString("09000000-0000-0000-0000-000000000002");
    private static final UUID ID_ASPIRANTE_1         = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ASPIRANTE_CREACION  = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_1            = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2            = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    /**
     * GET /inscripciones_prueba/{id} con un id existente debe devolver 200 y el cuerpo esperado.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("inscripciones_prueba/" + ID_INSCRIPCION_1);

        assertEquals(200, response.getStatus());

        InscripcionesPrueba entidad = response.readEntity(InscripcionesPrueba.class);
        assertNotNull(entidad);
        // Validamos que el recurso correcto fue devuelto.
        assertEquals(ID_INSCRIPCION_1, entidad.getId());
    }

    /**
     * GET /inscripciones_prueba/{id} con un id inexistente debe devolver 404 y header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("inscripciones_prueba/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /inscripciones_prueba?aspiranteId=... con un aspiranteId valido debe devolver las inscripciones del aspirante.
     */
    @Test
    void findRange_ConAspiranteIdValido_DebeRetornar200() {
        Response response =
                get("inscripciones_prueba?aspiranteId=" + ID_ASPIRANTE_1);

        assertEquals(200, response.getStatus());

        InscripcionesPrueba[] arreglo =
                response.readEntity(InscripcionesPrueba[].class);

        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1);

        // Con los datos iniciales del init.sql, el aspirante 1 solo tiene
        // asociada la inscripcion ID_INSCRIPCION_1, por lo que el filtro
        // por aspiranteId debe devolver precisamente esa inscripcion.
        boolean encontro = false;
        for (InscripcionesPrueba i : arreglo) {
            if (i != null && ID_INSCRIPCION_1.equals(i.getId())) {
                encontro = true;
                break;
            }
        }
        assertTrue(encontro, "El filtro por aspiranteId debe incluir la inscripción semilla 1");
    }

    /**
     * POST /inscripciones_prueba con una entidad valida debe devolver 201 y permitir consultar luego el recurso creado.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();

        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setId(ID_ASPIRANTE_CREACION);
        nueva.setIdAspirante(aspirante);

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(ID_PRUEBA_2);
        nueva.setIdPrueba(prueba);

        nueva.setEstado("PENDIENTE");

        Response responseCreacion = post("inscripciones_prueba", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("inscripciones_prueba/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        InscripcionesPrueba creada = responseConsulta.readEntity(InscripcionesPrueba.class);
        assertNotNull(creada);
        assertEquals(idCreado, creada.getId());
        assertNotNull(creada.getIdAspirante());
        assertEquals(ID_ASPIRANTE_CREACION, creada.getIdAspirante().getId());
        assertNotNull(creada.getIdPrueba());
        assertEquals(ID_PRUEBA_2, creada.getIdPrueba().getId());
    }

    /**
     * POST /inscripciones_prueba con una entidad invalida (sin aspirante) debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinAspirante_DebeRetornar422() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(ID_PRUEBA_1);
        nueva.setIdPrueba(prueba);

        Response response = post("inscripciones_prueba", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * GET /inscripciones_prueba?aspiranteId=... con un aspiranteId con formato invalido debe devolver 422.
     */
    @Test
    void findRange_ConAspiranteIdInvalido_DebeRetornar422() {
        Response response = get("inscripciones_prueba?aspiranteId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * GET /inscripciones_prueba?pruebaId=... con un pruebaId valido debe devolver
     * las inscripciones asociadas a esa prueba.
     */
    @Test
    void findRange_ConPruebaIdValido_DebeRetornar200() {
        Response response = get("inscripciones_prueba?pruebaId=" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        InscripcionesPrueba[] arreglo = response.readEntity(InscripcionesPrueba[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1);

        // Con los datos iniciales del init.sql, la prueba 1 tiene
        // asociadas las inscripciones 1 y 2. El filtro por pruebaId
        // debe incluir al menos esas inscripciones.
        boolean contieneInscripcion1 = false;
        boolean contieneInscripcion2 = false;

        for (InscripcionesPrueba i : arreglo) {
            if (ID_INSCRIPCION_1.equals(i.getId())) {
                contieneInscripcion1 = true;
            }
            if (ID_INSCRIPCION_2.equals(i.getId())) {
                contieneInscripcion2 = true;
            }
        }

        assertTrue(contieneInscripcion1);
        assertTrue(contieneInscripcion2);
    }

    /**
     * GET /inscripciones_prueba?pruebaId=... con un pruebaId con formato invalido
     * debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void findRange_ConPruebaIdInvalido_DebeRetornar422() {
        Response response = get("inscripciones_prueba?pruebaId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /inscripciones_prueba con combinacion aspirante+prueba duplicada
     * debe devolver 422 y el header REGISTRO-DUPLICADO indicando violacion de constraint.
     *
     * Estrategia: Crear un aspirante DINAMICO + PRUEBA_2, crear dos inscripciones
     * con esa misma combinacion para provocar violacion de UNIQUE constraint.
     *
     * Validaciones:
     * - Status 201 primera inscripcion ✓
     * - Status 422 segunda (duplicada) ✓
     * - Header REGISTRO-DUPLICADO = "true" ✓
     * - Mensaje contiene indicativo de constraint ✓
     */
    @Test
    void create_ConInscripcionDuplicada_DebeRetornar422_YHeaderDuplicado() {
        // 1) Crear un aspirante dinamico (no exista en init.sql)
        UUID idAspiranteDinamico = crearAspiranteDinamico();
        
        // 2) Primera inscripcion: aspirante dinamico + prueba 2 (debe ser 201)
        InscripcionesPrueba primera = new InscripcionesPrueba();
        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setId(idAspiranteDinamico);
        primera.setIdAspirante(aspirante);
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(ID_PRUEBA_2);
        primera.setIdPrueba(prueba);
        primera.setEstado("INSCRITO");

        Response responseFirst = post("inscripciones_prueba", primera);
        assertEquals(201, responseFirst.getStatus(), "Primera inscripcion debe crearse correctamente");

        // 3) Segunda inscripcion: MISMO aspirante + MISMA prueba (debe ser 422 por UNIQUE)
        InscripcionesPrueba duplicada = new InscripcionesPrueba();
        duplicada.setIdAspirante(aspirante);
        duplicada.setIdPrueba(prueba);
        duplicada.setEstado("INSCRITO");

        Response responseDuplicate = post("inscripciones_prueba", duplicada);

        // Validaciones estrictas
        assertEquals(422, responseDuplicate.getStatus(), 
            "POST con inscripcion duplicada debe retornar 422 (unprocessable entity)");
        
        String headerDuplicado = responseDuplicate.getHeaderString("REGISTRO-DUPLICADO");
        assertNotNull(headerDuplicado, "Debe existir header REGISTRO-DUPLICADO");
        assertEquals("true", headerDuplicado, "Header REGISTRO-DUPLICADO debe ser 'true'");
        
        // Validar el cuerpo del error contiene mensaje util
        String responseBody = responseDuplicate.readEntity(String.class);
        assertNotNull(responseBody, "Debe haber un cuerpo de error");
        assertTrue(responseBody.toLowerCase().contains("inscrito") ||
               responseBody.toLowerCase().contains("duplicado") ||
               responseBody.toLowerCase().contains("constraint"),
            "El mensaje debe indicar la violacion de constraint o duplicidad");
    }

    /**
     * Crea un aspirante dinamico para evitar conflictos con datos de init.sql.
     */
    private UUID crearAspiranteDinamico() {
        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setNombres("Aspirante ST");
        nuevoAspirante.setApellidos("Duplicado");
        nuevoAspirante.setDui(String.valueOf(System.currentTimeMillis()).substring(4));
        nuevoAspirante.setUsaSillaRuedas(false);

        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(UUID.fromString("b1000000-0000-0000-0000-000000000001"));
        nuevoAspirante.setIdUsuario(usuario);

        Response response = post("aspirantes_datos", nuevoAspirante);
        assertEquals(201, response.getStatus(), "Fallo al crear aspirante dinamico");
        String location = response.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
