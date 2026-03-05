package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClavesExamenFrmTest {

    @Mock
    ClavesExamanDAO clavesExamanDAO;

    @Mock
    PruebasAdmisionDAO pruebasAdmisionDAO;

    @InjectMocks
    ClavesExamenFrm frm;

    @Test
    void inicializarListas() {
        // Simulamos la respuesta de la base de datos
        Mockito.when(pruebasAdmisionDAO.findRange(0, 100)).thenReturn(new ArrayList<>());

        // Ejecutamos el método
        frm.inicializarListas();

        // Verificamos que la lista se haya llenado y el getter funcione
        assertNotNull(frm.getListPruebas());
    }

    @Test
    void getFacesContext() {
        // Como no hay servidor web, JSF lanza error
        assertThrows(NoClassDefFoundError.class, () -> {
            frm.getFacesContext();
        });
    }

    @Test
    void getDao() {
        assertEquals(clavesExamanDAO, frm.getDao());
    }

    @Test
    void nuevoRegistro_y_createNewEntity() {
        // Ejecutamos
        ClavesExaman nuevo = frm.nuevoRegistro();

        // Verificamos que se cree y que la foránea esté inicializada
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdPrueba(), "La foránea Prueba no debe ser nula para evitar errores de JSF");
    }

    @Test
    void buscarRegistroPorId() {
        UUID idPrueba = UUID.randomUUID();
        ClavesExaman esperado = new ClavesExaman();
        Mockito.when(clavesExamanDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: ID Válido
        assertEquals(esperado, frm.buscarRegistroPorId(idPrueba));
        // Caso 2: ID Nulo
        assertNull(frm.buscarRegistroPorId(null));
    }

    @Test
    void getIdAsText() {
        ClavesExaman registro = new ClavesExaman();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        // Caso 1: Registro con ID
        assertEquals(idPrueba.toString(), frm.getIdAsText(registro));
        // Caso 2: Registro nulo
        assertNull(frm.getIdAsText(null));
        // Caso 3: Registro sin ID
        assertNull(frm.getIdAsText(new ClavesExaman()));
    }

    @Test
    void getIdByText() {
        UUID idPrueba = UUID.randomUUID();
        ClavesExaman esperado = new ClavesExaman();
        Mockito.when(clavesExamanDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: String de UUID válido
        assertEquals(esperado, frm.getIdByText(idPrueba.toString()));

        // Caso 2: String vacío o nulo
        assertNull(frm.getIdByText(""));
        assertNull(frm.getIdByText(null));

        // Caso 3: String Inválido (Cubre el catch de IllegalArgumentException)
        assertNull(frm.getIdByText("hola-soy-un-uuid-invalido"));
    }

    @Test
    void getEntityId() {
        ClavesExaman registro = new ClavesExaman();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        assertEquals(idPrueba, frm.getEntityId(registro));
        assertNull(frm.getEntityId(null));
    }

    @Test
    void getEntityName() {
        assertEquals("clavesExamenFrm", frm.getEntityName());
    }

    @Test
    void getListPruebas() {
        // Queda vacío porque este comportamiento ya se evalúa y cubre en inicializarListas()
    }
}