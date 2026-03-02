package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;

import java.io.Serializable;

@Named("clavesExamenFrm")
@ViewScoped
public class ClavesExamenFrm extends DefaultFrm<ClavesExaman> implements Serializable {

    /**
     * DAO inyectado para acceder a la base de datos.
     *
     * Se utiliza para realizar operaciones CRUD sobre la entidad ClavesExaman.
     */
    @Inject
    private ClavesExamanDAO clavesExamanDAO;

    /**
     * Proporciona el FacesContext actual.
     *
     * FacesContext permite:
     * - Acceder a la sesión
     * - Manejar mensajes JSF
     * - Obtener información del ciclo de vida de la vista
     */
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Retorna el DAO correspondiente a la entidad manejada por el formulario.
     *
     * Este método es utilizado por la clase padre (DefaultFrm)
     * para ejecutar las operaciones CRUD de forma genérica.
     *
     * @return DAO de ClavesExaman
     */
    @Override
    protected IngresoDAOInterface<ClavesExaman> getDao() {
        return clavesExamanDAO;
    }

    /**
     * Crea una nueva instancia vacía de la entidad.
     *
     * Se usa cuando el usuario desea crear un nuevo registro.
     *
     * @return nueva entidad ClavesExaman
     */
    @Override
    protected ClavesExaman nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * Busca una entidad en la base de datos utilizando su identificador.
     *
     * Se utiliza cuando se desea editar o visualizar un registro existente.
     *
     * @param id Identificador del registro
     * @return entidad encontrada o null si no existe
     */
    @Override
    protected ClavesExaman buscarRegistroPorId(Object id) {
        if (id != null && clavesExamanDAO != null) {
            return clavesExamanDAO.leer(id);
        }
        return null;
    }

    /**
     * Convierte el identificador de la entidad a texto.
     *
     * Se usa principalmente para:
     * - Enlaces
     * - Componentes JSF que trabajan con String
     *
     * @param r Entidad ClavesExaman
     * @return ID en formato String
     */
    @Override
    protected String getIdAsText(ClavesExaman r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    /**
     * Convierte un identificador en formato texto a una entidad.
     *
     * JSF envía los valores como String desde la vista,
     * por lo que este método permite reconstruir el objeto real.
     *
     * @param id Identificador recibido desde la vista
     * @return entidad correspondiente al ID
     */
    @Override
    protected ClavesExaman getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            return clavesExamanDAO.leer(id);
        }
        return null;
    }

    /**
     * Crea una nueva entidad vacía.
     *
     * Se utiliza para inicializar formularios de creación.
     *
     * @return nueva instancia de ClavesExaman
     */
    @Override
    protected ClavesExaman createNewEntity() {
        return new ClavesExaman();
    }

    /**
     * Obtiene el identificador de la entidad.
     *
     * Permite a la clase padre manejar entidades
     * sin conocer su estructura interna.
     *
     * @param entity Entidad ClavesExaman
     * @return ID de la entidad
     */
    @Override
    protected Object getEntityId(ClavesExaman entity) {
        if (entity != null) {
            return entity.getId();
        }
        return null;
    }

    /**
     * Retorna el nombre lógico de la entidad.
     *
     * Se usa para:
     * - Mensajes
     * - Títulos
     * - Logs
     *
     * @return nombre de la entidad
     */
    @Override
    protected String getEntityName() {
        return "Claves Examen";
    }
}