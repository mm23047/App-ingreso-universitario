package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "aspirante_datos", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"dui"}),
    @UniqueConstraint(columnNames = {"correo"})
})
public class AspirantesDato {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aspirante", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Size(max = 100)
    @NotNull
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Size(max = 12)
    @NotNull
    @Column(name = "dui", nullable = false, unique = true, length = 12)
    private String dui;

    @NotNull
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Size(max = 100)
    @NotNull
    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "fecha_creacion_perfil")
    private LocalDate fechaCreacionPerfil;

    @NotNull
    @Column(name = "usa_silla_ruedas", nullable = false)
    private Boolean usaSillaRuedas = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public LocalDate getFechaCreacionPerfil() {
        return fechaCreacionPerfil;
    }

    public void setFechaCreacionPerfil(LocalDate fechaCreacionPerfil) {
        this.fechaCreacionPerfil = fechaCreacionPerfil;
    }

    public Boolean getUsaSillaRuedas() {
        return usaSillaRuedas;
    }

    public void setUsaSillaRuedas(Boolean usaSillaRuedas) {
        this.usaSillaRuedas = usaSillaRuedas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AspirantesDato that = (AspirantesDato) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}