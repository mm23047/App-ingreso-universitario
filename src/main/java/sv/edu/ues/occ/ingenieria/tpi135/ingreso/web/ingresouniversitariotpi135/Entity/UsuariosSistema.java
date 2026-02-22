package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "usuarios_sistema")
public class UsuariosSistema {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id_usuario", nullable = false)
private java.lang.Integer id;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "nombre_usuario", nullable = false, length = 50)
private java.lang.String nombreUsuario;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "correo", nullable = false, length = 100)
private java.lang.String correo;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Lob
@jakarta.persistence.Column(name = "contrasena_hash", nullable = false)
private java.lang.String contrasenaHash;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "rol", nullable = false, length = 20)
private java.lang.String rol;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getNombreUsuario() {
  return nombreUsuario;
}public void setNombreUsuario(java.lang.String nombreUsuario) {
  this.nombreUsuario = nombreUsuario;
}

public java.lang.String getCorreo() {
  return correo;
}public void setCorreo(java.lang.String correo) {
  this.correo = correo;
}

public java.lang.String getContrasenaHash() {
  return contrasenaHash;
}public void setContrasenaHash(java.lang.String contrasenaHash) {
  this.contrasenaHash = contrasenaHash;
}

public java.lang.String getRol() {
  return rol;
}public void setRol(java.lang.String rol) {
  this.rol = rol;
}

}