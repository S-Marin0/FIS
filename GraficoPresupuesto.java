package Interfaz;

// Archivo: GraficoPresupuesto.java
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.Map;
import java.util.Random;

public class GraficoPresupuesto extends JPanel {
    private Map<String, Double> gastosPorCategoria;
    private Color[] colores;
    
    public GraficoPresupuesto() {
        setBorder(BorderFactory.createTitledBorder("Distribución de Gastos por Categoría"));
        setPreferredSize(new Dimension(400, 300));
        
        // Colores predefinidos para las categorías
        colores = new Color[]{
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA,
            Color.CYAN, Color.PINK, Color.YELLOW, Color.GRAY, Color.LIGHT_GRAY
        };
    }
    
    public void actualizarDatos(Map<String, Double> gastosPorCategoria) {
        this.gastosPorCategoria = gastosPorCategoria;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gastosPorCategoria == null || gastosPorCategoria.isEmpty()) {
            g.drawString("No hay datos para mostrar", getWidth()/2 - 80, getHeight()/2);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calcular total
        double total = gastosPorCategoria.values().stream().mapToDouble(Double::doubleValue).sum();
        
        if (total == 0) {
            g2d.drawString("No hay gastos registrados", getWidth()/2 - 80, getHeight()/2);
            g2d.dispose();
            return;
        }
        
        // Dimensiones del gráfico
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(getWidth(), getHeight()) / 3;
        
        // Dibujar gráfico circular
        double startAngle = 0;
        int colorIndex = 0;
        
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            double monto = entry.getValue();
            double porcentaje = (monto / total) * 100;
            double angle = (monto / total) * 360;
            
            // Dibujar sector
            g2d.setColor(colores[colorIndex % colores.length]);
            Arc2D.Double arc = new Arc2D.Double(
                centerX - radius, centerY - radius,
                radius * 2, radius * 2,
                startAngle, angle, Arc2D.PIE
            );
            g2d.fill(arc);
            
            // Dibujar borde
            g2d.setColor(Color.BLACK);
            g2d.draw(arc);
            
            startAngle += angle;
            colorIndex++;
        }
        
        // Dibujar leyenda
        int legendY = 20;
        colorIndex = 0;
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            double monto = entry.getValue();
            double porcentaje = (monto / total) * 100;
            
            // Cuadro de color
            g2d.setColor(colores[colorIndex % colores.length]);
            g2d.fillRect(10, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(10, legendY, 15, 15);
            
            // Texto
            String texto = String.format("%s: $%.2f (%.1f%%)", categoria, monto, porcentaje);
            g2d.drawString(texto, 30, legendY + 12);
            
            legendY += 20;
            colorIndex++;
        }
        
        g2d.dispose();
    }
}
