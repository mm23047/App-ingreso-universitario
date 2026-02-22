package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Embeddable
public class PreguntasPorClaveId {
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_clave", nullable = false)
private java.lang.Integer idClave;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_pregunta", nullable = false)
private java.lang.Integer idPregunta;

public java.lang.Integer getIdClave() {
  return idClave;
}public void setIdClave(java.lang.Integer idClave) {
  this.idClave = idClave;
}

public java.lang.Integer getIdPregunta() {
  return idPregunta;
}public void setIdPregunta(java.lang.Integer idPregunta) {
  this.idPregunta = idPregunta;
}

@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreguntasPorClaveId entity = (PreguntasPorClaveId) o;
        return java.util.Objects.equals(this.idClave, entity.idClave) &&
                java.util.Objects.equals(this.idPregunta, entity.idPregunta);
    }
@Override
    public int hashCode() {
        return java.util.Objects.hash(idClave, idPregunta);
    }
}