package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Proveedor de ObjectMapper para el cliente Jersey usado en pruebas de sistema.
 * Configura Jackson con soporte para tipos Java Time (LocalDate, LocalTime, etc.).
 */
@Provider
public class ProveedorJacksonTiempo implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public ProveedorJacksonTiempo() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
