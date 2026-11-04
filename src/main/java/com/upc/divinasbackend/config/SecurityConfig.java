package com.upc.divinasbackend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROL_ADMIN = "ADMIN_TIENDA";
    public static final String ROL_CLIENTE = "CLIENTE";

    @Value("${app.jwt.secret}")
    private String secreto;

    @Bean
    public SecretKey claveJwt() {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey claveJwt) {
        return NimbusJwtDecoder.withSecretKey(claveJwt).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/tiendas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tiendas").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers("/api/tiendas/{idTienda}/clientes/{idCliente}/**")
                        .access(AutorizacionRutas.adminOClientePropio())

                        .requestMatchers("/api/tiendas/{idTienda}/productos/**")
                        .access(AutorizacionRutas.adminOLecturaDeTienda())
                        .requestMatchers(HttpMethod.GET, "/api/tiendas/{idTienda}")
                        .access(AutorizacionRutas.adminOLecturaDeTienda())

                        .requestMatchers("/api/tiendas/{idTienda}/**")
                        .access(AutorizacionRutas.soloAdmin())

                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(RespuestasSeguridad.noAutenticado())
                        .accessDeniedHandler(RespuestasSeguridad.sinPermiso()))
                .oauth2ResourceServer(o -> o
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(RespuestasSeguridad.noAutenticado())
                        .accessDeniedHandler(RespuestasSeguridad.sinPermiso()));
        return http.build();
    }

    static final class AutorizacionRutas {

        private AutorizacionRutas() {
        }

        static AuthorizationManager<RequestAuthorizationContext> soloAdmin() {
            return (auth, ctx) -> new AuthorizationDecision(esAdminDeLaTienda(auth, ctx));
        }

        static AuthorizationManager<RequestAuthorizationContext> adminOLecturaDeTienda() {
            return (auth, ctx) -> {
                if (esAdminDeLaTienda(auth, ctx)) {
                    return new AuthorizationDecision(true);
                }
                return new AuthorizationDecision(esLectura(ctx) && esClienteDeLaTienda(auth, ctx));
            };
        }

        static AuthorizationManager<RequestAuthorizationContext> adminOClientePropio() {
            return (auth, ctx) -> {
                if (esAdminDeLaTienda(auth, ctx)) {
                    return new AuthorizationDecision(true);
                }
                if (!esClienteDeLaTienda(auth, ctx)) {
                    return new AuthorizationDecision(false);
                }
                Jwt token = token(auth);
                boolean esElMismoCliente = textoDe(token, "idCliente").equals(ctx.getVariables().get("idCliente"));
                boolean puedeSimular = "POST".equals(ctx.getRequest().getMethod())
                        && ctx.getRequest().getRequestURI().endsWith("/compras/simular");
                return new AuthorizationDecision(esElMismoCliente && (esLectura(ctx) || puedeSimular));
            };
        }

        private static boolean esLectura(RequestAuthorizationContext ctx) {
            return "GET".equals(ctx.getRequest().getMethod());
        }

        private static boolean esAdminDeLaTienda(Supplier<? extends Authentication> auth, RequestAuthorizationContext ctx) {
            Jwt token = token(auth);
            return token != null && ROL_ADMIN.equals(token.getClaimAsString("rol")) && esDeLaTienda(token, ctx);
        }

        private static boolean esClienteDeLaTienda(Supplier<? extends Authentication> auth, RequestAuthorizationContext ctx) {
            Jwt token = token(auth);
            return token != null && ROL_CLIENTE.equals(token.getClaimAsString("rol")) && esDeLaTienda(token, ctx);
        }

        private static boolean esDeLaTienda(Jwt token, RequestAuthorizationContext ctx) {
            return textoDe(token, "idTienda").equals(ctx.getVariables().get("idTienda"));
        }

        private static String textoDe(Jwt token, String claim) {
            Object valor = token.getClaim(claim);
            return String.valueOf(valor);
        }

        private static Jwt token(Supplier<? extends Authentication> auth) {
            Authentication autenticacion = auth.get();
            if (autenticacion instanceof JwtAuthenticationToken jwt && autenticacion.isAuthenticated()) {
                return jwt.getToken();
            }
            return null;
        }
    }
}
