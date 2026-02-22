package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PreguntasPorClaveId implements Serializable {
    private static final long serialVersionUID = -9086369621147257690L;
    @NotNull
    @Column(name = "id_clave", nullable = false)
    private Integer idClave;

    @NotNull
    @Column(name = "id_pregunta", nullable = false)
    private Integer idPregunta;

    public Integer getIdClave() {
        return idClave;
    }

    public void setIdClave(Integer idClave) {
        this.idClave = idClave;
    }

    public Integer getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(Integer idPregunta) {
        this.idPregunta = idPregunta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreguntasPorClaveId entity = (PreguntasPorClaveId) o;
        return Objects.equals(this.idClave, entity.idClave) &&
                Objects.equals(this.idPregunta, entity.idPregunta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idClave, idPregunta);
    }
}