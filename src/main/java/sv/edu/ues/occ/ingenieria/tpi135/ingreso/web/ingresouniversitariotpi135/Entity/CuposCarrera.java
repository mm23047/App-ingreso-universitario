package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "CuposCarrera.findByCarrera",
                query = "SELECT c FROM CuposCarrera c WHERE c.catalogoCarrera.idCarrera = :idCarrera"
        ),
        // NUEVO: Permite buscar la cuota exacta cruzando los tres criterios de selección
        @NamedQuery(
                name = "CuposCarrera.findUniqueCupo",
                query = "SELECT c.cupos FROM CuposCarrera c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba AND c.catalogoCarrera.idCarrera = :idCarrera AND c.etapaAdmision.idEtapaAdmision = :idEtapa"
        )
})
public class CuposCarrera implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CuposCarreraId idCupoCarrera;

    @MapsId("idPrueba")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prueba", nullable = false)
    private PruebasAdmision pruebaAdmision;

    @MapsId("idCarrera")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private CatalogoCarrera catalogoCarrera;

    @MapsId("idEtapa")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa", nullable = false)
    private EtapasAdmision etapaAdmision;

    @NotNull
    @Column(name = "cupos", nullable = false)
    private Integer cupos;

    public CuposCarreraId getIdCupoCarrera() {
        return idCupoCarrera;
    }

    public void setIdCupoCarrera(CuposCarreraId id) {
        this.idCupoCarrera = id;
    }

    public PruebasAdmision getPruebaAdmision() {
        return pruebaAdmision;
    }

    public void setPruebaAdmision(PruebasAdmision idPrueba) {
        this.pruebaAdmision = idPrueba;
    }

    public CatalogoCarrera getCatalogoCarrera() {
        return catalogoCarrera;
    }

    public void setCatalogoCarrera(CatalogoCarrera idCarrera) {
        this.catalogoCarrera = idCarrera;
    }

    public EtapasAdmision getEtapaAdmision() {
        return etapaAdmision;
    }

    public void setEtapaAdmision(EtapasAdmision idEtapa) {
        this.etapaAdmision = idEtapa;
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
        if (this.idCupoCarrera == null && this.pruebaAdmision != null && this.catalogoCarrera != null && this.etapaAdmision != null) {
            CuposCarreraId compuesto = new CuposCarreraId();
            compuesto.setIdPrueba(this.pruebaAdmision.getIdPruebaAdmision());
            compuesto.setIdCarrera(this.catalogoCarrera.getIdCarrera());
            compuesto.setIdEtapa(this.etapaAdmision.getIdEtapaAdmision());
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