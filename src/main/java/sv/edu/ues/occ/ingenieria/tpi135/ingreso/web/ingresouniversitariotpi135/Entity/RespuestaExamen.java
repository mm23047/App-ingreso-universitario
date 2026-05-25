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
// NUEVA CONSULTA: Para buscar por ID evitando LazyInitializationException
        @NamedQuery(
                name = "RespuestaExamen.findById",
                query = "SELECT r FROM RespuestaExamen r JOIN FETCH r.examenRealizado JOIN FETCH r.preguntaOpcion WHERE r.idRespuestaExamen = :idRespuestaExamen"
        ),
        // Si vas a devolver esta lista al Frontend, agrégale JOIN FETCH
        @NamedQuery(
                name = "RespuestaExamen.findByExamenId",
                query = "SELECT r FROM RespuestaExamen r JOIN FETCH r.examenRealizado JOIN FETCH r.preguntaOpcion WHERE r.examenRealizado.idExamenRealizado = :idExamen"
        ),
        // Si este método es solo para actualización interna, el JOIN FETCH es opcional,
        // pero agregarlo no hace daño si la entidad viajará a la capa web.
        @NamedQuery(
                name = "RespuestaExamen.findByExamenAndPregunta",
                query = "SELECT r FROM RespuestaExamen r JOIN FETCH r.examenRealizado JOIN FETCH r.preguntaOpcion WHERE r.examenRealizado.idExamenRealizado = :idExamen AND r.preguntaOpcion.bancoPregunta.idBancoPregunta = :idPregunta"
        ),
        @NamedQuery(
                name = "RespuestaExamen.countRespuestasByExamen",
                query = "SELECT COUNT(r) FROM RespuestaExamen r WHERE r.examenRealizado.idExamenRealizado = :idExamen"
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
    private ExamenRealizado examenRealizado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta_opcion", nullable = false)
    private PreguntaOpcion preguntaOpcion;

    public UUID getIdRespuestaExamen() {
        return idRespuestaExamen;
    }

    public void setIdRespuestaExamen(UUID id) {
        this.idRespuestaExamen = id;
    }

    public ExamenRealizado getExamenRealizado() {
        return examenRealizado;
    }

    public void setExamenRealizado(ExamenRealizado idExamen) {
        this.examenRealizado = idExamen;
    }

    public PreguntaOpcion getPreguntaOpcion() {
        return preguntaOpcion;
    }

    public void setPreguntaOpcion(PreguntaOpcion idPreguntaOpcion) {
        this.preguntaOpcion = idPreguntaOpcion;
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