package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "inscripciones_prueba", uniqueConstraints = {@UniqueConstraint(name = "inscripciones_prueba_id_aspirante_id_prueba_key",
        columnNames = {
                "id_aspirante",
                "id_prueba"})})
public class InscripcionesPrueba {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aspirante", nullable = false)
    private AspirantesDato idAspirante;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prueba", nullable = false)
    private PruebasAdmision idPrueba;

    @Size(max = 20)
    @Column(name = "estado", length = 20)
    private String estado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AspirantesDato getIdAspirante() {
        return idAspirante;
    }

    public void setIdAspirante(AspirantesDato idAspirante) {
        this.idAspirante = idAspirante;
    }

    public PruebasAdmision getIdPrueba() {
        return idPrueba;
    }

    public void setIdPrueba(PruebasAdmision idPrueba) {
        this.idPrueba = idPrueba;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}