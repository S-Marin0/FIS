package database.dao;

import database.DatabaseConnection;
import modelos.MetaFinanciera;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MetaDAO {
    
    public void insertarMeta(MetaFinanciera meta) throws SQLException {
        String sql = "INSERT INTO metas_financieras (nombre, monto_objetivo, monto_actual, fecha_limite, descripcion, completada) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, meta.getNombre());
            stmt.setDouble(2, meta.getMontoObjetivo());
            stmt.setDouble(3, meta.getMontoActual());
            stmt.setDate(4, Date.valueOf(meta.getFechaLimite()));
            stmt.setString(5, meta.getDescripcion());
            stmt.setBoolean(6, meta.estaCompletada());
            
            stmt.executeUpdate();
        }
    }
    
    public List<MetaFinanciera> obtenerTodasMetas() throws SQLException {
        List<MetaFinanciera> metas = new ArrayList<>();
        String sql = "SELECT * FROM metas_financieras ORDER BY fecha_limite ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                MetaFinanciera meta = new MetaFinanciera(
                    rs.getString("nombre"),
                    rs.getDouble("monto_objetivo"),
                    rs.getDate("fecha_limite").toLocalDate(),
                    rs.getString("descripcion")
                );
                meta.setMontoActual(rs.getDouble("monto_actual"));
                metas.add(meta);
            }
        }
        
        return metas;
    }
    
    public List<MetaFinanciera> obtenerMetasCompletadas() throws SQLException {
        List<MetaFinanciera> metas = new ArrayList<>();
        String sql = "SELECT * FROM metas_financieras WHERE completada = TRUE ORDER BY fecha_limite ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                MetaFinanciera meta = new MetaFinanciera(
                    rs.getString("nombre"),
                    rs.getDouble("monto_objetivo"),
                    rs.getDate("fecha_limite").toLocalDate(),
                    rs.getString("descripcion")
                );
                meta.setMontoActual(rs.getDouble("monto_actual"));
                metas.add(meta);
            }
        }
        
        return metas;
    }
    
    public List<MetaFinanciera> obtenerMetasEnProgreso() throws SQLException {
        List<MetaFinanciera> metas = new ArrayList<>();
        String sql = "SELECT * FROM metas_financieras WHERE completada = FALSE ORDER BY fecha_limite ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                MetaFinanciera meta = new MetaFinanciera(
                    rs.getString("nombre"),
                    rs.getDouble("monto_objetivo"),
                    rs.getDate("fecha_limite").toLocalDate(),
                    rs.getString("descripcion")
                );
                meta.setMontoActual(rs.getDouble("monto_actual"));
                metas.add(meta);
            }
        }
        
        return metas;
    }
    
    public void actualizarMontoMeta(String nombreMeta, double nuevoMonto) throws SQLException {
        String sql = "UPDATE metas_financieras SET monto_actual = ?, completada = (monto_actual >= monto_objetivo) WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, nuevoMonto);
            stmt.setString(2, nombreMeta);
            
            stmt.executeUpdate();
        }
    }
    
    public void contribuirAMeta(String nombreMeta, double montoContribucion) throws SQLException {
        String sql = "UPDATE metas_financieras SET monto_actual = monto_actual + ?, completada = (monto_actual + ? >= monto_objetivo) WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, montoContribucion);
            stmt.setDouble(2, montoContribucion);
            stmt.setString(3, nombreMeta);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró una meta con el nombre: " + nombreMeta);
            }
        }
    }
    
    public void eliminarMeta(String nombreMeta) throws SQLException {
        String sql = "DELETE FROM metas_financieras WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombreMeta);
            stmt.executeUpdate();
        }
    }
    
    public MetaFinanciera obtenerMetaPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM metas_financieras WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MetaFinanciera meta = new MetaFinanciera(
                        rs.getString("nombre"),
                        rs.getDouble("monto_objetivo"),
                        rs.getDate("fecha_limite").toLocalDate(),
                        rs.getString("descripcion")
                    );
                    meta.setMontoActual(rs.getDouble("monto_actual"));
                    return meta;
                }
            }
        }
        
        return null;
    }
}
