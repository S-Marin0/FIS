package fachada;

import database.DatabaseConnection;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        configureLogging(); // Configurar logging primero
        LOGGER.info("Aplicación Financiera Personal iniciada.");

        try {
            // Intentar establecer el Look and Feel Nimbus
            boolean nimbusFound = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusFound = true;
                    LOGGER.info("Look and Feel Nimbus aplicado.");
                    break;
                }
            }
            if (!nimbusFound) {
                LOGGER.info("Nimbus L&F no encontrado, usando el L&F del sistema.");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.WARNING, "No se pudo aplicar el Look and Feel preferido. Usando el L&F por defecto de Metal.", e);
            // La aplicación continuará con el L&F por defecto si Nimbus o el del sistema fallan.
        }

        // Inicializar la base de datos
        try {
            LOGGER.info("Inicializando base de datos...");
            DatabaseConnection.initializeDatabase();
            LOGGER.info("Base de datos inicializada correctamente.");
        } catch (SQLException e) { // Ser más específico con la excepción
            LOGGER.log(Level.SEVERE, "Error crítico al inicializar la base de datos. La aplicación podría no funcionar correctamente.", e);
            // Considerar mostrar un JOptionPane aquí si es un error crítico para el arranque
            // javax.swing.JOptionPane.showMessageDialog(null, "Error crítico al inicializar la base de datos: " + e.getMessage(), "Error de Base de Datos", javax.swing.JOptionPane.ERROR_MESSAGE);
            // System.exit(1); // Salir si la BD es esencial
        }

        // Agregar hook para cerrar la conexión de la BD al salir
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Cerrando conexión a la base de datos...");
            DatabaseConnection.closeConnection();
            LOGGER.info("Conexión a la base de datos cerrada.");
        }));

        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear y mostrar el sistema financiero
            SistemaFinancieroFacade sistemaFinanciero = new SistemaFinancieroFacade();
            sistemaFinanciero.mostrarInterfaz();
        });
    }

    private static void configureLogging() {
        try {
            // Cargar la configuración de logging desde el archivo
            // Asumiendo que logging.properties está en el classpath (ej. src/main/resources)
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        } catch (IOException | SecurityException | NullPointerException e) {
            System.err.println("Advertencia: No se pudo cargar la configuración de logging. Usando configuración por defecto. Error: " + e.getMessage());
            // Configuración básica por si falla la carga del archivo
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            // Remover handlers existentes para evitar duplicados si este método se llama varias veces
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL); // Ajustar según necesidad
            consoleHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(consoleHandler);
            rootLogger.setLevel(Level.INFO); // Nivel por defecto para el root logger

            // Ajustar niveles específicos si es necesario (ejemplo)
            // Logger.getLogger("fachada").setLevel(Level.FINE);
        }
        // Este log se hará con la configuración recién establecida
        LOGGER.config("Logging configurado.");
    }
}
