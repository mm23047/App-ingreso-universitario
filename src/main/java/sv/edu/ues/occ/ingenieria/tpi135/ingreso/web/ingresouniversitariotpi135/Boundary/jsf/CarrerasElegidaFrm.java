package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CarrerasElegidaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class CarrerasElegidaFrm extends DefaultFrm<CarrerasElegida> implements Serializable {

    @Inject
    CarrerasElegidaDAO carrerasElegidaDAO;

    @Inject
    InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @Inject
    CatalogoCarreraDAO catalogoCarreraDAO;

    private List<InscripcionesPrueba> inscripcionesDisponibles = Collections.emptyList();
    private List<CatalogoCarrera> carrerasDisponibles = Collections.emptyList();

    public CarrerasElegidaFrm() {
        this.nombreBean = "Carreras Elegidas";
    }

    /**
     * Carga las listas auxiliares (inscripciones y catálogo de carreras) necesarias para
     * los dropdowns del formulario. Cada bloque maneja su propia excepción de forma
     * independiente para evitar que un fallo impida cargar la otra lista.
     */
    @Override
    public void inicializarListas() {
        try {
            this.inscripcionesDisponibles = inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.inscripcionesDisponibles = Collections.emptyList();
            Logger.getLogger(CarrerasElegidaFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar inscripciones disponibles", e);
        }
        try {
            this.carrerasDisponibles = catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.carrerasDisponibles = Collections.emptyList();
            Logger.getLogger(CarrerasElegidaFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar catálogo de carreras", e);
        }
    }

    /**
     * Serializa la clave compuesta como {@code "idInscripcion|idCarrera"}.
     * Retorna {@code null} si la entidad, su id, o alguno de los campos de la clave es nulo.
     */
    @Override
    protected String getIdAsText(CarrerasElegida r) {
        if (r != null && r.getId() != null
                && r.getId().getIdInscripcion() != null
                && r.getId().getIdCarrera() != null) {
            return r.getId().getIdInscripcion() + "|" + r.getId().getIdCarrera();
        }
        return null;
    }

    /**
     * Reconstruye la clave compuesta a partir del texto {@code "idInscripcion|idCarrera"}
     * y delega la búsqueda en el DAO.
     */
    @Override
    protected CarrerasElegida getIdByText(String id) {
        if (id != null) {
            try {
                String[] partes = id.split("\\|", 2);
                if (partes.length == 2) {
                    CarrerasElegidaId clave = new CarrerasElegidaId();
                    clave.setIdInscripcion(Integer.parseInt(partes[0]));
                    clave.setIdCarrera(partes[1]);
                    return carrerasElegidaDAO.leer(clave);
                }
            } catch (Exception e) {
                Logger.getLogger(CarrerasElegidaFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<CarrerasElegida> getDao() {
        return carrerasElegidaDAO;
    }

    @Override
    protected CarrerasElegida nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected CarrerasElegida buscarRegistroPorId(Object id) {
        if (id != null && carrerasElegidaDAO != null) {
            return carrerasElegidaDAO.leer(id);
        }
        return null;
    }

    @Override
    protected CarrerasElegida createNewEntity() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(new CarrerasElegidaId());
        nueva.setPrioridad((short) 0);
        return nueva;
    }

    @Override
    protected Object getEntityId(CarrerasElegida entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Las relaciones idInscripcion e idCarrera las elige el usuario desde los dropdowns
    }

    // ==================== Listas auxiliares para la vista ====================

    public List<InscripcionesPrueba> getInscripcionesDisponibles() {
        return inscripcionesDisponibles;
    }

    public List<CatalogoCarrera> getCarrerasDisponibles() {
        return carrerasDisponibles;
    }
}
