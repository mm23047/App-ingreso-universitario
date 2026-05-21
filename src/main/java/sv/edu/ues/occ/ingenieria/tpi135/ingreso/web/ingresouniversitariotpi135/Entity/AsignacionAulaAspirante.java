package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "asignacion_aula_aspirante", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inscripcion_turno", columnNames = {"id_inscripcion", "id_turno"})
})
@NamedQueries({
        @NamedQuery(
                name = "AsignacionAulaAspirante.countByAulaAndTurno",
                query = "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE a.disponibilidad.idAula.idAula = :idAula AND a.disponibilidad.idTurno.idTurnoExamen = :idTurno"
        ),
        @NamedQuery(
                name = "AsignacionAulaAspirante.countByInscripcionAndTurno",
                query = "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE a.inscripcionPrueba.idInscripcionPrueba = :idInscripcion AND a.disponibilidad.idTurno.idTurnoExamen = :idTurno"
        ),
        @NamedQuery(
                name = "AsignacionAulaAspirante.findByInscripcion",
                query = "SELECT a FROM AsignacionAulaAspirante a WHERE a.inscripcionPrueba.idInscripcionPrueba = :idInscripcion"
        )
})
public class AsignacionAulaAspirante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_asignacion", nullable = false)
    private UUID idAsignacionAulaAspirante;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba inscripcionPrueba;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "id_aula", referencedColumnName = "id_aula", nullable = false),
            @JoinColumn(name = "id_turno", referencedColumnName = "id_turno", nullable = false)
    })
    private DisponibilidadAulaTurno disponibilidad;

    public UUID getIdAsignacionAulaAspirante() {
        return idAsignacionAulaAspirante;
    }

    public void setIdAsignacionAulaAspirante(UUID id) {
        this.idAsignacionAulaAspirante = id;
    }

    public InscripcionesPrueba getInscripcionPrueba() {
        return inscripcionPrueba;
    }

    public void setInscripcionPrueba(InscripcionesPrueba idInscripcion) {
        this.inscripcionPrueba = idInscripcion;
    }

    public DisponibilidadAulaTurno getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(DisponibilidadAulaTurno disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    @Transient
    public Aula getIdAula() {
        return disponibilidad != null ? disponibilidad.getIdAula() : null;
    }

    @Transient
    public TurnosExamen getIdTurno() {
        return disponibilidad != null ? disponibilidad.getIdTurno() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsignacionAulaAspirante)) return false;
        AsignacionAulaAspirante other = (AsignacionAulaAspirante) o;
        // Si el ID existe, comparamos por ID; de lo contrario, evaluamos por estado de persistencia
        return idAsignacionAulaAspirante != null && idAsignacionAulaAspirante.equals(other.getIdAsignacionAulaAspirante());
    }

    @Override
    public int hashCode() {
        // Un hash constante para entidades administradas por JPA garantiza consistencia en ciclos de vida transaccionales
        return getClass().hashCode();
    }
}