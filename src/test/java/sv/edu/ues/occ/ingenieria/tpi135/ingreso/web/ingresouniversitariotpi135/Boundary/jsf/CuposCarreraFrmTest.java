package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CuposCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CuposCarreraFrmTest {

    @Mock CuposCarreraDAO cuposCarreraDAO;
    @Mock PruebasAdmisionDAO pruebasAdmisionDAO;
    @Mock CatalogoCarreraDAO catalogoCarrerasDAO;
    @Mock EtapasAdmisionDAO etapasAdmisionDAO;

    @InjectMocks
    CuposCarreraFrm frm;

    @Test
    void inicializarListas() {
        Mockito.when(pruebasAdmisionDAO.findRange(0, 100)).thenReturn(new ArrayList<>());
        Mockito.when(catalogoCarrerasDAO.findRange(0, 100)).thenReturn(new ArrayList<>());
        Mockito.when(etapasAdmisionDAO.findRange(0, 100)).thenReturn(new ArrayList<>());

        frm.inicializarListas();

        assertNotNull(frm.getListPruebas());
        assertNotNull(frm.getListCarreras());
        assertNotNull(frm.getListEtapas());
    }

    @Test
    void getFacesContext() {
        assertThrows(NoClassDefFoundError.class, () -> {
            frm.getFacesContext();
        });
    }

    @Test
    void getDao() {
        assertEquals(cuposCarreraDAO, frm.getDao());
    }

    @Test
    void nuevoRegistro_Y_createNewEntity() {
        // Probamos ambos métodos aquí ya que nuevoRegistro() llama a createNewEntity()
        CuposCarrera nuevo = frm.nuevoRegistro();

        assertNotNull(nuevo);
        assertNotNull(nuevo.getId(), "El ID compuesto no debe ser nulo");
        assertNotNull(nuevo.getIdPrueba(), "La foránea Prueba no debe ser nula");
        assertNotNull(nuevo.getIdCarrera(), "La foránea Carrera no debe ser nula");
        assertNotNull(nuevo.getIdEtapa(), "La foránea Etapa no debe ser nula");
    }

    @Test
    void buscarRegistroPorId() {
        CuposCarreraId idPrueba = new CuposCarreraId();
        CuposCarrera esperado = new CuposCarrera();
        Mockito.when(cuposCarreraDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: ID Válido
        assertEquals(esperado, frm.buscarRegistroPorId(idPrueba));
        // Caso 2: ID Nulo
        assertNull(frm.buscarRegistroPorId(null));
    }

    @Test
    void getIdAsText() {
        // Preparamos el ID compuesto
        UUID idPrueba = UUID.randomUUID();
        String idCarrera = "SIS";
        UUID idEtapa = UUID.randomUUID();

        CuposCarreraId idCompuesto = new CuposCarreraId();
        idCompuesto.setIdPrueba(idPrueba);
        idCompuesto.setIdCarrera(idCarrera);
        idCompuesto.setIdEtapa(idEtapa);

        CuposCarrera registro = new CuposCarrera();
        registro.setId(idCompuesto);

        String textoEsperado = idPrueba + "_" + idCarrera + "_" + idEtapa;

        // Caso 1: Registro con ID compuesto
        assertEquals(textoEsperado, frm.getIdAsText(registro));
        // Caso 2: Registro nulo
        assertNull(frm.getIdAsText(null));
        // Caso 3: Registro sin ID
        assertNull(frm.getIdAsText(new CuposCarrera()));
    }

    @Test
    void getIdByText() {
        // Datos para armar el String
        UUID idPrueba = UUID.randomUUID();
        String idCarrera = "SIS";
        UUID idEtapa = UUID.randomUUID();

        String stringValido = idPrueba + "_" + idCarrera + "_" + idEtapa;

        CuposCarreraId idCompuesto = new CuposCarreraId();
        idCompuesto.setIdPrueba(idPrueba);
        idCompuesto.setIdCarrera(idCarrera);
        idCompuesto.setIdEtapa(idEtapa);

        CuposCarrera esperado = new CuposCarrera();
        // Como CuposCarreraId tiene implementado equals() y hashCode(), Mockito sabe compararlos
        Mockito.when(cuposCarreraDAO.leer(idCompuesto)).thenReturn(esperado);

        // Caso 1: String válido (3 partes unidas por guion bajo)
        assertEquals(esperado, frm.getIdByText(stringValido));

        // Caso 2: String nulo o vacío
        assertNull(frm.getIdByText(null));
        assertNull(frm.getIdByText(""));

        // Caso 3: String incompleto (menos de 3 partes, no cumple el .split("_").length == 3)
        assertNull(frm.getIdByText(idPrueba + "_" + idCarrera));

        // Caso 4: String inválido (Las partes no son UUIDs, lanza IllegalArgumentException y lo atrapa el catch)
        assertNull(frm.getIdByText("no-es-uuid_SIS_tampoco-es-uuid"));
    }

    @Test
    void getEntityId() {
        CuposCarrera registro = new CuposCarrera();
        CuposCarreraId idPrueba = new CuposCarreraId();
        registro.setId(idPrueba);

        assertEquals(idPrueba, frm.getEntityId(registro));
        assertNull(frm.getEntityId(null));
    }

    @Test
    void getEntityName() {
        assertEquals("cuposCarreraFrm", frm.getEntityName());
    }

    // Dejamos los Getters vacíos porque ya fueron probados en inicializarListas()
    @Test void getListPruebas() {}
    @Test void getListCarreras() {}
    @Test void getListEtapas() {}
}