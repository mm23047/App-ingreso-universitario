package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "disponibilidad_aula_turno")
@NamedQueries({
        @NamedQuery(
                name = "DisponibilidadAulaTurno.countByAulaAndTurno",
                query = "SELECT COUNT(d) FROM DisponibilidadAulaTurno d WHERE d.aula.idAula = :idAula AND d.turnoExamen.idTurnoExamen = :idTurno"
        ),
        // NUEVO: Requerimiento de negocio para calcular el aforo global por turno
        @NamedQuery(
                name = "DisponibilidadAulaTurno.findByTurno",
                query = "SELECT d FROM DisponibilidadAulaTurno d WHERE d.turnoExamen.idTurnoExamen = :idTurno"
        )
})
public class DisponibilidadAulaTurno implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DisponibilidadAulaTurnoId idDisponibilidadAulaTurno;

    @MapsId("idAula")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aula", nullable = false)
    private Aula aula;

    @MapsId("idTurno")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno", nullable = false)
    private TurnosExamen turnoExamen;

    public DisponibilidadAulaTurnoId getIdDisponibilidadAulaTurno() {
        return idDisponibilidadAulaTurno;
    }

    public void setIdDisponibilidadAulaTurno(DisponibilidadAulaTurnoId id) {
        this.idDisponibilidadAulaTurno = id;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula idAula) {
        this.aula = idAula;
    }

    public TurnosExamen getTurnoExamen() {
        return turnoExamen;
    }

    public void setTurnoExamen(TurnosExamen idTurno) {
        this.turnoExamen = idTurno;
    }
    @PrePersist
    @PreUpdate
    private void sincronizarIdPersistible() {
        sincronizarId();
    }

    // CORRECCIÓN: Evita el guardado erróneo con llaves nulas en PostgreSQL mapeando los objetos asignados
    private void sincronizarId() {
        if (this.idDisponibilidadAulaTurno == null && this.aula != null && this.turnoExamen != null) {
            DisponibilidadAulaTurnoId compuesto = new DisponibilidadAulaTurnoId();
            compuesto.setIdAula(this.aula.getIdAula());
            compuesto.setIdTurno(this.turnoExamen.getIdTurnoExamen());
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