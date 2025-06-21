package servicios;

import java.util.*;
import java.util.stream.Collectors;
import modelos.MetaFinanciera;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestorMetas {
    private static final Logger LOGGER = Logger.getLogger(GestorMetas.class.getName());
    private List<MetaFinanciera> metas; // Opera en memoria, no usa DAO

    public GestorMetas() {
        this.metas = new ArrayList<>();
        LOGGER.info("GestorMetas (en memoria) inicializado.");
    }

    public void agregarMeta(MetaFinanciera meta) {
        if (meta == null) {
            LOGGER.warning("Intento de agregar meta null.");
            return;
        }
        LOGGER.log(Level.INFO, "Agregando meta (en memoria): Nombre=''{0}'', Objetivo={1}, Actual={2}, FechaLimite={3}",
            new Object[]{meta.getNombre(), meta.getMontoObjetivo(), meta.getMontoActual(), meta.getFechaLimite()});
        metas.add(meta);
        LOGGER.fine("Meta agregada. Total actual (en memoria): " + metas.size());
    }

    public List<MetaFinanciera> obtenerMetas() {
        LOGGER.log(Level.INFO, "Obteniendo todas las metas (en memoria). Total: {0}", metas.size());
        return new ArrayList<>(metas); // Devuelve copia
    }

    public List<MetaFinanciera> obtenerMetasCompletadas() {
        LOGGER.info("Obteniendo metas completadas (en memoria).");
        List<MetaFinanciera> resultado = metas.stream()
                .filter(MetaFinanciera::estaCompletada)
                .collect(Collectors.toList());
        LOGGER.log(Level.FINE, "Encontradas {0} metas completadas.", resultado.size());
        return resultado;
    }

    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        LOGGER.info("Obteniendo metas en progreso (en memoria).");
        List<MetaFinanciera> resultado = metas.stream()
                .filter(m -> !m.estaCompletada())
                .collect(Collectors.toList());
        LOGGER.log(Level.FINE, "Encontradas {0} metas en progreso.", resultado.size());
        return resultado;
    }

    public void contribuirAMeta(String nombreMeta, double monto) {
        LOGGER.log(Level.INFO, "Intentando contribuir {0} a meta (en memoria) ''{1}''", new Object[]{monto, nombreMeta});
        if (nombreMeta == null || nombreMeta.trim().isEmpty()) {
            LOGGER.warning("Intento de contribuir a meta con nombre null o vacío.");
            return;
        }
        Optional<MetaFinanciera> metaOptional = metas.stream()
                .filter(m -> m.getNombre().equals(nombreMeta))
                .findFirst();

        if (metaOptional.isPresent()) {
            MetaFinanciera meta = metaOptional.get();
            meta.agregarMonto(monto); // Asumiendo que este método existe en MetaFinanciera y actualiza montoActual y completada
            LOGGER.log(Level.INFO, "Contribución de {0} realizada a meta ''{1}''. Nuevo monto actual: {2}, Completada: {3}",
                new Object[]{monto, nombreMeta, meta.getMontoActual(), meta.estaCompletada()});
        } else {
            LOGGER.log(Level.WARNING, "No se encontró la meta ''{0}'' para realizar la contribución.", nombreMeta);
        }
    }
}
