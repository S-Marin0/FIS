package fachada;

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
        
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Crear y mostrar el sistema financiero
            SistemaFinancieroFacade sistemaFinanciero = new SistemaFinancieroFacade();
            sistemaFinanciero.mostrarInterfaz();
        });
    }
}