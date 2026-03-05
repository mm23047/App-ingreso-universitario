package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CuposCarreraId implements Serializable {
    private static final long serialVersionUID = 2241823185805802734L;
    @NotNull
    @Column(name = "id_prueba", nullable = false)
    private UUID idPrueba;

    @Size(max = 10)
    @NotNull
    @Column(name = "id_carrera", nullable = false, length = 10)
    private String idCarrera;

    @NotNull
    @Column(name = "id_etapa", nullable = false)
    private UUID idEtapa;

    public UUID getIdPrueba() {
        return idPrueba;
    }

    public void setIdPrueba(UUID idPrueba) {
        this.idPrueba = idPrueba;
    }

    public String getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(String idCarrera) {
        this.idCarrera = idCarrera;
    }

    public UUID getIdEtapa() {
        return idEtapa;
    }

    public void setIdEtapa(UUID idEtapa) {
        this.idEtapa = idEtapa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuposCarreraId entity = (CuposCarreraId) o;
        return Objects.equals(this.idPrueba, entity.idPrueba) &&
                Objects.equals(this.idCarrera, entity.idCarrera) &&
                Objects.equals(this.idEtapa, entity.idEtapa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrueba, idCarrera, idEtapa);
    }
}