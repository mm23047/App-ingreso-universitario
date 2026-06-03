package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ProcesoAdmisionAspiranteDAO extends IngresoDefaultDataAccess<ProcesoAdmisionAspirante> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    // MAGIA AQUÍ: Inyectamos el DAO que tiene el método de descuento seguro en SQL
    @Inject
    protected CuposCarreraDAO cuposCarreraDAO;

    public CuposCarreraDAO getCuposCarreraDAO() {
        return cuposCarreraDAO;
    }

    public void setCuposCarreraDAO(CuposCarreraDAO cuposCarreraDAO) {
        this.cuposCarreraDAO = cuposCarreraDAO;
    }

    public ProcesoAdmisionAspiranteDAO() {
        super(ProcesoAdmisionAspirante.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Asigna la carrera final a un aspirante garantizando que el cupo se reste a nivel SQL.
     */
    public ProcesoAdmisionAspirante asignarCarreraFinal(UUID idInscripcion) {
        if (idInscripcion == null) {
            throw new IllegalArgumentException("idInscripcion es requerido");
        }

        ProcesoAdmisionAspirante proceso = em.find(ProcesoAdmisionAspirante.class, idInscripcion);
        if (proceso == null) {
            return null;
        }

        UUID idPrueba = proceso.getInscripcionesPrueba().getPruebaAdmision().getIdPruebaAdmision();
        UUID idEtapa = proceso.getEtapaAdmision().getIdEtapaAdmision();

        List<CarrerasElegida> elegidas = em.createNamedQuery("ProcesoAdmisionAspirante.findCarrerasElegidas", CarrerasElegida.class)
                .setParameter("idInscripcion", idInscripcion)
                .getResultList();

        for (CarrerasElegida elegida : elegidas) {
            String idCarrera = elegida.getCatalogoCarrera().getIdCarrera();

            // REGLA APLICADA: Usamos el método atómico del otro DAO CUPOS_Carrera.
            // Si devuelve TRUE, significa que había cupo y la BD ya lo descontó.
            boolean cupoConcedido = cuposCarreraDAO.decrementarCupo(idPrueba, idCarrera, idEtapa);

            if (cupoConcedido) {
                proceso.setCarreraAsignada(em.getReference(CatalogoCarrera.class, idCarrera));
                proceso.setEstado("ADMITIDO");
                return proceso;
            }
        }

        // Si el bucle termina y ningún decrementarCupo devolvió true, se quedó sin cupos en todas sus opciones
        proceso.setCarreraAsignada(null);
        proceso.setEstado("NO_ADMITIDO");
        return proceso;
    }
//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    /**
     * Si en las pruebas de estrés notas que tu endpoint POST /asignar-masivo se demora más de 30 segundos,
     * considera volver el método de tu DAO asíncrono agregando la anotación @Asynchronous de EJB,
     * y responde desde tu Resource con un código HTTP 202 ACCEPTED (que significa: "Recibí la orden,
     * la estoy procesando en segundo plano, revisa más tarde").
     */
    /**
     * Procesa en lote a todos los estudiantes garantizando Todo o Nada (Transaccionalidad)
     */
    public void procesarAsignacionMasiva(UUID idEtapa) {
        if (idEtapa == null) {
            throw new IllegalArgumentException("El id de la etapa es requerido.");
        }

        List<ProcesoAdmisionAspirante> aspirantesOrdenados = em.createNamedQuery(
                        "ProcesoAdmisionAspirante.findPendientesPorPuntaje", ProcesoAdmisionAspirante.class)
                .setParameter("idEtapa", idEtapa)
                .getResultList();

        int batchSize = 50; // Tamaño prudente del lote
        int count = 0;

        for (ProcesoAdmisionAspirante aspirante : aspirantesOrdenados) {
            this.asignarCarreraFinal(aspirante.getIdProcesoAdmisionAspirante());
            count++;

            // MAGIA DE RENDIMIENTO: Sincroniza y destruye la basura en la memoria de Java
            if (count % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }
        // Empujar los remanentes
        em.flush();
        em.clear();
    }

    /**
     * Sobrescribimos el método leer del padre para evitar LazyInitializationException en REST.
     * Carga de una vez la inscripción, la etapa y (si existe) la carrera asignada.
     */
    @Override
    public ProcesoAdmisionAspirante leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del proceso no puede ser nulo");
        }
        try {
            return em.createNamedQuery("ProcesoAdmisionAspirante.findByIdConRelaciones", ProcesoAdmisionAspirante.class)
                    .setParameter("idProceso", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null; // Replicamos el comportamiento de em.find() devolviendo null si no existe
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de ProcesoAdmisionAspirante con relaciones", ex);
        }
    }
}