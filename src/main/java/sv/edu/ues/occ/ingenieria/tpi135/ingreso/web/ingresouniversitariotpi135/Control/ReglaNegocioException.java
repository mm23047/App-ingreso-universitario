package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.ApplicationException;

/**
 * Excepción de negocio del dominio de ingreso universitario.
 *
 * Al llevar @ApplicationException(rollback=false):
 *  - El contenedor EJB NO la envuelve en EJBException.
 *  - La transacción no se marca para rollback (ninguna operación se persistió).
 *  - El Resource puede capturarla directamente y mapearla a la respuesta HTTP correcta.
 *
 * El campo Tipo permite al Resource distinguir el caso sin comparar texto del mensaje.
 */
@ApplicationException(rollback = false)
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        EDAD_MINIMA,
        DUI_DUPLICADO,
        CORREO_DUPLICADO
    }

    private final Tipo tipo;

    public ReglaNegocioException(Tipo tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
