package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cupos_carrera", schema = "public")
public class CuposCarrera {
    @EmbeddedId
    private CuposCarreraId id;

    @MapsId("idPrueba")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prueba", nullable = false)
    private PruebasAdmision idPrueba;

    @MapsId("idCarrera")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private CatalogoCarrera idCarrera;

    @MapsId("idEtapa")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa", nullable = false)
    private EtapasAdmision idEtapa;

    @NotNull
    @Column(name = "cupos", nullable = false)
    private Integer cupos;

    public CuposCarreraId getId() {
        return id;
    }

    public void setId(CuposCarreraId id) {
        this.id = id;
    }

    public PruebasAdmision getIdPrueba() {
        return idPrueba;
    }

    public void setIdPrueba(PruebasAdmision idPrueba) {
        this.idPrueba = idPrueba;
    }

    public CatalogoCarrera getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(CatalogoCarrera idCarrera) {
        this.idCarrera = idCarrera;
    }

    public EtapasAdmision getIdEtapa() {
        return idEtapa;
    }

    public void setIdEtapa(EtapasAdmision idEtapa) {
        this.idEtapa = idEtapa;
    }

    public Integer getCupos() {
        return cupos;
    }

    public void setCupos(Integer cupos) {
        this.cupos = cupos;
    }

}