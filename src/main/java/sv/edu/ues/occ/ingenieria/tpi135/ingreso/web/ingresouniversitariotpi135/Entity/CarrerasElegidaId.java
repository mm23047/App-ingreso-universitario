package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Embeddable
public class CarrerasElegidaId {
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_inscripcion", nullable = false)
private java.lang.Integer idInscripcion;

@jakarta.validation.constraints.Size(max = 10)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_carrera", nullable = false, length = 10)
private java.lang.String idCarrera;

public java.lang.Integer getIdInscripcion() {
  return idInscripcion;
}public void setIdInscripcion(java.lang.Integer idInscripcion) {
  this.idInscripcion = idInscripcion;
}

public java.lang.String getIdCarrera() {
  return idCarrera;
}public void setIdCarrera(java.lang.String idCarrera) {
  this.idCarrera = idCarrera;
}

@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrerasElegidaId entity = (CarrerasElegidaId) o;
        return java.util.Objects.equals(this.idInscripcion, entity.idInscripcion) &&
                java.util.Objects.equals(this.idCarrera, entity.idCarrera);
    }
@Override
    public int hashCode() {
        return java.util.Objects.hash(idInscripcion, idCarrera);
    }
}