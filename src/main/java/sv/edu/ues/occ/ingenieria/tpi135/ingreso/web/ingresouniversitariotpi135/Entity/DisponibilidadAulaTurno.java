package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "disponibilidad_aula_turno")
@NamedQueries({
        @NamedQuery(
                name = "DisponibilidadAulaTurno.countByAulaAndTurno",
                query = "SELECT COUNT(d) FROM DisponibilidadAulaTurno d WHERE d.idAula.idAula = :idAula AND d.idTurno.idTurnoExamen = :idTurno"
        ),
        // NUEVO: Requerimiento de negocio para calcular el aforo global por turno
        @NamedQuery(
                name = "DisponibilidadAulaTurno.findByTurno",
                query = "SELECT d FROM DisponibilidadAulaTurno d WHERE d.idTurno.idTurnoExamen = :idTurno"
        )
})
public class DisponibilidadAulaTurno implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DisponibilidadAulaTurnoId idDisponibilidadAulaTurno;

    @MapsId("idAula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aula", nullable = false)
    private Aula idAula;

    @MapsId("idTurno")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno", nullable = false)
    private TurnosExamen idTurno;

    public DisponibilidadAulaTurnoId getIdDisponibilidadAulaTurno() {
        return idDisponibilidadAulaTurno;
    }

    public void setIdDisponibilidadAulaTurno(DisponibilidadAulaTurnoId id) {
        this.idDisponibilidadAulaTurno = id;
    }

    public Aula getIdAula() {
        return idAula;
    }

    public void setIdAula(Aula idAula) {
        this.idAula = idAula;
    }

    public TurnosExamen getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(TurnosExamen idTurno) {
        this.idTurno = idTurno;
    }
    @PrePersist
    @PreUpdate
    private void sincronizarIdPersistible() {
        sincronizarId();
    }

    // CORRECCIÓN: Evita el guardado erróneo con llaves nulas en PostgreSQL mapeando los objetos asignados
    private void sincronizarId() {
        if (this.idDisponibilidadAulaTurno == null && this.idAula != null && this.idTurno != null) {
            DisponibilidadAulaTurnoId compuesto = new DisponibilidadAulaTurnoId();
            compuesto.setIdAula(this.idAula.getIdAula());
            compuesto.setIdTurno(this.idTurno.getIdTurnoExamen());
            this.idDisponibilidadAulaTurno = compuesto;
        }
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
        return Objects.equals(idDisponibilidadAulaTurno, that.idDisponibilidadAulaTurno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDisponibilidadAulaTurno);
    }
}