package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PreguntaOpcionTest {

    @Test
    void equalsYHashCode_DebenDependerDelId() {
        UUID id = UUID.randomUUID();

        PreguntaOpcion primera = new PreguntaOpcion();
        primera.setId(id);

        PreguntaOpcion segunda = new PreguntaOpcion();
        segunda.setId(id);

        assertEquals(primera, segunda);
        assertEquals(primera.hashCode(), segunda.hashCode());
    }

    @Test
    void equals_ConIdsDistintos_DebeSerFalse() {
        PreguntaOpcion primera = new PreguntaOpcion();
        primera.setId(UUID.randomUUID());

        PreguntaOpcion segunda = new PreguntaOpcion();
        segunda.setId(UUID.randomUUID());

        assertNotEquals(primera, segunda);
    }
}