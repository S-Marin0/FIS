package fachada;

import Interfaz.InterfazFinanciera;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import modelos.MetaFinanciera;
import modelos.Presupuesto;
import modelos.Transaccion;
import servicios.GestorMetas;
import servicios.GestorPresupuestos;
import servicios.GestorTransacciones;

public class SistemaFinancieroFacade {
    private GestorTransacciones gestorTransacciones;
    private GestorMetas gestorMetas;
    private GestorPresupuestos gestorPresupuestos;
    private InterfazFinanciera interfaz;
    
    public SistemaFinancieroFacade() {
        this.gestorTransacciones = new GestorTransacciones();
        this.gestorMetas = new GestorMetas();
        this.gestorPresupuestos = new GestorPresupuestos();
        this.interfaz = new InterfazFinanciera(this);
    }
    
    // Métodos para Transacciones
    public void registrarIngreso(String descripcion, double monto, String categoria) {
        Transaccion ingreso = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.INGRESO);
        gestorTransacciones.agregarTransaccion(ingreso);
    }
    
    public void registrarGasto(String descripcion, double monto, String categoria) {
        Transaccion gasto = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.GASTO);
        gestorTransacciones.agregarTransaccion(gasto);
        gestorPresupuestos.registrarGastoEnPresupuesto(categoria, monto);
    }
    
    public List<Transaccion> obtenerTransacciones() {
        return gestorTransacciones.obtenerTransacciones();
    }
    
    public double obtenerBalance() {
        return gestorTransacciones.obtenerBalance();
    }
    
    public double obtenerTotalIngresos() {
        return gestorTransacciones.obtenerTotalIngresos();
    }
    
    public double obtenerTotalGastos() {
        return gestorTransacciones.obtenerTotalGastos();
    }
    
    public Map<String, Double> obtenerGastosPorCategoria() {
        return gestorTransacciones.obtenerGastosPorCategoria();
    }
    
    // Métodos para Metas
    public void crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite, String descripcion) {
        MetaFinanciera meta = new MetaFinanciera(nombre, montoObjetivo, fechaLimite, descripcion);
        gestorMetas.agregarMeta(meta);
    }
    
    public void contribuirAMeta(String nombreMeta, double monto) {
        gestorMetas.contribuirAMeta(nombreMeta, monto);
    }
    
    public List<MetaFinanciera> obtenerMetas() {
        return gestorMetas.obtenerMetas();
    }
    
    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        return gestorMetas.obtenerMetasEnProgreso();
    }
    
    // Métodos para Presupuestos
    public void crearPresupuesto(String nombre, int mes, int año) {
        Presupuesto presupuesto = new Presupuesto(nombre, mes, año);
        gestorPresupuestos.agregarPresupuesto(presupuesto);
    }
    
    public void agregarCategoriaAPresupuesto(String nombrePresupuesto, String categoria, double limite) {
        gestorPresupuestos.obtenerPresupuestos().stream()
                .filter(p -> p.getNombre().equals(nombrePresupuesto))
                .findFirst()
                .ifPresent(p -> p.agregarLimiteCategoria(categoria, limite));
    }
    
    public List<Presupuesto> obtenerPresupuestos() {
        return gestorPresupuestos.obtenerPresupuestos();
    }
    
    public Presupuesto obtenerPresupuestoActual() {
        return gestorPresupuestos.obtenerPresupuestoActual();
    }
    
    // Método para mostrar la interfaz
    public void mostrarInterfaz() {
        interfaz.setVisible(true);
    }
}