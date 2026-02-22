package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "preguntas_por_clave")
public class PreguntasPorClave {
    @EmbeddedId
    private PreguntasPorClaveId id;

    @MapsId("idClave")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clave", nullable = false)
    private ClavesExaman idClave;

    @MapsId("idPregunta")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta idPregunta;

    public PreguntasPorClaveId getId() {
        return id;
    }

    public void setId(PreguntasPorClaveId id) {
        this.id = id;
    }

    public ClavesExaman getIdClave() {
        return idClave;
    }

    public void setIdClave(ClavesExaman idClave) {
        this.idClave = idClave;
    }

    public BancoPregunta getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(BancoPregunta idPregunta) {
        this.idPregunta = idPregunta;
    }

}