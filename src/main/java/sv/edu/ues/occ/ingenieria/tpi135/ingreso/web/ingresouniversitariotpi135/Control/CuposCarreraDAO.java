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
    private static final String PARAM_ID_CARRERA = "idCarrera";

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
            throw new IllegalArgumentException("El ID de la         Carrera no puede ser nulo o estar en blanco");
        }
        return em.createNamedQuery("CuposCarrera.findByCarrera", CuposCarrera.class)
                .setParameter(PARAM_ID_CARRERA, idCarrera)
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
                    .setParameter(PARAM_ID_CARRERA, idCarrera.trim())
                    .setParameter("idEtapa", idEtapa)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return 0; // Si no hay configuración previa registrada, asumimos cero espacios disponibles
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al consultar la asignación de cupos.", e);
        }
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 2 COMPLETADA)
     * Decrementa atómicamente un cupo directamente en la base de datos.
     * Evita problemas de concurrencia (Race Conditions) delegando la operación matemática al motor SQL.
     * * @return true si se logró restar el cupo con éxito, false si ya no hay cupos (cupos <= 0) o no existe la configuración.
     */
    public boolean decrementarCupo(UUID idPrueba, String idCarrera, UUID idEtapa) {
        if (idPrueba == null || idCarrera == null || idCarrera.isBlank() || idEtapa == null) {
            throw new IllegalArgumentException("Todos los parámetros son requeridos para decrementar el cupo.");
        }
        try {
            int updated = em.createNamedQuery("CuposCarrera.decrementarCupoAtomico")
                    .setParameter("idPrueba", idPrueba)
                    .setParameter(PARAM_ID_CARRERA, idCarrera.trim())
                    .setParameter("idEtapa", idEtapa)
                    .executeUpdate();

            return updated == 1;
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al intentar decrementar el cupo.", e);
        }
    }

    /**
     * Sobrescribimos el método leer del padre para incluir los JOIN FETCH
     * y prevenir el LazyInitializationException al serializar la entidad compuesta en REST.
     */
    @Override
    public CuposCarrera leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("CuposCarrera.findByIdConRelaciones", CuposCarrera.class)
                    .setParameter("idCupo", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Replicamos el comportamiento original de em.find() devolviendo null
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer el cupo de carrera con sus relaciones", ex);
        }
    }
}