package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "aula", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo_aula_api"})
})
public class Aula {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aula", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo_aula_api", nullable = false, unique = true, length = 50)
    private String codigoAulaApi;

    @NotNull
    @Column(name = "capacidad_fisica", nullable = false)
    private Integer capacidadFisica;

    @Column(name = "accesible_silla_ruedas")
    private Boolean accesibleSillaRuedas = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Aula aula = (Aula) o;
        return Objects.equals(id, aula.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}