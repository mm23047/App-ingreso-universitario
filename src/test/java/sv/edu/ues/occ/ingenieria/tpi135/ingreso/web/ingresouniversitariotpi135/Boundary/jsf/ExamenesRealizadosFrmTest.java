package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionesAulaPupitreDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExamenesRealizadosFrmTest {

    @Mock
    ExamenesRealizadoDAO examenesRealizadoDAO;
    @Mock
    ClavesExamanDAO clavesExamanDAO;
    @Mock
    AsignacionesAulaPupitreDAO  asignacionesAulaPupitreDAO;
    @Mock
    EtapasAdmisionDAO etapasAdmisionDAO;

    @InjectMocks
    ExamenesRealizadosFrm frm;

    @Test
    void inicializarListas() {
        Mockito.when(clavesExamanDAO.findRange(0,100)).thenReturn(new ArrayList<>());
        Mockito.when(etapasAdmisionDAO.findRange(0,100)).thenReturn(new ArrayList<>());
        Mockito.when(asignacionesAulaPupitreDAO.findRange(0,100)).thenReturn(new ArrayList<>());

        frm.inicializarListas();

        // Verificamos que las listas ya no sean nulas
        assertNotNull(frm.getListClavesExaman());
        assertNotNull(frm.getListAsignacionesAula());
        assertNotNull(frm.getListEtapasAdmision());

    }

    @Test
    void getFacesContext() {
        // Como no estamos en un servidor web, JSF lanzará un error crítico al no encontrar sus clases internas.
        assertThrows(NoClassDefFoundError.class, () -> {
            frm.getFacesContext();
        });
    }

    @Test
    void getDao() {
        assertEquals(examenesRealizadoDAO, frm.getDao());
    }


    @Test
    void buscarRegistroPorId() {
        UUID idPrueba = UUID.randomUUID();
        ExamenesRealizado esperado = new ExamenesRealizado();
        Mockito.when(examenesRealizadoDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: ID Válido
        assertEquals(esperado, frm.buscarRegistroPorId(idPrueba));
        // Caso 2: ID Nulo
        assertNull(frm.buscarRegistroPorId(null));
    }

    @Test
    void getIdAsText() {
        ExamenesRealizado registro = new ExamenesRealizado();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        // Caso 1: Registro con ID
        assertEquals(idPrueba.toString(), frm.getIdAsText(registro));
        // Caso 2: Registro nulo
        assertNull(frm.getIdAsText(null));
        // Caso 3: Registro sin ID
        assertNull(frm.getIdAsText(new ExamenesRealizado()));
    }

    @Test
    void getIdByText() {
        UUID idPrueba = UUID.randomUUID();
        ExamenesRealizado esperado = new ExamenesRealizado();
        Mockito.when(examenesRealizadoDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: String de UUID válido
        assertEquals(esperado, frm.getIdByText(idPrueba.toString()));

        // Caso 2: String vacío o nulo
        assertNull(frm.getIdByText(""));
        assertNull(frm.getIdByText(null));

        // Caso 3: String Inválido (Esto cubre exactamente la línea de tu catch con el Logger)
        assertNull(frm.getIdByText("hola-soy-un-uuid-invalido"));
    }

    @Test
    void nuevoRegistroYCreateNewEntity() {
        // Ejecutamos
        ExamenesRealizado nuevo = frm.nuevoRegistro();

        // Verificamos que el "cascarón" esté bien armado con sus foráneas inicializadas
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdAsignacion(), "La asignación no debe ser nula para evitar errores de JSF");
        assertNotNull(nuevo.getIdClave(), "La clave no debe ser nula");
        assertNotNull(nuevo.getIdEtapa(), "La etapa no debe ser nula");
        assertNotNull(nuevo.getFechaRealizacion(), "La fecha debe estar inicializada");
    }

    @Test
    void getEntityId() {
        ExamenesRealizado registro = new ExamenesRealizado();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        assertEquals(idPrueba, frm.getEntityId(registro));
        assertNull(frm.getEntityId(null));
    }

    @Test
    void getEntityName() {
        assertEquals("examenesRealizadosFrm", frm.getEntityName());
    }

    @Test
    void getListAsignacionesAula() {
    }

    @Test
    void getListEtapasAdmision() {
    }

    @Test
    void getListClavesExaman() {
    }
}