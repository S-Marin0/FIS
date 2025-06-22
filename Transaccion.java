package modelos;

import java.time.LocalDate;

public class Transaccion {
    private String descripcion;
    private double monto;
    private String categoria;
    private TipoTransaccion tipo;
    private LocalDate fecha;
    private int id; // ID de la transacción, asignado por la base de datos
    
    public enum TipoTransaccion {
        INGRESO, GASTO
    }
    
    public Transaccion(String descripcion, double monto, String categoria, TipoTransaccion tipo) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoria = categoria;
        this.tipo = tipo;
        this.fecha = LocalDate.now();
        // El ID no se asigna en el constructor, se espera que lo asigne el DAO al leer de la BD
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    @Override
    public String toString() {
        return String.format("%s - $%.2f (%s) - %s", 
            descripcion, monto, categoria, fecha.toString());
    }
}