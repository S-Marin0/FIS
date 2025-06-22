package database.dao;

import database.DatabaseConnection;
import modelos.Presupuesto;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PresupuestoDAO {
    private static final Logger LOGGER = Logger.getLogger(PresupuestoDAO.class.getName());

    public void insertarPresupuesto(Presupuesto presupuesto) throws SQLException {
        LOGGER.log(Level.INFO, "Intentando insertar presupuesto: {0}", presupuesto.getNombre());
        String sql = "INSERT INTO presupuestos (nombre, mes, año) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, presupuesto.getNombre());
            stmt.setInt(2, presupuesto.getMes());
            stmt.setInt(3, presupuesto.getAño());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Inserción de presupuesto fallida, no se afectaron filas para: {0}", presupuesto.getNombre());
                throw new SQLException("No se pudo insertar el presupuesto, no se afectaron filas.");
            }
            LOGGER.log(Level.INFO, "Presupuesto base insertado para: {0}", presupuesto.getNombre());

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int presupuestoId = generatedKeys.getInt(1);
                    LOGGER.log(Level.INFO, "ID generado para presupuesto {0}: {1}", new Object[]{presupuesto.getNombre(), presupuestoId});
                    if (presupuesto.getLimitesPorCategoria() != null && !presupuesto.getLimitesPorCategoria().isEmpty()) {
                        insertarCategorias(presupuestoId, presupuesto);
                    } else {
                        LOGGER.log(Level.INFO, "Presupuesto {0} no tiene categorías para insertar.", presupuesto.getNombre());
                    }
                } else {
                    LOGGER.log(Level.WARNING, "No se pudo obtener ID generado para presupuesto: {0}", presupuesto.getNombre());
                    throw new SQLException("No se pudo obtener ID generado para el presupuesto.");
                }
            }
            LOGGER.log(Level.INFO, "Presupuesto {0} insertado completamente.", presupuesto.getNombre());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al insertar presupuesto " + presupuesto.getNombre(), e);
            throw e;
        }
    }

    private void insertarCategorias(int presupuestoId, Presupuesto presupuesto) throws SQLException {
        LOGGER.log(Level.INFO, "Insertando categorías para presupuesto ID: {0}", presupuestoId);
        String sql = "INSERT INTO categorias_presupuesto (presupuesto_id, categoria, limite_monto, gasto_actual) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); // Podría reutilizar la conexión del método padre si se pasa como parámetro
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (presupuesto.getLimitesPorCategoria().isEmpty()) {
                LOGGER.log(Level.INFO, "No hay categorías para agregar al presupuesto ID: {0}", presupuestoId);
                return;
            }

            for (String categoria : presupuesto.getLimitesPorCategoria().keySet()) {
                LOGGER.log(Level.FINE, "Agregando al batch categoría: {0} para presupuesto ID: {1}", new Object[]{categoria, presupuestoId});
                stmt.setInt(1, presupuestoId);
                stmt.setString(2, categoria);
                stmt.setDouble(3, presupuesto.getLimitesPorCategoria().get(categoria));
                stmt.setDouble(4, presupuesto.getGastosPorCategoria().getOrDefault(categoria, 0.0));
                stmt.addBatch();
            }

            int[] batchResults = stmt.executeBatch();
            LOGGER.log(Level.INFO, "Batch de inserción de categorías ejecutado para presupuesto ID: {0}. Resultados: {1}", new Object[]{presupuestoId, batchResults.length});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al insertar categorías para presupuesto ID " + presupuestoId, e);
            throw e;
        }
    }

    public List<Presupuesto> obtenerTodosPresupuestos() throws SQLException {
        LOGGER.info("Intentando obtener todos los presupuestos.");
        List<Presupuesto> presupuestos = new ArrayList<>();
        String sql = """
            SELECT p.id as presupuesto_id, p.nombre, p.mes, p.año, c.categoria, c.limite_monto, c.gasto_actual
            FROM presupuestos p
            LEFT JOIN categorias_presupuesto c ON p.id = c.presupuesto_id
            ORDER BY p.año DESC, p.mes DESC, p.nombre, p.id
            """; // Añadido p.id al ORDER BY para consistencia si hay nombres duplicados en diferentes meses/años

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Presupuesto presupuestoActual = null;
            int currentPresupuestoId = -1;

            while (rs.next()) {
                int presupuestoId = rs.getInt("presupuesto_id");
                if (presupuestoActual == null || presupuestoId != currentPresupuestoId) {
                    if (presupuestoActual != null) {
                        presupuestos.add(presupuestoActual);
                        LOGGER.log(Level.FINER, "Presupuesto agregado a la lista: {0}", presupuestoActual.getNombre());
                    }
                    String nombre = rs.getString("nombre");
                    int mes = rs.getInt("mes");
                    int año = rs.getInt("año");
                    presupuestoActual = new Presupuesto(nombre, mes, año);
                    // presupuestoActual.setId(presupuestoId); // Si el modelo Presupuesto tuviera un campo ID
                    currentPresupuestoId = presupuestoId;
                    LOGGER.log(Level.FINER, "Procesando nuevo presupuesto: {0} (ID: {1})", new Object[]{nombre, presupuestoId});
                }

                String categoria = rs.getString("categoria");
                if (categoria != null && presupuestoActual != null) {
                    double limite = rs.getDouble("limite_monto");
                    double gasto = rs.getDouble("gasto_actual");
                    presupuestoActual.agregarLimiteCategoria(categoria, limite);
                    // El gasto se registra como parte del límite, no directamente como gasto separado aquí.
                    // Si la tabla `categorias_presupuesto` tiene `gasto_actual`, se puede setear.
                    // Asumiendo que el modelo `Presupuesto` tiene un método para registrar el gasto inicial de la categoría.
                    presupuestoActual.registrarGasto(categoria, gasto);
                    LOGGER.log(Level.FINEST, "Agregada categoría {0} (Límite: {1}, Gasto: {2}) a presupuesto {3}", new Object[]{categoria, limite, gasto, presupuestoActual.getNombre()});
                }
            }

            if (presupuestoActual != null) {
                presupuestos.add(presupuestoActual);
                LOGGER.log(Level.FINER, "Último presupuesto agregado a la lista: {0}", presupuestoActual.getNombre());
            }
            LOGGER.log(Level.INFO, "Obtenidos {0} presupuestos.", presupuestos.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al obtener todos los presupuestos.", e);
            throw e;
        }
        return presupuestos;
    }

    public Presupuesto obtenerPresupuestoActual() throws SQLException {
        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        int añoActual = ahora.getYear();
        LOGGER.log(Level.INFO, "Intentando obtener presupuesto actual para Mes: {0}, Año: {1}", new Object[]{mesActual, añoActual});

        String sql = """
            SELECT p.id as presupuesto_id, p.nombre, p.mes, p.año, c.categoria, c.limite_monto, c.gasto_actual
            FROM presupuestos p
            LEFT JOIN categorias_presupuesto c ON p.id = c.presupuesto_id
            WHERE p.mes = ? AND p.año = ?
            ORDER BY p.id, c.categoria
            """;
            // Asumimos que puede haber múltiples presupuestos para el mismo mes/año (aunque la UI no lo facilita)
            // Si solo debe haber uno, se podría añadir LIMIT 1 a la query de presupuesto y luego cargar categorías.
            // La lógica actual de la UI parece implicar un solo presupuesto por mes/año por el nombre en txtNombrePresupuesto.
            // Pero la tabla `presupuestos` tiene `UNIQUE KEY unique_presupuesto (nombre, mes, año)`
            // Esto significa que puede haber varios presupuestos en un mes/año si tienen nombres diferentes.
            // Este método DAO podría necesitar un nombre de presupuesto si se quiere uno específico.
            // Por ahora, tomará el primero que encuentre y sus categorías. Si hay varios, solo procesará el primero.

        Presupuesto presupuesto = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mesActual);
            stmt.setInt(2, añoActual);

            LOGGER.fine("Ejecutando consulta para presupuesto actual.");
            try (ResultSet rs = stmt.executeQuery()) {
                int currentPresupuestoId = -1;
                while (rs.next()) {
                    int presupuestoId = rs.getInt("presupuesto_id");
                    if (presupuesto == null) { // Solo procesa el primer presupuesto encontrado para el mes/año
                        String nombre = rs.getString("nombre");
                        presupuesto = new Presupuesto(nombre, rs.getInt("mes"), rs.getInt("año"));
                        // presupuesto.setId(presupuestoId);
                        currentPresupuestoId = presupuestoId;
                         LOGGER.log(Level.INFO, "Presupuesto actual encontrado: {0} (ID: {1})", new Object[]{nombre, presupuestoId});
                    }
                     // Asegurarse de que la categoría pertenece al presupuesto que se está procesando
                    if (presupuesto != null && presupuestoId == currentPresupuestoId) {
                        String categoria = rs.getString("categoria");
                        if (categoria != null) {
                            double limite = rs.getDouble("limite_monto");
                            double gasto = rs.getDouble("gasto_actual");
                            presupuesto.agregarLimiteCategoria(categoria, limite);
                            presupuesto.registrarGasto(categoria, gasto);
                            LOGGER.log(Level.FINEST, "Agregada categoría {0} a presupuesto actual {1}", new Object[]{categoria, presupuesto.getNombre()});
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al obtener presupuesto actual.", e);
            throw e;
        }

        if (presupuesto != null) {
            LOGGER.log(Level.INFO, "Presupuesto actual obtenido: {0}", presupuesto.getNombre());
        } else {
            LOGGER.log(Level.INFO, "No se encontró presupuesto para el mes/año actual ({0}/{1}).", new Object[]{mesActual, añoActual});
        }
        return presupuesto;
    }

    public void agregarCategoriaAPresupuesto(String nombrePresupuesto, int mes, int año, String categoriaNombre, double limite) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando agregar categoría ''{0}'' (Límite: {1}) al presupuesto ''{2}'' ({3}/{4})",
                   new Object[]{categoriaNombre, limite, nombrePresupuesto, mes, año});

        String sqlPresupuesto = "SELECT id FROM presupuestos WHERE nombre = ? AND mes = ? AND año = ?";
        String sqlCategoria = "INSERT INTO categorias_presupuesto (presupuesto_id, categoria, limite_monto, gasto_actual) VALUES (?, ?, ?, 0.0)";

        Connection conn = null;
        PreparedStatement stmtPresupuesto = null;
        PreparedStatement stmtCategoria = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            stmtPresupuesto = conn.prepareStatement(sqlPresupuesto);
            stmtPresupuesto.setString(1, nombrePresupuesto);
            stmtPresupuesto.setInt(2, mes);
            stmtPresupuesto.setInt(3, año);
            LOGGER.fine("DAO: Buscando ID para presupuesto: " + nombrePresupuesto + ", Mes: " + mes + ", Año: " + año);
            rs = stmtPresupuesto.executeQuery();

            if (rs.next()) {
                int presupuestoId = rs.getInt("id");
                LOGGER.log(Level.INFO, "DAO: Presupuesto ''{0}'' ({1}/{2}) encontrado con ID: {3}. Agregando categoría ''{4}''.",
                           new Object[]{nombrePresupuesto, mes, año, presupuestoId, categoriaNombre});

                stmtCategoria = conn.prepareStatement(sqlCategoria);
                stmtCategoria.setInt(1, presupuestoId);
                stmtCategoria.setString(2, categoriaNombre);
                stmtCategoria.setDouble(3, limite);

                int affectedRows = stmtCategoria.executeUpdate();
                if (affectedRows == 0) {
                    LOGGER.log(Level.WARNING, "Inserción de categoría ''{0}'' fallida, no se afectaron filas.", categoriaNombre);
                    throw new SQLException("No se pudo insertar la categoría.");
                }
                conn.commit(); // Confirmar transacción
                LOGGER.log(Level.INFO, "DAO: Categoría ''{0}'' agregada exitosamente al presupuesto ID: {1}", new Object[]{categoriaNombre, presupuestoId});
            } else {
                LOGGER.log(Level.WARNING, "DAO: No se encontró el presupuesto con nombre: {0}, Mes: {1}, Año: {2}", new Object[]{nombrePresupuesto, mes, año});
                throw new SQLException("No se encontró el presupuesto: " + nombrePresupuesto + " para " + mes + "/" + año);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al agregar categoría '" + categoriaNombre + "' a presupuesto '" + nombrePresupuesto + "' ("+mes+"/"+año+").", e);
            if (conn != null) {
                try {
                    LOGGER.info("Intentando rollback de transacción...");
                    conn.rollback();
                    LOGGER.info("Rollback de transacción realizado.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error durante el rollback de transacción.", ex);
                }
            }
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { LOGGER.log(Level.FINEST, "Error cerrando ResultSet", e); }
            if (stmtCategoria != null) try { stmtCategoria.close(); } catch (SQLException e) { LOGGER.log(Level.FINEST, "Error cerrando PreparedStatement de categoría", e); }
            if (stmtPresupuesto != null) try { stmtPresupuesto.close(); } catch (SQLException e) { LOGGER.log(Level.FINEST, "Error cerrando PreparedStatement de presupuesto", e); }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar auto-commit
                    // La conexión es de DatabaseConnection, no la cerramos aquí directamente.
                    // conn.close();
                } catch (SQLException e) { LOGGER.log(Level.FINEST, "Error restaurando auto-commit", e); }
            }
        }
    }

    public void registrarGastoEnPresupuesto(String categoria, double monto) throws SQLException {
        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        int añoActual = ahora.getYear();
        LOGGER.log(Level.INFO, "Registrando gasto de {0} en categoría ''{1}'' para presupuesto de {2}/{3}", new Object[]{monto, categoria, mesActual, añoActual});

        // Esta query asume que solo hay UN presupuesto por mes/año, o que se aplica a todos los que coincidan.
        // Si hay varios presupuestos con esa categoría en el mes/año, se actualizarán todos.
        // Sería más preciso si se pasa el ID del presupuesto o nombre del presupuesto.
        String sql = """
            UPDATE categorias_presupuesto cp
            JOIN presupuestos p ON cp.presupuesto_id = p.id
            SET cp.gasto_actual = cp.gasto_actual + ?
            WHERE cp.categoria = ? AND p.mes = ? AND p.año = ?
            """;
            // AND p.nombre = ? (si se quisiera especificar el nombre del presupuesto)

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, monto);
            stmt.setString(2, categoria);
            stmt.setInt(3, mesActual);
            stmt.setInt(4, añoActual);
            // stmt.setString(5, nombrePresupuesto); // Si se añade filtro por nombre

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "{0} filas afectadas al registrar gasto en categoría ''{1}''.", new Object[]{affectedRows, categoria});
            } else {
                LOGGER.log(Level.WARNING, "No se afectaron filas al registrar gasto en categoría ''{0}'' para {1}/{2}. ¿Existe la categoría en un presupuesto de este mes/año?", new Object[]{categoria, mesActual, añoActual});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al registrar gasto en categoría " + categoria, e);
            throw e;
        }
    }

    public void eliminarPresupuesto(String nombre, int mes, int anio) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando eliminar presupuesto: Nombre=''{0}'', Mes={1}, Año={2}", new Object[]{nombre, mes, anio});
        // Asumimos que la tabla categorias_presupuesto tiene una FK a presupuestos.id con ON DELETE CASCADE
        // para que al eliminar un presupuesto, sus categorías asociadas también se eliminen.
        // Si no, se necesitaría eliminar primero las categorías explícitamente.
        String sql = "DELETE FROM presupuestos WHERE nombre = ? AND mes = ? AND año = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setInt(2, mes);
            stmt.setInt(3, anio);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Presupuesto ''{0}'' ({1}/{2}) eliminado. Filas afectadas: {3}. (Categorías asociadas también deberían eliminarse si hay ON DELETE CASCADE)", new Object[]{nombre, mes, anio, affectedRows});
            } else {
                 LOGGER.log(Level.WARNING, "DAO: No se encontró presupuesto ''{0}'' ({1}/{2}) para eliminar.", new Object[]{nombre, mes, anio});
                 // Considerar lanzar una excepción si se espera que siempre exista para eliminar.
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al eliminar presupuesto " + nombre + " (" + mes + "/" + anio + ").", e);
            throw e;
        }
    }
}
