package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "carreras_elegidas")
public class CarrerasElegida {
    @EmbeddedId
    private CarrerasElegidaId id;

    @MapsId("idInscripcion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba idInscripcion;

    @MapsId("idCarrera")
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
    }

    public InscripcionesPrueba getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(InscripcionesPrueba idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public CatalogoCarrera getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(CatalogoCarrera idCarrera) {
        this.idCarrera = idCarrera;
    }

    public Short getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Short prioridad) {
        this.prioridad = prioridad;
    }

}