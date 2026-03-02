package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CuposCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;

import java.io.Serializable;

@Named("cuposCarreraFrm")
@ViewScoped
public class CuposCarreraFrm extends DefaultFrm<CuposCarrera > implements Serializable {

    //DAO para acceder a la BD y hacer CRUD
    @Inject
    CuposCarreraDAO cuposCarreraDAO;

    /**
     * Para poder saber en que html se esta trabajando
     * @return FC
     */
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * DAO que utiliza el padre para hacer CRUD
     * @return DAO de la clase actual
     */
    @Override
    protected IngresoDAOInterface<CuposCarrera> getDao() {
        return cuposCarreraDAO;
    }

    /**
     * Para que el usuario pueda crear un registro vacio
     * @return La entidad a utilizar
     */
    @Override
    protected CuposCarrera nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * BUscamos la entidad en la BD para hacer CRUD
     * @param id de la entidad actual
     * @return entidad encontrada
     */
    @Override
    protected CuposCarrera buscarRegistroPorId(Object id) {
        if (id != null && cuposCarreraDAO != null) {
            return cuposCarreraDAO.leer(id);
        }
        return null;
    }

    /**
     * COnvertimos el ID a String para poder mostrarlo como texto
     * @param r entidad actual de cupos
     * @return ID registro/entity en formato texto
     */
    @Override
    protected String getIdAsText(CuposCarrera r) {
        if (r != null && r.getId()!=null) {
            return r.getId().toString();
        }
        return null;
    }

    /**
     * COnvertimos el texto recibido desde el html a un tipo objeto para poder ser utilizado por java
     * @param id del registro recibido desde el frontEnd
     * @return EL objeto para poder utilizarlo en el crud
     */
    @Override
    protected CuposCarrera getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            return cuposCarreraDAO.leer(id);
        }
        return null;
    }

    /**
     * Creamos una entidad para poder llenarlo en el formulario
     * @return la entidad actual
     */
    @Override
    protected CuposCarrera createNewEntity() {
        return new CuposCarrera();
    }

    /**
     * ENtidad actual para que el padre pueda hacer CRUD
     * @param entity ENtidad actual en clase
     * @return ID de la entity actual
     */
    @Override
    protected Object getEntityId(CuposCarrera entity) {
        if (entity != null) {
            return entity.getId();
        }
        return null;
    }

    //NOmbre de la entidad utilizada
    @Override
    protected String getEntityName() {
        return "Cupos Carrera";
    }
}
