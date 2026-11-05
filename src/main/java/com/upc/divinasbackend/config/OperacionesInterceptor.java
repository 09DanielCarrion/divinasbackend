package com.upc.divinasbackend.config;

import com.upc.divinasbackend.serviceinterfaces.OperacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class OperacionesInterceptor implements HandlerInterceptor {

    private static final String TIENDA = "/api/tiendas/{idTienda}";
    private static final String CLIENTE = TIENDA + "/clientes/{idCliente}";

    private static final Map<String, String> ACCIONES = Map.ofEntries(
            Map.entry("POST /api/tiendas", "Alta de tienda"),
            Map.entry("PUT " + TIENDA, "Modificación de tienda"),
            Map.entry("DELETE " + TIENDA, "Baja de tienda"),
            Map.entry("POST " + TIENDA + "/clientes", "Alta de cliente"),
            Map.entry("PUT " + CLIENTE, "Modificación de cliente"),
            Map.entry("DELETE " + CLIENTE, "Baja de cliente"),
            Map.entry("POST " + TIENDA + "/productos", "Alta de producto o servicio"),
            Map.entry("PUT " + TIENDA + "/productos/{idProducto}", "Modificación de producto o servicio"),
            Map.entry("DELETE " + TIENDA + "/productos/{idProducto}", "Baja de producto o servicio"),
            Map.entry("POST " + CLIENTE + "/compras", "Registro de compra"),
            Map.entry("PUT " + CLIENTE + "/compras/{idCompra}/cuotas/{numeroCuota}/pagar", "Pago de cuota")
    );

    @Autowired
    private OperacionService oS;

    @Override
    @SuppressWarnings("unchecked")
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String patron = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String accion = ACCIONES.get(request.getMethod() + " " + patron);
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (accion == null || !(autenticacion instanceof JwtAuthenticationToken jwt)) {
            return;
        }
        Jwt token = jwt.getToken();
        Integer idTienda = token.getClaim("idTienda") instanceof Number n ? Integer.valueOf(n.intValue()) : null;
        if (idTienda == null) {
            Map<String, String> variables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (variables != null && variables.get("idTienda") != null) {
                idTienda = Integer.valueOf(variables.get("idTienda"));
            }
        }
        oS.registrar(token.getSubject(), token.getClaimAsString("rol"), idTienda, accion, request.getMethod(),
                request.getRequestURI(), response.getStatus());
    }
}
