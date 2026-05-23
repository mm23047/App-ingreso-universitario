package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.io.Serializable;
import java.util.*;

@Stateless
@LocalBean
public class TemaDAO extends IngresoDefaultDataAccess<Tema> implements Serializable {

    private static final long serialVersionUID = 1L;

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
            return em.createNamedQuery("Tema.findByNombreTema", Tema.class)
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
        if (padre == tema || (tema.getIdTema() != null && tema.getIdTema().equals(padre.getIdTema()))) {
            throw new IllegalArgumentException("Tema no puede ser su propio padre");
        }
        Set<Tema> visitados = Collections.newSetFromMap(new IdentityHashMap<>());
        visitados.add(tema);
        Tema cursor = padre;
        while (cursor != null) {
            if (!visitados.add(cursor)) {
                throw new IllegalArgumentException("La jerarquia de temas contiene un ciclo");
            }
            if (tema.getIdTema() != null && tema.getIdTema().equals(cursor.getIdTema())) {
                throw new IllegalArgumentException("La jerarquia de temas contiene un ciclo");
            }
            cursor = cursor.getIdTemaPadre();
        }
    }

    /**
     * REGLA DE NEGOCIO (FASE DE CONFIGURACIÓN):
     * Busca todos los temas (sin importar su jerarquía) pertenecientes a un Área de Conocimiento.
     */
    public List<Tema> findByArea(UUID idArea) {
        if (idArea == null) {
            return Collections.emptyList();
        }
        return em.createNamedQuery("Tema.findByArea", Tema.class)
                .setParameter("idArea", idArea)
                .getResultList();
    }
    /**
     * REGLA DE NEGOCIO Opcional:
     * Busca únicamente los temas principales/raíces (sin padre) de un Área específica.
     * Ideal para cargar el primer nivel de un menú jerárquico.
     */
    public List<Tema> findRaicesByArea(UUID idArea) {
        if (idArea == null) {
            return Collections.emptyList();
        }
        return em.createNamedQuery("Tema.findRaicesByArea", Tema.class)
                .setParameter("idArea", idArea)
                .getResultList();
    }
    /**
     * REGLA DE NEGOCIO (FASE DE CONFIGURACIÓN):
     * Busca todos los nodos hijos de un Tema en específico (Despliegue del árbol).
     */
    public List<Tema> findByTemaPadre(UUID idTemaPadre) {
        if (idTemaPadre == null) {
            return Collections.emptyList();
        }
        return em.createNamedQuery("Tema.findByTemaPadre", Tema.class)
                .setParameter("idTemaPadre", idTemaPadre)
                .getResultList();
    }

    /**
     * Sobrescribimos el método padre para prevenir LazyInitializationException.
     * Carga el Área y el Padre (si existe) en un solo viaje a la BD.
     */
    @Override
    public Tema leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("Tema.findById", Tema.class)
                    .setParameter("idTema", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Replicamos el comportamiento de em.find()
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de Tema con relaciones", ex);
        }
    }
}