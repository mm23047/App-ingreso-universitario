package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion REST para el recurso AspirantesDatoResource.
 *
 * Estas pruebas:
    * - Usan WAR real desplegada en Liberty (via BaseSistemaBDD / AbstractResourceST).
 * - Consumen endpoints reales via HTTP.
 * - Validan el contrato REST (codigos HTTP, headers, cuerpo JSON) y la integracion
 *   con la base de datos inicializada por ingresoTPI135_init.sql.
 */
public class AspirantesDatoResourceST extends AbstractResourceST {

    // UUIDs tomados del init.sql (mismos que en AspirantesDatoDAOIT)
    private static final UUID ID_ASPIRANTE_1   = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_USUARIO_ADMIN = UUID.fromString("b1000000-0000-0000-0000-000000000001");

    /**
     * GET /aspirantes_datos debe devolver al menos los 2 registros iniciales
     * y un header Total-records coherente.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("aspirantes_datos");

        assertEquals(200, response.getStatus());

        AspirantesDato[] arreglo = response.readEntity(AspirantesDato[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);

        boolean encontrado = false;
        for (AspirantesDato a : arreglo) {
            if (ID_ASPIRANTE_1.equals(a.getId())) {
                encontrado = true;
                assertEquals("Juan Carlos", a.getNombres());
                assertEquals("Pérez López", a.getApellidos());
            }
        }
        assertTrue(encontrado);
    }

    /**
     * GET /aspirantes_datos/{id} con un id existente debe devolver 200 y el cuerpo esperado.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("aspirantes_datos/" + ID_ASPIRANTE_1);

        assertEquals(200, response.getStatus());

        AspirantesDato entidad = response.readEntity(AspirantesDato.class);
        assertNotNull(entidad);
        assertEquals(ID_ASPIRANTE_1, entidad.getId());
        assertEquals("Juan Carlos", entidad.getNombres());
        assertEquals("Pérez López", entidad.getApellidos());
        assertNotNull(entidad.getIdUsuario());
    }

    /**
     * GET /aspirantes_datos/{id} con un id inexistente debe devolver 404 y header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("aspirantes_datos/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * POST /aspirantes_datos con una entidad valida debe devolver 201 y permitir consultar luego el recurso creado.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        AspirantesDato nuevo = crearAspirante(ID_USUARIO_ADMIN,
            "Aspirante IT",
            "Integracion",
            "12345678-9",
            false);

        Response responseCreacion = post("aspirantes_datos", nuevo);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("aspirantes_datos/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        AspirantesDato creado = responseConsulta.readEntity(AspirantesDato.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getId());
        assertEquals("Aspirante IT", creado.getNombres());
        assertEquals("Integracion", creado.getApellidos());
        assertNotNull(creado.getIdUsuario());
        assertEquals(ID_USUARIO_ADMIN, creado.getIdUsuario().getId());
    }

    /**
     * POST /aspirantes_datos con una entidad invalida (sin usuario) debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinUsuario_DebeRetornar422() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Sin Usuario");
        nuevo.setApellidos("Prueba");
        nuevo.setDui("11111111-1");
        nuevo.setUsaSillaRuedas(false);

        Response response = post("aspirantes_datos", nuevo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * PUT /aspirantes_datos/{id} con datos validos debe devolver 200 y reflejar los cambios.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Crear primero un aspirante temporal
        UUID idCreado = crearAspiranteReal(ID_USUARIO_ADMIN,
            "Para actualizar",
            "Original",
            "22222222-2",
            false);

        // Construir payload actualizado
        AspirantesDato actualizado = crearAspirante(ID_USUARIO_ADMIN,
            "Actualizado",
            "Integracion",
            "22222222-2",
            true);

        Response responseUpdate = put("aspirantes_datos/" + idCreado, actualizado);

        assertEquals(200, responseUpdate.getStatus());

        AspirantesDato cuerpo = responseUpdate.readEntity(AspirantesDato.class);
        assertNotNull(cuerpo);
        assertEquals(idCreado, cuerpo.getId());
        assertEquals("Actualizado", cuerpo.getNombres());
        assertEquals("Integracion", cuerpo.getApellidos());
        assertEquals(Boolean.TRUE, cuerpo.getUsaSillaRuedas());

        // Verificar persistencia via GET
        Response responseConsulta = get("aspirantes_datos/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        AspirantesDato verificado = responseConsulta.readEntity(AspirantesDato.class);
        assertEquals("Actualizado", verificado.getNombres());
        assertEquals(Boolean.TRUE, verificado.getUsaSillaRuedas());
    }

    /**
     * DELETE /aspirantes_datos/{id} con un id existente debe devolver 204 y luego 404 al consultar.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Crear aspirante temporal
        UUID idCreado = crearAspiranteReal(ID_USUARIO_ADMIN,
            "Para eliminar",
            "Temporal",
            "33333333-3",
            false);

        Response responseDelete = delete("aspirantes_datos/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("aspirantes_datos/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    /**
     * Construye un AspirantesDato coherente a partir del usuario, los datos
     * basicos y el indicador de uso de silla de ruedas.
     */
    private AspirantesDato crearAspirante(UUID idUsuario, String nombres, String apellidos,
                                          String dui, boolean usaSillaRuedas) {
        AspirantesDato aspirante = new AspirantesDato();

        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(idUsuario);
        aspirante.setIdUsuario(usuario);

        aspirante.setNombres(nombres);
        aspirante.setApellidos(apellidos);
        aspirante.setDui(dui);
        aspirante.setUsaSillaRuedas(usaSillaRuedas);

        return aspirante;
    }

    /**
     * Crea realmente un aspirante via el recurso REST, reutilizado por pruebas
     * que solo necesitan un aspirante existente como precondicion. Encapsula
     * el POST y la extraccion del UUID desde el header Location.
     */
    private UUID crearAspiranteReal(UUID idUsuario, String nombres, String apellidos,
                                    String dui, boolean usaSillaRuedas) {
        AspirantesDato nuevo = crearAspirante(idUsuario, nombres, apellidos, dui, usaSillaRuedas);

        Response responseCreacion = post("aspirantes_datos", nuevo);
        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
