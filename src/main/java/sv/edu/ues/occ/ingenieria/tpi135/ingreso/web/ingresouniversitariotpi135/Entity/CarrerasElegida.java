package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "carrera_elegida", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inscripcion_prioridad", columnNames = {"id_inscripcion", "prioridad"})
})
@NamedQueries({
        @NamedQuery(
                name = "CarrerasElegida.countByInscripcionAndPrioridad",
                query = "SELECT COUNT(c) FROM CarrerasElegida c WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND c.prioridad = :prioridad"
        ),
        @NamedQuery(
                name = "CarrerasElegida.countByInscripcionAndPrioridadNotId",
                query = "SELECT COUNT(c) FROM CarrerasElegida c WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND c.prioridad = :prioridad AND c.catalogoCarrera.idCarrera <> :idCarrera"
        ),
        @NamedQuery(
                name = "CarrerasElegida.findByInscripcionAndCarrera",
                query = "SELECT c FROM CarrerasElegida c WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND c.catalogoCarrera.idCarrera = :idCarrera"
        ),
        // NUEVO: Requerimiento de negocio para el algoritmo de asignación de cupos
        @NamedQuery(
                name = "CarrerasElegida.findByInscripcionOrderByPrioridad",
                query = "SELECT c FROM CarrerasElegida c WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion ORDER BY c.prioridad ASC"
        )
})

public class CarrerasElegida implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CarrerasElegidaId idCarreraElegida;

    @MapsId("idInscripcion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba inscripcionesPrueba;

    @MapsId("idCarrera")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private CatalogoCarrera catalogoCarrera;

    @NotNull
    @Column(name = "prioridad", nullable = false)
    private Short prioridad;

    public CarrerasElegidaId getIdCarreraElegida() {
        return idCarreraElegida;
    }

    public void setIdCarreraElegida(CarrerasElegidaId id) {
        this.idCarreraElegida = id;
        if (id != null) {
            if (this.inscripcionesPrueba == null) {
                InscripcionesPrueba inscripcion = new InscripcionesPrueba();
                inscripcion.setIdInscripcionPrueba(id.getIdInscripcion());
                this.inscripcionesPrueba = inscripcion;
            }
            if (this.catalogoCarrera == null) {
                CatalogoCarrera carrera = new CatalogoCarrera();
                carrera.setIdCarrera(id.getIdCarrera());
                this.catalogoCarrera = carrera;
            }
        }
    }

    public InscripcionesPrueba getInscripcionesPrueba() {
        return inscripcionesPrueba;
    }

    public void setInscripcionesPrueba(InscripcionesPrueba idInscripcion) {
        this.inscripcionesPrueba = idInscripcion;
        sincronizarId();
    }

    public CatalogoCarrera getCatalogoCarrera() {
        return catalogoCarrera;
    }

    public void setCatalogoCarrera(CatalogoCarrera idCarrera) {
        this.catalogoCarrera = idCarrera;
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
        if (this.idCarreraElegida == null && this.inscripcionesPrueba != null && this.catalogoCarrera != null) {
            CarrerasElegidaId compuesto = new CarrerasElegidaId();
            compuesto.setIdInscripcion(this.inscripcionesPrueba.getIdInscripcionPrueba());
            compuesto.setIdCarrera(this.catalogoCarrera.getIdCarrera());
            this.idCarreraElegida = compuesto;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrerasElegida that = (CarrerasElegida) o;
        return Objects.equals(idCarreraElegida, that.idCarreraElegida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCarreraElegida);
    }
}