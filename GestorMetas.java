package servicios;

import database.dao.MetaDAO; // Importar DAO
import modelos.MetaFinanciera;
import java.sql.SQLException; // Importar SQLException
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
// Quitar import java.util.stream.Collectors; y java.util.Optional si ya no se usan para ops en memoria

public class GestorMetas {
    private static final Logger LOGGER = Logger.getLogger(GestorMetas.class.getName());
    private MetaDAO metaDAO;

    public GestorMetas() {
        this.metaDAO = new MetaDAO(); // Inicializar el DAO
        LOGGER.info("GestorMetas inicializado con MetaDAO.");
    }

    public boolean agregarMeta(MetaFinanciera meta) {
        if (meta == null) {
            LOGGER.warning("Gestor: Intento de agregar meta null.");
            return false;
        }
        LOGGER.log(Level.INFO, "Gestor: Intentando agregar meta: Nombre=''{0}'', Objetivo={1}",
            new Object[]{meta.getNombre(), meta.getMontoObjetivo()});
        try {
            metaDAO.insertarMeta(meta);
            LOGGER.info("Gestor: Meta pasada a DAO para inserción.");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al agregar meta " + meta.getNombre(), e);
            return false;
        }
    }

    public List<MetaFinanciera> obtenerMetas() {
        LOGGER.info("Gestor: Intentando obtener todas las metas desde DAO.");
        try {
            List<MetaFinanciera> metas = metaDAO.obtenerTodasMetas();
            LOGGER.log(Level.INFO, "Gestor: Obtenidas {0} metas desde DAO.", metas != null ? metas.size() : "null (error en DAO)");
            return metas;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener todas las metas.", e);
            return Collections.emptyList();
        }
    }

    public List<MetaFinanciera> obtenerMetasCompletadas() {
        LOGGER.info("Gestor: Intentando obtener metas completadas desde DAO.");
        try {
            List<MetaFinanciera> metas = metaDAO.obtenerMetasCompletadas();
            LOGGER.log(Level.INFO, "Gestor: Obtenidas {0} metas completadas desde DAO.", metas != null ? metas.size() : "null");
            return metas;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener metas completadas.", e);
            return Collections.emptyList();
        }
    }

    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        LOGGER.info("Gestor: Intentando obtener metas en progreso desde DAO.");
        try {
            List<MetaFinanciera> metas = metaDAO.obtenerMetasEnProgreso();
            LOGGER.log(Level.INFO, "Gestor: Obtenidas {0} metas en progreso desde DAO.", metas != null ? metas.size() : "null");
            return metas;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener metas en progreso.", e);
            return Collections.emptyList();
        }
    }

    public boolean contribuirAMeta(String nombreMeta, double monto) {
        LOGGER.log(Level.INFO, "Gestor: Intentando contribuir {0} a meta ''{1}'' usando DAO.", new Object[]{monto, nombreMeta});
        if (nombreMeta == null || nombreMeta.trim().isEmpty()) {
            LOGGER.warning("Gestor: Intento de contribuir a meta con nombre null o vacío.");
            return false;
        }
        try {
            // MetaDAO.contribuirAMeta ya actualiza el monto y el estado 'completada' en la BD
            // y lanza SQLException si la meta no se encuentra.
            metaDAO.contribuirAMeta(nombreMeta, monto);
            LOGGER.log(Level.INFO, "Gestor: Contribución a meta ''{0}'' pasada a DAO.", nombreMeta);
            return true;
        } catch (SQLException e) {
            // SQLException puede ser porque la meta no existe o por un error SQL general.
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al contribuir a meta " + nombreMeta, e);
            return false;
        }
    }

    // Si se necesita un método para obtener una meta específica por nombre
    public MetaFinanciera obtenerMetaPorNombre(String nombreMeta) {
        LOGGER.log(Level.INFO, "Gestor: Intentando obtener meta por nombre ''{0}'' desde DAO.", nombreMeta);
        if (nombreMeta == null || nombreMeta.trim().isEmpty()) {
            LOGGER.warning("Gestor: Intento de obtener meta con nombre null o vacío.");
            return null;
        }
        try {
            MetaFinanciera meta = metaDAO.obtenerMetaPorNombre(nombreMeta);
            if (meta != null) {
                LOGGER.log(Level.INFO, "Gestor: Meta ''{0}'' obtenida desde DAO.", nombreMeta);
            } else {
                LOGGER.log(Level.INFO, "Gestor: No se encontró meta ''{0}'' en DAO.", nombreMeta);
            }
            return meta;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al obtener meta por nombre " + nombreMeta, e);
            return null;
        }
    }

    // Si se necesita un método para eliminar una meta
    public boolean eliminarMeta(String nombreMeta) {
        LOGGER.log(Level.INFO, "Gestor: Intentando eliminar meta ''{0}'' usando DAO.", nombreMeta);
        if (nombreMeta == null || nombreMeta.trim().isEmpty()) {
            LOGGER.warning("Gestor: Intento de eliminar meta con nombre null o vacío.");
            return false;
        }
        try {
            metaDAO.eliminarMeta(nombreMeta);
            LOGGER.log(Level.INFO, "Gestor: Solicitud de eliminación para meta ''{0}'' pasada a DAO.", nombreMeta);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gestor: Error SQL al eliminar meta " + nombreMeta, e);
            return false;
        }
    }

    // Método para el resumen mejorado
    public List<MetaFinanciera> obtenerMetasProximas(java.time.LocalDate fechaReferencia, int diasProximidad) throws SQLException {
        LOGGER.log(Level.INFO, "Gestor: Solicitando metas próximas a {0} (dentro de {1} días) desde DAO.", new Object[]{fechaReferencia, diasProximidad});
        // El SQLException se propaga para que la fachada lo maneje
        return metaDAO.obtenerMetasProximas(fechaReferencia, diasProximidad);
    }
}
 
