package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Embeddable
public class CuposCarreraId {
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_prueba", nullable = false)
private java.lang.Integer idPrueba;

@jakarta.validation.constraints.Size(max = 10)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_carrera", nullable = false, length = 10)
private java.lang.String idCarrera;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "id_etapa", nullable = false)
private java.lang.Short idEtapa;

public java.lang.Integer getIdPrueba() {
  return idPrueba;
}public void setIdPrueba(java.lang.Integer idPrueba) {
  this.idPrueba = idPrueba;
}

public java.lang.String getIdCarrera() {
  return idCarrera;
}public void setIdCarrera(java.lang.String idCarrera) {
  this.idCarrera = idCarrera;
}

public java.lang.Short getIdEtapa() {
  return idEtapa;
}public void setIdEtapa(java.lang.Short idEtapa) {
  this.idEtapa = idEtapa;
}

@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuposCarreraId entity = (CuposCarreraId) o;
        return java.util.Objects.equals(this.idPrueba, entity.idPrueba) &&
                java.util.Objects.equals(this.idCarrera, entity.idCarrera) &&
                java.util.Objects.equals(this.idEtapa, entity.idEtapa);
    }
@Override
    public int hashCode() {
        return java.util.Objects.hash(idPrueba, idCarrera, idEtapa);
    }
}