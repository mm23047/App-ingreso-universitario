package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

@Entity
// CORRECCIÓN: Se añadió el UniqueConstraint compuesto indispensable para la regla de negocio
@Table(name = "clave_examen", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_clave_examen_prueba_nombre", columnNames = {"id_prueba", "nombre_clave"})
})
@NamedQueries({
        @NamedQuery(
                name = "ClavesExaman.findByPrueba",
                query = "SELECT c FROM ClavesExamen c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba"
        ),
        @NamedQuery(
                name = "ClavesExaman.countByPrueba",
                query = "SELECT COUNT(c) FROM ClavesExamen c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba"
        ),
        @NamedQuery(
                name = "ClavesExaman.countByPruebaAndNombre",
                query = "SELECT COUNT(c) FROM ClavesExamen c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba AND c.nombreClave = :nombreClave"
        ),
        @NamedQuery(
                name = "ClavesExaman.countByPruebaAndNombreNotId",
                query = "SELECT COUNT(c) FROM ClavesExamen c WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba AND c.nombreClave = :nombreClave AND c.idClaveExaman <> :idClave"
        ),
        @NamedQuery(
                name = "ClavesExaman.findByIdWithEtapa",
                query="SELECT c FROM ClavesExamen c JOIN FETCH c.etapaAdmision WHERE c.idClaveExaman = :idClave"
        )
})
public class ClavesExamen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_clave", nullable = false)
    private UUID idClaveExaman;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prueba", nullable = false)
    private PruebasAdmision pruebaAdmision;

    @Size(max = 50)
    @NotNull
    @Column(name = "nombre_clave", nullable = false, length = 50)
    private String nombreClave;

    // NUEVA RELACIÓN: Conecta el examen con la etapa para heredar sus reglas
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa", nullable = false)
    private EtapasAdmision etapaAdmision;

    // Getter y Setter
    public EtapasAdmision getEtapaAdmision() {
        return etapaAdmision;
    }

    public void setEtapaAdmision(EtapasAdmision etapaAdmision) {
        this.etapaAdmision = etapaAdmision;
    }

    public UUID getIdClaveExaman() {
        return idClaveExaman;
    }

    public void setIdClaveExaman(UUID id) {
        this.idClaveExaman = id;
    }

    public PruebasAdmision getPruebaAdmision() {
        return pruebaAdmision;
    }

    public void setPruebaAdmision(PruebasAdmision idPrueba) {
        this.pruebaAdmision = idPrueba;
    }

    public String getNombreClave() {
        return nombreClave;
    }

    public void setNombreClave(String nombreClave) {
        this.nombreClave = nombreClave;
    }

    @PrePersist
    @PreUpdate
    private void normalizarTextos() {
        if (this.nombreClave != null) {
            this.nombreClave = this.nombreClave.trim();
        }
    }

    // CORRECCIÓN: Implementación segura de equals y hashCode para JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClavesExamen)) return false;
        ClavesExamen that = (ClavesExamen) o;
        return idClaveExaman != null && idClaveExaman.equals(that.getIdClaveExaman());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}