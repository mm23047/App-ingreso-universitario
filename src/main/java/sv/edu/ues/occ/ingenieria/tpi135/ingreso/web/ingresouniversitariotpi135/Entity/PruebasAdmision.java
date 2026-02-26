package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "pruebas_admision", schema = "public")
public class PruebasAdmision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_prueba", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_prueba", nullable = false, length = 100)
    private String nombrePrueba;

    @NotNull
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "activa")
    private Boolean activa;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

}