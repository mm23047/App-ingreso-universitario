package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "prueba_admision", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_nombre_anio", columnNames = {"nombre_prueba", "anio"})
})
@NamedQueries({
        @NamedQuery(
                name = "PruebasAdmision.findActivas",
                query = "SELECT p FROM PruebasAdmision p WHERE p.activa = TRUE"
        ),
        @NamedQuery(
                name = "PruebasAdmision.findByNombreAndAnio",
                query = "SELECT p FROM PruebasAdmision p WHERE p.nombrePrueba = :nombre AND p.anio = :anio"
        ),
        // NUEVO: Consulta para apagar masivamente cualquier prueba que no sea la recién activada
        @NamedQuery(
                name = "PruebasAdmision.desactivarOtras",
                query = "UPDATE PruebasAdmision p SET p.activa = FALSE WHERE p.idPruebaAdmision <> :idExcluido"
        )
})
public class PruebasAdmision implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_prueba", nullable = false)
    private UUID idPruebaAdmision;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_prueba", nullable = false, length = 100)
    private String nombrePrueba;

    @NotNull
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "activa")
    private Boolean activa;

    public PruebasAdmision() {
    }

    public UUID getIdPruebaAdmision() {
        return idPruebaAdmision;
    }

    public void setIdPruebaAdmision(UUID id) {
        this.idPruebaAdmision = id;
    }

    public String getNombrePrueba() {
        return nombrePrueba;
    }

    public void setNombrePrueba(String nombrePrueba) {
        this.nombrePrueba = nombrePrueba;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    // CORRECCIÓN: Garantiza que nunca quede nulo en la BD al crearse
    @PrePersist
    private void prePersist() {
        if (this.activa == null) {
            this.activa = false;
        }
    }

    // CORRECCIÓN: equals y hashCode basados en la llave primaria
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PruebasAdmision)) return false;
        PruebasAdmision that = (PruebasAdmision) o;
        return idPruebaAdmision != null && idPruebaAdmision.equals(that.getIdPruebaAdmision());
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPruebaAdmision);
    }
}