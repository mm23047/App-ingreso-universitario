package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tema", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "tema_nombre_tema_key", columnNames = {"nombre_tema"})
})
@NamedQueries({
        // NUEVA CONSULTA: Para el método leer() del DAO
        @NamedQuery(
                name = "Tema.findById",
                query = "SELECT t FROM Tema t JOIN FETCH t.areaConocimiento LEFT JOIN FETCH t.idTemaPadre WHERE t.idTema = :idTema"
        ),
        @NamedQuery(
                name = "Tema.findByNombreTema",
                query = "SELECT t FROM Tema t JOIN FETCH t.areaConocimiento LEFT JOIN FETCH t.idTemaPadre WHERE t.nombreTema = :nombreTema"
        ),
        @NamedQuery(
                name = "Tema.findByArea",
                query = "SELECT t FROM Tema t JOIN FETCH t.areaConocimiento LEFT JOIN FETCH t.idTemaPadre WHERE t.areaConocimiento.idAreaConocimiento = :idArea ORDER BY t.nombreTema ASC"
        ),
        @NamedQuery(
                name = "Tema.findByTemaPadre",
                query = "SELECT t FROM Tema t JOIN FETCH t.areaConocimiento LEFT JOIN FETCH t.idTemaPadre WHERE t.idTemaPadre.idTema = :idTemaPadre ORDER BY t.nombreTema ASC"
        ),
        @NamedQuery(
                name = "Tema.findRaicesByArea",
                query = "SELECT t FROM Tema t JOIN FETCH t.areaConocimiento WHERE t.areaConocimiento.idAreaConocimiento = :idArea AND t.idTemaPadre IS NULL ORDER BY t.nombreTema ASC"
        ),
        @NamedQuery(
                name = "Tema.findByPrueba",
                query = "SELECT DISTINCT t FROM PreguntasPorClave pxc JOIN pxc.claveExamen c JOIN pxc.bancoPregunta bp JOIN bp.tema t JOIN FETCH t.areaConocimiento a LEFT JOIN FETCH t.idTemaPadre WHERE c.pruebaAdmision.idPruebaAdmision = :idPrueba ORDER BY a.nombreArea ASC, t.nombreTema ASC"
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
    private AreasConocimiento areaConocimiento;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre_tema", nullable = false, length = 100)
    private String nombreTema;

    @JsonbTransient // ESTO EVITA EL BUCLE INFINITO EN EL JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tema_padre")
    private Tema idTemaPadre;

    public UUID getIdTema() {
        return idTema;
    }

    public void setIdTema(UUID id) {
        this.idTema = id;
    }

    public AreasConocimiento getAreaConocimiento() {
        return areaConocimiento;
    }

    public void setAreaConocimiento(AreasConocimiento idArea) {
        this.areaConocimiento = idArea;
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

    @OneToMany(mappedBy = "idTemaPadre", fetch = FetchType.LAZY)
    private List<Tema> subtemas;

    public List<Tema> getSubtemas() {
        return subtemas;
    }

    public void setSubtemas(List<Tema> subtemas) {
        this.subtemas = subtemas;
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