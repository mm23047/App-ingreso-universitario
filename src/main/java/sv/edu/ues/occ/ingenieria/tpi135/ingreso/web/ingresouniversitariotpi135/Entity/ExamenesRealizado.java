package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "examenes_realizados", schema = "public")
public class ExamenesRealizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private AsignacionesAulaPupitre idAsignacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave", nullable = false)
    private ClavesExaman idClave;

    @Column(name = "puntaje_final", precision = 5, scale = 2)
    private BigDecimal puntajeFinal;

    @Column(name = "fecha_realizacion")
    private OffsetDateTime fechaRealizacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa", nullable = false)
    private EtapasAdmision idEtapa;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AsignacionesAulaPupitre getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(AsignacionesAulaPupitre idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public ClavesExaman getIdClave() {
        return idClave;
    }

    public void setIdClave(ClavesExaman idClave) {
        this.idClave = idClave;
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

    public EtapasAdmision getIdEtapa() {
        return idEtapa;
    }

    public void setIdEtapa(EtapasAdmision idEtapa) {
        this.idEtapa = idEtapa;
    }

}