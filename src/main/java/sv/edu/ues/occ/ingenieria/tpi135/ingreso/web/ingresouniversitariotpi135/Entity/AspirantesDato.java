package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "aspirantes_datos")
public class AspirantesDato {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_aspirante", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@jakarta.persistence.JoinColumn(name = "id_usuario", nullable = false)
private sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema idUsuario;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombres", nullable = false, length = 100)
private java.lang.String nombres;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "apellidos", nullable = false, length = 100)
private java.lang.String apellidos;

@jakarta.validation.constraints.Size(max = 12)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "dui", nullable = false, length = 12)
private java.lang.String dui;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "usa_silla_ruedas", nullable = false)
private java.lang.Boolean usaSillaRuedas;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema getIdUsuario() {
  return idUsuario;
}public void setIdUsuario(sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema idUsuario) {
  this.idUsuario = idUsuario;
}

public java.lang.String getNombres() {
  return nombres;
}public void setNombres(java.lang.String nombres) {
  this.nombres = nombres;
}

public java.lang.String getApellidos() {
  return apellidos;
}public void setApellidos(java.lang.String apellidos) {
  this.apellidos = apellidos;
}

public java.lang.String getDui() {
  return dui;
}public void setDui(java.lang.String dui) {
  this.dui = dui;
}

public java.lang.Boolean getUsaSillaRuedas() {
  return usaSillaRuedas;
}public void setUsaSillaRuedas(java.lang.Boolean usaSillaRuedas) {
  this.usaSillaRuedas = usaSillaRuedas;
}

}