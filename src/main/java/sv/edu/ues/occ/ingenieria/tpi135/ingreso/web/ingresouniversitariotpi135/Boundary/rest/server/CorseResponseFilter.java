package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.List;

@Provider
public class CorseResponseFilter implements ContainerResponseFilter {

    public static final String METODOS_PERMITIDOS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
    public static final int MAXIMO_CACHE = 30 * 60 * 60;
    public static final String CABECERAS_PERMITIDAS = "origin, content-type, accept";
    public static final String CABECERAS_EXPUESTAS = "location,info";

    @Override
    public void filter(ContainerRequestContext crc,
                       ContainerResponseContext responseContext) throws IOException {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers",
                getRequestAllowedHeaders(responseContext));
        headers.add("Access-Control-Allow-Methods", METODOS_PERMITIDOS);
        headers.add("Access-Control-Max-Age", MAXIMO_CACHE);
        headers.add("Access-Control-Expose-Headers", CABECERAS_EXPUESTAS);
    }

    private String getRequestAllowedHeaders(ContainerResponseContext responseContext) {
        List<Object> headers =
                responseContext.getHeaders().get("Access-Control-Allow-Headers");

        return crearCabeceras(headers, CABECERAS_PERMITIDAS);
    }

    private String crearCabeceras(List<Object> cabeceras,
                                  String cabecerasPorDefecto) {

        if (cabeceras == null || cabeceras.isEmpty()) {
            return cabecerasPorDefecto;
        }

        StringBuilder sb = new StringBuilder();

        for (Object cabecera : cabeceras) {
            sb.append(cabecera.toString());
            sb.append(",");
        }

        sb.append(cabecerasPorDefecto);

        return sb.toString();
    }
}