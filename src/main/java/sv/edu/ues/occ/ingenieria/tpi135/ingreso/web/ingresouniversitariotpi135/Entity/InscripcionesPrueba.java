package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "inscripciones_prueba", schema = "public")
@NamedQueries({
    @NamedQuery(
        name = "InscripcionesPrueba.findByAspiranteId",
        query = "SELECT i FROM InscripcionesPrueba i WHERE i.idAspirante.id = :idAspirante"
    ),
    @NamedQuery(
        name = "InscripcionesPrueba.findByPruebaId",
        query = "SELECT i FROM InscripcionesPrueba i WHERE i.idPrueba.id = :idPrueba"
    ),
        @NamedQuery(
                name = "InscripcionesPrueba.countByAspiranteAndPrueba",
                query = "SELECT COUNT(i) FROM InscripcionesPrueba i WHERE i.idAspirante.id = :idAspirante AND i.idPrueba.id = :idPrueba"
        )
})
public class InscripcionesPrueba {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_inscripcion", nullable = false)
    private UUID id;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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