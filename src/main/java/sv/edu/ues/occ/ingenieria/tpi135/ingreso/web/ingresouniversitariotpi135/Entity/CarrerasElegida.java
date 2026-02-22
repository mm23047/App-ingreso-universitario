package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "carreras_elegidas")
public class CarrerasElegida {
@jakarta.persistence.EmbeddedId
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId id;

@jakarta.persistence.MapsId("idInscripcion")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_inscripcion", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba idInscripcion;

@jakarta.persistence.MapsId("idCarrera")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_carrera", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera idCarrera;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "prioridad", nullable = false)
private java.lang.Short prioridad;

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId getId() {
  return id;
}public void setId(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba getIdInscripcion() {
  return idInscripcion;
}public void setIdInscripcion(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba idInscripcion) {
  this.idInscripcion = idInscripcion;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera getIdCarrera() {
  return idCarrera;
}public void setIdCarrera(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera idCarrera) {
  this.idCarrera = idCarrera;
}

public java.lang.Short getPrioridad() {
  return prioridad;
}public void setPrioridad(java.lang.Short prioridad) {
  this.prioridad = prioridad;
}

}