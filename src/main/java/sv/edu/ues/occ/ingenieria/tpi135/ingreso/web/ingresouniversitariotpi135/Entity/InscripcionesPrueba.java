package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "inscripciones_prueba")
public class InscripcionesPrueba {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_inscripcion", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_aspirante", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato idAspirante;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_prueba", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision idPrueba;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "estado", length = 20)
private java.lang.String estado;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato getIdAspirante() {
  return idAspirante;
}public void setIdAspirante(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato idAspirante) {
  this.idAspirante = idAspirante;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision getIdPrueba() {
  return idPrueba;
}public void setIdPrueba(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision idPrueba) {
  this.idPrueba = idPrueba;
}

public java.lang.String getEstado() {
  return estado;
}public void setEstado(java.lang.String estado) {
  this.estado = estado;
}

}