package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Operacion;
import com.upc.divinasbackend.repositories.OperacionRepository;
import com.upc.divinasbackend.serviceinterfaces.OperacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperacionServiceImplement implements OperacionService {

    private static final Logger log = LoggerFactory.getLogger(OperacionServiceImplement.class);

    @Autowired
    private OperacionRepository oR;

    @Override
    public void registrar(String usuario, String rol, Integer idTienda, String accion, String metodo, String ruta, int codigoHttp) {
        try {
            oR.save(new Operacion(LocalDateTime.now(), recortar(usuario, 100), rol, idTienda, accion, metodo,
                    recortar(ruta, 255), codigoHttp));
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar la operación '{}'", accion, e);
        }
    }

    @Override
    public List<Operacion> listar(Integer idTienda, int limite) {
        PageRequest pagina = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fechaHora", "idOperacion"));
        if (idTienda == null) {
            return oR.findAll(pagina).getContent();
        }
        return oR.findByIdTienda(idTienda, pagina);
    }

    private String recortar(String texto, int maximo) {
        if (texto == null || texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo);
    }
}
