package database.dao;

import database.DatabaseConnection;
import modelos.Transaccion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransaccionDAO {
    
    public void insertarTransaccion(Transaccion transaccion) throws SQLException {
        String sql = "INSERT INTO transacciones (descripcion, monto, categoria, tipo, fecha) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, transaccion.getDescripcion());
            stmt.setDouble(2, transaccion.getMonto());
            stmt.setString(3, transaccion.getCategoria());
            stmt.setString(4, transaccion.getTipo().name());
            stmt.setDate(5, Date.valueOf(transaccion.getFecha()));
            
            stmt.executeUpdate();
        }
    }
    
    public List<Transaccion> obtenerTodasTransacciones() throws SQLException {
        List<Transaccion> transacciones = new ArrayList<>();
        String sql = "SELECT * FROM transacciones ORDER BY fecha DESC, id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Transaccion transaccion = new Transaccion(
                    rs.getString("descripcion"),
                    rs.getDouble("monto"),
                    rs.getString("categoria"),
                    Transaccion.TipoTransaccion.valueOf(rs.getString("tipo"))
                );
                transaccion.setFecha(rs.getDate("fecha").toLocalDate());
                transacciones.add(transaccion);
            }
        }
        
        return transacciones;
    }
    
    public List<Transaccion> obtenerTransaccionesPorTipo(Transaccion.TipoTransaccion tipo) throws SQLException {
        List<Transaccion> transacciones = new ArrayList<>();
        String sql = "SELECT * FROM transacciones WHERE tipo = ? ORDER BY fecha DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaccion transaccion = new Transaccion(
                        rs.getString("descripcion"),
                        rs.getDouble("monto"),
                        rs.getString("categoria"),
                        Transaccion.TipoTransaccion.valueOf(rs.getString("tipo"))
                    );
                    transaccion.setFecha(rs.getDate("fecha").toLocalDate());
                    transacciones.add(transaccion);
                }
            }
        }
        
        return transacciones;
    }
    
    public Map<String, Double> obtenerGastosPorCategoria() throws SQLException {
        Map<String, Double> gastosPorCategoria = new HashMap<>();
        String sql = "SELECT categoria, SUM(monto) as total FROM transacciones WHERE tipo = 'GASTO' GROUP BY categoria";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                gastosPorCategoria.put(rs.getString("categoria"), rs.getDouble("total"));
            }
        }
        
        return gastosPorCategoria;
    }
    
    public double obtenerTotalIngresos() throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM transacciones WHERE tipo = 'INGRESO'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        
        return 0.0;
    }
    
    public double obtenerTotalGastos() throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM transacciones WHERE tipo = 'GASTO'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        
        return 0.0;
    }
    
    public void eliminarTransaccion(int id) throws SQLException {
        String sql = "DELETE FROM transacciones WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
