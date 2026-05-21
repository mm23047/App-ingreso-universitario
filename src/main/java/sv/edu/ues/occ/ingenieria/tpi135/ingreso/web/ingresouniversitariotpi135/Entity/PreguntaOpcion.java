package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

@Entity
// CORRECCIÓN: Se agrega explícitamente el schema "public" para consistencia con todo el proyecto
@Table(name = "pregunta_opcion", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pregunta_respuesta", columnNames = {"id_pregunta", "id_respuesta_global"})
})
@NamedQueries({
        @NamedQuery(
                name = "PreguntaOpcion.findByPregunta",
                query = "SELECT p FROM PreguntaOpcion p WHERE p.bancoPregunta.idBancoPregunta = :idPregunta ORDER BY p.idPreguntaOpcion"
        ),
        @NamedQuery(
                name = "PreguntaOpcion.countByPreguntaAndRespuesta",
                query = "SELECT COUNT(p) FROM PreguntaOpcion p WHERE p.bancoPregunta.idBancoPregunta = :idPregunta AND p.idRespuestaGlobal.idBancoRespuesta = :idRespuestaGlobal"
        ),
        // NUEVO: Query optimizado para extraer la hoja de respuestas válidas para calificar
        @NamedQuery(
                name = "PreguntaOpcion.findOpcionesCorrectasByPregunta",
                query = "SELECT p FROM PreguntaOpcion p WHERE p.bancoPregunta.idBancoPregunta = :idPregunta AND p.esCorrecta = true"
        )
})
public class PreguntaOpcion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pregunta_opcion", nullable = false)
    private UUID idPreguntaOpcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta bancoPregunta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_respuesta_global", nullable = false)
    private BancoRespuesta idRespuestaGlobal;

    @NotNull
    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;

    public UUID getIdPreguntaOpcion() {
        return idPreguntaOpcion;
    }

    public void setIdPreguntaOpcion(UUID id) {
        this.idPreguntaOpcion = id;
    }

    public BancoPregunta getBancoPregunta() {
        return bancoPregunta;
    }

    public void setBancoPregunta(BancoPregunta idPregunta) {
        this.bancoPregunta = idPregunta;
    }

    public BancoRespuesta getIdRespuestaGlobal() {
        return idRespuestaGlobal;
    }

    public void setIdRespuestaGlobal(BancoRespuesta idRespuestaGlobal) {
        this.idRespuestaGlobal = idRespuestaGlobal;
    }

    public Boolean getEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(Boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }

    @PrePersist
    @PreUpdate
    private void asegurarBooleano() {
        if (this.esCorrecta == null) {
            this.esCorrecta = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreguntaOpcion that = (PreguntaOpcion) o;
        return idPreguntaOpcion != null && idPreguntaOpcion.equals(that.idPreguntaOpcion);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}