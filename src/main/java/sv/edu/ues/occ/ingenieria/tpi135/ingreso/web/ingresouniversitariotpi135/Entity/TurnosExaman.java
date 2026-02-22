package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "turnos_examen")
public class TurnosExaman {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_turno", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_prueba", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision idPrueba;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombre_turno", nullable = false, length = 50)
private java.lang.String nombreTurno;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "fecha", nullable = false)
private java.time.LocalDate fecha;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "hora_inicio", nullable = false)
private java.time.LocalTime horaInicio;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "hora_fin", nullable = false)
private java.time.LocalTime horaFin;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision getIdPrueba() {
  return idPrueba;
}public void setIdPrueba(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision idPrueba) {
  this.idPrueba = idPrueba;
}

public java.lang.String getNombreTurno() {
  return nombreTurno;
}public void setNombreTurno(java.lang.String nombreTurno) {
  this.nombreTurno = nombreTurno;
}

public java.time.LocalDate getFecha() {
  return fecha;
}public void setFecha(java.time.LocalDate fecha) {
  this.fecha = fecha;
}

public java.time.LocalTime getHoraInicio() {
  return horaInicio;
}public void setHoraInicio(java.time.LocalTime horaInicio) {
  this.horaInicio = horaInicio;
}

public java.time.LocalTime getHoraFin() {
  return horaFin;
}public void setHoraFin(java.time.LocalTime horaFin) {
  this.horaFin = horaFin;
}

}