package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(name = "carrera_elegida", schema = "public", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_inscripcion", "prioridad"})
})
public class CarrerasElegida {
    @EmbeddedId
    private CarrerasElegidaId id;

    @MapsId("idInscripcion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba idInscripcion;

    @MapsId("idCarrera")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private CatalogoCarrera idCarrera;

    @NotNull
    @Column(name = "prioridad", nullable = false)
    private Short prioridad;

    public CarrerasElegidaId getId() {
        return id;
    }

    public void setId(CarrerasElegidaId id) {
        this.id = id;
        if (id != null) {
            if (this.idInscripcion == null) {
                InscripcionesPrueba inscripcion = new InscripcionesPrueba();
                inscripcion.setId(id.getIdInscripcion());
                this.idInscripcion = inscripcion;
            }
            if (this.idCarrera == null) {
                CatalogoCarrera carrera = new CatalogoCarrera();
                carrera.setIdCarrera(id.getIdCarrera());
                this.idCarrera = carrera;
            }
        }
    }

    public InscripcionesPrueba getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(InscripcionesPrueba idInscripcion) {
        this.idInscripcion = idInscripcion;
        sincronizarId();
    }

    public CatalogoCarrera getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(CatalogoCarrera idCarrera) {
        this.idCarrera = idCarrera;
        sincronizarId();
    }

    public Short getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Short prioridad) {
        this.prioridad = prioridad;
    }

    @PrePersist
    @PreUpdate
    private void sincronizarIdPersistible() {
        sincronizarId();
    }

    private void sincronizarId() {
        if (this.id == null && this.idInscripcion != null && this.idCarrera != null) {
            CarrerasElegidaId compuesto = new CarrerasElegidaId();
            compuesto.setIdInscripcion(this.idInscripcion.getId());
            compuesto.setIdCarrera(this.idCarrera.getIdCarrera());
            this.id = compuesto;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CarrerasElegida that = (CarrerasElegida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}