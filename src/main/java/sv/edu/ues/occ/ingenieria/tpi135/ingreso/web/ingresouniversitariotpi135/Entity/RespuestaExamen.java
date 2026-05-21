package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "respuesta_examen", schema = "public")
@NamedQueries({
        @NamedQuery(
                name = "RespuestaExamen.findByExamenId",
                query = "SELECT r FROM RespuestaExamen r WHERE r.idExamen.idExamenRealizado = :idExamen"
        ),
        @NamedQuery(
                name = "RespuestaExamen.countByExamenAndPregunta",
                query = "SELECT COUNT(r) FROM RespuestaExamen r WHERE r.idExamen.idExamenRealizado = :idExamen AND r.idPreguntaOpcion.bancoPregunta.idBancoPregunta = :idPregunta"
        ),
        // NUEVAS CONSULTAS: Para actualización de respuestas y validación final
        @NamedQuery(
                name = "RespuestaExamen.findByExamenAndPregunta",
                query = "SELECT r FROM RespuestaExamen r WHERE r.idExamen.idExamenRealizado = :idExamen AND r.idPreguntaOpcion.bancoPregunta.idBancoPregunta = :idPregunta"
        ),
        @NamedQuery(
                name = "RespuestaExamen.countRespuestasByExamen",
                query = "SELECT COUNT(r) FROM RespuestaExamen r WHERE r.idExamen.idExamenRealizado = :idExamen"
        )
})
public class RespuestaExamen implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_respuesta_aspirante", nullable = false)
    private UUID idRespuestaExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_examen", nullable = false)
    private ExamenRealizado idExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta_opcion", nullable = false)
    private PreguntaOpcion idPreguntaOpcion;

    public UUID getIdRespuestaExamen() {
        return idRespuestaExamen;
    }

    public void setIdRespuestaExamen(UUID id) {
        this.idRespuestaExamen = id;
    }

    public ExamenRealizado getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(ExamenRealizado idExamen) {
        this.idExamen = idExamen;
    }

    public PreguntaOpcion getIdPreguntaOpcion() {
        return idPreguntaOpcion;
    }

    public void setIdPreguntaOpcion(PreguntaOpcion idPreguntaOpcion) {
        this.idPreguntaOpcion = idPreguntaOpcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RespuestaExamen that = (RespuestaExamen) o;
        return Objects.equals(idRespuestaExamen, that.idRespuestaExamen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRespuestaExamen);
    }
}