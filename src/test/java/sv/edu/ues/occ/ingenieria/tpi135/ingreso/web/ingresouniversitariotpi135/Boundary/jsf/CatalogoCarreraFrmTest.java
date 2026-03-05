package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// 1. Habilitamos Mockito en nuestra clase de prueba
@ExtendWith(MockitoExtension.class)
class CatalogoCarreraFrmTest {

    // 2. Creamos el "clon falso" de tu DAO
    @Mock
    CatalogoCarreraDAO mockDao;

    // 3. Inyectamos ese clon falso dentro de tu Frm real
    @InjectMocks
    CatalogoCarreraFrm frm;

    @Test
    void getFacesContext() {
        // Como no estamos en un servidor web, JSF lanzará un error crítico al no encontrar sus clases internas.
        assertThrows(NoClassDefFoundError.class, () -> {
            frm.getFacesContext();
        });
    }

    @Test
    void getDao() {
        // Verificamos que el DAO que devuelve es nuestro clon falso
        assertEquals(mockDao, frm.getDao());
    }

    @Test
    void nuevoRegistro() {
        // Verificamos que cree un objeto limpio
        CatalogoCarrera nuevo = frm.nuevoRegistro();
        assertNotNull(nuevo);
        assertEquals("", nuevo.getIdCarrera());
        assertEquals("", nuevo.getNombre());
    }

    @Test
    void buscarRegistroPorId() {
        String idSimulado = "SIS-01";
        CatalogoCarrera entidadSimulada = new CatalogoCarrera();

        // REGLA DE MOCKITO: "Cuando el DAO falso intente leer 'SIS-01', devuelve entidadSimulada"
        when(mockDao.leer(idSimulado)).thenReturn(entidadSimulada);

        // Camino 1: ID válido
        assertEquals(entidadSimulada, frm.buscarRegistroPorId(idSimulado));

        // Camino 2: ID nulo (no debería llamar al DAO, devuelve null)
        assertNull(frm.buscarRegistroPorId(null));
    }

    @Test
    void getIdAsText() {
        CatalogoCarrera entidad = new CatalogoCarrera();

        // Camino 1: Entidad válida con ID válido
        entidad.setIdCarrera("MED-01");
        assertEquals("MED-01", frm.getIdAsText(entidad));

        // Camino 2: Entidad válida, pero su ID es nulo
        entidad.setIdCarrera(null);
        assertNull(frm.getIdAsText(entidad));

        // Camino 3: Entidad completamente nula
        assertNull(frm.getIdAsText(null));
    }

    @Test
    void getIdByText() {
        String idValido = "ARQ-01";
        CatalogoCarrera entidadSimulada = new CatalogoCarrera();

        // REGLA DE MOCKITO
        when(mockDao.leer(idValido)).thenReturn(entidadSimulada);

        // Camino 1: Texto válido
        assertEquals(entidadSimulada, frm.getIdByText(idValido));

        // Camino 2: Texto nulo
        assertNull(frm.getIdByText(null));

        // Camino 3: Texto vacío (solo espacios)
        assertNull(frm.getIdByText("   "));
    }

    @Test
    void createNewEntity() {
        CatalogoCarrera nueva = frm.createNewEntity();
        assertNotNull(nueva);
        assertEquals("", nueva.getIdCarrera());
    }

    @Test
    void getEntityId() {
        CatalogoCarrera entidad = new CatalogoCarrera();
        entidad.setIdCarrera("IND-01");

        // Camino 1: Entidad no es nula
        assertEquals("IND-01", frm.getEntityId(entidad));

        // Camino 2: Entidad nula
        assertNull(frm.getEntityId(null));
    }

    @Test
    void getEntityName() {
        assertEquals("catalogoCarreraFrm", frm.getEntityName());
    }
}