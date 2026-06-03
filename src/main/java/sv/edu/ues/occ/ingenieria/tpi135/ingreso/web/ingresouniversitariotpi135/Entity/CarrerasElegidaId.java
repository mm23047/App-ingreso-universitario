package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CarrerasElegidaId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID idInscripcion;
    private String idCarrera;

    public CarrerasElegidaId() {}

    public CarrerasElegidaId(UUID idInscripcion, String idCarrera) {
        this.idInscripcion = idInscripcion;
        this.idCarrera = idCarrera;
    }

    public UUID getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(UUID idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public String getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(String idCarrera) {
        this.idCarrera = idCarrera;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrerasElegidaId that = (CarrerasElegidaId) o;
        return Objects.equals(idInscripcion, that.idInscripcion) &&
               Objects.equals(idCarrera, that.idCarrera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInscripcion, idCarrera);
    }
}
