package servicios;

import java.sql.SQLException;
import java.util.List;
import database.dao.PresupuestoDAO;
import modelos.Presupuesto;

public class GestorPresupuestos {
    private PresupuestoDAO presupuestoDAO;
    
    public GestorPresupuestos() {
        this.presupuestoDAO = new PresupuestoDAO();
    }
    
    /**
     * Agrega un nuevo presupuesto a la base de datos
     */
    public void agregarPresupuesto(Presupuesto presupuesto) throws SQLException {
        presupuestoDAO.insertarPresupuesto(presupuesto);
    }
    
    /**
     * Obtiene todos los presupuestos de la base de datos
     */
    public List<Presupuesto> obtenerPresupuestos() throws SQLException {
        return presupuestoDAO.obtenerTodosPresupuestos();
    }
    
    /**
     * Obtiene el presupuesto del mes y año actual
     */
    public Presupuesto obtenerPresupuestoActual() throws SQLException {
        return presupuestoDAO.obtenerPresupuestoActual();
    }
    
    /**
     * Registra un gasto en el presupuesto actual
     */
    public void registrarGastoEnPresupuesto(String categoria, double monto) throws SQLException {
        presupuestoDAO.registrarGastoEnPresupuesto(categoria, monto);
    }
    
    /**
     * Agrega una nueva categoría a un presupuesto existente
     */
    public void agregarCategoriaAPresupuesto(String nombrePresupuesto, String categoria, double limite) throws SQLException {
        presupuestoDAO.agregarCategoriaAPresupuesto(nombrePresupuesto, categoria, limite);
    }
    
    /**
     * Elimina un presupuesto de la base de datos
     */
    public void eliminarPresupuesto(String nombrePresupuesto) throws SQLException {
        presupuestoDAO.eliminarPresupuesto(nombrePresupuesto);
    }
    
    /**
     * Verifica si existe un presupuesto para el mes y año actuales
     */
    public boolean existePresupuestoActual() throws SQLException {
        return obtenerPresupuestoActual() != null;
    }
    
    /**
     * Obtiene el total presupuestado para el mes actual
     */
    public double getTotalPresupuestadoActual() throws SQLException {
        Presupuesto presupuestoActual = obtenerPresupuestoActual();
        return presupuestoActual != null ? presupuestoActual.getTotalPresupuestado() : 0.0;
    }
    
    /**
     * Obtiene el total gastado del presupuesto actual
     */
    public double getTotalGastadoActual() throws SQLException {
        Presupuesto presupuestoActual = obtenerPresupuestoActual();
        return presupuestoActual != null ? presupuestoActual.getTotalGastado() : 0.0;
    }
    
    /**
     * Verifica si alguna categoría del presupuesto actual excede su límite
     */
    public boolean hayCategoriasExcedidas() throws SQLException {
        Presupuesto presupuestoActual = obtenerPresupuestoActual();
        if (presupuestoActual == null) return false;
        
        return presupuestoActual.getLimitesPorCategoria().keySet().stream()
                .anyMatch(categoria -> presupuestoActual.excedeLimite(categoria));
    }
}
