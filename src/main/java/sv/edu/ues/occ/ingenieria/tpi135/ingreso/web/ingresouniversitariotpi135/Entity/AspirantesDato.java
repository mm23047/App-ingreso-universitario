package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "aspirantes_datos", schema = "public")
public class AspirantesDato {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aspirante", nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuariosSistema idUsuario;

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
    @Column(name = "dui", nullable = false, length = 12)
    private String dui;

    @NotNull
    @Column(name = "usa_silla_ruedas", nullable = false)
    private Boolean usaSillaRuedas;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UsuariosSistema getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UsuariosSistema idUsuario) {
        this.idUsuario = idUsuario;
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

    public Boolean getUsaSillaRuedas() {
        return usaSillaRuedas;
    }

    public void setUsaSillaRuedas(Boolean usaSillaRuedas) {
        this.usaSillaRuedas = usaSillaRuedas;
    }

}