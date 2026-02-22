package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "examenes_realizados")
public class ExamenesRealizado {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_examen", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_asignacion", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre idAsignacion;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_clave", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman idClave;

@jakarta.persistence.Column(name = "puntaje_final", precision = 5, scale = 2)
private java.math.BigDecimal puntajeFinal;

@jakarta.persistence.Column(name = "fecha_realizacion")
private java.time.OffsetDateTime fechaRealizacion;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_etapa", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision idEtapa;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre getIdAsignacion() {
  return idAsignacion;
}public void setIdAsignacion(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre idAsignacion) {
  this.idAsignacion = idAsignacion;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman getIdClave() {
  return idClave;
}public void setIdClave(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman idClave) {
  this.idClave = idClave;
}

public java.math.BigDecimal getPuntajeFinal() {
  return puntajeFinal;
}public void setPuntajeFinal(java.math.BigDecimal puntajeFinal) {
  this.puntajeFinal = puntajeFinal;
}

public java.time.OffsetDateTime getFechaRealizacion() {
  return fechaRealizacion;
}public void setFechaRealizacion(java.time.OffsetDateTime fechaRealizacion) {
  this.fechaRealizacion = fechaRealizacion;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision getIdEtapa() {
  return idEtapa;
}public void setIdEtapa(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision idEtapa) {
  this.idEtapa = idEtapa;
}

}