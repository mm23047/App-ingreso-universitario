package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "opciones_respuesta", schema = "public")
@NamedQueries({
    @NamedQuery(
        name = "OpcionesRespuesta.findByPreguntaId",
        query = "SELECT o FROM OpcionesRespuesta o WHERE o.idPregunta.id = :preguntaId ORDER BY o.id"
    ),
    @NamedQuery(
        name = "OpcionesRespuesta.countByPreguntaId",
        query = "SELECT COUNT(o) FROM OpcionesRespuesta o WHERE o.idPregunta.id = :preguntaId"
    )
})
public class OpcionesRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_opcion", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta idPregunta;

    @NotNull
    @Lob
    @Column(name = "texto_opcion", nullable = false)
    private String textoOpcion;

    @NotNull
    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;

    @JsonbProperty("id")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BancoPregunta getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(BancoPregunta idPregunta) {
        this.idPregunta = idPregunta;
    }

    @JsonbProperty("textoOpcion")
    public String getTextoOpcion() {
        return textoOpcion;
    }

    public void setTextoOpcion(String textoOpcion) {
        this.textoOpcion = textoOpcion;
    }

    @JsonbProperty("esCorrecta")
    public Boolean getEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(Boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }

}