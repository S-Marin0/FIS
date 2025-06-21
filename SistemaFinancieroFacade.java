package fachada;

import Interfaz.InterfazFinanciera;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelos.MetaFinanciera;
import modelos.Presupuesto;
import modelos.Transaccion;
import servicios.GestorMetas;
import servicios.GestorPresupuestos;
import servicios.GestorTransacciones;

public class SistemaFinancieroFacade {
    private static final Logger LOGGER = Logger.getLogger(SistemaFinancieroFacade.class.getName());

    private GestorTransacciones gestorTransacciones;
    private GestorMetas gestorMetas;
    private GestorPresupuestos gestorPresupuestos;
    private InterfazFinanciera interfaz;

    public SistemaFinancieroFacade() {
        LOGGER.info("Inicializando SistemaFinancieroFacade...");
        this.gestorTransacciones = new GestorTransacciones(); // Ahora usa DAO
        LOGGER.fine("GestorTransacciones inicializado.");
        this.gestorMetas = new GestorMetas(); // Ahora usa DAO
        LOGGER.fine("GestorMetas inicializado.");
        this.gestorPresupuestos = new GestorPresupuestos(); // Ya usaba DAO
        LOGGER.fine("GestorPresupuestos inicializado.");
        LOGGER.info("SistemaFinancieroFacade inicializado.");
    }

    public void setInterfaz(InterfazFinanciera interfaz) {
        this.interfaz = interfaz;
    }

    // Métodos para Transacciones
    public boolean registrarIngreso(String descripcion, double monto, String categoria) {
        LOGGER.log(Level.INFO, "Facade: Registrar Ingreso - Desc: {0}, Monto: {1}, Cat: {2}", new Object[]{descripcion, monto, categoria});
        Transaccion ingreso = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.INGRESO);
        boolean exito = gestorTransacciones.agregarTransaccion(ingreso); // Gestor ahora devuelve boolean
        if (exito) {
            LOGGER.info("Facade: Ingreso pasado a GestorTransacciones y procesado con éxito.");
        } else {
            LOGGER.warning("Facade: GestorTransacciones reportó un fallo al agregar ingreso.");
        }
        return exito;
    }

    public boolean registrarGasto(String descripcion, double monto, String categoria) {
        LOGGER.log(Level.INFO, "Facade: Registrar Gasto - Desc: {0}, Monto: {1}, Cat: {2}", new Object[]{descripcion, monto, categoria});
        Transaccion gasto = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.GASTO);
        boolean exito = gestorTransacciones.agregarTransaccion(gasto); // Gestor ahora devuelve boolean
        if (exito) {
            LOGGER.info("Facade: Gasto pasado a GestorTransacciones y procesado con éxito.");
        } else {
            LOGGER.warning("Facade: GestorTransacciones reportó un fallo al agregar gasto.");
        }
        return exito;
    }

    public List<Transaccion> obtenerTransacciones() {
        LOGGER.info("Facade: Obtener todas las transacciones.");
        // GestorTransacciones ahora devuelve lista vacía en caso de error SQL, y loguea el error.
        return gestorTransacciones.obtenerTransacciones();
    }

    public double obtenerBalance() {
        LOGGER.info("Facade: Obtener balance.");
        // GestorTransacciones ahora devuelve 0.0 en caso de error SQL en sus componentes.
        return gestorTransacciones.obtenerBalance();
    }

    public double obtenerTotalIngresos() {
        LOGGER.info("Facade: Obtener total de ingresos.");
        return gestorTransacciones.obtenerTotalIngresos();
    }

    public double obtenerTotalGastos() {
        LOGGER.info("Facade: Obtener total de gastos.");
        return gestorTransacciones.obtenerTotalGastos();
    }

    public Map<String, Double> obtenerGastosPorCategoria() {
        LOGGER.info("Facade: Obtener gastos por categoría.");
        return gestorTransacciones.obtenerGastosPorCategoria();
    }

    // Métodos para Metas
    public boolean crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite, String descripcion) {
        LOGGER.log(Level.INFO, "Facade: Crear Meta - Nombre: {0}, Objetivo: {1}", new Object[]{nombre, montoObjetivo});
        MetaFinanciera meta = new MetaFinanciera(nombre, montoObjetivo, fechaLimite, descripcion);
        boolean exito = gestorMetas.agregarMeta(meta); // Gestor ahora devuelve boolean
        if (exito) {
            LOGGER.info("Facade: Meta pasada a GestorMetas y procesada con éxito.");
        } else {
            LOGGER.warning("Facade: GestorMetas reportó un fallo al crear meta.");
        }
        return exito;
    }

    public boolean contribuirAMeta(String nombreMeta, double monto) {
        LOGGER.log(Level.INFO, "Facade: Contribuir a Meta ''{0}'' con Monto: {1}", new Object[]{nombreMeta, monto});
        boolean exito = gestorMetas.contribuirAMeta(nombreMeta, monto); // Gestor ahora devuelve boolean
        if (exito) {
            LOGGER.info("Facade: Contribución a meta pasada a GestorMetas y procesada con éxito.");
        } else {
            LOGGER.warning("Facade: GestorMetas reportó un fallo al contribuir a meta.");
        }
        return exito;
    }

    public List<MetaFinanciera> obtenerMetas() {
        LOGGER.info("Facade: Obtener todas las metas.");
        // GestorMetas ahora devuelve lista vacía en caso de error SQL, y loguea el error.
        return gestorMetas.obtenerMetas();
    }

    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        LOGGER.info("Facade: Obtener metas en progreso.");
        return gestorMetas.obtenerMetasEnProgreso();
    }

    public List<MetaFinanciera> obtenerMetasCompletadas() {
        LOGGER.info("Facade: Obtener metas completadas.");
        return gestorMetas.obtenerMetasCompletadas();
    }

    // Métodos para Presupuestos
    public boolean crearPresupuesto(String nombre, int mes, int año) {
        LOGGER.log(Level.INFO, "Facade: Crear Presupuesto - Nombre: {0}, Mes: {1}, Año: {2}", new Object[]{nombre, mes, año});
        Presupuesto presupuesto = new Presupuesto(nombre, mes, año);
        // GestorPresupuestos.agregarPresupuesto ahora devuelve boolean.
        boolean exito = gestorPresupuestos.agregarPresupuesto(presupuesto);
        if (exito) {
            LOGGER.info("Facade: Presupuesto pasado a GestorPresupuestos y procesado con éxito.");
        } else {
            LOGGER.warning("Facade: GestorPresupuestos reportó un fallo al agregar presupuesto.");
            // El error específico (SQLException) ya fue logueado por el Gestor y el DAO.
        }
        return exito;
    }

    public List<Presupuesto> obtenerPresupuestos() {
        LOGGER.info("Facade: Obtener todos los presupuestos.");
        return gestorPresupuestos.obtenerPresupuestos();
    }

    public Presupuesto obtenerPresupuestoActual() {
        LOGGER.info("Facade: Obtener presupuesto actual.");
        return gestorPresupuestos.obtenerPresupuestoActual();
    }

    public boolean registrarGastoEnPresupuesto(String categoria, double monto) {
        LOGGER.log(Level.INFO, "Facade: Registrar Gasto en Presupuesto - Cat: {0}, Monto: {1}", new Object[]{categoria, monto});
        // Similar a crearPresupuesto, GestorPresupuestos.registrarGastoEnPresupuesto no devuelve boolean.
        try {
            gestorPresupuestos.registrarGastoEnPresupuesto(categoria, monto);
            LOGGER.info("Facade: Gasto en presupuesto pasado a GestorPresupuestos.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al registrar gasto en presupuesto", e);
            return false;
        }
    }

    public boolean agregarCategoriaAPresupuesto(String nombrePresupuesto, int mes, int año, String categoria, double limite) {
        LOGGER.log(Level.INFO, "Facade: Agregar Categoría ''{0}'' a Presupuesto ''{1}'' ({2}/{3}) con Límite: {4}",
                   new Object[]{categoria, nombrePresupuesto, mes, año, limite});
        try {
            // Pasar mes y año al gestor
            gestorPresupuestos.agregarCategoria(nombrePresupuesto, mes, año, categoria, limite);
            LOGGER.info("Facade: Solicitud de agregar categoría pasada a GestorPresupuestos y procesada con éxito.");
            return true;
        } catch (IllegalArgumentException e) {
             LOGGER.log(Level.WARNING, "Facade: Argumento inválido al agregar categoría a presupuesto.", e);
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Facade: Error SQL al agregar categoría ''" + categoria + "'' a presupuesto ''" + nombrePresupuesto + "'' ("+mes+"/"+año+").", e);
            return false;
        } catch (Exception e) { // Captura genérica para otros errores
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al agregar categoría ("+categoria+") a presupuesto ("+nombrePresupuesto+ " " + mes+"/"+año+").", e);
            return false;
        }
    }

    public void mostrarInterfaz() {
        LOGGER.info("Facade: Solicitando mostrar la interfaz gráfica.");
        if (this.interfaz == null) {
            LOGGER.info("Facade: Interfaz es null, creando una nueva instancia.");
            this.interfaz = new InterfazFinanciera(this);
        }
        this.interfaz.setVisible(true);
        LOGGER.info("Facade: Interfaz gráfica configurada como visible.");
    }

    public String obtenerUltimoError() {
        LOGGER.warning("Facade: obtenerUltimoError() llamado, pero no hay implementación detallada de errores.");
        return "Consulta los logs de la consola para más detalles";
    }
}
