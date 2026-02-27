package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.UUID;

@Named
@ViewScoped
public class AulasExamanFrm extends DefaultFrm<AulasExaman> {

    @Inject
    AulasExamanDAO aulasExamanDAO;

    @Inject
    TurnosExamanDAO turnosExamanDAO;

    private List<TurnosExaman> turnosDisponibles = Collections.emptyList();

    public AulasExamanFrm() {
        this.nombreBean = "Aulas de Examen";
    }

    /**
     * Carga la lista de turnos disponibles para el dropdown de {@code idTurno}.
     * Se invoca automáticamente desde {@code inicializar()} vía la clase base.
     */
    @Override
    public void inicializarListas() {
        try {
            this.turnosDisponibles = turnosExamanDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.turnosDisponibles = Collections.emptyList();
            Logger.getLogger(AulasExamanFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar turnos disponibles", e);
        }
    }

    @Override
    protected String getIdAsText(AulasExaman r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected AulasExaman getIdByText(String id) {
        if (id != null) {
            try {
                UUID buscado = UUID.fromString(id);
                return aulasExamanDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(AulasExamanFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<AulasExaman> getDao() {
        return aulasExamanDAO;
    }

    @Override
    protected AulasExaman nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected AulasExaman buscarRegistroPorId(Object id) {
        if (id != null && aulasExamanDAO != null) {
            return aulasExamanDAO.leer(id);
        }
        return null;
    }

    @Override
    protected AulasExaman createNewEntity() {
        AulasExaman nueva = new AulasExaman();
        nueva.setIdAulaApi("");
        nueva.setCapacidad(0);
        nueva.setCuposOcupados(0);
        nueva.setAccesibleSillaRuedas(false);
        return nueva;
    }

    @Override
    protected Object getEntityId(AulasExaman entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // La relación idTurno la elige el usuario desde el dropdown
    }

    // ==================== Lista auxiliar para la vista ====================

    public List<TurnosExaman> getTurnosDisponibles() {
        return turnosDisponibles;
    }
}
