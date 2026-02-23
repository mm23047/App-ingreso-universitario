package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public abstract class IngresoDefaultDataAccess<T> implements IngresoDAOInterface<T> {

    private final Class<T> tipoDato;

    public IngresoDefaultDataAccess(Class<T> tipoDato) {
        this.tipoDato = tipoDato;
    }

    // Método abstracto que cada DAO debe implementar
    public abstract EntityManager getEntityManager();

    @Override
    public void crear(T registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        try {
            EntityManager em = getEntityManager();
            if (em != null) {
                em.persist(registro);
                em.flush();
            } else {
                throw new IllegalStateException("EntityManager no inicializado");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Error al ingresar el registro: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(T registro) throws IllegalStateException, IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager no inicializado");
        }
        try {
            if (!em.contains(registro)) {
                registro = em.merge(registro);
            }
            em.remove(registro);
        } catch (Exception ex) {
            throw new IllegalStateException("Error al eliminar el registro", ex);
        }
    }

    @Override
    public T actualizar(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro a actualizar no puede ser nulo");
        }
        try {
            EntityManager em = getEntityManager();
            if (em != null) {
                T resultado = em.merge(registro);
                em.flush();
                return resultado;
            } else {
                throw new IllegalStateException("EntityManager no inicializado");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Error al actualizar registro de " + tipoDato.getSimpleName(), ex);
        }
    }

    @Override
    public T leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            EntityManager em = getEntityManager();
            if (em != null) {
                return em.find(tipoDato, id);
            } else {
                throw new IllegalStateException("EntityManager no inicializado");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de " + tipoDato.getSimpleName(), ex);
        }
    }

    @Override
    public List<T> findRange(int first, int max) throws IllegalStateException {
        if (first < 0 || max < 1) {
            throw new IllegalArgumentException();
        }
        try {
            EntityManager em = getEntityManager();
            if (em != null) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<T> cq = cb.createQuery(tipoDato);
                Root<T> rootEntry = cq.from(tipoDato);
                CriteriaQuery<T> all = cq.select(rootEntry);
                TypedQuery<T> allQuery = em.createQuery(all);
                allQuery.setFirstResult(first);
                allQuery.setMaxResults(max);
                return allQuery.getResultList();
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo acceder al repositorio");
        }
        throw new IllegalStateException("No se pudo acceder al repositorio");
    }

    @Override
    public int count() throws IllegalStateException {
        try {
            EntityManager em = getEntityManager();
            if (em != null) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Long> cq = cb.createQuery(Long.class);
                Root<T> rootEntry = cq.from(tipoDato);
                CriteriaQuery<Long> all = cq.select(cb.count(rootEntry));
                TypedQuery<Long> allQuery = em.createQuery(all);
                return allQuery.getSingleResult().intValue();
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo acceder al repositorio");
        }
        return -1;
    }

}
