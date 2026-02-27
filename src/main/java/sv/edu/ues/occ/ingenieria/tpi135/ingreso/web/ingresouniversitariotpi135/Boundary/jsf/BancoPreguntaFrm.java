package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AreasConocimientoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.UUID;

@Named
@ViewScoped
public class BancoPreguntaFrm extends DefaultFrm<BancoPregunta> {

    @Inject
    BancoPreguntaDAO bancoPreguntaDAO;

    @Inject
    AreasConocimientoDAO areasConocimientoDAO;

    private List<AreasConocimiento> areasDisponibles = Collections.emptyList();

    public BancoPreguntaFrm() {
        this.nombreBean = "Banco de Preguntas";
    }

    /**
     * Carga la lista de áreas de conocimiento para el dropdown de {@code idArea}.
     * Se invoca automáticamente desde {@code inicializar()} vía la clase base.
     */
    @Override
    public void inicializarListas() {
        try {
            this.areasDisponibles = areasConocimientoDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.areasDisponibles = Collections.emptyList();
            Logger.getLogger(BancoPreguntaFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar áreas disponibles", e);
        }
    }

    @Override
    protected String getIdAsText(BancoPregunta r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected BancoPregunta getIdByText(String id) {
        if (id != null) {
            try {
                UUID buscado = UUID.fromString(id);
                return bancoPreguntaDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(BancoPreguntaFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<BancoPregunta> getDao() {
        return bancoPreguntaDAO;
    }

    @Override
    protected BancoPregunta nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected BancoPregunta buscarRegistroPorId(Object id) {
        if (id != null && bancoPreguntaDAO != null) {
            return bancoPreguntaDAO.leer(id);
        }
        return null;
    }

    @Override
    protected BancoPregunta createNewEntity() {
        BancoPregunta nueva = new BancoPregunta();
        nueva.setEnunciado("");
        return nueva;
    }

    @Override
    protected Object getEntityId(BancoPregunta entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // La relación idArea la elige el usuario desde el dropdown
    }

    // ==================== Lista auxiliar para la vista ====================

    public List<AreasConocimiento> getAreasDisponibles() {
        return areasDisponibles;
    }
}
