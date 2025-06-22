package database.dao;

import database.DatabaseConnection;
import modelos.Transaccion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransaccionDAO {
    private static final Logger LOGGER = Logger.getLogger(TransaccionDAO.class.getName());

    public void insertarTransaccion(Transaccion transaccion) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando insertar transacción: Tipo={0}, Desc=''{1}'', Monto={2}, Cat=''{3}'', Fecha={4}",
            new Object[]{transaccion.getTipo(), transaccion.getDescripcion(), transaccion.getMonto(), transaccion.getCategoria(), transaccion.getFecha()});
        String sql = "INSERT INTO transacciones (descripcion, monto, categoria, tipo, fecha) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, transaccion.getDescripcion());
            stmt.setDouble(2, transaccion.getMonto());
            stmt.setString(3, transaccion.getCategoria());
            stmt.setString(4, transaccion.getTipo().name());
            stmt.setDate(5, Date.valueOf(transaccion.getFecha()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Transacción insertada exitosamente. Filas afectadas: {0}", affectedRows);
            } else {
                LOGGER.log(Level.WARNING, "DAO: Inserción de transacción no afectó filas.");
                // Considerar lanzar una excepción si esto se considera un error crítico
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al insertar transacción.", e);
            throw e;
        }
    }

    public List<Transaccion> obtenerTodasTransacciones() throws SQLException {
        LOGGER.info("DAO: Intentando obtener todas las transacciones.");
        List<Transaccion> transacciones = new ArrayList<>();
        String sql = "SELECT id, descripcion, monto, categoria, tipo, fecha FROM transacciones ORDER BY fecha DESC, id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                Transaccion transaccion = new Transaccion(
                    rs.getString("descripcion"),
                    rs.getDouble("monto"),
                    rs.getString("categoria"),
                    Transaccion.TipoTransaccion.valueOf(rs.getString("tipo"))
                );
                transaccion.setId(rs.getInt("id")); // Establecer el ID de la transacción
                transaccion.setFecha(rs.getDate("fecha").toLocalDate());
                transacciones.add(transaccion);
                count++;
                LOGGER.log(Level.FINEST, "DAO: Transacción mapeada: ID={0}, Desc=''{1}''", new Object[]{transaccion.getId(), transaccion.getDescripcion()});
            }
            LOGGER.log(Level.INFO, "DAO: Obtenidas {0} transacciones.", count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener todas las transacciones.", e);
            throw e;
        }
        return transacciones;
    }

    public List<Transaccion> obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion tipo) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando obtener transacciones por tipo: {0}", tipo);
        List<Transaccion> transacciones = new ArrayList<>();
        String sql = "SELECT id, descripcion, monto, categoria, tipo, fecha FROM transacciones WHERE tipo = ? ORDER BY fecha DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());
            LOGGER.fine("DAO: Ejecutando consulta para transacciones tipo " + tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                     Transaccion transaccion = new Transaccion(
                        rs.getString("descripcion"),
                        rs.getDouble("monto"),
                        rs.getString("categoria"),
                        Transaccion.TipoTransaccion.valueOf(rs.getString("tipo"))
                    );
                    transaccion.setId(rs.getInt("id")); // Establecer el ID de la transacción
                    transaccion.setFecha(rs.getDate("fecha").toLocalDate());
                    transacciones.add(transaccion);
                    count++;
                    LOGGER.log(Level.FINEST, "DAO: Transacción (tipo {0}) mapeada: ID={1}", new Object[]{tipo, transaccion.getId()});
                }
                LOGGER.log(Level.INFO, "DAO: Obtenidas {0} transacciones de tipo {1}.", new Object[]{count, tipo});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener transacciones por tipo " + tipo, e);
            throw e;
        }
        return transacciones;
    }

    public Map<String, Double> obtenerGastosPorCategoria() throws SQLException {
        LOGGER.info("DAO: Intentando obtener gastos por categoría.");
        Map<String, Double> gastosPorCategoria = new HashMap<>();
        String sql = "SELECT categoria, SUM(monto) as total FROM transacciones WHERE tipo = 'GASTO' GROUP BY categoria";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                String categoria = rs.getString("categoria");
                double total = rs.getDouble("total");
                gastosPorCategoria.put(categoria, total);
                count++;
                LOGGER.log(Level.FINEST, "DAO: Gasto por categoría: Cat=''{0}'', Total={1}", new Object[]{categoria, total});
            }
            LOGGER.log(Level.INFO, "DAO: Obtenidos gastos para {0} categorías.", count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener gastos por categoría.", e);
            throw e;
        }
        return gastosPorCategoria;
    }

    public double obtenerTotalIngresos() throws SQLException {
        LOGGER.info("DAO: Intentando obtener total de ingresos.");
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM transacciones WHERE tipo = 'INGRESO'";
        double totalIngresos = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                totalIngresos = rs.getDouble("total");
            }
            LOGGER.log(Level.INFO, "DAO: Total de ingresos obtenido: {0}", totalIngresos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener total de ingresos.", e);
            throw e;
        }
        return totalIngresos;
    }

    public double obtenerTotalGastos() throws SQLException {
        LOGGER.info("DAO: Intentando obtener total de gastos.");
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM transacciones WHERE tipo = 'GASTO'";
        double totalGastos = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                totalGastos = rs.getDouble("total");
            }
            LOGGER.log(Level.INFO, "DAO: Total de gastos obtenido: {0}", totalGastos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener total de gastos.", e);
            throw e;
        }
        return totalGastos;
    }

    public double obtenerTotalIngresosDelMes(int mes, int año) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando obtener total de ingresos para Mes: {0}, Año: {1}", new Object[]{mes, año});
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM transacciones WHERE tipo = 'INGRESO' AND MONTH(fecha) = ? AND YEAR(fecha) = ?";
        double totalIngresos = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mes);
            stmt.setInt(2, año);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalIngresos = rs.getDouble("total");
                }
            }
            LOGGER.log(Level.INFO, "DAO: Total de ingresos para {0}/{1} obtenido: {2}", new Object[]{mes, año, totalIngresos});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener total de ingresos para " + mes + "/" + año, e);
            throw e;
        }
        return totalIngresos;
    }

    public double obtenerTotalGastosDelMes(int mes, int año) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando obtener total de gastos para Mes: {0}, Año: {1}", new Object[]{mes, año});
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM transacciones WHERE tipo = 'GASTO' AND MONTH(fecha) = ? AND YEAR(fecha) = ?";
        double totalGastos = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mes);
            stmt.setInt(2, año);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalGastos = rs.getDouble("total");
                }
            }
            LOGGER.log(Level.INFO, "DAO: Total de gastos para {0}/{1} obtenido: {2}", new Object[]{mes, año, totalGastos});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener total de gastos para " + mes + "/" + año, e);
            throw e;
        }
        return totalGastos;
    }

    public Map<String, Double> obtenerGastosPorCategoriaDelMes(int mes, int año) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando obtener gastos por categoría para Mes: {0}, Año: {1}", new Object[]{mes, año});
        Map<String, Double> gastosPorCategoria = new HashMap<>();
        String sql = "SELECT categoria, SUM(monto) as total FROM transacciones WHERE tipo = 'GASTO' AND MONTH(fecha) = ? AND YEAR(fecha) = ? GROUP BY categoria";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, mes);
            stmt.setInt(2, año);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String categoria = rs.getString("categoria");
                    double total = rs.getDouble("total");
                    gastosPorCategoria.put(categoria, total);
                    count++;
                    LOGGER.log(Level.FINEST, "DAO: Gasto por categoría ({0}/{1}): Cat=''{2}'', Total={3}", new Object[]{mes, año, categoria, total});
                }
                LOGGER.log(Level.INFO, "DAO: Obtenidos gastos para {0} categorías en {1}/{2}.", new Object[]{count, mes, año});
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener gastos por categoría para " + mes + "/" + año, e);
            throw e;
        }
        return gastosPorCategoria;
    }

    public void eliminarTransaccion(int id) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando eliminar transacción con ID: {0}", id);
        String sql = "DELETE FROM transacciones WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Transacción con ID {0} eliminada. Filas afectadas: {1}", new Object[]{id, affectedRows});
            } else {
                LOGGER.log(Level.WARNING, "DAO: No se encontró transacción con ID {0} para eliminar.", id);
                // Considerar lanzar una excepción si se espera que siempre exista
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al eliminar transacción con ID " + id, e);
            throw e;
        }
    }
}
