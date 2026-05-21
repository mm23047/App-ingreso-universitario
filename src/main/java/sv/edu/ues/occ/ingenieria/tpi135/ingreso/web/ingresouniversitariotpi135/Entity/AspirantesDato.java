package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "aspirante_datos", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "aspirante_datos_dui_key", columnNames = {"dui"}),
        @UniqueConstraint(name = "aspirante_datos_correo_key", columnNames = {"correo"})
})
@NamedQueries({
        @NamedQuery(
                name = "AspirantesDato.findByDui",
                query = "SELECT a FROM AspirantesDato a WHERE a.dui = :dui"
        ),
        @NamedQuery(
                name = "AspirantesDato.findByCorreo",
                query = "SELECT a FROM AspirantesDato a WHERE a.correo = :correo"
        ),
        // NUEVO: Query solicitado para la logística de asignación de aulas accesibles
        @NamedQuery(
                name = "AspirantesDato.findByUsaSillaRuedas",
                query = "SELECT a FROM AspirantesDato a WHERE a.usaSillaRuedas = :usaSilla ORDER BY a.apellidos ASC, a.nombres ASC"
        ),
        // NUEVOS: Queries optimizados para conteo de duplicados
        @NamedQuery(
                name = "AspirantesDato.countByDuiAndNotId",
                query = "SELECT COUNT(a) FROM AspirantesDato a WHERE a.dui = :dui AND a.id <> :id"
        ),
        @NamedQuery(
                name = "AspirantesDato.countByCorreoAndNotId",
                query = "SELECT COUNT(a) FROM AspirantesDato a WHERE a.correo = :correo AND a.id <> :id"
        )
})
public class AspirantesDato {

    private static final long serialVersionUID = 1L;

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

    @NotNull
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Size(max = 12)
    @NotNull
    @Column(name = "dui", nullable = false, length = 12)
    private String dui;

    @Size(max = 100)
    @NotNull
    @Column(name = "correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "fecha_creacion_perfil")
    private LocalDate fechaCreacionPerfil;

    @NotNull
    @Column(name = "usa_silla_ruedas", nullable = false)
    private Boolean usaSillaRuedas = false;

    // Callbacks de ciclo de vida para automatizar reglas básicas de datos
    @PrePersist
    protected void onCreate() {
        this.fechaCreacionPerfil = LocalDate.now();
        normalizarTextos();
    }
    @PreUpdate
    protected void onUpdate() {
        normalizarTextos();
    }

    private void normalizarTextos() {
        if (this.nombres != null) this.nombres = this.nombres.trim().replaceAll("\\s+", " ");
        if (this.apellidos != null) this.apellidos = this.apellidos.trim().replaceAll("\\s+", " ");
        if (this.dui != null) this.dui = this.dui.trim();
        if (this.correo != null) this.correo = this.correo.trim().toLowerCase();
    }

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
        if (this == o) return true;
        if (!(o instanceof AspirantesDato)) return false;
        AspirantesDato other = (AspirantesDato) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}