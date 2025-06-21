package servicios;

import java.util.*;
import modelos.Presupuesto;

public class GestorPresupuestos {
    private List<Presupuesto> presupuestos;
    
    public GestorPresupuestos() {
        this.presupuestos = new ArrayList<>();
    }
    
    public void agregarPresupuesto(Presupuesto presupuesto) {
        presupuestos.add(presupuesto);
    }
    
    public List<Presupuesto> obtenerPresupuestos() {
        return new ArrayList<>(presupuestos);
    }
    
    public Presupuesto obtenerPresupuestoActual() {
        int mesActual = java.time.LocalDate.now().getMonthValue();
        int añoActual = java.time.LocalDate.now().getYear();
        
        return presupuestos.stream()
                .filter(p -> p.getMes() == mesActual && p.getAño() == añoActual)
                .findFirst()
                .orElse(null);
    }
    
    public void registrarGastoEnPresupuesto(String categoria, double monto) {
        Presupuesto presupuestoActual = obtenerPresupuestoActual();
        if (presupuestoActual != null) {
            presupuestoActual.registrarGasto(categoria, monto);
        }
    }
}