package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "aulas_examen")
public class AulasExaman {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_aula", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_turno", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman idTurno;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_aula_api", nullable = false, length = 50)
private java.lang.String idAulaApi;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "capacidad", nullable = false)
private java.lang.Integer capacidad;

@jakarta.persistence.Column(name = "cupos_ocupados")
private java.lang.Integer cuposOcupados;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "accesible_silla_ruedas", nullable = false)
private java.lang.Boolean accesibleSillaRuedas;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman getIdTurno() {
  return idTurno;
}public void setIdTurno(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman idTurno) {
  this.idTurno = idTurno;
}

public java.lang.String getIdAulaApi() {
  return idAulaApi;
}public void setIdAulaApi(java.lang.String idAulaApi) {
  this.idAulaApi = idAulaApi;
}

public java.lang.Integer getCapacidad() {
  return capacidad;
}public void setCapacidad(java.lang.Integer capacidad) {
  this.capacidad = capacidad;
}

public java.lang.Integer getCuposOcupados() {
  return cuposOcupados;
}public void setCuposOcupados(java.lang.Integer cuposOcupados) {
  this.cuposOcupados = cuposOcupados;
}

public java.lang.Boolean getAccesibleSillaRuedas() {
  return accesibleSillaRuedas;
}public void setAccesibleSillaRuedas(java.lang.Boolean accesibleSillaRuedas) {
  this.accesibleSillaRuedas = accesibleSillaRuedas;
}

}