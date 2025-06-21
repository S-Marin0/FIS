package servicios;

import database.dao.TransaccionDAO; // Importar DAO
import modelos.Transaccion;
import java.sql.SQLException; // Importar SQLException
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
// Quitar import java.util.stream.Collectors; si ya no se usa para operaciones en memoria

public class GestorTransacciones {
    private static final Logger LOGGER = Logger.getLogger(GestorTransacciones.class.getName());
    private TransaccionDAO transaccionDAO;

    public GestorTransacciones() {
        this.transaccionDAO = new TransaccionDAO(); // Inicializar el DAO
        LOGGER.info("GestorTransacciones inicializado con TransaccionDAO.");
    }

    public boolean agregarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            LOGGER.warning("Gestor: Intento de agregar transacción null.");
            return false; // Indicar fallo
        }
        LOGGER.log(Level.INFO, "Gestor: Intentando agregar transacción: Tipo={0}, Desc=''{1}'', Monto={2}, Cat=''{3}''",
            new Object[]{transaccion.getTipo(), transaccion.getDescripcion(), transaccion.getMonto(), transaccion.getCategoria()});
        try {
            transaccionDAO.insertarTransaccion(transaccion);
            LOGGER.info("Gestor: Transacción pasada a DAO para inserción.");
            return true; // Indicar éxito
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al agregar transacción.", e);
            return false; // Indicar fallo
        }
    }

    public List<Transaccion> obtenerTransacciones() {
        LOGGER.info("Gestor: Intentando obtener todas las transacciones desde DAO.");
        try {
            List<Transaccion> transacciones = transaccionDAO.obtenerTodasTransacciones();
            LOGGER.log(Level.INFO, "Gestor: Obtenidas {0} transacciones desde DAO.", transacciones != null ? transacciones.size() : "null (error en DAO)");
            return transacciones;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener todas las transacciones.", e);
            return Collections.emptyList(); // Devolver lista vacía en caso de error
        }
    }

    public List<Transaccion> obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion tipo) {
        LOGGER.log(Level.INFO, "Gestor: Intentando obtener transacciones por tipo: {0} desde DAO.", tipo);
        try {
            List<Transaccion> transacciones = transaccionDAO.obtenerTransaccionesPorTipo(tipo);
            LOGGER.log(Level.INFO, "Gestor: Obtenidas {0} transacciones tipo {1} desde DAO.", new Object[]{transacciones != null ? transacciones.size() : "null", tipo});
            return transacciones;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener transacciones por tipo " + tipo, e);
            return Collections.emptyList();
        }
    }

    public Map<String, Double> obtenerGastosPorCategoria() {
        LOGGER.info("Gestor: Intentando obtener gastos por categoría desde DAO.");
        try {
            Map<String, Double> gastos = transaccionDAO.obtenerGastosPorCategoria();
            LOGGER.log(Level.INFO, "Gestor: Obtenidos gastos para {0} categorías desde DAO.", gastos != null ? gastos.size() : "null");
            return gastos;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener gastos por categoría.", e);
            return Collections.emptyMap();
        }
    }

    public double obtenerTotalIngresos() {
        LOGGER.info("Gestor: Intentando obtener total de ingresos desde DAO.");
        try {
            double total = transaccionDAO.obtenerTotalIngresos();
            LOGGER.log(Level.INFO, "Gestor: Total de ingresos obtenido desde DAO: {0}", total);
            return total;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener total de ingresos.", e);
            return 0.0;
        }
    }

    public double obtenerTotalGastos() {
        LOGGER.info("Gestor: Intentando obtener total de gastos desde DAO.");
        try {
            double total = transaccionDAO.obtenerTotalGastos();
            LOGGER.log(Level.INFO, "Gestor: Total de gastos obtenido desde DAO: {0}", total);
            return total;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener total de gastos.", e);
            return 0.0;
        }
    }

    public double obtenerBalance() {
        LOGGER.info("Gestor: Calculando balance (usando DAO para ingresos/gastos).");
        // Estos métodos ahora llaman al DAO
        double ingresos = obtenerTotalIngresos();
        double gastos = obtenerTotalGastos();
        double balance = ingresos - gastos;
        LOGGER.log(Level.INFO, "Gestor: Balance calculado: {0} (Ingresos: {1}, Gastos: {2})", new Object[]{balance, ingresos, gastos});
        return balance;
    }

    // Método para eliminar transacción, si se necesita exponer a través de la fachada
    public boolean eliminarTransaccion(int idTransaccion) {
        LOGGER.log(Level.INFO, "Gestor: Intentando eliminar transacción con ID: {0}", idTransaccion);
        try {
            transaccionDAO.eliminarTransaccion(idTransaccion);
            LOGGER.log(Level.INFO, "Gestor: Solicitud de eliminación para transacción ID {0} pasada a DAO.", idTransaccion);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al eliminar transacción ID " + idTransaccion, e);
            return false;
        }
    }
}
