package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "catalogo_carrera", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "catalogo_carrera_nombre_key", columnNames = {"nombre"})
})
@NamedQueries({
        @NamedQuery(
                name = "CatalogoCarrera.findByNombre",
                query = "SELECT c FROM CatalogoCarrera c WHERE c.nombreCatalogoCarrera = :nombre"
        )
})
public class CatalogoCarrera {
    @Id
    @Size(max = 10)
    @Column(name = "id_carrera", nullable = false, length = 10)
    private String idCarrera;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombreCatalogoCarrera;

    public String getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(String idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombreCatalogoCarrera() {
        return nombreCatalogoCarrera;
    }

    public void setNombreCatalogoCarrera(String nombre) {
        this.nombreCatalogoCarrera = nombre;
    }

}