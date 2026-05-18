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

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "respuesta_examen", schema = "public")
@NamedQueries({
        @NamedQuery(
                name = "RespuestaExamen.findByExamenId",
                query = "SELECT r FROM RespuestaExamen r WHERE r.idExamen.id = :idExamen"
        )
})
public class RespuestaExamen {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_respuesta_aspirante", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_examen", nullable = false)
    private ExamenRealizado idExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta_opcion", nullable = false)
    private PreguntaOpcion idPreguntaOpcion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
