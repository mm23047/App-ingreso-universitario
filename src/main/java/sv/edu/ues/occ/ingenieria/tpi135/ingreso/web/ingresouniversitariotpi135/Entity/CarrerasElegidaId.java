package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CarrerasElegidaId implements Serializable {
    private static final long serialVersionUID = -6297830737150961833L;
    @NotNull
    @Column(name = "id_inscripcion", nullable = false)
    private Integer idInscripcion;

    @Size(max = 10)
    @NotNull
    @Column(name = "id_carrera", nullable = false, length = 10)
    private String idCarrera;

    public Integer getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(Integer idInscripcion) {
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
        CarrerasElegidaId entity = (CarrerasElegidaId) o;
        return Objects.equals(this.idInscripcion, entity.idInscripcion) &&
                Objects.equals(this.idCarrera, entity.idCarrera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInscripcion, idCarrera);
    }
}