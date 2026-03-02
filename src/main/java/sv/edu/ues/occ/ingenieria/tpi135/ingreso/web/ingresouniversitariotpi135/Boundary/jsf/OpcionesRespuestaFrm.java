package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.OpcionesRespuestaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

import java.io.Serializable;

@Named("opciobesRespuestaFrm")
@ViewScoped
public class OpcionesRespuestaFrm extends DefaultFrm<OpcionesRespuesta> implements Serializable {

    /**
     * DAO parea acceder a la BD y hacer CRUD
     */
    @Inject
    OpcionesRespuestaDAO opcionesRespuestaDAO;

    //Para acceder al FC que se esta utilizando
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }


    /**
     * Metodo utilizdado por el padre para hacer CRUD
     * @return DAO de la entity utilizada en el momento actual
     */
    @Override
    protected IngresoDAOInterface<OpcionesRespuesta> getDao() {
        return opcionesRespuestaDAO;
    }

    /**
     * CRea Instancia vacia para que el usuario la pueda llenar
     * @return entidad vacia
     */
    @Override
    protected OpcionesRespuesta nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * Para poder ver o editar un registro de la BD
     * @param id Utilizado para buscar el Registro
     * @return ENtidad encontrada
     */
    @Override
    protected OpcionesRespuesta buscarRegistroPorId(Object id) {
        if ( id!=null && opcionesRespuestaDAO != null) {
            return opcionesRespuestaDAO.leer(id);
        }
        return null;
    }

    /**
     * COnvertimos el ID a texto para que lo utilice el JSF
     * @param r entidad utilizada en el momento actual
     * @return El ID en formato string
     */
    @Override
    protected String getIdAsText(OpcionesRespuesta r) {
        if ( r != null && r.getId() != null ) {
            return r.getId().toString();
        }
        return null;
    }

    /**
     * Recivimos de JSF en formato texto y lo convertimos a un objeto utilizado por java
     * @param id Recivido desde JSF
     * @return entidad que corresponde al ID
     */
    @Override
    protected OpcionesRespuesta getIdByText(String id) {
        if (id != null && !id.isEmpty()) {
            return opcionesRespuestaDAO.leer(id);
        }
        return null;
    }

    /**
     * Para crear entidad vacia
     * @return nueva instancia para llenar el formulario
     */
    @Override
    protected OpcionesRespuesta createNewEntity() {
        return new OpcionesRespuesta();
    }

    /**
     * OBtenemos el ID  de la entity para que el padre las utilice
     * @param entity entidad opcionesRespuesta
     * @return ID de la entity
     */
    @Override
    protected Object getEntityId(OpcionesRespuesta entity) {
        if (entity != null) {
            return entity.getId();
        }

        return null;
    }


    //Retorna el nombre de la entidad UTILIZADA en el instante actual
    @Override
    protected String getEntityName() {
        return "Opciones Respuesta";
    }
}
