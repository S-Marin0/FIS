package modelos;

import java.time.LocalDate;

public class MetaFinanciera {
    private String nombre;
    private double montoObjetivo;
    private double montoActual;
    private LocalDate fechaLimite;
    private String descripcion;
    
    public MetaFinanciera(String nombre, double montoObjetivo, LocalDate fechaLimite, String descripcion) {
        this.nombre = nombre;
        this.montoObjetivo = montoObjetivo;
        this.fechaLimite = fechaLimite;
        this.descripcion = descripcion;
        this.montoActual = 0.0;
    }
    
    public double getPorcentajeCompletado() {
        return (montoActual / montoObjetivo) * 100;
    }
    
    public boolean estaCompletada() {
        return montoActual >= montoObjetivo;
    }
    
    public void agregarMonto(double monto) {
        this.montoActual += monto;
    }
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public double getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(double montoObjetivo) { this.montoObjetivo = montoObjetivo; }
    
    public double getMontoActual() { return montoActual; }
    public void setMontoActual(double montoActual) { this.montoActual = montoActual; }
    
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    @Override
    public String toString() {
        return String.format("%s - $%.2f/$%.2f (%.1f%%) - %s", 
            nombre, montoActual, montoObjetivo, getPorcentajeCompletado(), fechaLimite.toString());
    }
}