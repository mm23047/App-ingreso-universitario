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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tema", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "tema_nombre_tema_key", columnNames = {"nombre_tema"})
})
@NamedQueries({
        @NamedQuery(
                name = "Tema.findByNombreTema",
                query = "SELECT t FROM Tema t WHERE t.nombreTema = :nombreTema"
        ),
        // NUEVAS CONSULTAS DE NEGOCIO
        @NamedQuery(
                name = "Tema.findByArea",
                query = "SELECT t FROM Tema t WHERE t.idArea.idAreaConocimiento = :idArea ORDER BY t.nombreTema ASC"
        ),
        @NamedQuery(
                name = "Tema.findByTemaPadre",
                query = "SELECT t FROM Tema t WHERE t.idTemaPadre.idTema = :idTemaPadre ORDER BY t.nombreTema ASC"
        ),
        @NamedQuery(
                name = "Tema.findRaicesByArea",
                query = "SELECT t FROM Tema t WHERE t.idArea.idAreaConocimiento = :idArea AND t.idTemaPadre IS NULL ORDER BY t.nombreTema ASC"
        )
})
public class Tema implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tema", nullable = false)
    private UUID idTema;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private AreasConocimiento idArea;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_tema", nullable = false, length = 100)
    private String nombreTema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema_padre")
    private Tema idTemaPadre;

    public UUID getIdTema() {
        return idTema;
    }

    public void setIdTema(UUID id) {
        this.idTema = id;
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
        return Objects.equals(idTema, tema.idTema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTema);
    }
}