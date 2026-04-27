package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

/**
 * Constantes para los nombres de headers HTTP personalizados
 * utilizados en todos los recursos REST del sistema de ingreso universitario.
 * <p>
 * Centraliza los nombres para evitar errores tipográficos y facilitar
 * cambios futuros en un único lugar.
 * </p>
 */
public final class RestHeaders {

    private RestHeaders() { }

    /** Header que indica el total de registros disponibles en BD (para paginación). */
    public static final String TOTAL_RECORDS     = "Total-records";

    /** Header que indica qué parámetro faltó o es inválido en la solicitud. */
    public static final String MISSING_PARAMETER = "Missing-parameter";

    /** Header que indica el ID que no fue encontrado en BD. */
    public static final String NOT_FOUND_ID      = "Not-found-id";

    /** Header que describe la excepción ocurrida en el servidor. */
    public static final String SERVER_EXCEPTION  = "Server-exception";

    /** Header que indica una razon de conflicto de negocio. */
    public static final String CONFLICT_REASON   = "Conflict-reason";
}
