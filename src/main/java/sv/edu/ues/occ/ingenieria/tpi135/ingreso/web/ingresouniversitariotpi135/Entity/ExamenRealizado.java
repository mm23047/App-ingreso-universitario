package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "examen_realizado", schema = "public", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_inscripcion", "id_etapa"})
})
@NamedQueries({
        @NamedQuery(
                name = "ExamenRealizado.findByAspiranteId",
                query = "SELECT e FROM ExamenRealizado e " +
                        "JOIN e.idInscripcion i " +
                        "WHERE i.idAspirante.id = :aspiranteId"
        ),
        @NamedQuery(
                name = "ExamenRealizado.findByPruebaId",
                query = "SELECT e FROM ExamenRealizado e " +
                        "WHERE e.idClave.idPrueba.id = :pruebaId"
        )
})
public class ExamenRealizado {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_examen", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba idInscripcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave", nullable = false)
    private ClavesExaman idClave;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa", nullable = false)
    private EtapasAdmision idEtapa;

    @Column(name = "puntaje_final", precision = 5, scale = 2)
    private BigDecimal puntajeFinal;

    @Column(name = "fecha_realizacion")
    private OffsetDateTime fechaRealizacion;

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

    public ClavesExaman getIdClave() {
        return idClave;
    }

    public void setIdClave(ClavesExaman idClave) {
        this.idClave = idClave;
    }

    public EtapasAdmision getIdEtapa() {
        return idEtapa;
    }

    public void setIdEtapa(EtapasAdmision idEtapa) {
        this.idEtapa = idEtapa;
    }

    public BigDecimal getPuntajeFinal() {
        return puntajeFinal;
    }

    public void setPuntajeFinal(BigDecimal puntajeFinal) {
        this.puntajeFinal = puntajeFinal;
    }

    public OffsetDateTime getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(OffsetDateTime fechaRealizacion) {
        this.fechaRealizacion = fechaRealizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExamenRealizado that = (ExamenRealizado) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
