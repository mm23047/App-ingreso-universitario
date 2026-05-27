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
        // 1. NUEVA: Para leer por ID desde el controlador REST sin que explote
        @NamedQuery(
                name = "AsignacionAulaAspirante.findByIdConRelaciones",
                query = "SELECT a FROM AsignacionAulaAspirante a JOIN FETCH a.inscripcionPrueba JOIN FETCH a.disponibilidad d JOIN FETCH d.aula JOIN FETCH d.turnoExamen WHERE a.idAsignacionAulaAspirante = :id"
        ),
        // 2. MODIFICADA: Se agregó JOIN FETCH para que cargue todo de una vez
        @NamedQuery(
                name = "AsignacionAulaAspirante.findByInscripcion",
                query = "SELECT a FROM AsignacionAulaAspirante a JOIN FETCH a.disponibilidad d JOIN FETCH d.aula JOIN FETCH d.turnoExamen WHERE a.inscripcionPrueba.idInscripcionPrueba = :idInscripcion"
        ),
        @NamedQuery(
                name = "AsignacionAulaAspirante.countByAulaAndTurno",
                query = "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE a.disponibilidad.idDisponibilidadAulaTurno.idAula = :idAula AND a.disponibilidad.idDisponibilidadAulaTurno.idTurno = :idTurno"
        ),
        @NamedQuery(
                name = "AsignacionAulaAspirante.countByInscripcionAndTurno",
                query = "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE a.inscripcionPrueba.idInscripcionPrueba = :idInscripcion AND a.disponibilidad.idDisponibilidadAulaTurno.idTurno = :idTurno"
        ),
        @NamedQuery(
                name = "AsignacionAulaAspirante.findByAulaAndTurno",
                query = "SELECT a FROM AsignacionAulaAspirante a JOIN FETCH a.inscripcionPrueba JOIN FETCH a.disponibilidad d JOIN FETCH d.aula JOIN FETCH d.turnoExamen WHERE d.idDisponibilidadAulaTurno.idAula = :idAula AND d.idDisponibilidadAulaTurno.idTurno = :idTurno"
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
    public Aula getAulaDetalle() {
        return disponibilidad != null ? disponibilidad.getAula() : null;
    }

    @Transient
    public TurnosExamen getTurnoDetalle() {
        return disponibilidad != null ? disponibilidad.getTurnoExamen() : null;
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