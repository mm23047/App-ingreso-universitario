package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class CuposCarreraDAO extends IngresoDefaultDataAccess<CuposCarrera> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    @Override
    public void crear(CuposCarrera entity) {
        validarReglasDeNegocio(entity);
        super.crear(entity);
    }

    @Override
    public CuposCarrera actualizar(CuposCarrera entity) {
        validarReglasDeNegocio(entity);
        return super.actualizar(entity);
    }

    private void validarReglasDeNegocio(CuposCarrera entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula.");
        }
        if (entity.getCupos() == null || entity.getCupos() < 0) {
            throw new IllegalArgumentException("La cantidad de cupos parametrizada debe ser un número entero mayor o igual a cero.");
        }
    }

    public CuposCarreraDAO() {
        super(CuposCarrera.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<CuposCarrera> findByCarrera(String idCarrera) {
        if (idCarrera == null || idCarrera.isBlank()) {
            throw new IllegalArgumentException("El idCarrera no puede ser nulo o estar en blanco");
        }
        return em.createNamedQuery("CuposCarrera.findByCarrera", CuposCarrera.class)
                .setParameter("idCarrera", idCarrera)
                .getResultList();
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Obtiene de manera exacta la cantidad de cupos máximos configurados para una combinación específica.
     * Devuelve 0 si no se encuentra ninguna parametrización registrada.
     */
    public Integer findCuposConfigurados(UUID idPrueba, String idCarrera, UUID idEtapa) {
        if (idPrueba == null || idCarrera == null || idCarrera.isBlank() || idEtapa == null) {
            throw new IllegalArgumentException("Todos los parámetros de la llave compuesta son requeridos para la consulta de cupos.");
        }
        try {
            return em.createNamedQuery("CuposCarrera.findUniqueCupo", Integer.class)
                    .setParameter("idPrueba", idPrueba)
                    .setParameter("idCarrera", idCarrera.trim())
                    .setParameter("idEtapa", idEtapa)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return 0; // Si no hay configuración previa registrada, asumimos cero espacios disponibles
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al consultar la asignación de cupos.", e);
        }
    }
}