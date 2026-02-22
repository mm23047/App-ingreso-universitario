package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "opciones_respuesta")
public class OpcionesRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion", nullable = false)
    private Integer id;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BancoPregunta getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(BancoPregunta idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getTextoOpcion() {
        return textoOpcion;
    }

    public void setTextoOpcion(String textoOpcion) {
        this.textoOpcion = textoOpcion;
    }

    public Boolean getEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(Boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }

}