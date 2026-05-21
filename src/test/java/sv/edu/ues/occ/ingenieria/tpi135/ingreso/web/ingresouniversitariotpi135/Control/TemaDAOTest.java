package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TemaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Test
    void crear_CuandoEsSuPropioPadre_LanzaExcepcion() {
        TemaDAO cut = new TemaDAO();
        cut.em = entityManager;

        Tema tema = new Tema();
        tema.setIdTema(UUID.randomUUID());
        tema.setIdTemaPadre(tema);

        assertThrows(IllegalArgumentException.class, () -> cut.crear(tema));
        verify(entityManager, never()).persist(tema);
        verify(entityManager, never()).flush();
    }

    @Test
    void actualizar_CuandoHayCicloIndirecto_LanzaExcepcion() {
        TemaDAO cut = new TemaDAO();
        cut.em = entityManager;

        Tema temaA = new Tema();
        temaA.setIdTema(UUID.randomUUID());
        Tema temaB = new Tema();
        temaB.setIdTema(UUID.randomUUID());
        Tema temaC = new Tema();
        temaC.setIdTema(UUID.randomUUID());

        temaA.setIdTemaPadre(temaB);
        temaB.setIdTemaPadre(temaC);
        temaC.setIdTemaPadre(temaA);

        assertThrows(IllegalArgumentException.class, () -> cut.actualizar(temaA));
        verify(entityManager, never()).merge(temaA);
        verify(entityManager, never()).flush();
    }

    @Test
    void crear_CuandoJerarquiaEsValida_Persiste() {
        TemaDAO cut = new TemaDAO();
        cut.em = entityManager;

        AreasConocimiento area = new AreasConocimiento();
        area.setIdAreaConocimiento(UUID.randomUUID());

        Tema padre = new Tema();
        padre.setIdTema(UUID.randomUUID());
        padre.setAreaConocimiento(area);
        padre.setNombreTema("Padre");

        Tema tema = new Tema();
        tema.setAreaConocimiento(area);
        tema.setNombreTema("Hijo");
        tema.setIdTemaPadre(padre);

        doNothing().when(entityManager).persist(tema);
        doNothing().when(entityManager).flush();

        cut.crear(tema);

        verify(entityManager).persist(tema);
        verify(entityManager).flush();
    }
}
