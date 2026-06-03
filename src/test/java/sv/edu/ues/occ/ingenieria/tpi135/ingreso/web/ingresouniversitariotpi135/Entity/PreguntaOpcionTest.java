package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaOpcionTest {

    @Test
    void equalsYHashCode_DebenDependerDelId() {
        UUID id = UUID.randomUUID();

        PreguntaOpcion primera = new PreguntaOpcion();
        primera.setIdPreguntaOpcion(id);

        PreguntaOpcion segunda = new PreguntaOpcion();
        segunda.setIdPreguntaOpcion(id);

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void equals_ConIdsDistintos_DebeSerFalse() {
        PreguntaOpcion primera = new PreguntaOpcion();
        primera.setIdPreguntaOpcion(UUID.randomUUID());

        PreguntaOpcion segunda = new PreguntaOpcion();
        segunda.setIdPreguntaOpcion(UUID.randomUUID());

        assertNotEquals(primera, segunda);
    }
}