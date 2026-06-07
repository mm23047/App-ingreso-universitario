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
                name = "ExamenRealizado.findByIdConRelaciones",
                query = "SELECT e FROM ExamenRealizado e JOIN FETCH e.inscripcionesPrueba JOIN FETCH e.claveExamen JOIN FETCH e.etapaAdmision WHERE e.idExamenRealizado = :id"
        ),
        @NamedQuery(
                name = "ExamenRealizado.findByAspiranteId",
                query = "SELECT e FROM ExamenRealizado e JOIN FETCH e.inscripcionesPrueba ip JOIN FETCH ip.aspiranteDato JOIN FETCH e.claveExamen ce JOIN FETCH ce.pruebaAdmision JOIN FETCH e.etapaAdmision WHERE ip.aspiranteDato.id = :aspiranteId ORDER BY e.fechaRealizacion DESC"
        ),
        @NamedQuery(
                name = "ExamenRealizado.findByPruebaId",
                query = "SELECT e FROM ExamenRealizado e JOIN FETCH e.inscripcionesPrueba JOIN FETCH e.claveExamen JOIN FETCH e.etapaAdmision WHERE e.claveExamen.pruebaAdmision.idPruebaAdmision = :pruebaId"
        ),
        @NamedQuery(
                name = "ExamenRealizado.countPreguntasByClave",
                query = "SELECT COUNT(DISTINCT p.idPreguntaPorClave.idPregunta) FROM PreguntasPorClave p WHERE p.idPreguntaPorClave.idClave = :idClave"
        ),
        @NamedQuery(
                name = "ExamenRealizado.countRespuestasCorrectas",
                query = "SELECT COUNT(DISTINCT r.preguntaOpcion.bancoPregunta.idBancoPregunta) FROM RespuestaExamen r JOIN r.preguntaOpcion o WHERE r.examenRealizado.idExamenRealizado = :idExamen AND o.esCorrecta = TRUE AND o.bancoPregunta.idBancoPregunta IN (SELECT p2.idPreguntaPorClave.idPregunta FROM PreguntasPorClave p2 WHERE p2.idPreguntaPorClave.idClave = :idClave)"
        ),
        @NamedQuery(
                name = "ExamenRealizado.findRankingByPruebaAndEtapa",
                query = "SELECT e FROM ExamenRealizado e JOIN FETCH e.inscripcionesPrueba JOIN FETCH e.claveExamen JOIN FETCH e.etapaAdmision WHERE e.claveExamen.pruebaAdmision.idPruebaAdmision = :idPrueba AND e.etapaAdmision.idEtapaAdmision = :idEtapa AND e.puntajeFinal IS NOT NULL ORDER BY e.puntajeFinal DESC"
        ),
        @NamedQuery(
                name = "ExamenRealizado.countByClave",
                query = "SELECT COUNT(e) FROM ExamenRealizado e WHERE e.claveExamen.idClaveExaman = :idClave"
        ),
        // NUEVA: Para evitar violar el constraint único de inscripción y etapa
        @NamedQuery(
                name = "ExamenRealizado.findByInscripcionAndEtapa",
                query = "SELECT e FROM ExamenRealizado e WHERE e.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND e.etapaAdmision.idEtapaAdmision = :idEtapa"
        ),
        // NUEVA: Mueve el query dinámico que estaba suelto en el método iniciarExamen
        @NamedQuery(
                name = "ExamenRealizado.findClavesByPrueba",
                query = "SELECT c FROM ClavesExamen c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba"
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

    @Transient // No se guarda en BD, se calcula en memoria
    public boolean isFinalizado() {
        return this.puntajeFinal != null;
    }
}