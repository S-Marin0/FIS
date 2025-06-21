package servicios;

import database.dao.PresupuestoDAO;
import modelos.Presupuesto;
import java.sql.SQLException;
import java.util.List;

public class GestorPresupuestos {
    private PresupuestoDAO presupuestoDAO;

    public GestorPresupuestos() {
        this.presupuestoDAO = new PresupuestoDAO();
    }

    public void agregarPresupuesto(Presupuesto presupuesto) {
        try {
            presupuestoDAO.insertarPresupuesto(presupuesto);
        } catch (SQLException e) {
            System.err.println("Error al agregar presupuesto: " + e.getMessage());
            e.printStackTrace();
            // Considerar lanzar una RuntimeException o una excepción de servicio específica
        }
    }

    public List<Presupuesto> obtenerPresupuestos() {
        try {
            return presupuestoDAO.obtenerTodosPresupuestos();
        } catch (SQLException e) {
            System.err.println("Error al obtener presupuestos: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Devolver lista vacía en caso de error
        }
    }

    public Presupuesto obtenerPresupuestoActual() {
        try {
            return presupuestoDAO.obtenerPresupuestoActual();
        } catch (SQLException e) {
            System.err.println("Error al obtener presupuesto actual: " + e.getMessage());
            e.printStackTrace();
            return null; // Devolver null en caso de error
        }
    }

    public void registrarGastoEnPresupuesto(String categoria, double monto) {
        try {
            presupuestoDAO.registrarGastoEnPresupuesto(categoria, monto);
        } catch (SQLException e) {
            System.err.println("Error al registrar gasto en presupuesto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void agregarCategoria(String nombrePresupuesto, String categoria, double limiteMonto) throws SQLException {
        // Propagar SQLException para que la fachada y la UI puedan manejarla
        presupuestoDAO.agregarCategoriaAPresupuesto(nombrePresupuesto, categoria, limiteMonto);
    }
}
