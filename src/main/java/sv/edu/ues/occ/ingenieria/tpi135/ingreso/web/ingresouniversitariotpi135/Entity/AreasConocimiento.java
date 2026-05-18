package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "area_conocimiento", schema = "public", uniqueConstraints = {
    @UniqueConstraint(name = "uk_area_conocimiento_nombre_area", columnNames = "nombre_area")
})
public class AreasConocimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_area", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_area", nullable = false, unique = true, length = 100)
    private String nombreArea;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombreArea() {
        return nombreArea;
    }

    public void setNombreArea(String nombreArea) {
        this.nombreArea = nombreArea;
    }

}