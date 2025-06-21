package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger; // Importar Logger

public class DatabaseConnection {
    // Logger para esta clase
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String URL = "jdbc:mysql://localhost:3306/finanzas_personales";
    private static final String USER = "root";
    private static final String PASSWORD = "santiago123"; // Cambiar por tu contraseña

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        LOGGER.info("Intentando obtener conexión a la base de datos...");
        if (connection == null || connection.isClosed()) {
            LOGGER.info("No hay conexión existente o está cerrada. Creando nueva conexión.");
            try {
                LOGGER.fine("Cargando driver MySQL: com.mysql.cj.jdbc.Driver");
                Class.forName("com.mysql.cj.jdbc.Driver");
                LOGGER.fine("Driver cargado. Conectando a URL: " + URL + " con usuario: " + USER);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                LOGGER.info("Conexión establecida exitosamente con la base de datos MySQL.");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Driver MySQL no encontrado.", e);
                throw new SQLException("Driver MySQL no encontrado", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al establecer la conexión SQL.", e);
                throw e; // Relanzar la excepción original
            }
        } else {
            LOGGER.info("Reutilizando conexión existente a la base de datos.");
        }
        return connection;
    }

    public static void closeConnection() {
        LOGGER.info("Intentando cerrar la conexión a la base de datos...");
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Conexión a la base de datos cerrada exitosamente.");
                } else {
                    LOGGER.info("La conexión ya estaba cerrada.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al cerrar la conexión a la base de datos.", e);
            } finally {
                connection = null; // Asegurar que se limpie la referencia
            }
        } else {
            LOGGER.info("No había conexión activa para cerrar.");
        }
    }

    public static void initializeDatabase() {
        LOGGER.info("Iniciando el proceso de inicialización de la base de datos...");
        // Usar una conexión local para la inicialización para asegurar su cierre
        try (Connection initConn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = initConn.createStatement()) {

            LOGGER.info("Conexión para inicialización establecida.");

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
            LOGGER.fine("Ejecutando creación de tabla transacciones: \n" + createTransacciones);
            stmt.executeUpdate(createTransacciones);
            LOGGER.info("Tabla 'transacciones' verificada/creada.");

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
            LOGGER.fine("Ejecutando creación de tabla metas_financieras: \n" + createMetas);
            stmt.executeUpdate(createMetas);
            LOGGER.info("Tabla 'metas_financieras' verificada/creada.");

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
            LOGGER.fine("Ejecutando creación de tabla presupuestos: \n" + createPresupuestos);
            stmt.executeUpdate(createPresupuestos);
            LOGGER.info("Tabla 'presupuestos' verificada/creada.");

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
            LOGGER.fine("Ejecutando creación de tabla categorias_presupuesto: \n" + createCategorias);
            stmt.executeUpdate(createCategorias);
            LOGGER.info("Tabla 'categorias_presupuesto' verificada/creada.");

            LOGGER.info("Inicialización de la base de datos completada exitosamente.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crítico durante la inicialización de la base de datos.", e);
            // Considerar si se debe relanzar o manejar de otra forma crítica
        }
    }
}
