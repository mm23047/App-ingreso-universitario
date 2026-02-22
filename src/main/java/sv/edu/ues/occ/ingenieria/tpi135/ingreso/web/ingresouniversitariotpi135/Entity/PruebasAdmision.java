package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "pruebas_admision")
public class PruebasAdmision {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_prueba", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombre_prueba", nullable = false, length = 100)
private java.lang.String nombrePrueba;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "anio", nullable = false)
private java.lang.Integer anio;

@jakarta.persistence.Column(name = "activa")
private java.lang.Boolean activa;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getNombrePrueba() {
  return nombrePrueba;
}public void setNombrePrueba(java.lang.String nombrePrueba) {
  this.nombrePrueba = nombrePrueba;
}

public java.lang.Integer getAnio() {
  return anio;
}public void setAnio(java.lang.Integer anio) {
  this.anio = anio;
}

public java.lang.Boolean getActiva() {
  return activa;
}public void setActiva(java.lang.Boolean activa) {
  this.activa = activa;
}

}