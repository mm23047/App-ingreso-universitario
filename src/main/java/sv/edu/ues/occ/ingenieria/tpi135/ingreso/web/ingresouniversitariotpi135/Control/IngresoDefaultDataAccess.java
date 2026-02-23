package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;

public abstract class IngresoDefaultDataAccess<T> implements IngresoDAOInterface<T> {

    private final Class<T> tipoDato;

    public IngresoDefaultDataAccess(Class<T> tipoDato) {
        this.tipoDato = tipoDato;
    }

    // Método abstracto que cada DAO debe implementar
    public abstract EntityManager getEntityManager();

    private EntityManager resolverEntityManager() {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("EntityManager no inicializado");
        }
        return em;
    }

    @Override
    public void crear(T registro) throws IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        try {
            EntityManager em = resolverEntityManager();
            em.persist(registro);
            em.flush();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al ingresar el registro: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(T registro) throws IllegalStateException, IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        try {
            EntityManager em = resolverEntityManager();
            if (!em.contains(registro)) {
                registro = em.merge(registro);
            }
            em.remove(registro);
        } catch (IllegalStateException ex) {
            throw ex;
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
            EntityManager em = resolverEntityManager();
            T resultado = em.merge(registro);
            em.flush();
            return resultado;
        } catch (IllegalStateException ex) {
            throw ex;
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
            return resolverEntityManager().find(tipoDato, id);
        } catch (IllegalStateException ex) {
            throw ex;
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
            EntityManager em = resolverEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(tipoDato);
            cq.select(cq.from(tipoDato));
            TypedQuery<T> allQuery = em.createQuery(cq);
            allQuery.setFirstResult(first);
            allQuery.setMaxResults(max);
            return allQuery.getResultList();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo acceder al repositorio", e);
        }
    }

    @Override
    public int count() throws IllegalStateException {
        try {
            EntityManager em = resolverEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            cq.select(cb.count(cq.from(tipoDato)));
            return em.createQuery(cq).getSingleResult().intValue();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo acceder al repositorio", e);
        }
    }

}
