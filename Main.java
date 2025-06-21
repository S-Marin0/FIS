package fachada;

import database.DatabaseConnection;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializar la base de datos
        try {
            System.out.println("Inicializando base de datos...");
            DatabaseConnection.initializeDatabase();
            System.out.println("Base de datos inicializada correctamente.");
        } catch (Exception e) {
            System.err.println("Error crítico al inicializar la base de datos. La aplicación podría no funcionar correctamente.");
            e.printStackTrace();
        }

        // Agregar hook para cerrar la conexión de la BD al salir
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando conexión a la base de datos...");
            DatabaseConnection.closeConnection();
            System.out.println("Conexión a la base de datos cerrada.");
        }));

        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear y mostrar el sistema financiero
            SistemaFinancieroFacade sistemaFinanciero = new SistemaFinancieroFacade();
            sistemaFinanciero.mostrarInterfaz();
        });
    }
}
