package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "area_conocimiento", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_area_conocimiento_nombre_area", columnNames = "nombre_area")
})
// Definición de las consultas centralizadas en la entidad
@NamedQueries({
        @NamedQuery(
                name = "AreasConocimiento.countByNombre",
                query = "SELECT COUNT(a) FROM AreasConocimiento a WHERE LOWER(a.nombreArea) = LOWER(:nombreArea)"
        ),
        @NamedQuery(
                name = "AreasConocimiento.findByNombreLike",
                query = "SELECT a FROM AreasConocimiento a WHERE LOWER(a.nombreArea) LIKE LOWER(:patron) ORDER BY a.nombreArea ASC"
        ),
        // NUEVO: Para validar al actualizar (ignora el ID del área que se está modificando)
        @NamedQuery(
                name = "AreasConocimiento.countByNombreAndNotId",
                query = "SELECT COUNT(a) FROM AreasConocimiento a WHERE LOWER(a.nombreArea) = LOWER(:nombreArea) AND a.idAreaConocimiento <> :idArea"
        ),
        // NUEVO: Busca áreas que tengan al menos un tema que tenga al menos una pregunta.
        @NamedQuery(
                name = "AreasConocimiento.findConPreguntas",
                query = "SELECT a FROM AreasConocimiento a WHERE EXISTS (" +
                        "  SELECT t FROM Tema t WHERE t.areaConocimiento = a AND EXISTS (" +
                        "    SELECT bp FROM BancoPregunta bp WHERE bp.tema = t" +
                        "  )" +
                        ") ORDER BY a.nombreArea ASC"
        ),
        @NamedQuery(
                name = "AreasConocimiento.countDependencias",
                query = "SELECT COUNT(t) FROM Tema t WHERE t.areaConocimiento.idAreaConocimiento = :idArea"
        )
})
public class AreasConocimiento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_area", nullable = false)
    private UUID idAreaConocimiento;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_area", nullable = false, unique = true, length = 100)
    private String nombreArea;

    public UUID getIdAreaConocimiento() {
        return idAreaConocimiento;
    }

    public void setIdAreaConocimiento(UUID id) {
        this.idAreaConocimiento = id;
    }

    public String getNombreArea() {
        return nombreArea;
    }

    public void setNombreArea(String nombreArea) {
        this.nombreArea = nombreArea;
    }

    // Eliminar espacios no deseados
    @PrePersist
    @PreUpdate
    private void normalizarDatos() {
        if (this.nombreArea != null) {
            // Elimina espacios al inicio y final, y convierte múltiples espacios internos en uno solo
            this.nombreArea = this.nombreArea.trim().replaceAll("\\s+", " ");
        }
    }
}