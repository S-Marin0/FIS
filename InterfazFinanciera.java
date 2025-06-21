package Interfaz;

// Archivo: InterfazFinanciera.java
import fachada.SistemaFinancieroFacade;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import modelos.MetaFinanciera;
import modelos.Presupuesto;
import modelos.Transaccion;

public class InterfazFinanciera extends JFrame {
    private SistemaFinancieroFacade sistema;
    private JTabbedPane tabbedPane;
    
    // Componentes para transacciones
    private JTextField txtDescripcion, txtMonto, txtCategoria;
    private JComboBox<String> cmbTipoTransaccion;
    private JTable tablaTransacciones;
    private DefaultTableModel modeloTablaTransacciones;
    
    // Componentes para metas
    private JTextField txtNombreMeta, txtMontoMeta, txtDescripcionMeta;
    private JSpinner spnFechaMeta;
    private JTable tablaMetas;
    private DefaultTableModel modeloTablaMetas;
    
    // Componentes para presupuestos
    private JTextField txtNombrePresupuesto, txtCategoriaPresupuesto, txtLimitePresupuesto;
    private JSpinner spnMesPresupuesto, spnAñoPresupuesto;
    private JTable tablaPresupuestos;
    private DefaultTableModel modeloTablaPresupuestos;
    
    // Componentes para resumen
    private JLabel lblBalance, lblIngresos, lblGastos;
    private GraficoPresupuesto graficoPresupuesto;
    
    public InterfazFinanciera(SistemaFinancieroFacade sistema) {
        this.sistema = sistema;
        initComponents();
        configurarVentana();
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Crear pestañas
        tabbedPane.addTab("Transacciones", crearPanelTransacciones());
        tabbedPane.addTab("Metas", crearPanelMetas());
        tabbedPane.addTab("Presupuestos", crearPanelPresupuestos());
        tabbedPane.addTab("Resumen", crearPanelResumen());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel crearPanelTransacciones() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de entrada
        JPanel panelEntrada = new JPanel(new GridBagLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Nueva Transacción"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campos de entrada
        gbc.gridx = 0; gbc.gridy = 0; panelEntrada.add(new JLabel("Tipo:"), gbc);
        cmbTipoTransaccion = new JComboBox<>(new String[]{"INGRESO", "GASTO"});
        gbc.gridx = 1; panelEntrada.add(cmbTipoTransaccion, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panelEntrada.add(new JLabel("Descripción:"), gbc);
        txtDescripcion = new JTextField(15);
        gbc.gridx = 1; panelEntrada.add(txtDescripcion, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; panelEntrada.add(new JLabel("Monto:"), gbc);
        txtMonto = new JTextField(15);
        gbc.gridx = 1; panelEntrada.add(txtMonto, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; panelEntrada.add(new JLabel("Categoría:"), gbc);
        txtCategoria = new JTextField(15);
        gbc.gridx = 1; panelEntrada.add(txtCategoria, gbc);
        
        JButton btnAgregar = new JButton("Agregar Transacción");
        btnAgregar.addActionListener(e -> agregarTransaccion());
        gbc.gridx = 1; gbc.gridy = 4; panelEntrada.add(btnAgregar, gbc);
        
        // Tabla de transacciones
        String[] columnas = {"Fecha", "Tipo", "Descripción", "Monto", "Categoría"};
        modeloTablaTransacciones = new DefaultTableModel(columnas, 0);
        tablaTransacciones = new JTable(modeloTablaTransacciones);
        JScrollPane scrollTransacciones = new JScrollPane(tablaTransacciones);
        scrollTransacciones.setBorder(BorderFactory.createTitledBorder("Historial de Transacciones"));
        
        panel.add(panelEntrada, BorderLayout.NORTH);
        panel.add(scrollTransacciones, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelMetas() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de entrada
        JPanel panelEntrada = new JPanel(new GridBagLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Nueva Meta"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; panelEntrada.add(new JLabel("Nombre:"), gbc);
        txtNombreMeta = new JTextField(15);
        gbc.gridx = 1; panelEntrada.add(txtNombreMeta, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; panelEntrada.add(new JLabel("Monto Objetivo:"), gbc);
        txtMontoMeta = new JTextField(15);
        gbc.gridx = 1; panelEntrada.add(txtMontoMeta, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; panelEntrada.add(new JLabel("Fecha Límite:"), gbc);
        spnFechaMeta = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spnFechaMeta, "dd/MM/yyyy");
        spnFechaMeta.setEditor(editor);
        gbc.gridx = 1; panelEntrada.add(spnFechaMeta, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; panelEntrada.add(new JLabel("Descripción:"), gbc);
        txtDescripcionMeta = new JTextField(15);
        gbc.gridx = 1; panelEntrada.add(txtDescripcionMeta, gbc);
        
        JButton btnCrearMeta = new JButton("Crear Meta");
        btnCrearMeta.addActionListener(e -> crearMeta());
        gbc.gridx = 1; gbc.gridy = 4; panelEntrada.add(btnCrearMeta, gbc);
        
        // Tabla de metas
        String[] columnas = {"Nombre", "Objetivo", "Actual", "Progreso", "Fecha Límite"};
        modeloTablaMetas = new DefaultTableModel(columnas, 0);
        tablaMetas = new JTable(modeloTablaMetas);
        JScrollPane scrollMetas = new JScrollPane(tablaMetas);
        scrollMetas.setBorder(BorderFactory.createTitledBorder("Metas Financieras"));
        
        panel.add(panelEntrada, BorderLayout.NORTH);
        panel.add(scrollMetas, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelPresupuestos() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de entrada
        JPanel panelEntrada = new JPanel(new GridBagLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Gestión de Presupuestos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Primera fila - crear presupuesto
        gbc.gridx = 0; gbc.gridy = 0; panelEntrada.add(new JLabel("Nombre Presupuesto:"), gbc);
        txtNombrePresupuesto = new JTextField(10);
        gbc.gridx = 1; panelEntrada.add(txtNombrePresupuesto, gbc);
        
        gbc.gridx = 2; panelEntrada.add(new JLabel("Mes:"), gbc);
        spnMesPresupuesto = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
        gbc.gridx = 3; panelEntrada.add(spnMesPresupuesto, gbc);
        
        gbc.gridx = 4; panelEntrada.add(new JLabel("Año:"), gbc);
        spnAñoPresupuesto = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2030, 1));
        gbc.gridx = 5; panelEntrada.add(spnAñoPresupuesto, gbc);
        
        JButton btnCrearPresupuesto = new JButton("Crear Presupuesto");
        btnCrearPresupuesto.addActionListener(e -> crearPresupuesto());
        gbc.gridx = 6; panelEntrada.add(btnCrearPresupuesto, gbc);
        
        // Segunda fila - agregar categoría
        gbc.gridx = 0; gbc.gridy = 1; panelEntrada.add(new JLabel("Categoría:"), gbc);
        txtCategoriaPresupuesto = new JTextField(10);
        gbc.gridx = 1; panelEntrada.add(txtCategoriaPresupuesto, gbc);
        
        gbc.gridx = 2; panelEntrada.add(new JLabel("Límite:"), gbc);
        txtLimitePresupuesto = new JTextField(10);
        gbc.gridx = 3; panelEntrada.add(txtLimitePresupuesto, gbc);
        
        JButton btnAgregarCategoria = new JButton("Agregar Categoría");
        btnAgregarCategoria.addActionListener(e -> agregarCategoriaPresupuesto());
        gbc.gridx = 4; gbc.gridwidth = 2; panelEntrada.add(btnAgregarCategoria, gbc);
        
        // Tabla de presupuestos
        String[] columnas = {"Presupuesto", "Mes/Año", "Total Presupuestado", "Total Gastado", "Estado"};
        modeloTablaPresupuestos = new DefaultTableModel(columnas, 0);
        tablaPresupuestos = new JTable(modeloTablaPresupuestos);
        JScrollPane scrollPresupuestos = new JScrollPane(tablaPresupuestos);
        scrollPresupuestos.setBorder(BorderFactory.createTitledBorder("Presupuestos"));
        
        panel.add(panelEntrada, BorderLayout.NORTH);
        panel.add(scrollPresupuestos, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de estadísticas
        JPanel panelEstadisticas = new JPanel(new GridLayout(1, 3, 10, 10));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("Resumen Financiero"));
        
        lblIngresos = new JLabel("Ingresos: $0.00", SwingConstants.CENTER);
        lblIngresos.setOpaque(true);
        lblIngresos.setBackground(Color.GREEN);
        lblIngresos.setForeground(Color.WHITE);
        lblIngresos.setFont(lblIngresos.getFont().deriveFont(Font.BOLD, 16f));
        
        lblGastos = new JLabel("Gastos: $0.00", SwingConstants.CENTER);
        lblGastos.setOpaque(true);
        lblGastos.setBackground(Color.RED);
        lblGastos.setForeground(Color.WHITE);
        lblGastos.setFont(lblGastos.getFont().deriveFont(Font.BOLD, 16f));
        
        lblBalance = new JLabel("Balance: $0.00", SwingConstants.CENTER);
        lblBalance.setOpaque(true);
        lblBalance.setBackground(Color.BLUE);
        lblBalance.setForeground(Color.WHITE);
        lblBalance.setFont(lblBalance.getFont().deriveFont(Font.BOLD, 16f));
        
        panelEstadisticas.add(lblIngresos);
        panelEstadisticas.add(lblGastos);
        panelEstadisticas.add(lblBalance);
        
        // Gráfico de presupuesto
        graficoPresupuesto = new GraficoPresupuesto();
        
        // Botón de actualizar
        JButton btnActualizar = new JButton("Actualizar Resumen");
        btnActualizar.addActionListener(e -> actualizarResumen());
        
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnActualizar);
        
        panel.add(panelEstadisticas, BorderLayout.NORTH);
        panel.add(graficoPresupuesto, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void configurarVentana() {
        setTitle("Sistema Financiero Personal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Actualizar datos iniciales
        actualizarTablas();
        actualizarResumen();
    }
    
    // Métodos de eventos
    private void agregarTransaccion() {
        try {
            String descripcion = txtDescripcion.getText().trim();
            double monto = Double.parseDouble(txtMonto.getText().trim());
            String categoria = txtCategoria.getText().trim();
            String tipo = (String) cmbTipoTransaccion.getSelectedItem();
            
            if (descripcion.isEmpty() || categoria.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor complete todos los campos");
                return;
            }
            
            if (tipo.equals("INGRESO")) {
                sistema.registrarIngreso(descripcion, monto, categoria);
            } else {
                sistema.registrarGasto(descripcion, monto, categoria);
            }
            
            // Limpiar campos
            txtDescripcion.setText("");
            txtMonto.setText("");
            txtCategoria.setText("");
            
            // Actualizar tablas
            actualizarTablaTransacciones();
            actualizarResumen();
            
            JOptionPane.showMessageDialog(this, "Transacción registrada exitosamente");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido");
        }
    }
    
    private void crearMeta() {
        try {
            String nombre = txtNombreMeta.getText().trim();
            double monto = Double.parseDouble(txtMontoMeta.getText().trim());
            String descripcion = txtDescripcionMeta.getText().trim();
            
            if (nombre.isEmpty() || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor complete todos los campos");
                return;
            }
            
            java.util.Date fechaUtil = (java.util.Date) spnFechaMeta.getValue();
            LocalDate fecha = fechaUtil.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            sistema.crearMeta(nombre, monto, fecha, descripcion);
            
            // Limpiar campos
            txtNombreMeta.setText("");
            txtMontoMeta.setText("");
            txtDescripcionMeta.setText("");
            
            actualizarTablaMetas();
            JOptionPane.showMessageDialog(this, "Meta creada exitosamente");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido");
        }
    }
    
    private void crearPresupuesto() {
        String nombre = txtNombrePresupuesto.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un nombre para el presupuesto");
            return;
        }
        
        int mes = (Integer) spnMesPresupuesto.getValue();
        int año = (Integer) spnAñoPresupuesto.getValue();
        
        sistema.crearPresupuesto(nombre, mes, año);
        
        txtNombrePresupuesto.setText("");
        actualizarTablaPresupuestos();
        JOptionPane.showMessageDialog(this, "Presupuesto creado exitosamente");
    }
    
    private void agregarCategoriaPresupuesto() {
        try {
            String nombrePresupuesto = txtNombrePresupuesto.getText().trim();
            String categoria = txtCategoriaPresupuesto.getText().trim();
            double limite = Double.parseDouble(txtLimitePresupuesto.getText().trim());
            
            if (nombrePresupuesto.isEmpty() || categoria.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor complete todos los campos");
                return;
            }
            
            sistema.agregarCategoriaAPresupuesto(nombrePresupuesto, categoria, limite);
            
            txtCategoriaPresupuesto.setText("");
            txtLimitePresupuesto.setText("");
            
            actualizarTablaPresupuestos();
            JOptionPane.showMessageDialog(this, "Categoría agregada al presupuesto exitosamente");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El límite debe ser un número válido");
        }
    }
    
    private void actualizarTablas() {
        actualizarTablaTransacciones();
        actualizarTablaMetas();
        actualizarTablaPresupuestos();
    }
    
    private void actualizarTablaTransacciones() {
        modeloTablaTransacciones.setRowCount(0);
        List<Transaccion> transacciones = sistema.obtenerTransacciones();
        
        for (Transaccion t : transacciones) {
            Object[] fila = {
                t.getFecha().toString(),
                t.getTipo().toString(),
                t.getDescripcion(),
                String.format("$%.2f", t.getMonto()),
                t.getCategoria()
            };
            modeloTablaTransacciones.addRow(fila);
        }
    }
    
    private void actualizarTablaMetas() {
        modeloTablaMetas.setRowCount(0);
        List<MetaFinanciera> metas = sistema.obtenerMetas();
        
        for (MetaFinanciera m : metas) {
            Object[] fila = {
                m.getNombre(),
                String.format("$%.2f", m.getMontoObjetivo()),
                String.format("$%.2f", m.getMontoActual()),
                String.format("%.1f%%", m.getPorcentajeCompletado()),
                m.getFechaLimite().toString()
            };
            modeloTablaMetas.addRow(fila);
        }
    }
    
    private void actualizarTablaPresupuestos() {
        modeloTablaPresupuestos.setRowCount(0);
        List<Presupuesto> presupuestos = sistema.obtenerPresupuestos();
        
        for (Presupuesto p : presupuestos) {
            String estado = p.getTotalGastado() > p.getTotalPresupuestado() ? "Excedido" : "Normal";
            Object[] fila = {
                p.getNombre(),
                p.getMes() + "/" + p.getAño(),
                String.format("$%.2f", p.getTotalPresupuestado()),
                String.format("$%.2f", p.getTotalGastado()),
                estado
            };
            modeloTablaPresupuestos.addRow(fila);
        }
    }
    
    private void actualizarResumen() {
        double ingresos = sistema.obtenerTotalIngresos();
        double gastos = sistema.obtenerTotalGastos();
        double balance = sistema.obtenerBalance();
        
        lblIngresos.setText(String.format("Ingresos: $%.2f", ingresos));
        lblGastos.setText(String.format("Gastos: $%.2f", gastos));
        lblBalance.setText(String.format("Balance: $%.2f", balance));
        
        // Cambiar color del balance según sea positivo o negativo
        if (balance >= 0) {
            lblBalance.setBackground(Color.BLUE);
        } else {
            lblBalance.setBackground(Color.ORANGE);
        }
        
        // Actualizar gráfico
        graficoPresupuesto.actualizarDatos(sistema.obtenerGastosPorCategoria());
        graficoPresupuesto.repaint();
    }
}