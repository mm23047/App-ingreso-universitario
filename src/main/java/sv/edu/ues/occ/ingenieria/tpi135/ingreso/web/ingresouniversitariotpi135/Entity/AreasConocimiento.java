package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "areas_conocimiento")
public class AreasConocimiento {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_area", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombre_area", nullable = false, length = 100)
private java.lang.String nombreArea;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getNombreArea() {
  return nombreArea;
}public void setNombreArea(java.lang.String nombreArea) {
  this.nombreArea = nombreArea;
}

}