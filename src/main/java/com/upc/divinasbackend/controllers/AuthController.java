package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.LoginRequestDTO;
import com.upc.divinasbackend.dtos.LoginResponseDTO;
import com.upc.divinasbackend.dtos.TiendaPublicaDTO;
import com.upc.divinasbackend.serviceinterfaces.AuthService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService aS;

    @Autowired
    private TiendaService tS;

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return aS.login(dto);
    }

    @GetMapping("/tiendas")
    public List<TiendaPublicaDTO> tiendas() {
        return tS.list().stream()
                .filter(t -> Boolean.TRUE.equals(t.getEstado()))
                .map(t -> new TiendaPublicaDTO(t.getIdTienda(), t.getNombreTienda()))
                .toList();
    }
}
