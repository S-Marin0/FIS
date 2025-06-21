package servicios;

import java.sql.SQLException;
import java.util.List;
import database.dao.MetaDAO;
import modelos.MetaFinanciera;

public class GestorMetas {
    private MetaDAO metaDAO;
    
    public GestorMetas() {
        this.metaDAO = new MetaDAO();
    }
    
    /**
     * Agrega una nueva meta financiera a la base de datos
     */
    public void agregarMeta(MetaFinanciera meta) throws SQLException {
        metaDAO.insertarMeta(meta);
    }
    
    /**
     * Obtiene todas las metas financieras de la base de datos
     */
    public List<MetaFinanciera> obtenerMetas() throws SQLException {
        return metaDAO.obtenerTodasMetas();
    }
    
    /**
     * Obtiene solo las metas completadas
     */
    public List<MetaFinanciera> obtenerMetasCompletadas() throws SQLException {
        return metaDAO.obtenerMetasCompletadas();
    }
    
    /**
     * Obtiene solo las metas en progreso (no completadas)
     */
    public List<MetaFinanciera> obtenerMetasEnProgreso() throws SQLException {
        return metaDAO.obtenerMetasEnProgreso();
    }
    
    /**
     * Contribuye un monto a una meta específica
     */
    public void contribuirAMeta(String nombreMeta, double monto) throws SQLException {
        metaDAO.contribuirAMeta(nombreMeta, monto);
    }
    
    /**
     * Actualiza el monto actual de una meta (reemplaza el valor existente)
     */
    public void actualizarMontoMeta(String nombreMeta, double nuevoMonto) throws SQLException {
        metaDAO.actualizarMontoMeta(nombreMeta, nuevoMonto);
    }
    
    /**
     * Elimina una meta de la base de datos
     */
    public void eliminarMeta(String nombreMeta) throws SQLException {
        metaDAO.eliminarMeta(nombreMeta);
    }
    
    /**
     * Obtiene una meta específica por su nombre
     */
    public MetaFinanciera obtenerMetaPorNombre(String nombre) throws SQLException {
        return metaDAO.obtenerMetaPorNombre(nombre);
    }
    
    /**
     * Verifica si una meta existe en la base de datos
     */
    public boolean existeMeta(String nombreMeta) throws SQLException {
        return metaDAO.obtenerMetaPorNombre(nombreMeta) != null;
    }
}
