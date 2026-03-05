package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InscripcionesPruebaFrmTest {

    @Mock
    InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @Mock
    AspirantesDatoDAO aspirantesDatoDAO;

    @Mock
    PruebasAdmisionDAO pruebasAdmisionDAO;

    @InjectMocks
    InscripcionesPruebaFrm frm;

    @Test
    void inicializarListas() {
        // Simulamos la respuesta de las tablas foráneas
        Mockito.when(aspirantesDatoDAO.findRange(0, 100)).thenReturn(new ArrayList<>());
        Mockito.when(pruebasAdmisionDAO.findRange(0, 100)).thenReturn(new ArrayList<>());

        // Ejecutamos
        frm.inicializarListas();

        // Verificamos
        assertNotNull(frm.getListAspirantes());
        assertNotNull(frm.getListPruebas());
    }

    @Test
    void getFacesContext() {
        assertThrows(NoClassDefFoundError.class, () -> {
            frm.getFacesContext();
        });
    }

    @Test
    void getDao() {
        assertEquals(inscripcionesPruebaDAO, frm.getDao());
    }

    @Test
    void nuevoRegistro_y_createNewEntity() {
        // Ejecutamos ambos métodos en conjunto
        InscripcionesPrueba nuevo = frm.nuevoRegistro();

        // Validamos inicialización de foráneas y estado por defecto
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdAspirante(), "Debe inicializar el Aspirante");
        assertNotNull(nuevo.getIdPrueba(), "Debe inicializar la Prueba");
        assertEquals("INSCRITO", nuevo.getEstado(), "El estado por defecto debe ser INSCRITO");
    }

    @Test
    void buscarRegistroPorId() {
        UUID idPrueba = UUID.randomUUID();
        InscripcionesPrueba esperado = new InscripcionesPrueba();
        Mockito.when(inscripcionesPruebaDAO.leer(idPrueba)).thenReturn(esperado);

        // ID válido
        assertEquals(esperado, frm.buscarRegistroPorId(idPrueba));
        // ID nulo
        assertNull(frm.buscarRegistroPorId(null));
    }

    @Test
    void getIdAsText() {
        InscripcionesPrueba registro = new InscripcionesPrueba();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        assertEquals(idPrueba.toString(), frm.getIdAsText(registro));
        assertNull(frm.getIdAsText(null));
        assertNull(frm.getIdAsText(new InscripcionesPrueba()));
    }

    @Test
    void getIdByText() {
        UUID idPrueba = UUID.randomUUID();
        InscripcionesPrueba esperado = new InscripcionesPrueba();
        Mockito.when(inscripcionesPruebaDAO.leer(idPrueba)).thenReturn(esperado);

        // 1. UUID válido
        assertEquals(esperado, frm.getIdByText(idPrueba.toString()));

        // 2. Textos vacíos o nulos
        assertNull(frm.getIdByText(""));
        assertNull(frm.getIdByText(null));

        // 3. String que no es UUID (Activa el catch y la línea del Logger)
        assertNull(frm.getIdByText("esto-no-es-un-uuid"));
    }

    @Test
    void getEntityId() {
        InscripcionesPrueba registro = new InscripcionesPrueba();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        assertEquals(idPrueba, frm.getEntityId(registro));
        assertNull(frm.getEntityId(null));
    }

    @Test
    void getEntityName() {
        assertEquals("inscripcionesPruebasFrm", frm.getEntityName());
    }

    // Los métodos getListAspirantes() y getListPruebas() ya quedan cubiertos en inicializarListas()
}