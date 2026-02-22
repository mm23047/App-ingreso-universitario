package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "catalogo_carreras")
public class CatalogoCarrera {
@jakarta.persistence.Id
@jakarta.validation.constraints.Size(max = 10)
@jakarta.persistence.Column(name = "id_carrera", nullable = false, length = 10)
private java.lang.String idCarrera;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombre", nullable = false, length = 100)
private java.lang.String nombre;

public java.lang.String getIdCarrera() {
  return idCarrera;
}public void setIdCarrera(java.lang.String idCarrera) {
  this.idCarrera = idCarrera;
}

public java.lang.String getNombre() {
  return nombre;
}public void setNombre(java.lang.String nombre) {
  this.nombre = nombre;
}

}