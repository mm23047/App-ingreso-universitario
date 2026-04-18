package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UsuariosSistemaResourceST extends AbstractResourceST {

    private static final UUID ID_ADMIN = UUID.fromString("b1000000-0000-0000-0000-000000000001");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("usuarios_sistema");

        assertEquals(200, response.getStatus());

        UsuariosSistema[] arreglo = response.readEntity(UsuariosSistema[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 3);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("usuarios_sistema/" + ID_ADMIN);

        assertEquals(200, response.getStatus());

        UsuariosSistema entidad = response.readEntity(UsuariosSistema.class);
        assertNotNull(entidad);
        assertEquals(ID_ADMIN, entidad.getId());
        assertEquals("admin", entidad.getNombreUsuario());
        assertEquals("admin@ues.edu.sv", entidad.getCorreo());
        assertEquals("ADMIN", entidad.getRol());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idNotExistente = UUID.randomUUID();

        Response response = get("usuarios_sistema/" + idNotExistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("nuevo_usuario");
        nuevo.setCorreo("nuevo_usuario@correo.com");
        nuevo.setContrasenaHash("$2a$10$hashTest");
        nuevo.setRol("ASPIRANTE");

        Response responseCreacion = post("usuarios_sistema", nuevo);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsultando = get("usuarios_sistema/" + idCreado);
        assertEquals(200, responseConsultando.getStatus());

        UsuariosSistema creado = responseConsultando.readEntity(UsuariosSistema.class);
        assertEquals(idCreado, creado.getId());
        assertEquals("nuevo_usuario", creado.getNombreUsuario());
        assertEquals("nuevo_usuario@correo.com", creado.getCorreo());
        assertEquals("ASPIRANTE", creado.getRol());
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearUsuarioReal("temp_update", "temp_update@correo.com", "ADMIN");

        UsuariosSistema actualizado = new UsuariosSistema();
        actualizado.setNombreUsuario("temp_update_mod");
        actualizado.setCorreo("temp_update_mod@correo.com");
        actualizado.setContrasenaHash("$2a$10$hashUpdated");
        actualizado.setRol("ADMIN");

        Response responseUpdate = put("usuarios_sistema/" + idCreado, actualizado);
        assertEquals(200, responseUpdate.getStatus());

        UsuariosSistema cuerpo = responseUpdate.readEntity(UsuariosSistema.class);
        assertEquals("temp_update_mod", cuerpo.getNombreUsuario());

        Response responseConsulta = get("usuarios_sistema/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("temp_update_mod", responseConsulta.readEntity(UsuariosSistema.class).getNombreUsuario());
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        UUID idCreado = crearUsuarioReal("temp_delete", "temp_delete@correo.com", "ASPIRANTE");

        Response responseDelete = delete("usuarios_sistema/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("usuarios_sistema/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    private UUID crearUsuarioReal(String nombre, String correo, String rol) {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario(nombre);
        nuevo.setCorreo(correo);
        nuevo.setContrasenaHash("$2a$10$hashHelper");
        nuevo.setRol(rol);

        Response responseCreacion = post("usuarios_sistema", nuevo);
        assertEquals(201, responseCreacion.getStatus());

        String location = responseCreacion.getHeaderString("Location");
        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
