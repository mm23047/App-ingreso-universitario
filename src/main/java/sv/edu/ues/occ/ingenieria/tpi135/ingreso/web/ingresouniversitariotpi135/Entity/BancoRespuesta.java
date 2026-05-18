package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "banco_respuesta", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"texto_respuesta"})
})
public class BancoRespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_respuesta_global", nullable = false)
    private UUID id;

    @NotNull
    @Lob
    @Column(name = "texto_respuesta", nullable = false, unique = true)
    private String textoRespuesta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private AreasConocimiento idArea;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTextoRespuesta() {
        return textoRespuesta;
    }

    public void setTextoRespuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
    }

    public AreasConocimiento getIdArea() {
        return idArea;
    }

    public void setIdArea(AreasConocimiento idArea) {
        this.idArea = idArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BancoRespuesta that = (BancoRespuesta) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}