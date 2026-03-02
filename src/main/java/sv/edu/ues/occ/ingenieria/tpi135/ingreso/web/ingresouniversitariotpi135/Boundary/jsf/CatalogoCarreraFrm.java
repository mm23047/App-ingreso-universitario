package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("catalogoCarreraFrm")
@ViewScoped
public class CatalogoCarreraFrm extends DefaultFrm<CatalogoCarrera> implements Serializable {

    /**
     * DAO inyectado para acceder a la base de datos.
     * Se utiliza para realizar las operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
     * sobre la tabla de catálogo de carreras.
     */
    @Inject
    private CatalogoCarreraDAO catalogoCarreraDAO;

    /**
     * Proporciona el FacesContext actual.
     * @return Instancia actual del contexto de JSF.
     */
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public CatalogoCarreraFrm(){
        this.nombreBean="catalogoCarreraFrm";
    }

    /**
     * Retorna el DAO correspondiente a la entidad manejada por el formulario.
     * Este método es el "puente" que permite a la clase padre ejecutar
     * comandos genéricos en la base de datos sin saber qué tabla específica es.
     *
     * @return La instancia inyectada de CatalogoCarreraDAO.
     */
    @Override
    protected IngresoDAOInterface<CatalogoCarrera> getDao() {
        return catalogoCarreraDAO;
    }

    /**
     * Crea una nueva instancia vacía de la entidad.
     * Se invoca desde la clase padre cuando el usuario hace clic en el botón "Nuevo",
     * preparando el formulario para recibir datos frescos.
     *
     * @return Nueva entidad CatalogoCarrera en blanco.
     */
    @Override
    protected CatalogoCarrera nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * Busca una entidad en la base de datos utilizando su identificador.
     * Es utilizado por el padre para verificar que un registro realmente existe
     * antes de intentar modificarlo o eliminarlo.
     *
     * @param id Identificador del registro (Llave Primaria).
     * @return Entidad CatalogoCarrera encontrada o null si no existe.
     */
    @Override
    protected CatalogoCarrera buscarRegistroPorId(Object id) {
        if (id != null && catalogoCarreraDAO != null) {
            return catalogoCarreraDAO.leer(id);
        }
        return null;
    }

    /**
     * Convierte el identificador de la entidad (UUID) a formato texto.
     * JSF y HTML no entienden objetos complejos como el UUID. Al pintar una tabla,
     * JSF usa este método para ponerle un "id" de texto a cada fila.
     *
     * @param r Entidad CatalogoCarrera evaluada.
     * @return El UUID de la carrera convertido a String.
     */
    @Override
    protected String getIdAsText(CatalogoCarrera r) {
        if (r != null && r.getIdCarrera() != null) {
            return r.getIdCarrera().toString();
        }
        return null;
    }

    /**
     * Convierte un identificador en formato texto recibido de HTML a un objeto UUID real.
     * Cuando el usuario selecciona una fila para editar, la web solo envía un String.
     * Aquí transformamos ese String de vuelta a UUID y buscamos la carrera en la BD.
     *
     * @param id Identificador en formato texto enviado desde la vista.
     * @return Entidad completa de la base de datos o null si falla la conversión.
     */
    @Override
    protected CatalogoCarrera getIdByText(String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                // Convertimos el String a UUID antes de consultar al DAO
                UUID idBuscado = UUID.fromString(id);
                return catalogoCarreraDAO.leer(idBuscado);
            } catch (Exception e) {
                // Si el texto no tiene formato de UUID, capturamos el error para que la app no explote
                Logger.getLogger(CatalogoCarreraFrm.class.getName())
                        .log(Level.SEVERE, "Error al convertir String a UUID en CatalogoCarrera", e);
            }
        }
        return null;
    }

    /**
     * Crea una nueva entidad vacía.
     * Sirve como el molde inicial que se carga en memoria para que los campos
     * del formulario web tengan un lugar donde guardar lo que el usuario digita.
     *
     * @return Nueva instancia de CatalogoCarrera.
     */
    @Override
    protected CatalogoCarrera createNewEntity() {
        CatalogoCarrera nueva = new CatalogoCarrera();
        nueva.setNombre(""); // Podemos inicializar valores por defecto si lo deseamos
        return nueva;
    }

    /**
     * Obtiene el identificador primario de la entidad.
     * Le sirve al Padre para saber si la entidad actual ya existe en la BD (tiene ID)
     * o si es un registro nuevo que aún no ha sido guardado.
     *
     * @param entity Entidad CatalogoCarrera.
     * @return El UUID de la entidad.
     */
    @Override
    protected Object getEntityId(CatalogoCarrera entity) {
        if (entity != null) {
            return entity.getIdCarrera();
        }
        return null;
    }

    /**
     * Retorna el nombre humano de la entidad para el usuario.
     * El Padre lo utiliza para construir mensajes automáticos en la interfaz,
     * por ejemplo: "Catálogo de Carrera guardado exitosamente".
     *
     * @return Nombre amigable de la entidad.
     */
    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }
}