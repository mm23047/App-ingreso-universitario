package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "aula", uniqueConstraints = {
        @UniqueConstraint(name = "aula_codigo_aula_api_key", columnNames = {"codigo_aula_api"})
})
@NamedQueries({
        @NamedQuery(
                name = "Aula.findByCodigoAulaApi",
                query = "SELECT a FROM Aula a WHERE a.codigoAulaApi = :codigoAulaApi"
        ),
        // NUEVO: Validación de duplicados para actualización
        @NamedQuery(
                name = "Aula.countByCodigoAndNotId",
                query = "SELECT COUNT(a) FROM Aula a WHERE a.codigoAulaApi = :codigoAulaApi AND a.idAula <> :idAula"
        ),
        // NUEVO: Requerimiento de negocio para infraestructura accesible
        @NamedQuery(
                name = "Aula.findAccesibles",
                query = "SELECT a FROM Aula a WHERE a.accesibleSillaRuedas = TRUE ORDER BY a.capacidadFisica DESC"
        ),
        // NUEVO: Requerimiento de negocio para control de cupos
        @NamedQuery(
                name = "Aula.findByCapacidadMinima",
                query = "SELECT a FROM Aula a WHERE a.capacidadFisica >= :capacidadMinima ORDER BY a.capacidadFisica ASC"
        )
})
public class Aula implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aula", nullable = false)
    private UUID idAula;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo_aula_api", nullable = false, unique = true, length = 50)
    private String codigoAulaApi;

    @NotNull
    @Column(name = "capacidad_fisica", nullable = false)
    private Integer capacidadFisica;

    // CORRECCIÓN: Agregado nullable = false para alinear el comportamiento con PostgreSQL
    @NotNull
    @Column(name = "accesible_silla_ruedas", nullable = false)
    private Boolean accesibleSillaRuedas = false;
    public UUID getIdAula() {
        return idAula;
    }

    public void setIdAula(UUID id) {
        this.idAula = id;
    }

    public String getCodigoAulaApi() {
        return codigoAulaApi;
    }

    public void setCodigoAulaApi(String codigoAulaApi) {
        this.codigoAulaApi = codigoAulaApi;
    }

    public Integer getCapacidadFisica() {
        return capacidadFisica;
    }

    public void setCapacidadFisica(Integer capacidadFisica) {
        this.capacidadFisica = capacidadFisica;
    }

    public Boolean getAccesibleSillaRuedas() {
        return accesibleSillaRuedas;
    }

    public void setAccesibleSillaRuedas(Boolean accesibleSillaRuedas) {
        this.accesibleSillaRuedas = accesibleSillaRuedas;
    }

    @PrePersist
    @PreUpdate
    private void limpiarDatos() {
        if (this.codigoAulaApi != null) {
            this.codigoAulaApi = this.codigoAulaApi.trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aula)) return false;
        Aula aula = (Aula) o;
        return idAula != null && idAula.equals(aula.getIdAula());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}