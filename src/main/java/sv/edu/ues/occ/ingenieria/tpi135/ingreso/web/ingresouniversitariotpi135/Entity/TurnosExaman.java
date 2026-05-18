package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import java.util.UUID;

@Entity
@Table(name = "turno_examen", schema = "public")
public class TurnosExaman {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_turno", nullable = false)
    private UUID id;

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

    @PrePersist
    @PreUpdate
    private void validarHorarioPersistible() {
        validarHorario();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
            throw new IllegalArgumentException("horaInicio must be before horaFin");
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
        TurnosExaman that = (TurnosExaman) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}