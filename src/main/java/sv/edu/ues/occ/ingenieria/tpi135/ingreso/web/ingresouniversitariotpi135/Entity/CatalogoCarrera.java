package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "catalogo_carrera", schema = "public", uniqueConstraints = {
    @UniqueConstraint(name = "uk_catalogo_carrera_nombre", columnNames = "nombre")
})
public class CatalogoCarrera {
    @Id
    @Size(max = 10)
    @Column(name = "id_carrera", nullable = false, length = 10)
    private String idCarrera;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    public String getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(String idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}