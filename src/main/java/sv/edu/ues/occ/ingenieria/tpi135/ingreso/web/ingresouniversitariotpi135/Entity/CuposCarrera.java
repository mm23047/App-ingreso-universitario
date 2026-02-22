package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "cupos_carrera")
public class CuposCarrera {
@jakarta.persistence.EmbeddedId
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId id;

@jakarta.persistence.MapsId("idPrueba")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_prueba", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision idPrueba;

@jakarta.persistence.MapsId("idCarrera")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_carrera", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera idCarrera;

@jakarta.persistence.MapsId("idEtapa")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_etapa", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision idEtapa;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "cupos", nullable = false)
private java.lang.Integer cupos;

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId getId() {
  return id;
}public void setId(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision getIdPrueba() {
  return idPrueba;
}public void setIdPrueba(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision idPrueba) {
  this.idPrueba = idPrueba;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera getIdCarrera() {
  return idCarrera;
}public void setIdCarrera(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera idCarrera) {
  this.idCarrera = idCarrera;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision getIdEtapa() {
  return idEtapa;
}public void setIdEtapa(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision idEtapa) {
  this.idEtapa = idEtapa;
}

public java.lang.Integer getCupos() {
  return cupos;
}public void setCupos(java.lang.Integer cupos) {
  this.cupos = cupos;
}

}