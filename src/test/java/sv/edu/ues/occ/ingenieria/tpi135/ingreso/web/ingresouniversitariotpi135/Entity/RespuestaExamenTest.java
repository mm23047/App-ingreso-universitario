package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RespuestaExamenTest {

    @Test
    void equalsYHashCode_DebenDependerDelId() {
        UUID id = UUID.randomUUID();

        RespuestaExamen primera = new RespuestaExamen();
        primera.setIdRespuestaExamen(id);

        RespuestaExamen segunda = new RespuestaExamen();
        segunda.setIdRespuestaExamen(id);

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void equals_ConIdsDistintos_DebeSerFalse() {
        RespuestaExamen primera = new RespuestaExamen();
        primera.setIdRespuestaExamen(UUID.randomUUID());

        RespuestaExamen segunda = new RespuestaExamen();
        segunda.setIdRespuestaExamen(UUID.randomUUID());

        assertNotEquals(primera, segunda);
    }
}