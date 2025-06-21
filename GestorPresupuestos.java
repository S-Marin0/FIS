package servicios;

import database.dao.PresupuestoDAO;
import modelos.Presupuesto;
import java.sql.SQLException;
import java.util.List;
import java.util.Collections; // Para List.of() en versiones antiguas, o Collections.emptyList()
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestorPresupuestos {
    private static final Logger LOGGER = Logger.getLogger(GestorPresupuestos.class.getName());
    private PresupuestoDAO presupuestoDAO;

    public GestorPresupuestos() {
        this.presupuestoDAO = new PresupuestoDAO();
        LOGGER.info("GestorPresupuestos inicializado con PresupuestoDAO.");
    }

    public void agregarPresupuesto(Presupuesto presupuesto) {
        LOGGER.log(Level.INFO, "Gestor: Intentando agregar presupuesto: {0}", presupuesto != null ? presupuesto.getNombre() : "null");
        if (presupuesto == null) {
            LOGGER.warning("Gestor: Intento de agregar un presupuesto null.");
            return; // O lanzar IllegalArgumentException
        }
        try {
            presupuestoDAO.insertarPresupuesto(presupuesto);
            LOGGER.log(Level.INFO, "Gestor: Presupuesto {0} pasado a DAO para inserción.", presupuesto.getNombre());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al agregar presupuesto " + presupuesto.getNombre(), e);
            // Considerar relanzar como una excepción de servicio personalizada
            // throw new ServicioException("Error al agregar presupuesto", e);
        }
    }

    public List<Presupuesto> obtenerPresupuestos() {
        LOGGER.info("Gestor: Intentando obtener todos los presupuestos.");
        try {
            List<Presupuesto> presupuestos = presupuestoDAO.obtenerTodosPresupuestos();
            LOGGER.log(Level.INFO, "Gestor: Obtenidos {0} presupuestos desde DAO.", presupuestos != null ? presupuestos.size() : "null (error en DAO)");
            return presupuestos;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener todos los presupuestos.", e);
            return Collections.emptyList(); // Devolver lista vacía en caso de error
        }
    }

    public Presupuesto obtenerPresupuestoActual() {
        LOGGER.info("Gestor: Intentando obtener presupuesto actual.");
        try {
            Presupuesto presupuesto = presupuestoDAO.obtenerPresupuestoActual();
            if (presupuesto != null) {
                LOGGER.log(Level.INFO, "Gestor: Presupuesto actual obtenido desde DAO: {0}", presupuesto.getNombre());
            } else {
                LOGGER.info("Gestor: No se encontró presupuesto actual según DAO.");
            }
            return presupuesto;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener presupuesto actual.", e);
            return null; // Devolver null en caso de error
        }
    }

    public void registrarGastoEnPresupuesto(String categoria, double monto) {
        LOGGER.log(Level.INFO, "Gestor: Intentando registrar gasto de {0} en categoría ''{1}'' en presupuesto actual.", new Object[]{monto, categoria});
        if (categoria == null || categoria.trim().isEmpty()) {
            LOGGER.warning("Gestor: Intento de registrar gasto con categoría null o vacía.");
            return;
        }
        try {
            presupuestoDAO.registrarGastoEnPresupuesto(categoria, monto);
            LOGGER.log(Level.INFO, "Gestor: Registro de gasto para categoría ''{0}'' pasado a DAO.", categoria);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al registrar gasto en categoría " + categoria, e);
        }
    }

    public void agregarCategoria(String nombrePresupuesto, String categoria, double limiteMonto) throws SQLException {
        LOGGER.log(Level.INFO, "Gestor: Intentando agregar categoría ''{0}'' al presupuesto ''{1}'' con límite {2}", new Object[]{categoria, nombrePresupuesto, limiteMonto});
        if (nombrePresupuesto == null || nombrePresupuesto.trim().isEmpty() ||
            categoria == null || categoria.trim().isEmpty()) {
            LOGGER.warning("Gestor: Intento de agregar categoría con nombre de presupuesto o categoría null/vacío.");
            throw new IllegalArgumentException("Nombre de presupuesto y categoría no pueden ser vacíos.");
        }
        try {
            presupuestoDAO.agregarCategoriaAPresupuesto(nombrePresupuesto, categoria, limiteMonto);
            LOGGER.log(Level.INFO, "Gestor: Solicitud para agregar categoría ''{0}'' a presupuesto ''{1}'' pasada a DAO.", new Object[]{categoria, nombrePresupuesto});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al agregar categoría ''" + categoria + "'' a presupuesto ''" + nombrePresupuesto + "''. Propagando excepción.", e);
            throw e; // Propagar SQLException para que la fachada y la UI puedan manejarla
        }
    }
}
