package database.dao;

import database.DatabaseConnection;
import modelos.MetaFinanciera;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetaDAO {
    private static final Logger LOGGER = Logger.getLogger(MetaDAO.class.getName());

    public void insertarMeta(MetaFinanciera meta) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando insertar meta: Nombre=''{0}'', Objetivo={1}, Actual={2}, FechaLimite={3}, Completada={4}",
            new Object[]{meta.getNombre(), meta.getMontoObjetivo(), meta.getMontoActual(), meta.getFechaLimite(), meta.estaCompletada()});
        String sql = "INSERT INTO metas_financieras (nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, meta.getNombre());
            stmt.setDouble(2, meta.getMontoObjetivo());
            stmt.setDouble(3, meta.getMontoActual());
            stmt.setDate(4, Date.valueOf(meta.getFechaLimite()));
            stmt.setString(5, meta.getDescripcion());
            stmt.setBoolean(6, meta.estaCompletada());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Meta ''{0}'' insertada exitosamente. Filas afectadas: {1}", new Object[]{meta.getNombre(), affectedRows});
            } else {
                LOGGER.log(Level.WARNING, "DAO: Inserción de meta ''{0}'' no afectó filas.", meta.getNombre());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al insertar meta " + meta.getNombre(), e);
            throw e;
        }
    }

    private MetaFinanciera mapResultSetToMeta(ResultSet rs) throws SQLException {
        // Helper para no repetir mapeo
        MetaFinanciera meta = new MetaFinanciera(
            rs.getString("nombre"),
            rs.getDouble("monto_objetivo"),
            rs.getDate("fecha_limite").toLocalDate(),
            rs.getString("descripcion")
        );
        meta.setMontoActual(rs.getDouble("monto_actual"));
        // meta.setCompletada(rs.getBoolean("completada")); // Asumiendo que el constructor o setMontoActual actualiza esto.
                                                        // El modelo MetaFinanciera debería manejar su estado 'completada' internamente
                                                        // basado en monto_actual vs monto_objetivo.
                                                        // Si la BD es la fuente de verdad para 'completada', entonces hay que setearlo.
                                                        // El constructor de MetaFinanciera no toma 'completada'.
                                                        // MetaFinanciera.estaCompletada() lo calcula.
                                                        // MetaFinanciera.agregarMonto() lo actualiza.
                                                        // Así que no es necesario setear 'completada' desde la BD directamente aquí,
                                                        // a menos que la lógica de 'completada' en BD sea diferente.
        LOGGER.log(Level.FINEST, "DAO: Meta mapeada: Nombre=''{0}'', Objetivo={1}, Actual={2}, FechaLimite={3}",
            new Object[]{meta.getNombre(), meta.getMontoObjetivo(), meta.getMontoActual(), meta.getFechaLimite()});
        return meta;
    }

    public List<MetaFinanciera> obtenerTodasMetas() throws SQLException {
        LOGGER.info("DAO: Intentando obtener todas las metas.");
        List<MetaFinanciera> metas = new ArrayList<>();
        String sql = "SELECT id, nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada FROM metas_financieras ORDER BY fecha_limite ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                metas.add(mapResultSetToMeta(rs));
                count++;
            }
            LOGGER.log(Level.INFO, "DAO: Obtenidas {0} metas.", count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener todas las metas.", e);
            throw e;
        }
        return metas;
    }

    public List<MetaFinanciera> obtenerMetasCompletadas() throws SQLException {
        LOGGER.info("DAO: Intentando obtener metas completadas.");
        List<MetaFinanciera> metas = new ArrayList<>();
        String sql = "SELECT id, nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada FROM metas_financieras WHERE completada = TRUE ORDER BY fecha_limite ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                metas.add(mapResultSetToMeta(rs));
                count++;
            }
            LOGGER.log(Level.INFO, "DAO: Obtenidas {0} metas completadas.", count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener metas completadas.", e);
            throw e;
        }
        return metas;
    }

    public List<MetaFinanciera> obtenerMetasEnProgreso() throws SQLException {
        LOGGER.info("DAO: Intentando obtener metas en progreso.");
        List<MetaFinanciera> metas = new ArrayList<>();
        String sql = "SELECT id, nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada FROM metas_financieras WHERE completada = FALSE ORDER BY fecha_limite ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                metas.add(mapResultSetToMeta(rs));
                count++;
            }
            LOGGER.log(Level.INFO, "DAO: Obtenidas {0} metas en progreso.", count);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener metas en progreso.", e);
            throw e;
        }
        return metas;
    }

    public void actualizarMontoMeta(String nombreMeta, double nuevoMonto) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando actualizar monto de meta ''{0}'' a {1}", new Object[]{nombreMeta, nuevoMonto});
        // La columna 'completada' se actualiza en la BD basado en si monto_actual (que será nuevoMonto) >= monto_objetivo
        String sql = "UPDATE metas_financieras SET monto_actual = ?, completada = (CASE WHEN ? >= monto_objetivo THEN TRUE ELSE FALSE END) WHERE nombre = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuevoMonto);
            stmt.setDouble(2, nuevoMonto); // Para la comparación en CASE WHEN
            stmt.setString(3, nombreMeta);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Monto de meta ''{0}'' actualizado. Filas afectadas: {1}", new Object[]{nombreMeta, affectedRows});
            } else {
                LOGGER.log(Level.WARNING, "DAO: No se encontró meta ''{0}'' para actualizar monto.", nombreMeta);
                // Considerar lanzar una excepción si se espera que la meta siempre exista
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al actualizar monto de meta " + nombreMeta, e);
            throw e;
        }
    }

    public void contribuirAMeta(String nombreMeta, double montoContribucion) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando contribuir {0} a meta ''{1}''", new Object[]{montoContribucion, nombreMeta});
        // La columna 'completada' se actualiza en la BD basado en si (monto_actual + montoContribucion) >= monto_objetivo
        String sql = "UPDATE metas_financieras SET monto_actual = monto_actual + ?, completada = (CASE WHEN (monto_actual + ?) >= monto_objetivo THEN TRUE ELSE FALSE END) WHERE nombre = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, montoContribucion);
            stmt.setDouble(2, montoContribucion); // Para la comparación en CASE WHEN
            stmt.setString(3, nombreMeta);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Contribución a meta ''{0}'' realizada. Filas afectadas: {1}", new Object[]{nombreMeta, affectedRows});
            } else {
                LOGGER.log(Level.WARNING, "DAO: No se encontró una meta con el nombre ''{0}'' para contribuir.", nombreMeta);
                throw new SQLException("No se encontró una meta con el nombre: " + nombreMeta + " para contribuir.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al contribuir a meta " + nombreMeta, e);
            throw e;
        }
    }

    public void eliminarMeta(String nombreMeta) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando eliminar meta ''{0}''", nombreMeta);
        String sql = "DELETE FROM metas_financieras WHERE nombre = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreMeta);
            int affectedRows = stmt.executeUpdate();
             if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "DAO: Meta ''{0}'' eliminada. Filas afectadas: {1}", new Object[]{nombreMeta, affectedRows});
            } else {
                LOGGER.log(Level.WARNING, "DAO: No se encontró meta ''{0}'' para eliminar.", nombreMeta);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al eliminar meta " + nombreMeta, e);
            throw e;
        }
    }

    public MetaFinanciera obtenerMetaPorNombre(String nombre) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando obtener meta por nombre: ''{0}''", nombre);
        String sql = "SELECT id, nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada FROM metas_financieras WHERE nombre = ?";
        MetaFinanciera meta = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    meta = mapResultSetToMeta(rs);
                    LOGGER.log(Level.INFO, "DAO: Meta ''{0}'' encontrada.", nombre);
                } else {
                    LOGGER.log(Level.INFO, "DAO: No se encontró meta con nombre ''{0}''.", nombre);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener meta por nombre " + nombre, e);
            throw e;
        }
        return meta;
    }

    public List<MetaFinanciera> obtenerMetasProximas(LocalDate fechaReferencia, int diasProximidad) throws SQLException {
        LOGGER.log(Level.INFO, "DAO: Intentando obtener metas próximas a {0} (dentro de {1} días).", new Object[]{fechaReferencia, diasProximidad});
        List<MetaFinanciera> metasProximas = new ArrayList<>();
        LocalDate fechaLimiteSuperior = fechaReferencia.plusDays(diasProximidad);
        // Queremos metas no completadas cuya fecha límite esté entre hoy (o fechaReferencia) y la fecha límite superior.
        String sql = "SELECT id, nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada " +
                     "FROM metas_financieras " +
                     "WHERE completada = FALSE AND fecha_limite >= ? AND fecha_limite <= ? " +
                     "ORDER BY fecha_limite ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaReferencia));
            stmt.setDate(2, Date.valueOf(fechaLimiteSuperior));

            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    metasProximas.add(mapResultSetToMeta(rs));
                    count++;
                }
                LOGGER.log(Level.INFO, "DAO: Obtenidas {0} metas próximas.", count);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DAO: Error SQL al obtener metas próximas.", e);
            throw e;
        }
        return metasProximas;
    }
}
