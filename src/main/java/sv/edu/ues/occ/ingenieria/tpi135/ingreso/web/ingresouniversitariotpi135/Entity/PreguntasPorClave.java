package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "preguntas_por_clave", schema = "public")
@NamedQueries({
        @NamedQuery(
                name = "PreguntasPorClave.countByClaveAndPregunta",
                query = "SELECT COUNT(p) FROM PreguntasPorClave p WHERE p.idPreguntaPorClave.idClave = :idClave AND p.idPreguntaPorClave.idPregunta = :idPregunta"
        ),
        @NamedQuery(
                name = "PreguntasPorClave.findPreguntasByClave",
                query = "SELECT p FROM PreguntasPorClave p JOIN FETCH p.bancoPregunta WHERE p.idPreguntaPorClave.idClave = :idClave"
        ),
        @NamedQuery(
                name = "PreguntasPorClave.findById",
                query = "SELECT p FROM PreguntasPorClave p JOIN FETCH p.claveExamen JOIN FETCH p.bancoPregunta WHERE p.idPreguntaPorClave = :idPreguntaPorClave"
        ),
        @NamedQuery(
                name = "PreguntasPorClave.countByClave",
                query = "SELECT COUNT(p) FROM PreguntasPorClave p WHERE p.idPreguntaPorClave.idClave = :idClave"
        ),
})
public class PreguntasPorClave implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private PreguntasPorClaveId idPreguntaPorClave;

    @MapsId("idClave")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave", nullable = false)
    private ClavesExamen claveExamen;

    @MapsId("idPregunta")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta bancoPregunta;

    public PreguntasPorClaveId getIdPreguntaPorClave() {
        return idPreguntaPorClave;
    }

    public void setIdPreguntaPorClave(PreguntasPorClaveId id) {
        this.idPreguntaPorClave = id;
    }

    public ClavesExamen getClaveExamen() {
        return claveExamen;
    }

    public void setClaveExamen(ClavesExamen idClave) {
        this.claveExamen = idClave;
    }

    public BancoPregunta getBancoPregunta() {
        return bancoPregunta;
    }

    public void setBancoPregunta(BancoPregunta idPregunta) {
        this.bancoPregunta = idPregunta;
    }

    // CORRECCIÓN: Métodos indispensables para el manejo de colecciones en JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreguntasPorClave)) return false;
        PreguntasPorClave that = (PreguntasPorClave) o;
        return Objects.equals(this.idPreguntaPorClave, that.idPreguntaPorClave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPreguntaPorClave);
    }

}