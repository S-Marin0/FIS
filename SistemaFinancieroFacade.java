package fachada;

import Interfaz.InterfazFinanciera;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
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
    public boolean registrarIngreso(String descripcion, double monto, String categoria) {
        Transaccion ingreso = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.INGRESO);
        gestorTransacciones.agregarTransaccion(ingreso);
        return true;
    }
    
    public boolean registrarGasto(String descripcion, double monto, String categoria) {
        Transaccion gasto = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.GASTO);
        gestorTransacciones.agregarTransaccion(gasto);
        return true;
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
    public boolean crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite, String descripcion) {
        MetaFinanciera meta = new MetaFinanciera(nombre, montoObjetivo, fechaLimite, descripcion);
        gestorMetas.agregarMeta(meta);
        return true;
    }
    
    public boolean contribuirAMeta(String nombreMeta, double monto) {
        gestorMetas.contribuirAMeta(nombreMeta, monto);
        return true;
    }
    
    public List<MetaFinanciera> obtenerMetas() {
        return gestorMetas.obtenerMetas();
    }
    
    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        return gestorMetas.obtenerMetasEnProgreso();
    }
    
    public List<MetaFinanciera> obtenerMetasCompletadas() {
        return gestorMetas.obtenerMetasCompletadas();
    }
    
    // Métodos para Presupuestos
    public boolean crearPresupuesto(String nombre, int mes, int año) {
        Presupuesto presupuesto = new Presupuesto(nombre, mes, año);
        gestorPresupuestos.agregarPresupuesto(presupuesto);
        return true;
    }

    public List<Presupuesto> obtenerPresupuestos() {
        return gestorPresupuestos.obtenerPresupuestos();
    }
    
    public Presupuesto obtenerPresupuestoActual() {
        return gestorPresupuestos.obtenerPresupuestoActual();
    }
    
    public boolean registrarGastoEnPresupuesto(String categoria, double monto) {
        gestorPresupuestos.registrarGastoEnPresupuesto(categoria, monto);
        return true;
    }
    
    // Método para mostrar la interfaz
    public void mostrarInterfaz() {
        interfaz.setVisible(true);
    }
    
    // Método para obtener el último error (opcional)
    public String obtenerUltimoError() {
        // Podrías implementar un sistema de manejo de errores más sofisticado
        return "Consulta los logs de la consola para más detalles";
    }
}
