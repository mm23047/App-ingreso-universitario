package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "turno_examen", schema = "public")
@NamedQueries({
        @NamedQuery(
                name = "TurnosExamen.findByPrueba",
                query = "SELECT t FROM TurnosExamen t WHERE t.idPrueba.idPruebaAdmision = :idPrueba ORDER BY t.fecha, t.horaInicio"
        ),
        @NamedQuery(
                name = "TurnosExamen.findByFecha",
                query = "SELECT t FROM TurnosExamen t WHERE t.fecha = :fecha ORDER BY t.horaInicio"
        ),
        // NUEVA CONSULTA: Detecta traslapes de tiempo matemáticamente
        @NamedQuery(
                name = "TurnosExamen.countTraslapes",
                query = "SELECT COUNT(t) FROM TurnosExamen t WHERE t.idPrueba.idPruebaAdmision = :idPrueba AND t.fecha = :fecha AND (t.horaInicio < :horaFin AND t.horaFin > :horaInicio) AND (:idIgnorado IS NULL OR t.idTurnoExamen <> :idIgnorado)"
        ),
        @NamedQuery(
                name = "TurnosExamen.findTurnoActivoAspirante",
                query = "SELECT t FROM TurnosExamen t WHERE t.idTurnoExamen IN (" +
                        "  SELECT a.disponibilidad.idTurno.idTurnoExamen " +
                        "  FROM AsignacionAulaAspirante a " +
                        "  WHERE a.inscripcionPrueba.idAspirante.id = :idAspirante" +
                        ") " +
                        "AND t.fecha = :fechaActual " +
                        "AND t.horaInicio <= :horaActual " +
                        "AND t.horaFin >= :horaActual"
        )
})
public class TurnosExamen implements Serializable { // CORRECCIÓN: Nombre y Serializable

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_turno", nullable = false)
    private UUID idTurnoExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prueba", nullable = false)
    private PruebasAdmision idPrueba;

    @Size(max = 50)
    @NotNull
    @Column(name = "nombre_turno", nullable = false, length = 50)
    private String nombreTurno;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    public TurnosExamen() {
    }

    @PrePersist
    @PreUpdate
    private void validarHorarioPersistible() {
        validarHorario();
    }

    public UUID getIdTurnoExamen() {
        return idTurnoExamen;
    }

    public void setIdTurnoExamen(UUID id) {
        this.idTurnoExamen = id;
    }

    public PruebasAdmision getIdPrueba() {
        return idPrueba;
    }

    public void setIdPrueba(PruebasAdmision idPrueba) {
        this.idPrueba = idPrueba;
    }

    public String getNombreTurno() {
        return nombreTurno;
    }

    public void setNombreTurno(String nombreTurno) {
        this.nombreTurno = nombreTurno;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public void validarHorario() {
        if (horaInicio != null && horaFin != null && !horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser estrictamente anterior a la hora de fin.");
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
        TurnosExamen that = (TurnosExamen) o;
        return Objects.equals(idTurnoExamen, that.idTurnoExamen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTurnoExamen);
    }
}