package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "etapas_admision")
public class EtapasAdmision {
@jakarta.persistence.Id
@jakarta.persistence.Column(name = "id_etapa", nullable = false)
private java.lang.Short id;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombre", nullable = false, length = 50)
private java.lang.String nombre;

@jakarta.persistence.Column(name = "puntaje_minimo", precision = 5, scale = 2)
private java.math.BigDecimal puntajeMinimo;

@jakarta.persistence.Column(name = "puntaje_maximo", precision = 5, scale = 2)
private java.math.BigDecimal puntajeMaximo;

@jakarta.persistence.Lob
@jakarta.persistence.Column(name = "descripcion")
private java.lang.String descripcion;

public java.lang.Short getId() {
  return id;
}public void setId(java.lang.Short id) {
  this.id = id;
}

public java.lang.String getNombre() {
  return nombre;
}public void setNombre(java.lang.String nombre) {
  this.nombre = nombre;
}

public java.math.BigDecimal getPuntajeMinimo() {
  return puntajeMinimo;
}public void setPuntajeMinimo(java.math.BigDecimal puntajeMinimo) {
  this.puntajeMinimo = puntajeMinimo;
}

public java.math.BigDecimal getPuntajeMaximo() {
  return puntajeMaximo;
}public void setPuntajeMaximo(java.math.BigDecimal puntajeMaximo) {
  this.puntajeMaximo = puntajeMaximo;
}

public java.lang.String getDescripcion() {
  return descripcion;
}public void setDescripcion(java.lang.String descripcion) {
  this.descripcion = descripcion;
}

}