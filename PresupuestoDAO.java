package database.dao;

import database.DatabaseConnection;
import modelos.Presupuesto;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {
    
    public void insertarPresupuesto(Presupuesto presupuesto) throws SQLException {
        String sql = "INSERT INTO presupuestos (nombre, mes, año) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, presupuesto.getNombre());
            stmt.setInt(2, presupuesto.getMes());
            stmt.setInt(3, presupuesto.getAño());
            
            stmt.executeUpdate();
            
            // Obtener el ID generado para insertar las categorías
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int presupuestoId = generatedKeys.getInt(1);
                    insertarCategorias(presupuestoId, presupuesto);
                }
            }
        }
    }
    
    private void insertarCategorias(int presupuestoId, Presupuesto presupuesto) throws SQLException {
        String sql = "INSERT INTO categorias_presupuesto (presupuesto_id, categoria, limite_monto, gasto_actual) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (String categoria : presupuesto.getLimitesPorCategoria().keySet()) {
                stmt.setInt(1, presupuestoId);
                stmt.setString(2, categoria);
                stmt.setDouble(3, presupuesto.getLimitesPorCategoria().get(categoria));
                stmt.setDouble(4, presupuesto.getGastosPorCategoria().getOrDefault(categoria, 0.0));
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    public List<Presupuesto> obtenerTodosPresupuestos() throws SQLException {
        List<Presupuesto> presupuestos = new ArrayList<>();
        String sql = """
            SELECT p.*, c.categoria, c.limite_monto, c.gasto_actual 
            FROM presupuestos p 
            LEFT JOIN categorias_presupuesto c ON p.id = c.presupuesto_id 
            ORDER BY p.año DESC, p.mes DESC, p.nombre
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Presupuesto presupuestoActual = null;
            String nombreActual = null;
            
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int mes = rs.getInt("mes");
                int año = rs.getInt("año");
                
                // Si es un nuevo presupuesto, crear uno nuevo
                if (presupuestoActual == null || !nombre.equals(nombreActual)) {
                    if (presupuestoActual != null) {
                        presupuestos.add(presupuestoActual);
                    }
                    presupuestoActual = new Presupuesto(nombre, mes, año);
                    nombreActual = nombre;
                }
                
                // Agregar categoría si existe
                String categoria = rs.getString("categoria");
                if (categoria != null) {
                    double limite = rs.getDouble("limite_monto");
                    double gasto = rs.getDouble("gasto_actual");
                    
                    presupuestoActual.agregarLimiteCategoria(categoria, limite);
                    presupuestoActual.registrarGasto(categoria, gasto);
                }
            }
            
            // Agregar el último presupuesto
            if (presupuestoActual != null) {
                presupuestos.add(presupuestoActual);
            }
        }
        
        return presupuestos;
    }
    
    public Presupuesto obtenerPresupuestoActual() throws SQLException {
        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        int añoActual = ahora.getYear();
        
        String sql = """
            SELECT p.*, c.categoria, c.limite_monto, c.gasto_actual 
            FROM presupuestos p 
            LEFT JOIN categorias_presupuesto c ON p.id = c.presupuesto_id 
            WHERE p.mes = ? AND p.año = ?
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, mesActual);
            stmt.setInt(2, añoActual);
            
            try (ResultSet rs = stmt.executeQuery()) {
                Presupuesto presupuesto = null;
                
                while (rs.next()) {
                    if (presupuesto == null) {
                        presupuesto = new Presupuesto(
                            rs.getString("nombre"),
                            rs.getInt("mes"),
                            rs.getInt("año")
                        );
                    }
                    
                    String categoria = rs.getString("categoria");
                    if (categoria != null) {
                        double limite = rs.getDouble("limite_monto");
                        double gasto = rs.getDouble("gasto_actual");
                        
                        presupuesto.agregarLimiteCategoria(categoria, limite);
                        presupuesto.registrarGasto(categoria, gasto);
                    }
                }
                
                return presupuesto;
            }
        }
    }
    
    public void agregarCategoriaAPresupuesto(String nombrePresupuesto, String categoria, double limite) throws SQLException {
        String sqlPresupuesto = "SELECT id FROM presupuestos WHERE nombre = ?";
        String sqlCategoria = "INSERT INTO categorias_presupuesto (presupuesto_id, categoria, limite_monto, gasto_actual) VALUES (?, ?, ?, 0.0)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtPresupuesto = conn.prepareStatement(sqlPresupuesto)) {
            
            stmtPresupuesto.setString(1, nombrePresupuesto);
            
            try (ResultSet rs = stmtPresupuesto.executeQuery()) {
                if (rs.next()) {
                    int presupuestoId = rs.getInt("id");
                    
                    try (PreparedStatement stmtCategoria = conn.prepareStatement(sqlCategoria)) {
                        stmtCategoria.setInt(1, presupuestoId);
                        stmtCategoria.setString(2, categoria);
                        stmtCategoria.setDouble(3, limite);
                        
                        stmtCategoria.executeUpdate();
                    }
                } else {
                    throw new SQLException("No se encontró el presupuesto: " + nombrePresupuesto);
                }
            }
        }
    }
    
    public void registrarGastoEnPresupuesto(String categoria, double monto) throws SQLException {
        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        int añoActual = ahora.getYear();
        
        String sql = """
            UPDATE categorias_presupuesto c 
            JOIN presupuestos p ON c.presupuesto_id = p.id 
            SET c.gasto_actual = c.gasto_actual + ? 
            WHERE c.categoria = ? AND p.mes = ? AND p.año = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, monto);
            stmt.setString(2, categoria);
            stmt.setInt(3, mesActual);
            stmt.setInt(4, añoActual);
            
            stmt.executeUpdate();
        }
    }
    
    public void eliminarPresupuesto(String nombrePresupuesto) throws SQLException {
        String sql = "DELETE FROM presupuestos WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombrePresupuesto);
            stmt.executeUpdate();
        }
    }
}