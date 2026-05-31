package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemaResourceTest {

    @Mock private TemaDAO dao;
    @InjectMocks private TemaResource resource;

    private Tema testTema;

    @BeforeEach
    void setUp() {
        testTema = new Tema();
        testTema.setNombreTema("Tema de ejemplo");
    }

    @Test
    void findRange_ConParametrosValidos_Retorna200ConLista() {
        List<Tema> lista = List.of(testTema);
        when(dao.count()).thenReturn(1);
        when(dao.findRange(0, 10)).thenReturn(lista);

        Response response = resource.listTemas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConListaVacia_Retorna200ConTotalCero() {
        when(dao.count()).thenReturn(0);
        when(dao.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listTemas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConExcepcionEnDAO_Retorna500() {
        when(dao.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listTemas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
