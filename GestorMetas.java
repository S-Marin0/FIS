package servicios;

import java.util.*;
import java.util.stream.Collectors;
import modelos.MetaFinanciera;

public class GestorMetas {
    private List<MetaFinanciera> metas;
    
    public GestorMetas() {
        this.metas = new ArrayList<>();
    }
    
    public void agregarMeta(MetaFinanciera meta) {
        metas.add(meta);
    }
    
    public List<MetaFinanciera> obtenerMetas() {
        return new ArrayList<>(metas);
    }
    
    public List<MetaFinanciera> obtenerMetasCompletadas() {
        return metas.stream()
                .filter(MetaFinanciera::estaCompletada)
                .collect(Collectors.toList());
    }
    
    public List<MetaFinanciera> obtenerMetasEnProgreso() {
        return metas.stream()
                .filter(m -> !m.estaCompletada())
                .collect(Collectors.toList());
    }
    
    public void contribuirAMeta(String nombreMeta, double monto) {
        metas.stream()
                .filter(m -> m.getNombre().equals(nombreMeta))
                .findFirst()
                .ifPresent(m -> m.agregarMonto(monto));
    }
}