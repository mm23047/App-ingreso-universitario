package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "banco_pregunta", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"enunciado"})
})
public class BancoPregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pregunta", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tema", nullable = false)
    private Tema idTema;

    @NotNull
    @Lob
    @Column(name = "enunciado", nullable = false, unique = true)
    private String enunciado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tema getIdTema() {
        return idTema;
    }

    public void setIdTema(Tema idTema) {
        this.idTema = idTema;
    }

    @Transient
    public AreasConocimiento getIdArea() {
        return idTema != null ? idTema.getIdArea() : null;
    }

    @Transient
    public void setIdArea(AreasConocimiento idArea) {
        if (idArea == null) {
            return;
        }
        if (this.idTema == null) {
            this.idTema = new Tema();
        }
        this.idTema.setIdArea(idArea);
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BancoPregunta that = (BancoPregunta) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}