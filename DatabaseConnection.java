package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/finanzas_personales";
    private static final String USER = "root";
    private static final String PASSWORD = "santiago123"; // Cambiar por tu contraseña
    
    private static Connection connection = null;
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión establecida con la base de datos MySQL");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL no encontrado", e);
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Crear tabla transacciones
            String createTransacciones = """
                CREATE TABLE IF NOT EXISTS transacciones (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    descripcion VARCHAR(255) NOT NULL,
                    monto DECIMAL(10,2) NOT NULL,
                    categoria VARCHAR(100) NOT NULL,
                    tipo ENUM('INGRESO', 'GASTO') NOT NULL,
                    fecha DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.executeUpdate(createTransacciones);
            
            // Crear tabla metas_financieras
            String createMetas = """
                CREATE TABLE IF NOT EXISTS metas_financieras (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(255) NOT NULL,
                    monto_objetivo DECIMAL(10,2) NOT NULL,
                    monto_actual DECIMAL(10,2) DEFAULT 0.00,
                    fecha_limite DATE NOT NULL,
                    descripcion TEXT,
                    completada BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.executeUpdate(createMetas);
            
            // Crear tabla presupuestos
            String createPresupuestos = """
                CREATE TABLE IF NOT EXISTS presupuestos (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(255) NOT NULL,
                    mes INT NOT NULL,
                    año INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_presupuesto (nombre, mes, año)
                )
                """;
            stmt.executeUpdate(createPresupuestos);
            
            // Crear tabla categorias_presupuesto
            String createCategorias = """
                CREATE TABLE IF NOT EXISTS categorias_presupuesto (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    presupuesto_id INT NOT NULL,
                    categoria VARCHAR(100) NOT NULL,
                    limite_monto DECIMAL(10,2) NOT NULL,
                    gasto_actual DECIMAL(10,2) DEFAULT 0.00,
                    FOREIGN KEY (presupuesto_id) REFERENCES presupuestos(id) ON DELETE CASCADE,
                    UNIQUE KEY unique_categoria (presupuesto_id, categoria)
                )
                """;
            stmt.executeUpdate(createCategorias);
            
            System.out.println("Base de datos inicializada correctamente");
            
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
