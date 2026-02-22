package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "respuestas_examen", uniqueConstraints = {@UniqueConstraint(name = "respuestas_examen_id_examen_id_pregunta_key",
        columnNames = {
                "id_examen",
                "id_pregunta"})})
public class RespuestasExaman {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_examen", nullable = false)
    private ExamenesRealizado idExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_seleccionada")
    private OpcionesRespuesta idOpcionSeleccionada;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ExamenesRealizado getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(ExamenesRealizado idExamen) {
        this.idExamen = idExamen;
    }

    public BancoPregunta getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(BancoPregunta idPregunta) {
        this.idPregunta = idPregunta;
    }

    public OpcionesRespuesta getIdOpcionSeleccionada() {
        return idOpcionSeleccionada;
    }

    public void setIdOpcionSeleccionada(OpcionesRespuesta idOpcionSeleccionada) {
        this.idOpcionSeleccionada = idOpcionSeleccionada;
    }

}