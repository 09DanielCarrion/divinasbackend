package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Operacion;

import java.util.List;

public interface OperacionService {
    public void registrar(String usuario, String rol, Integer idTienda, String accion, String metodo, String ruta, int codigoHttp);
    public List<Operacion> listar(Integer idTienda, int limite);
}
