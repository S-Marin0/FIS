package servicios;

import java.util.*;
import java.util.stream.Collectors;
import modelos.Transaccion;

public class GestorTransacciones {
    private List<Transaccion> transacciones;
    
    public GestorTransacciones() {
        this.transacciones = new ArrayList<>();
    }
    
    public void agregarTransaccion(Transaccion transaccion) {
        transacciones.add(transaccion);
    }
    
    public List<Transaccion> obtenerTransacciones() {
        return new ArrayList<>(transacciones);
    }
    
    public List<Transaccion> obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion tipo) {
        return transacciones.stream()
                .filter(t -> t.getTipo() == tipo)
                .collect(Collectors.toList());
    }
    
    public Map<String, Double> obtenerGastosPorCategoria() {
        return transacciones.stream()
                .filter(t -> t.getTipo() == Transaccion.TipoTransaccion.GASTO)
                .collect(Collectors.groupingBy(
                    Transaccion::getCategoria,
                    Collectors.summingDouble(Transaccion::getMonto)
                ));
    }
    
    public double obtenerTotalIngresos() {
        return transacciones.stream()
                .filter(t -> t.getTipo() == Transaccion.TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto)
                .sum();
    }
    
    public double obtenerTotalGastos() {
        return transacciones.stream()
                .filter(t -> t.getTipo() == Transaccion.TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto)
                .sum();
    }
    
    public double obtenerBalance() {
        return obtenerTotalIngresos() - obtenerTotalGastos();
    }
}
