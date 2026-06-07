package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class AulaDAO extends IngresoDefaultDataAccess<Aula> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AulaDAO() {
        super(Aula.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(Aula entity) {
        validarLogicaNegocio(entity);
        if (findByCodigoAulaApi(entity.getCodigoAulaApi()) != null) {
            throw new IllegalStateException("Ya existe un Aula registrada con el código API: " + entity.getCodigoAulaApi());
        }
        super.crear(entity);
    }

    @Override
    public Aula actualizar(Aula entity) {
        if (entity == null || entity.getIdAula() == null) {
            throw new IllegalArgumentException("La entidad Aula no es válida para actualización.");
        }
        validarLogicaNegocio(entity);
        if (countByCodigoDiferenteId(entity.getCodigoAulaApi(), entity.getIdAula()) > 0) {
            throw new IllegalStateException("El código API ingresado ya pertenece a otra Aula.");
        }
        return super.actualizar(entity);
    }

    private void validarLogicaNegocio(Aula entity) {
        if (entity == null) {
            throw new IllegalArgumentException("El aula no puede ser nula.");
        }
        if (entity.getCodigoAulaApi() == null || entity.getCodigoAulaApi().isBlank()) {
            throw new IllegalArgumentException("El código API del aula es obligatorio.");
        }
        if (entity.getCapacidadFisica() == null || entity.getCapacidadFisica() <= 0) {
            throw new IllegalArgumentException("La capacidad física del aula debe ser un número entero mayor a cero.");
        }
        if (entity.getNombreSede() == null || entity.getNombreSede().isBlank()) {
            throw new IllegalArgumentException("El nombre de la sede es obligatorio.");
        }
        if (entity.getDepartamento() == null || entity.getDepartamento().isBlank()) {
            throw new IllegalArgumentException("El departamento de la sede es obligatorio.");
        }
    }

    public List<Aula> findAulasAccesibles() {
        try {
            return em.createNamedQuery("Aula.findAccesibles", Aula.class)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al consultar las aulas accesibles.", ex);
        }
    }

    public List<Aula> findByCapacidadMinima(Integer capacidadMinima) {
        if (capacidadMinima == null || capacidadMinima <= 0) {
            throw new IllegalArgumentException("La capacidad mínima a buscar debe ser mayor a cero.");
        }
        try {
            return em.createNamedQuery("Aula.findByCapacidadMinima", Aula.class)
                    .setParameter("capacidadMinima", capacidadMinima)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al filtrar las aulas por capacidad.", ex);
        }
    }
    private long countByCodigoDiferenteId(String codigo, UUID id) {
        return em.createNamedQuery("Aula.countByCodigoAndNotId", Long.class)
                .setParameter("codigoAulaApi", codigo.trim())
                .setParameter("idAula", id)
                .getSingleResult();
    }

    public Aula findByCodigoAulaApi(String codigoAulaApi) {
        if (codigoAulaApi == null || codigoAulaApi.isBlank()) {
            throw new IllegalArgumentException("codigoAulaApi must not be null or blank");
        }
        try {
            return em.createNamedQuery("Aula.findByCodigoAulaApi", Aula.class)
                    .setParameter("codigoAulaApi", codigoAulaApi)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}