package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "asignaciones_aula_pupitre", schema = "public")
@NamedQueries({
    @NamedQuery(
        name = "AsignacionesAulaPupitre.findByInscripcionId",
        query = "SELECT a FROM AsignacionesAulaPupitre a WHERE a.idInscripcion.id = :idInscripcion"
    ),
    @NamedQuery(
        name = "AsignacionesAulaPupitre.findByAspiranteId",
        query = "SELECT a FROM AsignacionesAulaPupitre a JOIN a.idInscripcion i WHERE i.idAspirante.id = :idAspirante"
    )
})
public class AsignacionesAulaPupitre {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_asignacion", nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba idInscripcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aula", nullable = false)
    private AulasExaman idAula;

    @Size(max = 20)
    @NotNull
    @Column(name = "pupitre", nullable = false, length = 20)
    private String pupitre;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InscripcionesPrueba getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(InscripcionesPrueba idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public AulasExaman getIdAula() {
        return idAula;
    }

    public void setIdAula(AulasExaman idAula) {
        this.idAula = idAula;
    }

    public String getPupitre() {
        return pupitre;
    }

    public void setPupitre(String pupitre) {
        this.pupitre = pupitre;
    }

}