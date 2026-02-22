package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "proceso_admision_aspirante")
public class ProcesoAdmisionAspirante {
@jakarta.persistence.Id
@jakarta.persistence.Column(name = "id_inscripcion", nullable = false)
private java.lang.Integer id;

@jakarta.persistence.MapsId
@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_inscripcion", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba inscripcionesPrueba;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_etapa_actual", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision idEtapaActual;

@jakarta.validation.constraints.Size(max = 30)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "estado", nullable = false, length = 30)
private java.lang.String estado;

@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
@jakarta.persistence.JoinColumn(name = "carrera_asignada")
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera carreraAsignada;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba getInscripcionesPrueba() {
  return inscripcionesPrueba;
}public void setInscripcionesPrueba(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba inscripcionesPrueba) {
  this.inscripcionesPrueba = inscripcionesPrueba;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision getIdEtapaActual() {
  return idEtapaActual;
}public void setIdEtapaActual(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision idEtapaActual) {
  this.idEtapaActual = idEtapaActual;
}

public java.lang.String getEstado() {
  return estado;
}public void setEstado(java.lang.String estado) {
  this.estado = estado;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera getCarreraAsignada() {
  return carreraAsignada;
}public void setCarreraAsignada(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera carreraAsignada) {
  this.carreraAsignada = carreraAsignada;
}

}