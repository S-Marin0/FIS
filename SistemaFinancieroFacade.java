package fachada;

import Interfaz.InterfazFinanciera;
import java.sql.SQLException; // Asegurar que esta importación exista
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
        this.gestorPresupuestos = new GestorPresupuestos(); // Ahora usa DAO
        this.interfaz = new InterfazFinanciera(this);
    }

    // Métodos para Transacciones
    public boolean registrarIngreso(String descripcion, double monto, String categoria) {
        Transaccion ingreso = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.INGRESO);
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        gestorTransacciones.agregarTransaccion(ingreso);
        return true;
    }

    public boolean registrarGasto(String descripcion, double monto, String categoria) {
        Transaccion gasto = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.GASTO);
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        gestorTransacciones.agregarTransaccion(gasto);
        return true;
    }

    public List<Transaccion> obtenerTransacciones() {
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        return gestorTransacciones.obtenerTransacciones();
    }

    public double obtenerBalance() {
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        return gestorTransacciones.obtenerBalance();
    }

    public double obtenerTotalIngresos() {
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        return gestorTransacciones.obtenerTotalIngresos();
    }

    public double obtenerTotalGastos() {
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        return gestorTransacciones.obtenerTotalGastos();
    }

    public Map<String, Double> obtenerGastosPorCategoria() {
        // Faltaría manejo de errores/excepciones de gestorTransacciones si los hubiera
        return gestorTransacciones.obtenerGastosPorCategoria();
    }

    // Métodos para Metas
    public boolean crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite, String descripcion) {
        MetaFinanciera meta = new MetaFinanciera(nombre, montoObjetivo, fechaLimite, descripcion);
        // Faltaría manejo de errores/excepciones de gestorMetas si los hubiera
        gestorMetas.agregarMeta(meta);
        return true;
    }

    public boolean contribuirAMeta(String nombreMeta, double monto) {
        // Faltaría manejo de errores/excepciones de gestorMetas si los hubiera
        gestorMetas.contribuirAMeta(nombreMeta, monto);
        return true;
    }

    public List<MetaFinanciera> obtenerMetas() {
        // Faltaría manejo de errores/excepciones de gestorMetas si los hubiera
        return gestorMetas.obtenerMetas();
    }

    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        // Faltaría manejo de errores/excepciones de gestorMetas si los hubiera
        return gestorMetas.obtenerMetasEnProgreso();
    }

    public List<MetaFinanciera> obtenerMetasCompletadas() {
        // Faltaría manejo de errores/excepciones de gestorMetas si los hubiera
        return gestorMetas.obtenerMetasCompletadas();
    }

    // Métodos para Presupuestos
    public boolean crearPresupuesto(String nombre, int mes, int año) {
        Presupuesto presupuesto = new Presupuesto(nombre, mes, año);
        // GestorPresupuestos ahora maneja SQLException internamente (imprimiéndola)
        // Para un mejor diseño, GestorPresupuestos.agregarPresupuesto debería lanzar SQLException
        // y la fachada manejarla aquí, similar a agregarCategoriaAPresupuesto.
        gestorPresupuestos.agregarPresupuesto(presupuesto);
        return true;
    }

    public List<Presupuesto> obtenerPresupuestos() {
        // GestorPresupuestos ahora maneja SQLException internamente (imprimiéndola)
        return gestorPresupuestos.obtenerPresupuestos();
    }

    public Presupuesto obtenerPresupuestoActual() {
        // GestorPresupuestos ahora maneja SQLException internamente (imprimiéndola)
        return gestorPresupuestos.obtenerPresupuestoActual();
    }

    public boolean registrarGastoEnPresupuesto(String categoria, double monto) {
        // GestorPresupuestos ahora maneja SQLException internamente (imprimiéndola)
        gestorPresupuestos.registrarGastoEnPresupuesto(categoria, monto);
        return true;
    }

    public boolean agregarCategoriaAPresupuesto(String nombrePresupuesto, String categoria, double limite) {
        try {
            gestorPresupuestos.agregarCategoria(nombrePresupuesto, categoria, limite);
            return true;
        } catch (SQLException e) {
            System.err.println("Error en Facade al agregar categoría '" + categoria + "' a presupuesto '" + nombrePresupuesto + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Método para mostrar la interfaz
    public void mostrarInterfaz() {
        interfaz.setVisible(true);
    }

    // Método para obtener el último error (opcional)
    public String obtenerUltimoError() {
        return "Consulta los logs de la consola para más detalles";
    }
}
