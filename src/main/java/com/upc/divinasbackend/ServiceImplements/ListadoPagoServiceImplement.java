package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.dtos.ItemListadoPagoDTO;
import com.upc.divinasbackend.dtos.ListadoPagoDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.finanzas.FilaCuota;
import com.upc.divinasbackend.finanzas.PlanDePagos;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.CompraService;
import com.upc.divinasbackend.serviceinterfaces.CuotaService;
import com.upc.divinasbackend.serviceinterfaces.ListadoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ListadoPagoServiceImplement implements ListadoPagoService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    @Autowired
    private CuotaService cuS;

    @Autowired
    private CompraService coS;

    @Autowired
    private ClienteService cS;

    @Override
    public ListadoPagoDTO generar(Cliente cliente, LocalDate fechaPago) {
        List<Cuota> pendientes = cuS.listByCliente(cliente.getIdCliente()).stream()
                .filter(c -> "Pendiente".equals(c.getEstado()))
                .toList();
        LocalDate fecha = fechaPago != null ? fechaPago
                : pendientes.stream().map(Cuota::getFechaVencimiento).min(Comparator.naturalOrder()).orElse(null);
        return armar(cliente, fecha, pendientes);
    }

    @Override
    public List<ListadoPagoDTO> generarTienda(int idTienda, LocalDate fechaPago) {
        LocalDate fecha = fechaPago;
        if (fecha == null) {
            fecha = cuS.proximosVencimientos(idTienda).stream()
                    .map(Cuota::getFechaVencimiento).findFirst().orElse(null);
        }
        LocalDate fechaFinal = fecha;
        return cS.listByTienda(idTienda).stream()
                .filter(c -> Boolean.TRUE.equals(c.getEstado()))
                .map(c -> generar(c, fechaFinal))
                .filter(l -> !l.detalle().isEmpty())
                .toList();
    }

    private ListadoPagoDTO armar(Cliente cliente, LocalDate fecha, List<Cuota> pendientes) {
        if (fecha == null) {
            return new ListadoPagoDTO(cliente.getIdCliente(), cliente.getNombreCliente(), cliente.getFechaCorte(),
                    null, CERO, CERO, List.of());
        }
        List<Cuota> delCiclo = pendientes.stream()
                .filter(c -> fecha.equals(c.getFechaVencimiento()))
                .sorted(Comparator.comparing((Cuota c) -> c.getCompra().getFechaCompra())
                        .thenComparing(c -> c.getCompra().getIdCompra())
                        .thenComparing(Cuota::getNumeroCuota))
                .toList();

        List<ItemListadoPagoDTO> detalle = delCiclo.stream().map(c -> aItem(cliente, c)).toList();
        BigDecimal totalAPagar = detalle.stream().map(ItemListadoPagoDTO::montoCuota).reduce(CERO, BigDecimal::add);
        BigDecimal totalIntereses = detalle.stream()
                .map(i -> i.interesGracia().add(i.interesCuota()))
                .reduce(CERO, BigDecimal::add);
        return new ListadoPagoDTO(cliente.getIdCliente(), cliente.getNombreCliente(), cliente.getFechaCorte(),
                fecha, totalIntereses, totalAPagar, detalle);
    }

    private ItemListadoPagoDTO aItem(Cliente cliente, Cuota cuota) {
        Compra compra = cuota.getCompra();
        PlanDePagos plan = coS.planDe(compra, cliente);
        FilaCuota fila = plan.cuotas().get(cuota.getNumeroCuota() - 1);
        boolean primera = cuota.getNumeroCuota() == 1;
        return new ItemListadoPagoDTO(compra.getIdCompra(), compra.getProductoServicio().getDescripcion(),
                compra.getFechaCompra(), compra.getMonto(), compra.getFormaPago(), cuota.getNumeroCuota(),
                plan.cuotas().size(), primera ? plan.diasGracia() : 0, primera ? plan.interesGracia() : CERO,
                fila.interes(), cuota.getMontoCuota());
    }
}
