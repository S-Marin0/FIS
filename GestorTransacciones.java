package servicios;

import java.util.*;
import java.util.stream.Collectors;
import modelos.Transaccion;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestorTransacciones {
    private static final Logger LOGGER = Logger.getLogger(GestorTransacciones.class.getName());
    private List<Transaccion> transacciones; // Opera en memoria, no usa DAO

    public GestorTransacciones() {
        this.transacciones = new ArrayList<>();
        LOGGER.info("GestorTransacciones (en memoria) inicializado.");
    }

    public void agregarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            LOGGER.warning("Intento de agregar transacción null.");
            return;
        }
        LOGGER.log(Level.INFO, "Agregando transacción (en memoria): Tipo={0}, Desc=''{1}'', Monto={2}, Cat=''{3}''",
            new Object[]{transaccion.getTipo(), transaccion.getDescripcion(), transaccion.getMonto(), transaccion.getCategoria()});
        transacciones.add(transaccion);
        LOGGER.fine("Transacción agregada. Total actual (en memoria): " + transacciones.size());
    }

    public List<Transaccion> obtenerTransacciones() {
        LOGGER.log(Level.INFO, "Obteniendo todas las transacciones (en memoria). Total: {0}", transacciones.size());
        return new ArrayList<>(transacciones); // Devuelve copia para evitar modificación externa
    }

    public List<Transaccion> obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion tipo) {
        LOGGER.log(Level.INFO, "Obteniendo transacciones (en memoria) por tipo: {0}", tipo);
        List<Transaccion> resultado = transacciones.stream()
                .filter(t -> t.getTipo() == tipo)
                .collect(Collectors.toList());
        LOGGER.log(Level.FINE, "Encontradas {0} transacciones de tipo {1}", new Object[]{resultado.size(), tipo});
        return resultado;
    }

    public Map<String, Double> obtenerGastosPorCategoria() {
        LOGGER.info("Calculando gastos por categoría (en memoria).");
        Map<String, Double> resultado = transacciones.stream()
                .filter(t -> t.getTipo() == Transaccion.TipoTransaccion.GASTO)
                .collect(Collectors.groupingBy(
                    Transaccion::getCategoria,
                    Collectors.summingDouble(Transaccion::getMonto)
                ));
        LOGGER.log(Level.FINE, "Cálculo de gastos por categoría completado. {0} categorías encontradas.", resultado.size());
        return resultado;
    }

    public double obtenerTotalIngresos() {
        LOGGER.info("Calculando total de ingresos (en memoria).");
        double total = transacciones.stream()
                .filter(t -> t.getTipo() == Transaccion.TipoTransaccion.INGRESO)
                .mapToDouble(Transaccion::getMonto)
                .sum();
        LOGGER.log(Level.FINE, "Total de ingresos calculado: {0}", total);
        return total;
    }

    public double obtenerTotalGastos() {
        LOGGER.info("Calculando total de gastos (en memoria).");
        double total = transacciones.stream()
                .filter(t -> t.getTipo() == Transaccion.TipoTransaccion.GASTO)
                .mapToDouble(Transaccion::getMonto)
                .sum();
        LOGGER.log(Level.FINE, "Total de gastos calculado: {0}", total);
        return total;
    }

    public double obtenerBalance() {
        LOGGER.info("Calculando balance (en memoria).");
        double balance = obtenerTotalIngresos() - obtenerTotalGastos();
        LOGGER.log(Level.FINE, "Balance calculado: {0}", balance);
        return balance;
    }
}
