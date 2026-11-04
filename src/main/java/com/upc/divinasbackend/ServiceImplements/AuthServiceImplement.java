package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.config.SecurityConfig;
import com.upc.divinasbackend.dtos.LoginRequestDTO;
import com.upc.divinasbackend.dtos.LoginResponseDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Tienda;
import com.upc.divinasbackend.exceptions.CredencialesInvalidasException;
import com.upc.divinasbackend.exceptions.SolicitudInvalidaException;
import com.upc.divinasbackend.repositories.ClienteRepository;
import com.upc.divinasbackend.repositories.TiendaRepository;
import com.upc.divinasbackend.serviceinterfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImplement implements AuthService {

    private static final String MENSAJE_INVALIDO = "Usuario o contraseña incorrectos.";

    @Autowired
    private TiendaRepository tR;

    @Autowired
    private ClienteRepository cR;

    @Autowired
    private PasswordEncoder pE;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiracion-minutos}")
    private long expiracionMinutos;

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        if ("ADMIN".equals(dto.tipo())) {
            return loginAdmin(dto);
        }
        return loginCliente(dto);
    }

    private LoginResponseDTO loginAdmin(LoginRequestDTO dto) {
        Tienda tienda = tR.findByUsuarioAdmin(dto.usuario())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_INVALIDO));
        if (!Boolean.TRUE.equals(tienda.getEstado()) || !pE.matches(dto.password(), tienda.getPasswordAdmin())) {
            throw new CredencialesInvalidasException(MENSAJE_INVALIDO);
        }
        return respuesta(SecurityConfig.ROL_ADMIN, tienda.getIdTienda(), null, tienda.getUsuarioAdmin(),
                tienda.getNombreTienda());
    }

    private LoginResponseDTO loginCliente(LoginRequestDTO dto) {
        if (dto.idTienda() == null) {
            throw new SolicitudInvalidaException("La tienda es obligatoria para iniciar sesión como cliente.");
        }
        Cliente cliente = cR.findByTienda_IdTiendaAndUsuarioCliente(dto.idTienda(), dto.usuario())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_INVALIDO));
        boolean activo = Boolean.TRUE.equals(cliente.getEstado())
                && Boolean.TRUE.equals(cliente.getTienda().getEstado());
        if (!activo || !pE.matches(dto.password(), cliente.getPasswordCliente())) {
            throw new CredencialesInvalidasException(MENSAJE_INVALIDO);
        }
        return respuesta(SecurityConfig.ROL_CLIENTE, dto.idTienda(), cliente.getIdCliente(),
                cliente.getUsuarioCliente(), cliente.getNombreCliente());
    }

    private LoginResponseDTO respuesta(String rol, Integer idTienda, Integer idCliente, String usuario, String nombre) {
        Instant ahora = Instant.now();
        long segundos = expiracionMinutos * 60;
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(usuario)
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(segundos))
                .claim("rol", rol)
                .claim("idTienda", idTienda);
        if (idCliente != null) {
            claims.claim("idCliente", idCliente);
        }
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
        return new LoginResponseDTO(token, "Bearer", rol, idTienda, idCliente, nombre, segundos);
    }
}
