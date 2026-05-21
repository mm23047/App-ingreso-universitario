package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "examen_realizado", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inscripcion_etapa", columnNames = {"id_inscripcion", "id_etapa"})
})
@NamedQueries({
        @NamedQuery(
                name = "ExamenRealizado.findByAspiranteId",
                query = "SELECT e FROM ExamenRealizado e JOIN e.inscripcionesPrueba i WHERE i.aspiranteDato.id = :aspiranteId"
        ),
        @NamedQuery(
                name = "ExamenRealizado.findByPruebaId",
                query = "SELECT e FROM ExamenRealizado e WHERE e.claveExamen.pruebaAdmision.idPruebaAdmision = :pruebaId"
        ),
        @NamedQuery(
                name = "ExamenRealizado.countPreguntasByClave",
                query = "SELECT COUNT(DISTINCT p.idPreguntaPorClave.idPregunta) FROM PreguntasPorClave p WHERE p.idPreguntaPorClave.idClave = :idClave"
        ),
        @NamedQuery(
                name = "ExamenRealizado.countRespuestasCorrectas",
                query = "SELECT COUNT(DISTINCT r.idPreguntaOpcion.idPregunta.idBancoPregunta) FROM RespuestaExamen r JOIN r.idPreguntaOpcion o WHERE r.idExamen.idExamenRealizado = :idExamen AND o.esCorrecta = TRUE AND o.idPregunta.idBancoPregunta IN (SELECT p2.idPreguntaPorClave.idPregunta FROM PreguntasPorClave p2 WHERE p2.idPreguntaPorClave.idClave = :idClave)"
        ),
        @NamedQuery(
                name = "ExamenRealizado.findRankingByPruebaAndEtapa",
                query = "SELECT e FROM ExamenRealizado e WHERE e.claveExamen.pruebaAdmision.idPruebaAdmision = :idPrueba AND e.etapaAdmision.idEtapaAdmision = :idEtapa AND e.puntajeFinal IS NOT NULL ORDER BY e.puntajeFinal DESC"
        )
})
public class ExamenRealizado implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_examen", nullable = false)
    private UUID idExamenRealizado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba inscripcionesPrueba;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave", nullable = false)
    private ClavesExamen claveExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa", nullable = false)
    private EtapasAdmision etapaAdmision;

    @Column(name = "puntaje_final", precision = 5, scale = 2)
    private BigDecimal puntajeFinal;

    // INCONSISTENCIA DDL: Mapeado para forzar columna timestamptz en PostgreSQL
    @Column(name = "fecha_realizacion", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime fechaRealizacion;

    public UUID getIdExamenRealizado() {
        return idExamenRealizado;
    }

    public void setIdExamenRealizado(UUID id) {
        this.idExamenRealizado = id;
    }

    public InscripcionesPrueba getInscripcionesPrueba() {
        return inscripcionesPrueba;
    }

    public void setInscripcionesPrueba(InscripcionesPrueba idInscripcion) {
        this.inscripcionesPrueba = idInscripcion;
    }

    public ClavesExamen getClaveExamen() {
        return claveExamen;
    }

    public void setClaveExamen(ClavesExamen idClave) {
        this.claveExamen = idClave;
    }

    public EtapasAdmision getEtapaAdmision() {
        return etapaAdmision;
    }

    public void setEtapaAdmision(EtapasAdmision idEtapa) {
        this.etapaAdmision = idEtapa;
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

    @PrePersist
    private void asegurarMetadatosFecha() {
        if (this.fechaRealizacion == null) {
            this.fechaRealizacion = OffsetDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamenRealizado that = (ExamenRealizado) o;
        return idExamenRealizado != null && idExamenRealizado.equals(that.idExamenRealizado);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}