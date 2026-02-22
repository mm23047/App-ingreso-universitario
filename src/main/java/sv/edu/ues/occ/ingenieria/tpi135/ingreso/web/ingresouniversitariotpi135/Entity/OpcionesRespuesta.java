package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "opciones_respuesta")
public class OpcionesRespuesta {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_opcion", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_pregunta", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta idPregunta;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Lob
@jakarta.persistence.Column(name = "texto_opcion", nullable = false)
private java.lang.String textoOpcion;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "es_correcta", nullable = false)
private java.lang.Boolean esCorrecta;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta getIdPregunta() {
  return idPregunta;
}public void setIdPregunta(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta idPregunta) {
  this.idPregunta = idPregunta;
}

public java.lang.String getTextoOpcion() {
  return textoOpcion;
}public void setTextoOpcion(java.lang.String textoOpcion) {
  this.textoOpcion = textoOpcion;
}

public java.lang.Boolean getEsCorrecta() {
  return esCorrecta;
}public void setEsCorrecta(java.lang.Boolean esCorrecta) {
  this.esCorrecta = esCorrecta;
}

}