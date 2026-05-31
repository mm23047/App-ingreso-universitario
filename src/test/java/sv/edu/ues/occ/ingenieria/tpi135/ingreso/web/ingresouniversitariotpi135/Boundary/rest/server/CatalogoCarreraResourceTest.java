package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CatalogoCarreraResource.
 * CatalogoCarrera usa PK String (idCarrera) provista por el cliente.
 */
@ExtendWith(MockitoExtension.class)
class CatalogoCarreraResourceTest {

    @Mock
    private CatalogoCarreraDAO catalogoCarreraDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private CatalogoCarreraResource resource;

    private CatalogoCarrera entidad;

    @BeforeEach
    void setUp() {
        entidad = new CatalogoCarrera();
        entidad.setIdCarrera("ING");
        entidad.setNombreCatalogoCarrera("Ingeniería Civil");
    }

    // ==================== listCarreras (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(catalogoCarreraDAO.count()).thenReturn(1);
        when(catalogoCarreraDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listCarreras(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(catalogoCarreraDAO).count();
        verify(catalogoCarreraDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(catalogoCarreraDAO.count()).thenReturn(0);
        when(catalogoCarreraDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listCarreras(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(catalogoCarreraDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listCarreras(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== getCarrera (GET /{idCarrera}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(catalogoCarreraDAO.leer("ING")).thenReturn(entidad);

        Response response = resource.getCarrera("ING");

        assertEquals(200, response.getStatus());
        CatalogoCarrera resultado = (CatalogoCarrera) response.getEntity();
        assertEquals("ING", resultado.getIdCarrera());
        verify(catalogoCarreraDAO).leer("ING");
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(catalogoCarreraDAO.leer("INEXISTENTE")).thenReturn(null);

        Response response = resource.getCarrera("INEXISTENTE");

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(catalogoCarreraDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getCarrera("ING");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== createCarrera (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        CatalogoCarrera nueva = new CatalogoCarrera();
        nueva.setIdCarrera("MED");
        nueva.setNombreCatalogoCarrera("Medicina");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/carreras/MED"));

        Response response = resource.createCarrera(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(catalogoCarreraDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createCarrera(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(catalogoCarreraDAO);
    }

    @Test
    void create_ConIdCarreraNulo_DebeRetornar400() {
        CatalogoCarrera sinId = new CatalogoCarrera();
        sinId.setNombreCatalogoCarrera("Sin Id");

        Response response = resource.createCarrera(sinId, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(catalogoCarreraDAO);
    }

    @Test
    void create_ConIdCarreraVacio_DebeRetornar400() {
        CatalogoCarrera conIdVacio = new CatalogoCarrera();
        conIdVacio.setIdCarrera("   ");
        conIdVacio.setNombreCatalogoCarrera("Sin Id Válido");

        Response response = resource.createCarrera(conIdVacio, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(catalogoCarreraDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        CatalogoCarrera nueva = new CatalogoCarrera();
        nueva.setIdCarrera("MED");
        nueva.setNombreCatalogoCarrera("Medicina");
        doThrow(new RuntimeException("Error de BD")).when(catalogoCarreraDAO).crear(any());

        Response response = resource.createCarrera(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== updateCarrera (PUT /{idCarrera}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(catalogoCarreraDAO.leer("ING")).thenReturn(entidad);
        CatalogoCarrera actualizada = new CatalogoCarrera();
        actualizada.setNombreCatalogoCarrera("Ingeniería Industrial");

        Response response = resource.updateCarrera("ING", actualizada);

        assertEquals(200, response.getStatus());
        verify(catalogoCarreraDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(catalogoCarreraDAO.leer("INEXISTENTE")).thenReturn(null);

        Response response = resource.updateCarrera("INEXISTENTE", entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(catalogoCarreraDAO.leer("ING")).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateCarrera("ING", entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== deleteCarrera (DELETE /{idCarrera}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(catalogoCarreraDAO.leer("ING")).thenReturn(entidad);

        Response response = resource.deleteCarrera("ING");

        assertEquals(204, response.getStatus());
        verify(catalogoCarreraDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(catalogoCarreraDAO.leer("INEXISTENTE")).thenReturn(null);

        Response response = resource.deleteCarrera("INEXISTENTE");

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(catalogoCarreraDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteCarrera("ING");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
