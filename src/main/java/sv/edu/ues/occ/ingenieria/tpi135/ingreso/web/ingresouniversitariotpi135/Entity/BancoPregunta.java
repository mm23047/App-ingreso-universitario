package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "banco_preguntas")
public class BancoPregunta {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_pregunta", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_area", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento idArea;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Lob
@jakarta.persistence.Column(name = "enunciado", nullable = false)
private java.lang.String enunciado;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento getIdArea() {
  return idArea;
}public void setIdArea(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento idArea) {
  this.idArea = idArea;
}

public java.lang.String getEnunciado() {
  return enunciado;
}public void setEnunciado(java.lang.String enunciado) {
  this.enunciado = enunciado;
}

}