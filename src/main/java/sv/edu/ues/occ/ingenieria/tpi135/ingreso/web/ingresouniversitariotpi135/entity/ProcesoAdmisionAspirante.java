package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "proceso_admision_aspirante")
public class ProcesoAdmisionAspirante {
    @Id
    @Column(name = "id_inscripcion", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba inscripcionesPrueba;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa_actual", nullable = false)
    private EtapasAdmision idEtapaActual;

    @Size(max = 30)
    @NotNull
    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_asignada")
    private CatalogoCarrera carreraAsignada;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public InscripcionesPrueba getInscripcionesPrueba() {
        return inscripcionesPrueba;
    }

    public void setInscripcionesPrueba(InscripcionesPrueba inscripcionesPrueba) {
        this.inscripcionesPrueba = inscripcionesPrueba;
    }

    public EtapasAdmision getIdEtapaActual() {
        return idEtapaActual;
    }

    public void setIdEtapaActual(EtapasAdmision idEtapaActual) {
        this.idEtapaActual = idEtapaActual;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public CatalogoCarrera getCarreraAsignada() {
        return carreraAsignada;
    }

    public void setCarreraAsignada(CatalogoCarrera carreraAsignada) {
        this.carreraAsignada = carreraAsignada;
    }

}