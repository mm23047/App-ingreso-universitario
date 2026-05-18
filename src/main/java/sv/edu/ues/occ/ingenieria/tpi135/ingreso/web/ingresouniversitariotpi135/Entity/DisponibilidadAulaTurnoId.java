package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DisponibilidadAulaTurnoId implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(name = "id_aula", nullable = false)
    private UUID idAula;

    @NotNull
    @Column(name = "id_turno", nullable = false)
    private UUID idTurno;

    public UUID getIdAula() {
        return idAula;
    }

    public void setIdAula(UUID idAula) {
        this.idAula = idAula;
    }

    public UUID getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(UUID idTurno) {
        this.idTurno = idTurno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisponibilidadAulaTurnoId that = (DisponibilidadAulaTurnoId) o;
        return Objects.equals(idAula, that.idAula) && Objects.equals(idTurno, that.idTurno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAula, idTurno);
    }
}