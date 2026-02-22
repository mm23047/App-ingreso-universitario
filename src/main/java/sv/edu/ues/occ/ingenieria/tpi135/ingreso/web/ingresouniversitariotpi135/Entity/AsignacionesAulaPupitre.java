package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "asignaciones_aula_pupitre")
public class AsignacionesAulaPupitre {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_asignacion", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_inscripcion", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba idInscripcion;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_aula", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman idAula;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "pupitre", nullable = false, length = 20)
private java.lang.String pupitre;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba getIdInscripcion() {
  return idInscripcion;
}public void setIdInscripcion(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba idInscripcion) {
  this.idInscripcion = idInscripcion;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman getIdAula() {
  return idAula;
}public void setIdAula(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman idAula) {
  this.idAula = idAula;
}

public java.lang.String getPupitre() {
  return pupitre;
}public void setPupitre(java.lang.String pupitre) {
  this.pupitre = pupitre;
}

}