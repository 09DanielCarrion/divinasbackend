package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.entities.Tienda;
import com.upc.divinasbackend.serviceinterfaces.OperacionService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba el controlador sin base de datos: el servicio se reemplaza por un simulado
@WebMvcTest(TiendaController.class)
class TiendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TiendaService tiendaService;

    @MockitoBean
    private OperacionService operacionService;

    @Test
    void crearConTelefonoInvalidoDevuelve400ConDetalle() throws Exception {
        String json = """
                {"nombreTienda":"Divinas Salón & Spa","direccion":"Av. Surco 1167",
                 "telefono":"123","usuarioAdmin":"Cecilia_Paola","passwordAdmin":"sixsevencito@123"}
                """;

        mockMvc.perform(post("/api/tiendas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detalles.telefono").exists());
    }

    @Test
    void crearValidoDevuelve201SinPassword() throws Exception {
        String json = """
                {"nombreTienda":"Divinas Salón & Spa","direccion":"Av. Surco 1167",
                 "telefono":"955988231","usuarioAdmin":"Cecilia_Paola","passwordAdmin":"sixsevencito@123"}
                """;

        mockMvc.perform(post("/api/tiendas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreTienda").value("Divinas Salón & Spa"))
                .andExpect(jsonPath("$.passwordAdmin").doesNotExist());
    }

    @Test
    void tiendaInexistenteDevuelve404() throws Exception {
        when(tiendaService.listId(99)).thenReturn(null);

        mockMvc.perform(get("/api/tiendas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }

    @Test
    void obtenerExistenteDevuelveLosDatos() throws Exception {
        Tienda t = new Tienda(1, "Divinas Salón & Spa", "Av. Surco 1167", "955988231",
                "Cecilia_Paola", "hash", true);
        when(tiendaService.listId(1)).thenReturn(t);

        mockMvc.perform(get("/api/tiendas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTienda").value(1))
                .andExpect(jsonPath("$.usuarioAdmin").value("Cecilia_Paola"))
                .andExpect(jsonPath("$.passwordAdmin").doesNotExist());
    }
}
