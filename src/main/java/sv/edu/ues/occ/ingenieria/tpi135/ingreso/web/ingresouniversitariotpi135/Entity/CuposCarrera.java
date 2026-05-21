package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "CuposCarrera.findByCarrera",
                query = "SELECT c FROM CuposCarrera c WHERE c.idCarrera.idCarrera = :idCarrera"
        ),
        // NUEVO: Permite buscar la cuota exacta cruzando los tres criterios de selección
        @NamedQuery(
                name = "CuposCarrera.findUniqueCupo",
                query = "SELECT c.cupos FROM CuposCarrera c WHERE c.idPrueba.idPruebaAdmision = :idPrueba AND c.idCarrera.idCarrera = :idCarrera AND c.idEtapa.idEtapaAdmision = :idEtapa"
        )
})
public class CuposCarrera implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CuposCarreraId idCupoCarrera;

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

    public CuposCarreraId getIdCupoCarrera() {
        return idCupoCarrera;
    }

    public void setIdCupoCarrera(CuposCarreraId id) {
        this.idCupoCarrera = id;
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

    @PrePersist
    @PreUpdate
    private void sincronizarIdPersistible() {
        sincronizarId();
    }

    // CORRECCIÓN: Garantiza que la llave compuesta interna esté en perfecta sincronía con los objetos asignados
    private void sincronizarId() {
        if (this.idCupoCarrera == null && this.idPrueba != null && this.idCarrera != null && this.idEtapa != null) {
            CuposCarreraId compuesto = new CuposCarreraId();
            compuesto.setIdPrueba(this.idPrueba.getIdPruebaAdmision());
            compuesto.setIdCarrera(this.idCarrera.getIdCarrera());
            compuesto.setIdEtapa(this.idEtapa.getIdEtapaAdmision());
            this.idCupoCarrera = compuesto;
        }
    }

    // CORRECCIÓN: Implementación del contrato de igualdad delegando en el @EmbeddedId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuposCarrera that = (CuposCarrera) o;
        return Objects.equals(idCupoCarrera, that.idCupoCarrera);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCupoCarrera);
    }

}