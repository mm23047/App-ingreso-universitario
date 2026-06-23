package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class AspirantesDatoDAO extends IngresoDefaultDataAccess<AspirantesDato> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AspirantesDatoDAO() {
        super(AspirantesDato.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(AspirantesDato entity) {
        validarReglasNegocio(entity);

        // Comprobación preventiva de llaves únicas (DUI y Correo)
        if (findByDui(entity.getDui()) != null) {
            throw new ReglaNegocioException(
                    ReglaNegocioException.Tipo.DUI_DUPLICADO,
                    "El DUI proporcionado ya pertenece a un aspirante registrado.");
        }
        if (findByCorreo(entity.getCorreo()) != null) {
            throw new ReglaNegocioException(
                    ReglaNegocioException.Tipo.CORREO_DUPLICADO,
                    "El correo electrónico ya se encuentra en uso.");
        }
        super.crear(entity);
    }

    @Override
    public AspirantesDato actualizar(AspirantesDato entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Entidad no válida para actualización.");
        }
        validarReglasNegocio(entity);

        // Validar unicidad excluyendo el registro actual
        if (countByDuiDiferenteId(entity.getDui(), entity.getId()) > 0) {
            throw new ReglaNegocioException(
                    ReglaNegocioException.Tipo.DUI_DUPLICADO,
                    "El nuevo DUI ingresado ya está asignado a otro aspirante.");
        }
        if (countByCorreoDiferenteId(entity.getCorreo(), entity.getId()) > 0) {
            throw new ReglaNegocioException(
                    ReglaNegocioException.Tipo.CORREO_DUPLICADO,
                    "El nuevo correo electrónico ingresado ya está en uso.");
        }
        return super.actualizar(entity);
    }

    /**
     * Valida restricciones lógicas de negocio como la edad permitida para aplicar.
     */
    private void validarReglasNegocio(AspirantesDato entity) {
        if (entity == null) throw new IllegalArgumentException("Los datos del aspirante no pueden ser nulos.");

        if (entity.getFechaNacimiento() != null) {
            int edad = Period.between(entity.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < 18) {
                throw new ReglaNegocioException(
                        ReglaNegocioException.Tipo.EDAD_MINIMA,
                        "El aspirante debe tener al menos 18 años de edad para registrarse.");
            }
        }
    }

    public AspirantesDato findByDui(String dui) {
        if (dui == null || dui.isBlank()) {
            throw new IllegalArgumentException("dui must not be null or blank");
        }
        try {
            return em.createNamedQuery("AspirantesDato.findByDui", AspirantesDato.class)
                    .setParameter("dui", dui)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public AspirantesDato findByCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("correo must not be null or blank");
        }
        try {
            return em.createNamedQuery("AspirantesDato.findByCorreo", AspirantesDato.class)
                    .setParameter("correo", correo)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<AspirantesDato> findByRequiereSillaRuedas() {
        try {
            return em.createNamedQuery("AspirantesDato.findByUsaSillaRuedas", AspirantesDato.class)
                    .setParameter("usaSilla", true)
                    .getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException("Error al consultar aspirantes con requerimientos de accesibilidad.", ex);
        }
    }

    private long countByDuiDiferenteId(String dui, UUID id) {
        return em.createNamedQuery("AspirantesDato.countByDuiAndNotId", Long.class)
                .setParameter("dui", dui.trim())
                .setParameter("id", id)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT)
                .getSingleResult();
    }
    private long countByCorreoDiferenteId(String correo, UUID id) {
        return em.createNamedQuery("AspirantesDato.countByCorreoAndNotId", Long.class)
                .setParameter("correo", correo.trim().toLowerCase())
                .setParameter("id", id)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT)
                .getSingleResult();
    }

}