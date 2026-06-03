package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "cupos_carrera", schema = "public")
@NamedQueries({
        // 1. NUEVA CONSULTA: Para buscar por ID y traer todo de una vez
        @NamedQuery(
                name = "CuposCarrera.findByIdConRelaciones",
                query = "SELECT c FROM CuposCarrera c JOIN FETCH c.pruebaAdmision JOIN FETCH c.catalogoCarrera JOIN FETCH c.etapaAdmision WHERE c.idCupoCarrera = :idCupo"
        ),
        // 2. ACTUALIZADA: Le agregamos los JOIN FETCH para que no falle al enviarse por REST
        @NamedQuery(
                name = "CuposCarrera.findByCarrera",
                query = "SELECT c FROM CuposCarrera c JOIN FETCH c.pruebaAdmision JOIN FETCH c.catalogoCarrera JOIN FETCH c.etapaAdmision WHERE c.catalogoCarrera.idCarrera = :idCarrera"
        ),
        @NamedQuery(
                name = "CuposCarrera.findUniqueCupo",
                query = "SELECT c.cupos FROM CuposCarrera c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba AND c.catalogoCarrera.idCarrera = :idCarrera AND c.etapaAdmision.idEtapaAdmision = :idEtapa"
        ),
        // NUEVO: Movimos el JPQL del DAO a la entidad.
        // NOTA: Incrementamos c.version = c.version + 1 para no romper el bloqueo optimista de JPA
        @NamedQuery(
                name = "CuposCarrera.decrementarCupoAtomico",
                query = "UPDATE CuposCarrera c SET c.cupos = c.cupos - 1, c.version = c.version + 1 " +
                        "WHERE c.idCupoCarrera.idPrueba = :idPrueba " +
                        "AND c.idCupoCarrera.idCarrera = :idCarrera " +
                        "AND c.idCupoCarrera.idEtapa = :idEtapa " +
                        "AND c.cupos > 0"
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

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

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