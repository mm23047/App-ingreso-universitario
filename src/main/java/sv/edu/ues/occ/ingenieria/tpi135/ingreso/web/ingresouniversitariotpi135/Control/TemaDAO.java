package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.io.Serializable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@Stateless
@LocalBean
public class TemaDAO extends IngresoDefaultDataAccess<Tema> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public TemaDAO() {
        super(Tema.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Tema registro) throws IllegalArgumentException {
        validarJerarquia(registro);
        super.crear(registro);
    }

    @Override
    public Tema actualizar(Tema registro) {
        validarJerarquia(registro);
        return super.actualizar(registro);
    }

    public Tema findByNombreTema(String nombreTema) {
        if (nombreTema == null || nombreTema.isBlank()) {
            throw new IllegalArgumentException("nombreTema must not be null or blank");
        }
        try {
            return em.createQuery(
                            "SELECT t FROM Tema t WHERE t.nombreTema = :nombreTema",
                            Tema.class)
                    .setParameter("nombreTema", nombreTema)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    private void validarJerarquia(Tema tema) {
        if (tema == null) {
            throw new IllegalArgumentException("tema must not be null");
        }
        Tema padre = tema.getIdTemaPadre();
        if (padre == null) {
            return;
        }
        if (padre == tema || (tema.getId() != null && tema.getId().equals(padre.getId()))) {
            throw new IllegalArgumentException("Tema no puede ser su propio padre");
        }
        Set<Tema> visitados = Collections.newSetFromMap(new IdentityHashMap<>());
        visitados.add(tema);
        Tema cursor = padre;
        while (cursor != null) {
            if (!visitados.add(cursor)) {
                throw new IllegalArgumentException("La jerarquia de temas contiene un ciclo");
            }
            if (tema.getId() != null && tema.getId().equals(cursor.getId())) {
                throw new IllegalArgumentException("La jerarquia de temas contiene un ciclo");
            }
            cursor = cursor.getIdTemaPadre();
        }
    }
}