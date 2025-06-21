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
    private InterfazFinanciera interfaz; // Mantener referencia para mostrarla

    public SistemaFinancieroFacade() {
        LOGGER.info("Inicializando SistemaFinancieroFacade...");
        this.gestorTransacciones = new GestorTransacciones();
        LOGGER.fine("GestorTransacciones inicializado.");
        this.gestorMetas = new GestorMetas();
        LOGGER.fine("GestorMetas inicializado.");
        this.gestorPresupuestos = new GestorPresupuestos();
        LOGGER.fine("GestorPresupuestos inicializado.");
        // La interfaz se crea aquí, pero se muestra con mostrarInterfaz()
        // No es ideal pasar 'this' (fachada) al constructor de la Interfaz si la Interfaz
        // también es creada por la fachada, puede crear un ciclo de dependencia en la construcción.
        // Sería mejor si la Interfaz recibe la fachada como dependencia después de ser creada,
        // o si un ensamblador externo las conecta. Por ahora, se mantiene.
        LOGGER.info("SistemaFinancieroFacade inicializado.");
    }

    // Método para establecer la interfaz, si se crea externamente
    public void setInterfaz(InterfazFinanciera interfaz) {
        this.interfaz = interfaz;
    }

    // Métodos para Transacciones
    public boolean registrarIngreso(String descripcion, double monto, String categoria) {
        LOGGER.log(Level.INFO, "Facade: Registrar Ingreso - Desc: {0}, Monto: {1}, Cat: {2}", new Object[]{descripcion, monto, categoria});
        Transaccion ingreso = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.INGRESO);
        try {
            gestorTransacciones.agregarTransaccion(ingreso); // Asumiendo que esto podría fallar silenciosamente o necesitar manejo de errores
            LOGGER.info("Facade: Ingreso pasado a GestorTransacciones.");
            return true; // Asumir éxito si no hay excepción; idealmente, el gestor daría feedback.
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al registrar ingreso", e);
            return false;
        }
    }

    public boolean registrarGasto(String descripcion, double monto, String categoria) {
        LOGGER.log(Level.INFO, "Facade: Registrar Gasto - Desc: {0}, Monto: {1}, Cat: {2}", new Object[]{descripcion, monto, categoria});
        Transaccion gasto = new Transaccion(descripcion, monto, categoria, Transaccion.TipoTransaccion.GASTO);
        try {
            gestorTransacciones.agregarTransaccion(gasto);
            LOGGER.info("Facade: Gasto pasado a GestorTransacciones.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al registrar gasto", e);
            return false;
        }
    }

    public List<Transaccion> obtenerTransacciones() {
        LOGGER.info("Facade: Obtener todas las transacciones.");
        try {
            return gestorTransacciones.obtenerTransacciones();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener transacciones", e);
            return Collections.emptyList();
        }
    }

    public double obtenerBalance() {
        LOGGER.info("Facade: Obtener balance.");
        try {
            return gestorTransacciones.obtenerBalance();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener balance", e);
            return 0.0;
        }
    }

    public double obtenerTotalIngresos() {
        LOGGER.info("Facade: Obtener total de ingresos.");
         try {
            return gestorTransacciones.obtenerTotalIngresos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener total de ingresos", e);
            return 0.0;
        }
    }

    public double obtenerTotalGastos() {
        LOGGER.info("Facade: Obtener total de gastos.");
        try {
            return gestorTransacciones.obtenerTotalGastos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener total de gastos", e);
            return 0.0;
        }
    }

    public Map<String, Double> obtenerGastosPorCategoria() {
        LOGGER.info("Facade: Obtener gastos por categoría.");
        try {
            return gestorTransacciones.obtenerGastosPorCategoria();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener gastos por categoría", e);
            return Collections.emptyMap();
        }
    }

    // Métodos para Metas
    public boolean crearMeta(String nombre, double montoObjetivo, LocalDate fechaLimite, String descripcion) {
        LOGGER.log(Level.INFO, "Facade: Crear Meta - Nombre: {0}, Objetivo: {1}", new Object[]{nombre, montoObjetivo});
        MetaFinanciera meta = new MetaFinanciera(nombre, montoObjetivo, fechaLimite, descripcion);
        try {
            gestorMetas.agregarMeta(meta);
            LOGGER.info("Facade: Meta pasada a GestorMetas.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al crear meta", e);
            return false;
        }
    }

    public boolean contribuirAMeta(String nombreMeta, double monto) {
        LOGGER.log(Level.INFO, "Facade: Contribuir a Meta ''{0}'' con Monto: {1}", new Object[]{nombreMeta, monto});
         try {
            gestorMetas.contribuirAMeta(nombreMeta, monto);
            LOGGER.info("Facade: Contribución a meta pasada a GestorMetas.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al contribuir a meta", e);
            return false;
        }
    }

    public List<MetaFinanciera> obtenerMetas() {
        LOGGER.info("Facade: Obtener todas las metas.");
        try {
            return gestorMetas.obtenerMetas();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener metas", e);
            return Collections.emptyList();
        }
    }

    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        LOGGER.info("Facade: Obtener metas en progreso.");
        try {
            return gestorMetas.obtenerMetasEnProgreso();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener metas en progreso", e);
            return Collections.emptyList();
        }
    }

    public List<MetaFinanciera> obtenerMetasCompletadas() {
        LOGGER.info("Facade: Obtener metas completadas.");
        try {
            return gestorMetas.obtenerMetasCompletadas();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error al obtener metas completadas", e);
            return Collections.emptyList();
        }
    }

    // Métodos para Presupuestos
    public boolean crearPresupuesto(String nombre, int mes, int año) {
        LOGGER.log(Level.INFO, "Facade: Crear Presupuesto - Nombre: {0}, Mes: {1}, Año: {2}", new Object[]{nombre, mes, año});
        Presupuesto presupuesto = new Presupuesto(nombre, mes, año);
        try {
            gestorPresupuestos.agregarPresupuesto(presupuesto);
            LOGGER.info("Facade: Presupuesto pasado a GestorPresupuestos.");
            return true; // GestorPresupuestos ahora loguea errores de DAO pero no los propaga como SQLException a la fachada aquí.
        } catch (Exception e) { // Captura genérica por si el gestor lanzara otra cosa
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al crear presupuesto", e);
            return false;
        }
    }

    public List<Presupuesto> obtenerPresupuestos() {
        LOGGER.info("Facade: Obtener todos los presupuestos.");
        try {
            return gestorPresupuestos.obtenerPresupuestos(); // Gestor ya maneja y loguea SQLException, devuelve lista vacía en error.
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al obtener presupuestos", e);
            return Collections.emptyList();
        }
    }

    public Presupuesto obtenerPresupuestoActual() {
        LOGGER.info("Facade: Obtener presupuesto actual.");
        try {
            return gestorPresupuestos.obtenerPresupuestoActual(); // Gestor ya maneja y loguea SQLException, devuelve null en error.
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al obtener presupuesto actual", e);
            return null;
        }
    }

    public boolean registrarGastoEnPresupuesto(String categoria, double monto) {
        LOGGER.log(Level.INFO, "Facade: Registrar Gasto en Presupuesto - Cat: {0}, Monto: {1}", new Object[]{categoria, monto});
        try {
            gestorPresupuestos.registrarGastoEnPresupuesto(categoria, monto);
            LOGGER.info("Facade: Gasto en presupuesto pasado a GestorPresupuestos.");
            return true; // GestorPresupuestos ahora loguea errores de DAO pero no los propaga como SQLException.
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al registrar gasto en presupuesto", e);
            return false;
        }
    }

    public boolean agregarCategoriaAPresupuesto(String nombrePresupuesto, String categoria, double limite) {
        LOGGER.log(Level.INFO, "Facade: Agregar Categoría ''{0}'' a Presupuesto ''{1}'' con Límite: {2}", new Object[]{categoria, nombrePresupuesto, limite});
        try {
            gestorPresupuestos.agregarCategoria(nombrePresupuesto, categoria, limite);
            LOGGER.info("Facade: Solicitud de agregar categoría pasada a GestorPresupuestos.");
            return true;
        } catch (IllegalArgumentException e) { // Capturar validación del gestor
             LOGGER.log(Level.WARNING, "Facade: Argumento inválido al agregar categoría a presupuesto.", e);
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Facade: Error SQL al agregar categoría ''" + categoria + "'' a presupuesto ''" + nombrePresupuesto + "''.", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Facade: Error inesperado al agregar categoría a presupuesto.", e);
            return false;
        }
    }

    // Método para mostrar la interfaz
    public void mostrarInterfaz() {
        LOGGER.info("Facade: Solicitando mostrar la interfaz gráfica.");
        if (this.interfaz == null) {
            // Crear la interfaz aquí si aún no existe.
            // Esto rompe un poco el patrón si la interfaz es la que crea la fachada,
            // pero es necesario si Main solo crea la fachada.
            LOGGER.info("Facade: Interfaz es null, creando una nueva instancia.");
            this.interfaz = new InterfazFinanciera(this);
        }
        this.interfaz.setVisible(true);
        LOGGER.info("Facade: Interfaz gráfica configurada como visible.");
    }

    public String obtenerUltimoError() {
        // Este método sigue siendo un placeholder.
        // Un sistema de logging más robusto o un mecanismo de estado de error sería mejor.
        LOGGER.warning("Facade: obtenerUltimoError() llamado, pero no hay implementación detallada de errores.");
        return "Consulta los logs de la consola para más detalles";
    }
}
