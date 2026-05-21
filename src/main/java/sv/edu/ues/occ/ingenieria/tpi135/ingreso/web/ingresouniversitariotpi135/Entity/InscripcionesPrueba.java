package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "inscripcion_prueba", schema = "public",  uniqueConstraints = {
        @UniqueConstraint(name = "uk_aspirante_prueba", columnNames = {"id_aspirante", "id_prueba"})
})
@NamedQueries({
        @NamedQuery(
                name = "InscripcionesPrueba.findByAspiranteId",
                query = "SELECT i FROM InscripcionesPrueba i WHERE i.aspiranteDato.id = :idAspirante"
        ),
        @NamedQuery(
                name = "InscripcionesPrueba.findByPruebaId",
                query = "SELECT i FROM InscripcionesPrueba i WHERE i.pruebaAdmision.idPruebaAdmision = :idPrueba"
        ),
        @NamedQuery(
                name = "InscripcionesPrueba.countByAspiranteAndPrueba",
                query = "SELECT COUNT(i) FROM InscripcionesPrueba i WHERE i.aspiranteDato.id = :idAspirante AND i.pruebaAdmision.idPruebaAdmision = :idPrueba"
        ),
        @NamedQuery(
                name = "InscripcionesPrueba.countByAspiranteAndPruebaExcludingId",
                query = "SELECT COUNT(i) FROM InscripcionesPrueba i WHERE i.aspiranteDato.id = :idAspirante AND i.pruebaAdmision.idPruebaAdmision = :idPrueba AND i.idInscripcionPrueba <> :excludeId"
        ),
        // NUEVO: Query táctico para el control de la logística pre-examen y distribución de butacas
        @NamedQuery(
                name = "InscripcionesPrueba.findByPruebaAndEstado",
                query = "SELECT i FROM InscripcionesPrueba i WHERE i.pruebaAdmision.idPruebaAdmision = :idPrueba AND UPPER(TRIM(i.estado)) = UPPER(TRIM(:estado))"
        )
})
public class InscripcionesPrueba implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_inscripcion", nullable = false)
    private UUID idInscripcionPrueba;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aspirante", nullable = false)
    private AspirantesDato aspiranteDato;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prueba", nullable = false)
    private PruebasAdmision pruebaAdmision;

    @Size(max = 20)
    @Column(name = "estado", length = 20)
    private String estado;

    public UUID getIdInscripcionPrueba() {
        return idInscripcionPrueba;
    }

    public void setIdInscripcionPrueba(UUID id) {
        this.idInscripcionPrueba = id;
    }

    public AspirantesDato getAspiranteDato() {
        return aspiranteDato;
    }

    public void setAspiranteDato(AspirantesDato idAspirante) {
        this.aspiranteDato = idAspirante;
    }

    public PruebasAdmision getPruebaAdmision() {
        return pruebaAdmision;
    }

    public void setPruebaAdmision(PruebasAdmision idPrueba) {
        this.pruebaAdmision = idPrueba;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @PrePersist
    private void inicializarYNormalizar() {
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "PENDIENTE";
        } else {
            this.estado = this.estado.trim().toUpperCase();
        }
    }

    @PreUpdate
    private void normalizarEstado() {
        if (this.estado != null) {
            this.estado = this.estado.trim().toUpperCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InscripcionesPrueba that = (InscripcionesPrueba) o;
        return idInscripcionPrueba != null && idInscripcionPrueba.equals(that.idInscripcionPrueba);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}