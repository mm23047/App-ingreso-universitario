package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogoCarreraDAOIT extends AbstractBaseIT {

    @Test
    public void testCount(){
        System.out.println("CatalogoCarreraDAOIT.count()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            int resultado = cut.count();
            // La BD tiene 7 carreras (ISI, MED, ARQ, ADM, ICS, ICC, MAT)
            assertEquals(7, resultado);
            return null;
        });
    }

    @Test
    public void testFindRange(){
        System.out.println("CatalogoCarreraDAOIT.findRange()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            List<CatalogoCarrera> resultado = cut.findRange(0, 10);
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(7, resultado.size());
            System.out.println("Mi resultado del test FindRange: " + resultado);
            return null;
        });
    }

    @Test
    public void testCrear(){
        System.out.println("CatalogoCarreraDAOIT.create()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            CatalogoCarrera nuevaCarrera = new CatalogoCarrera();
            nuevaCarrera.setIdCarrera("INGSO-98");
            nuevaCarrera.setNombreCatalogoCarrera("Ingenieria en Sistemas Informaticos");

            cut.crear(nuevaCarrera);

            // Después de insertar una, deben ser 8
            assertEquals(8, cut.count());
            System.out.println("Mi resultado del test Crear: " + cut.count());
            return null;
        });

        // Verificar rollback: debe volver a 7
        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;
            assertEquals(7, cut.count());
            return null;
        });
    }

    @Test
    public void testLeer(){
        System.out.println("CatalogoCarreraDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            // Tomar una carrera que ya existe en la BD
            CatalogoCarrera carreraExistente = cut.findRange(0, 1).get(0);

            CatalogoCarrera resultado = cut.leer(carreraExistente.getIdCarrera());

            assertNotNull(resultado, "El ID del catálogo no puede ser nulo porque ya debe existir");
            assertEquals(carreraExistente.getIdCarrera(), resultado.getIdCarrera());
            assertEquals(carreraExistente.getNombreCatalogoCarrera(), resultado.getNombreCatalogoCarrera());
            return null;
        });
    }

    @Test
    public void testActualizar(){
        System.out.println("CatalogoCarreraDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            // Leemos la primera carrera (ej. ISI)
            CatalogoCarrera catalogoCarrera = cut.findRange(0, 1).get(0);
            assertNotNull(catalogoCarrera);

            catalogoCarrera.setNombreCatalogoCarrera("Ingenieria NOMBRE Modificada - 2026");

            CatalogoCarrera resultado = cut.actualizar(catalogoCarrera);

            assertEquals("Ingenieria NOMBRE Modificada - 2026", resultado.getNombreCatalogoCarrera());
            return null;
        });
    }

    @Test
    public void testEliminar(){
        System.out.println("CatalogoCarreraDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            // Crear un dato TEMPORAL con ID único (diferente al usado en testCrear)
            CatalogoCarrera carreraTemporal = new CatalogoCarrera();
            carreraTemporal.setIdCarrera("TEMP99");
            carreraTemporal.setNombreCatalogoCarrera("Carrera Temporal");
            cut.crear(carreraTemporal);

            // Deben existir 8 carreras (7 originales + 1 temporal)
            assertEquals(8, cut.count());

            // Eliminar la carrera temporal
            cut.eliminar(carreraTemporal);

            // Verificar que ya no existe el registro (vuelve a 7)
            assertEquals(7, cut.count());
            assertNull(cut.leer("TEMP99"), "El registro temporal ya no debe existir");
            return null;
        });
    }

    // ===================== NAMED QUERY: findByNombre =====================

    @Test
    public void testFindByNombre() {
        System.out.println("CatalogoCarreraDAOIT.findByNombre()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            CatalogoCarrera resultado = cut.findByNombre("Ingeniería en Sistemas Informáticos");

            assertNotNull(resultado);
            assertEquals("ISI", resultado.getIdCarrera());
            assertEquals("Ingeniería en Sistemas Informáticos", resultado.getNombreCatalogoCarrera());
            return null;
        });
    }

    @Test
    public void testFindByNombreNoExiste() {
        System.out.println("CatalogoCarreraDAOIT.findByNombre() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            CatalogoCarrera resultado = cut.findByNombre("Carrera Inexistente");

            assertNull(resultado, "Debe retornar null si el nombre no existe");
            return null;
        });
    }

    @Test
    public void testFindByNombreInvalido() {
        System.out.println("CatalogoCarreraDAOIT.findByNombre() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByNombre(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByNombre(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByNombre("   "));
            return null;
        });
    }
}