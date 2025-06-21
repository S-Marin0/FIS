package modelos;

import java.util.HashMap;
import java.util.Map;

public class Presupuesto {
    private String nombre;
    private Map<String, Double> limitesPorCategoria;
    private Map<String, Double> gastosPorCategoria;
    private int mes;
    private int año;
    
    public Presupuesto(String nombre, int mes, int año) {
        this.nombre = nombre;
        this.mes = mes;
        this.año = año;
        this.limitesPorCategoria = new HashMap<>();
        this.gastosPorCategoria = new HashMap<>();
    }
    
    public void agregarLimiteCategoria(String categoria, double limite) {
        limitesPorCategoria.put(categoria, limite);
        gastosPorCategoria.putIfAbsent(categoria, 0.0);
    }
    
    public void registrarGasto(String categoria, double monto) {
        gastosPorCategoria.put(categoria, gastosPorCategoria.getOrDefault(categoria, 0.0) + monto);
    }
    
    public double getPorcentajeUsado(String categoria) {
        double limite = limitesPorCategoria.getOrDefault(categoria, 0.0);
        double gasto = gastosPorCategoria.getOrDefault(categoria, 0.0);
        return limite > 0 ? (gasto / limite) * 100 : 0;
    }
    
    public boolean excedeLimite(String categoria) {
        return getPorcentajeUsado(categoria) > 100;
    }
    
    public double getTotalPresupuestado() {
        return limitesPorCategoria.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    public double getTotalGastado() {
        return gastosPorCategoria.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Map<String, Double> getLimitesPorCategoria() { return limitesPorCategoria; }
    public Map<String, Double> getGastosPorCategoria() { return gastosPorCategoria; }
    
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    
    public int getAño() { return año; }
    public void setAño(int año) { this.año = año; }
}
