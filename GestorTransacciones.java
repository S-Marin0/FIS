package servicios;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import database.dao.TransaccionDAO;
import database.dao.PresupuestoDAO;
import modelos.Transaccion;

public class GestorTransacciones {
    private TransaccionDAO transaccionDAO;
    private PresupuestoDAO presupuestoDAO;
    
    public GestorTransacciones() {
        this.transaccionDAO = new TransaccionDAO();
        this.presupuestoDAO = new PresupuestoDAO();
    }
    
    /**
     * Agrega una nueva transacción a la base de datos
     * Si es un gasto, también actualiza el presupuesto correspondiente
     */
    public void agregarTransaccion(Transaccion transaccion) throws SQLException {
        // Insertar la transacción
        transaccionDAO.insertarTransaccion(transaccion);
        
        // Si es un gasto, actualizar el presupuesto
        if (transaccion.getTipo() == Transaccion.TipoTransaccion.GASTO) {
            try {
                presupuestoDAO.registrarGastoEnPresupuesto(
                    transaccion.getCategoria(), 
                    transaccion.getMonto()
                );
            } catch (SQLException e) {
                // El presupuesto podría no existir, pero la transacción sí se registra
                System.out.println("Advertencia: No se pudo actualizar el presupuesto - " + e.getMessage());
            }
        }
    }
    
    /**
     * Obtiene todas las transacciones de la base de datos
     */
    public List<Transaccion> obtenerTransacciones() throws SQLException {
        return transaccionDAO.obtenerTodasTransacciones();
    }
    
    /**
     * Obtiene transacciones filtradas por tipo (INGRESO o GASTO)
     */
    public List<Transaccion> obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion tipo) throws SQLException {
        return transaccionDAO.obtenerTransaccionesPorTipo(tipo);
    }
    
    /**
     * Obtiene un mapa con los gastos agrupados por categoría
     */
    public Map<String, Double> obtenerGastosPorCategoria() throws SQLException {
        return transaccionDAO.obtenerGastosPorCategoria();
    }
    
    /**
     * Obtiene el total de ingresos registrados
     */
    public double obtenerTotalIngresos() throws SQLException {
        return transaccionDAO.obtenerTotalIngresos();
    }
    
    /**
     * Obtiene el total de gastos registrados
     */
    public double obtenerTotalGastos() throws SQLException {
        return transaccionDAO.obtenerTotalGastos();
    }
    
    /**
     * Calcula el balance (ingresos - gastos)
     */
    public double obtenerBalance() throws SQLException {
        return obtenerTotalIngresos() - obtenerTotalGastos();
    }
    
    /**
     * Elimina una transacción por su ID
     */
    public void eliminarTransaccion(int id) throws SQLException {
        transaccionDAO.eliminarTransaccion(id);
    }
    
    /**
     * Obtiene solo los ingresos
     */
    public List<Transaccion> obtenerIngresos() throws SQLException {
        return obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion.INGRESO);
    }
    
    /**
     * Obtiene solo los gastos
     */
    public List<Transaccion> obtenerGastos() throws SQLException {
        return obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion.GASTO);
    }
    
    /**
     * Verifica si hay transacciones registradas
     */
    public boolean hayTransacciones() throws SQLException {
        return !obtenerTransacciones().isEmpty();
    }
}
