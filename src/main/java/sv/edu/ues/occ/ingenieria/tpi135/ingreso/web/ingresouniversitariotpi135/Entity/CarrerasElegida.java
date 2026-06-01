package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "carrera_elegida", schema = "public", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_inscripcion", "prioridad"})
})
@NamedQueries({
        @NamedQuery(
                name = "CarrerasElegida.countByInscripcionAndPrioridad",
                query = "SELECT COUNT(c) FROM CarrerasElegida c WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND c.prioridad = :prioridad"
        ),
        @NamedQuery(
                name = "CarrerasElegida.findByInscripcionOrderByPrioridad",
                query = "SELECT c FROM CarrerasElegida c JOIN FETCH c.inscripcionesPrueba JOIN FETCH c.catalogoCarrera WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion ORDER BY c.prioridad ASC"
        ),
        @NamedQuery(
                name = "CarrerasElegida.findByInscripcionAndCarrera",
                query = "SELECT c FROM CarrerasElegida c JOIN FETCH c.inscripcionesPrueba JOIN FETCH c.catalogoCarrera WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND c.catalogoCarrera.idCarrera = :idCarrera"
        ),
        @NamedQuery(
                name = "CarrerasElegida.findByInscripcionAndPrioridadLevel",
                query = "SELECT c FROM CarrerasElegida c JOIN FETCH c.inscripcionesPrueba JOIN FETCH c.catalogoCarrera WHERE c.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion AND c.prioridad = :prioridad"
        )
})
public class CarrerasElegida {

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

    public void setIdCarreraElegida(CarrerasElegidaId idCarreraElegida) {
        this.idCarreraElegida = idCarreraElegida;
    }

    public InscripcionesPrueba getInscripcionesPrueba() {
        return inscripcionesPrueba;
    }

    public void setInscripcionesPrueba(InscripcionesPrueba inscripcionesPrueba) {
        this.inscripcionesPrueba = inscripcionesPrueba;
        sincronizarId();
    }

    public CatalogoCarrera getCatalogoCarrera() {
        return catalogoCarrera;
    }

    public void setCatalogoCarrera(CatalogoCarrera catalogoCarrera) {
        this.catalogoCarrera = catalogoCarrera;
        sincronizarId();
    }

    public Short getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Short prioridad) {
        this.prioridad = prioridad;
    }

    private void sincronizarId() {
        if (this.inscripcionesPrueba != null && this.catalogoCarrera != null) {
            UUID uuid = this.inscripcionesPrueba.getIdInscripcionPrueba();
            String idCarrera = this.catalogoCarrera.getIdCarrera();
            if (uuid != null && idCarrera != null) {
                if (this.idCarreraElegida == null) {
                    this.idCarreraElegida = new CarrerasElegidaId();
                }
                this.idCarreraElegida.setIdInscripcion(uuid);
                this.idCarreraElegida.setIdCarrera(idCarrera);
            }
        }
    }

    @PrePersist
    @PreUpdate
    private void sincronizarIdPersist() {
        if (this.idCarreraElegida == null
                && this.inscripcionesPrueba != null
                && this.catalogoCarrera != null) {
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
