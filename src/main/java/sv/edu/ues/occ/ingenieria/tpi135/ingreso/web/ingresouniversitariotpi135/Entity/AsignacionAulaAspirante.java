package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "asignacion_aula_aspirante", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_inscripcion", "id_turno"})
})
public class AsignacionAulaAspirante {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_asignacion", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba idInscripcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "id_aula", referencedColumnName = "id_aula", nullable = false),
            @JoinColumn(name = "id_turno", referencedColumnName = "id_turno", nullable = false)
    })
    private DisponibilidadAulaTurno disponibilidad;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InscripcionesPrueba getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(InscripcionesPrueba idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public DisponibilidadAulaTurno getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(DisponibilidadAulaTurno disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    @jakarta.persistence.Transient
    public Aula getIdAula() {
        return disponibilidad != null ? disponibilidad.getIdAula() : null;
    }

    @jakarta.persistence.Transient
    public TurnosExaman getIdTurno() {
        return disponibilidad != null ? disponibilidad.getIdTurno() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AsignacionAulaAspirante that = (AsignacionAulaAspirante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}