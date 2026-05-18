package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "disponibilidad_aula_turno")
public class DisponibilidadAulaTurno {

    @EmbeddedId
    private DisponibilidadAulaTurnoId id;

    @MapsId("idAula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aula", nullable = false)
    private Aula idAula;

    @MapsId("idTurno")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno", nullable = false)
    private TurnosExaman idTurno;

    public DisponibilidadAulaTurnoId getId() {
        return id;
    }

    public void setId(DisponibilidadAulaTurnoId id) {
        this.id = id;
    }

    public Aula getIdAula() {
        return idAula;
    }

    public void setIdAula(Aula idAula) {
        this.idAula = idAula;
    }

    public TurnosExaman getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(TurnosExaman idTurno) {
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
        DisponibilidadAulaTurno that = (DisponibilidadAulaTurno) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}