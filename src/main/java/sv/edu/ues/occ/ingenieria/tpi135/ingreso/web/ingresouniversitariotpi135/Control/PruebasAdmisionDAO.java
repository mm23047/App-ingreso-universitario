package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class PruebasAdmisionDAO extends IngresoDefaultDataAccess<PruebasAdmision> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public PruebasAdmisionDAO() {
        super(PruebasAdmision.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<PruebasAdmision> findActivas() {
        try {
            return em.createNamedQuery("PruebasAdmision.findActivas", PruebasAdmision.class)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }

    public PruebasAdmision findByNombreAndAnio(String nombre, Integer anio) {
        if (nombre == null || nombre.isBlank() || anio == null) {
            throw new IllegalArgumentException("El nombre y el año no deben ser nulos");
        }
        try {
            return em.createNamedQuery("PruebasAdmision.findByNombreAndAnio", PruebasAdmision.class)
                    .setParameter("nombre", nombre)
                    .setParameter("anio", anio)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Retorna nulo si no encuentra coincidencias, manejable por el controlador.
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }
    public List<PruebasAdmision> findAllOrdenado(int first, int max) {
        try {
            return em.createNamedQuery("PruebasAdmision.findAllOrdenado", PruebasAdmision.class)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }

    public List<PruebasAdmision> buscarPorTermino(String termino, int first, int max) {
        try {
            String patron = "%" + termino.toLowerCase() + "%";
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PruebasAdmision> cq = cb.createQuery(PruebasAdmision.class);
            Root<PruebasAdmision> root = cq.from(PruebasAdmision.class);

            Predicate porNombre = cb.like(cb.lower(root.get("nombrePrueba")), patron);
            Predicate condicion;
            try {
                Integer anio = Integer.parseInt(termino.trim());
                condicion = cb.or(porNombre, cb.equal(root.get("anio"), anio));
            } catch (NumberFormatException nfe) {
                condicion = porNombre;
            }

            cq.select(root).where(condicion)
              .orderBy(cb.desc(root.get("anio")), cb.asc(root.get("nombrePrueba")));

            return em.createQuery(cq)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }

    /**
     * REGLA DE NEGOCIO PRINCIPAL:
     * Garantiza que solo exista una prueba de admisión activa en todo el sistema.
     * Desactiva todas las demás pruebas históricas y activa únicamente la solicitada.
     */
    public void setPruebaActivaExclusiva(UUID idPruebaAdmision) {
        if (idPruebaAdmision == null) {
            throw new IllegalArgumentException("El ID de la prueba no puede ser nulo.");
        }

        // 1. Apagar todas las demás pruebas mediante un UPDATE masivo eficiente
        em.createNamedQuery("PruebasAdmision.desactivarOtras")
                .setParameter("idExcluido", idPruebaAdmision)
                .executeUpdate();

        // 2. Encender la prueba objetivo
        PruebasAdmision pruebaObjetivo = em.find(PruebasAdmision.class, idPruebaAdmision);
        if (pruebaObjetivo != null) {
            pruebaObjetivo.setActiva(true);
            em.merge(pruebaObjetivo);
        } else {
            throw new IllegalArgumentException("No se encontró la prueba de admisión con el ID proporcionado.");
        }
    }
}
