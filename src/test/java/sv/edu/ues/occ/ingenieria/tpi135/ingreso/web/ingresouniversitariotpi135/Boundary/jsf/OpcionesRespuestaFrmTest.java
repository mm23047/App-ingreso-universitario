package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.OpcionesRespuestaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpcionesRespuestaFrmTest {

    @Mock
    OpcionesRespuestaDAO opcionesRespuestaDAO;

    @Mock
    BancoPreguntaDAO bancoPreguntasDAO;

    @InjectMocks
    OpcionesRespuestaFrm frm;

    @Test
    void inicializarListas() {
        // Simulamos la respuesta de la base de datos para la tabla foránea
        Mockito.when(bancoPreguntasDAO.findRange(0, 100)).thenReturn(new ArrayList<>());

        // Ejecutamos el método
        frm.inicializarListas();

        // Verificamos que se llene correctamente
        assertNotNull(frm.getListPreguntas());
    }

    @Test
    void getFacesContext() {
        // Al no haber un servidor JSF corriendo, debe lanzar este error
        assertThrows(NoClassDefFoundError.class, () -> {
            frm.getFacesContext();
        });
    }

    @Test
    void getDao() {
        assertEquals(opcionesRespuestaDAO, frm.getDao());
    }

    @Test
    void nuevoRegistro_y_createNewEntity() {
        // Ejecutamos (esto llama internamente a createNewEntity)
        OpcionesRespuesta nuevo = frm.nuevoRegistro();

        // Validamos que la entidad nazca bien configurada
        assertNotNull(nuevo);
        assertNotNull(nuevo.getIdPregunta(), "Debe inicializar la Pregunta para evitar NullPointerExceptions");
        assertFalse(nuevo.getEsCorrecta(), "El valor por defecto 'esCorrecta' debe ser false");
    }

    @Test
    void buscarRegistroPorId() {
        UUID idPrueba = UUID.randomUUID();
        OpcionesRespuesta esperado = new OpcionesRespuesta();
        Mockito.when(opcionesRespuestaDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: ID Válido
        assertEquals(esperado, frm.buscarRegistroPorId(idPrueba));
        // Caso 2: ID nulo
        assertNull(frm.buscarRegistroPorId(null));
    }

    @Test
    void getIdAsText() {
        OpcionesRespuesta registro = new OpcionesRespuesta();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        // Caso 1: Registro válido con ID
        assertEquals(idPrueba.toString(), frm.getIdAsText(registro));
        // Caso 2: Registro nulo
        assertNull(frm.getIdAsText(null));
        // Caso 3: Registro sin ID
        assertNull(frm.getIdAsText(new OpcionesRespuesta()));
    }

    @Test
    void getIdByText() {
        UUID idPrueba = UUID.randomUUID();
        OpcionesRespuesta esperado = new OpcionesRespuesta();
        Mockito.when(opcionesRespuestaDAO.leer(idPrueba)).thenReturn(esperado);

        // Caso 1: UUID en String válido
        assertEquals(esperado, frm.getIdByText(idPrueba.toString()));

        // Caso 2: Textos nulos o vacíos
        assertNull(frm.getIdByText(null));
        assertNull(frm.getIdByText(""));

        // Caso 3: String que no es un UUID (Cubre el IllegalArgumentException)
        assertNull(frm.getIdByText("no-soy-un-uuid-valido"));
    }

    @Test
    void getEntityId() {
        OpcionesRespuesta registro = new OpcionesRespuesta();
        UUID idPrueba = UUID.randomUUID();
        registro.setId(idPrueba);

        assertEquals(idPrueba, frm.getEntityId(registro));
        assertNull(frm.getEntityId(null));
    }

    @Test
    void getEntityName() {
        // Valida que el constructor haya seteado bien el nombreBean
        assertEquals("opcionesRespuestaFrm", frm.getEntityName());
    }

    @Test
    void getListPreguntas() {
        // Se deja vacío porque inicializarListas() ya lo cubre y valida
    }
}