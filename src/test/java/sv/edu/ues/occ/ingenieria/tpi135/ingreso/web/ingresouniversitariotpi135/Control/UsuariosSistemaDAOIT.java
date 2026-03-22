package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsuariosSistemaDAOIT extends AbstractBaseIT {

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 3 usuarios del sistema
            assertTrue(resultado > 0);
            assertEquals(3, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
            cut.em = em;

            List<UsuariosSistema> resultado = cut.findRange(0, 10);

            // BD recién iniciada con init.sql → 3 usuarios del sistema
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
            cut.em = em;

            UsuariosSistema nuevo = new UsuariosSistema();
            nuevo.setNombreUsuario("testuser");
            nuevo.setCorreo("testuser@ues.edu.sv");
            nuevo.setContrasenaHash("$2a$10$hashtest");
            nuevo.setRol("ASPIRANTE");

            cut.crear(nuevo);

            // Validación dentro de la transacción
            assertEquals(4, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 3
        ejecutarEnTransaccion(em -> {
            UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
            cut.em = em;

            assertEquals(3, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
            cut.em = em;

            // Obtener el primer usuario del init.sql
            UsuariosSistema usuario = cut.findRange(0, 1).get(0);
            assertNotNull(usuario);

            // Modificar dentro de la transacción
            usuario.setNombreUsuario("usuario_actualizado");

            UsuariosSistema resultado = cut.actualizar(usuario);

            assertNotNull(resultado);
            assertEquals("usuario_actualizado", resultado.getNombreUsuario());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
            cut.em = em;

            // Crear un nuevo usuario para eliminarlo
            UsuariosSistema nuevo = new UsuariosSistema();
            nuevo.setNombreUsuario("usuario_para_eliminar");
            nuevo.setCorreo("eliminar@ues.edu.sv");
            nuevo.setContrasenaHash("$2a$10$hashtest");
            nuevo.setRol("ADMIN");

            cut.crear(nuevo);
            assertEquals(4, cut.count());

            // Eliminar el usuario recién creado
            cut.eliminar(nuevo);
            assertEquals(3, cut.count());

            return null;
        });
    }
}
