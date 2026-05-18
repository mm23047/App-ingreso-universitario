package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tema", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nombre_tema"})
})
public class Tema {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tema", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private AreasConocimiento idArea;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_tema", nullable = false, unique = true, length = 100)
    private String nombreTema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema_padre")
    private Tema idTemaPadre;

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

    public String getNombreTema() {
        return nombreTema;
    }

    public void setNombreTema(String nombreTema) {
        this.nombreTema = nombreTema;
    }

    public Tema getIdTemaPadre() {
        return idTemaPadre;
    }

    public void setIdTemaPadre(Tema idTemaPadre) {
        this.idTemaPadre = idTemaPadre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tema tema = (Tema) o;
        return Objects.equals(id, tema.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}