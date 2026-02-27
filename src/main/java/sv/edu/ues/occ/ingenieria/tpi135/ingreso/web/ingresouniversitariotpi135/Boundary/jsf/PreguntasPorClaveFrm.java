package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.UUID;

@Named
@ViewScoped
public class PreguntasPorClaveFrm extends DefaultFrm<PreguntasPorClave> {

    @Inject
    PreguntasPorClaveDAO preguntasPorClaveDAO;

    @Inject
    ClavesExamanDAO clavesExamanDAO;

    @Inject
    BancoPreguntaDAO bancoPreguntaDAO;

    private List<ClavesExaman> clavesDisponibles = Collections.emptyList();
    private List<BancoPregunta> preguntasDisponibles = Collections.emptyList();

    public PreguntasPorClaveFrm() {
        this.nombreBean = "Preguntas por Clave";
    }

    /**
     * Carga las listas auxiliares de claves de examen y preguntas del banco para
     * los dropdowns del formulario. Cada bloque maneja su excepción de forma
     * independiente para que un fallo no bloquee la otra lista.
     */
    @Override
    public void inicializarListas() {
        try {
            this.clavesDisponibles = clavesExamanDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.clavesDisponibles = Collections.emptyList();
            Logger.getLogger(PreguntasPorClaveFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar claves disponibles", e);
        }
        try {
            this.preguntasDisponibles = bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.preguntasDisponibles = Collections.emptyList();
            Logger.getLogger(PreguntasPorClaveFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar preguntas disponibles", e);
        }
    }

    /**
     * Serializa la clave compuesta como {@code "idClave|idPregunta"}.
     * Retorna {@code null} si la entidad, su id, o alguno de los campos de la clave es nulo.
     */
    @Override
    protected String getIdAsText(PreguntasPorClave r) {
        if (r != null && r.getId() != null
                && r.getId().getIdClave() != null
                && r.getId().getIdPregunta() != null) {
            return r.getId().getIdClave() + "|" + r.getId().getIdPregunta();
        }
        return null;
    }

    /**
     * Reconstruye la clave compuesta a partir del texto {@code "idClave|idPregunta"}
     * y delega la búsqueda en el DAO.
     */
    @Override
    protected PreguntasPorClave getIdByText(String id) {
        if (id != null) {
            try {
                String[] partes = id.split("\\|", 2);
                if (partes.length == 2) {
                    PreguntasPorClaveId clave = new PreguntasPorClaveId();
                    clave.setIdClave(UUID.fromString(partes[0]));
                    clave.setIdPregunta(UUID.fromString(partes[1]));
                    return preguntasPorClaveDAO.leer(clave);
                }
            } catch (Exception e) {
                Logger.getLogger(PreguntasPorClaveFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<PreguntasPorClave> getDao() {
        return preguntasPorClaveDAO;
    }

    @Override
    protected PreguntasPorClave nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected PreguntasPorClave buscarRegistroPorId(Object id) {
        if (id != null && preguntasPorClaveDAO != null) {
            return preguntasPorClaveDAO.leer(id);
        }
        return null;
    }

    @Override
    protected PreguntasPorClave createNewEntity() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(new PreguntasPorClaveId());
        // idClave e idPregunta son obligatorios y se asignan desde los dropdowns
        return nueva;
    }

    @Override
    protected Object getEntityId(PreguntasPorClave entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Las relaciones idClave e idPregunta las elige el usuario desde los dropdowns
    }

    // ==================== Listas auxiliares para la vista ====================

    public List<ClavesExaman> getClavesDisponibles() {
        return clavesDisponibles;
    }

    public List<BancoPregunta> getPreguntasDisponibles() {
        return preguntasDisponibles;
    }
}
