package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
// CORRECCIÓN: Se agrega UniqueConstraint para asegurar la integridad de los nombres de las etapas
@Table(name = "etapa_admision", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_etapa_admision_nombre", columnNames = {"nombre"})
})
@NamedQueries({
        @NamedQuery(
                name = "EtapasAdmision.findByNombre",
                query = "SELECT e FROM EtapasAdmision e WHERE e.nombre = :nombre"
        ),
        @NamedQuery(
                name = "EtapasAdmision.countByNombreNotId",
                query = "SELECT COUNT(e) FROM EtapasAdmision e WHERE e.nombre = :nombre AND e.idEtapaAdmision <> :idEtapa"
        ),
        // NUEVO: Query medular para el motor de evaluación de notas
        @NamedQuery(
                name = "EtapasAdmision.findEtapasAprobadasPorPuntaje",
                query = "SELECT e FROM EtapasAdmision e WHERE :puntajeObtenido >= e.puntajeMinimo AND :puntajeObtenido <= e.puntajeMaximo ORDER BY e.puntajeMinimo ASC"
        )
})
public class EtapasAdmision implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_etapa", nullable = false)
    private UUID idEtapaAdmision;

    @Size(max = 50)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "puntaje_minimo", precision = 5, scale = 2)
    private BigDecimal puntajeMinimo;

    @Column(name = "puntaje_maximo", precision = 5, scale = 2)
    private BigDecimal puntajeMaximo;

    // CORRECCIÓN: Reemplazo de @Lob por columnDefinition TEXT para evitar mapeos OID defectuosos en PostgreSQL
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    public UUID getIdEtapaAdmision() {
        return idEtapaAdmision;
    }

    public void setIdEtapaAdmision(UUID id) {
        this.idEtapaAdmision = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPuntajeMinimo() {
        return puntajeMinimo;
    }

    public void setPuntajeMinimo(BigDecimal puntajeMinimo) {
        this.puntajeMinimo = puntajeMinimo;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public void setPuntajeMaximo(BigDecimal puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @PrePersist
    @PreUpdate
    private void normalizarDatos() {
        if (this.nombre != null) {
            this.nombre = this.nombre.trim();
        }
    }

    // CORRECCIÓN: Implementación segura de equals y hashCode basada en la identidad UUID de JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EtapasAdmision)) return false;
        EtapasAdmision that = (EtapasAdmision) o;
        return idEtapaAdmision != null && idEtapaAdmision.equals(that.getIdEtapaAdmision());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}