package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "banco_preguntas", schema = "public")
public class BancoPregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pregunta", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private AreasConocimiento idArea;

    @NotNull
    @Lob
    @Column(name = "enunciado", nullable = false)
    private String enunciado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AreasConocimiento getIdArea() {
        return idArea;
    }

    public void setIdArea(AreasConocimiento idArea) {
        this.idArea = idArea;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

}