package Interfaz;

import fachada.SistemaFinancieroFacade;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelos.MetaFinanciera;
import modelos.Presupuesto;
import modelos.Transaccion;

public class InterfazFinanciera extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(InterfazFinanciera.class.getName());
    private SistemaFinancieroFacade sistema;
    private JTabbedPane tabbedPane;

    private JTextField txtDescripcion, txtMonto, txtCategoria;
    private JComboBox<String> cmbTipoTransaccion;
    private JTable tablaTransacciones;
    private DefaultTableModel modeloTablaTransacciones;

    private JTextField txtNombreMeta, txtMontoMeta, txtDescripcionMeta;
    private JSpinner spnFechaMeta;
    private JTable tablaMetas;
    private DefaultTableModel modeloTablaMetas;
    // Nuevos componentes para contribuir a metas
    private JComboBox<String> cmbMetasContribucion;
    private JTextField txtMontoContribucion;
    // El botón btnContribuirAMeta se puede declarar aquí o localmente en crearPanelMetas()

    private JTextField txtNombrePresupuesto, txtCategoriaPresupuesto, txtLimitePresupuesto;
    private JSpinner spnMesPresupuesto, spnAñoPresupuesto;
    private JTable tablaPresupuestos;
    private DefaultTableModel modeloTablaPresupuestos;

    private JLabel lblBalance, lblIngresos, lblGastos;
    private GraficoPresupuesto graficoPresupuesto;

    public InterfazFinanciera(SistemaFinancieroFacade sistema) {
        LOGGER.info("Inicializando InterfazFinanciera...");
        this.sistema = sistema;
        initComponents();
        configurarVentana();
        LOGGER.info("InterfazFinanciera inicializada y configurada.");
    }

    private void initComponents() {
        LOGGER.fine("initComponents() - Creando pestañas y componentes principales.");
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Transacciones", crearPanelTransacciones());
        tabbedPane.addTab("Metas", crearPanelMetas());
        tabbedPane.addTab("Presupuestos", crearPanelPresupuestos());
        tabbedPane.addTab("Resumen", crearPanelResumen());
        add(tabbedPane, BorderLayout.CENTER);
        LOGGER.fine("initComponents() - Pestañas agregadas.");
    }

    private JPanel crearPanelTransacciones() {
        LOGGER.fine("Creando panel de Transacciones...");
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelEntrada = new JPanel(new GridBagLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Nueva Transacción"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

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

        String[] columnas = {"Fecha", "Tipo", "Descripción", "Monto", "Categoría"};
        modeloTablaTransacciones = new DefaultTableModel(columnas, 0);
        tablaTransacciones = new JTable(modeloTablaTransacciones);
        JScrollPane scrollTransacciones = new JScrollPane(tablaTransacciones);
        scrollTransacciones.setBorder(BorderFactory.createTitledBorder("Historial de Transacciones"));

        panel.add(panelEntrada, BorderLayout.NORTH);
        panel.add(scrollTransacciones, BorderLayout.CENTER);
        LOGGER.fine("Panel de Transacciones creado.");
        return panel;
    }

    private JPanel crearPanelMetas() {
        LOGGER.fine("Creando panel de Metas...");
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelEntrada = new JPanel(new GridBagLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Nueva Meta"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

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

        // Panel para contribuir a meta
        JPanel panelContribucion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelContribucion.setBorder(BorderFactory.createTitledBorder("Contribuir a Meta Existente"));

        panelContribucion.add(new JLabel("Seleccionar Meta:"));
        cmbMetasContribucion = new JComboBox<>();
        // cmbMetasContribucion será poblado por actualizarComboBoxMetas()
        panelContribucion.add(cmbMetasContribucion);

        panelContribucion.add(new JLabel("Monto a Contribuir:"));
        txtMontoContribucion = new JTextField(10);
        panelContribucion.add(txtMontoContribucion);

        JButton btnContribuirAMeta = new JButton("Contribuir");
        btnContribuirAMeta.addActionListener(e -> contribuirAMetaSeleccionada());
        panelContribucion.add(btnContribuirAMeta);

        // Layout general del panel de metas
        // El panel de entrada para nueva meta y el panel de contribución irán en un panel superior.
        JPanel panelSuperiorMetas = new JPanel(new BorderLayout());
        panelSuperiorMetas.add(panelEntrada, BorderLayout.NORTH); // Panel para crear nueva meta
        panelSuperiorMetas.add(panelContribucion, BorderLayout.SOUTH); // Panel para contribuir

        String[] columnasMetas = {"Nombre", "Objetivo", "Actual", "Progreso", "Fecha Límite"};
        modeloTablaMetas = new DefaultTableModel(columnasMetas, 0);
        tablaMetas = new JTable(modeloTablaMetas);
        JScrollPane scrollMetas = new JScrollPane(tablaMetas);
        scrollMetas.setBorder(BorderFactory.createTitledBorder("Metas Financieras"));

        panel.add(panelSuperiorMetas, BorderLayout.NORTH); // Panel superior con ambos sub-paneles
        panel.add(scrollMetas, BorderLayout.CENTER); // Tabla de metas abajo
        LOGGER.fine("Panel de Metas creado con sección de contribución.");
        return panel;
    }

    private JPanel crearPanelPresupuestos() {
        LOGGER.fine("Creando panel de Presupuestos...");
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelEntrada = new JPanel(new GridBagLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Gestión de Presupuestos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

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

        // Segunda fila - agregar/actualizar especificación
        gbc.gridx = 0; gbc.gridy = 1; panelEntrada.add(new JLabel("Especificación:"), gbc); // Cambio de etiqueta
        txtCategoriaPresupuesto = new JTextField(10);
        gbc.gridx = 1; panelEntrada.add(txtCategoriaPresupuesto, gbc);

        gbc.gridx = 2; panelEntrada.add(new JLabel("Valor:"), gbc); // Cambio de etiqueta
        txtLimitePresupuesto = new JTextField(10);
        gbc.gridx = 3; panelEntrada.add(txtLimitePresupuesto, gbc);

        JButton btnActualizarPresupuesto = new JButton("Actualizar Presupuesto"); // Cambio de texto del botón
        btnActualizarPresupuesto.addActionListener(e -> agregarCategoriaPresupuesto());
        gbc.gridx = 4; gbc.gridwidth = 2; panelEntrada.add(btnActualizarPresupuesto, gbc);

        // Tabla de presupuestos - Columnas actualizadas
        String[] columnasPresupuestos = {"Presupuesto", "Mes/Año", "Especificación", "Valor Asignado", "Gasto Realizado"};
        modeloTablaPresupuestos = new DefaultTableModel(columnasPresupuestos, 0);
        tablaPresupuestos = new JTable(modeloTablaPresupuestos);
        JScrollPane scrollPresupuestos = new JScrollPane(tablaPresupuestos);
        scrollPresupuestos.setBorder(BorderFactory.createTitledBorder("Detalle de Presupuestos por Especificación"));

        panel.add(panelEntrada, BorderLayout.NORTH);
        panel.add(scrollPresupuestos, BorderLayout.CENTER);
        LOGGER.fine("Panel de Presupuestos creado con nuevas etiquetas y columnas.");
        return panel;
    }

    private JPanel crearPanelResumen() {
        LOGGER.fine("Creando panel de Resumen...");
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelEstadisticas = new JPanel(new GridLayout(1, 3, 10, 10));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("Resumen Financiero"));

        lblIngresos = new JLabel("Ingresos: $0.00", SwingConstants.CENTER);
        lblIngresos.setOpaque(true);
        lblIngresos.setBackground(new Color(34, 139, 34));
        lblIngresos.setForeground(Color.WHITE);
        lblIngresos.setFont(lblIngresos.getFont().deriveFont(Font.BOLD, 16f));

        lblGastos = new JLabel("Gastos: $0.00", SwingConstants.CENTER);
        lblGastos.setOpaque(true);
        lblGastos.setBackground(new Color(220, 20, 60));
        lblGastos.setForeground(Color.WHITE);
        lblGastos.setFont(lblGastos.getFont().deriveFont(Font.BOLD, 16f));

        lblBalance = new JLabel("Balance: $0.00", SwingConstants.CENTER);
        lblBalance.setOpaque(true);
        lblBalance.setBackground(new Color(70, 130, 180));
        lblBalance.setForeground(Color.WHITE);
        lblBalance.setFont(lblBalance.getFont().deriveFont(Font.BOLD, 16f));

        panelEstadisticas.add(lblIngresos);
        panelEstadisticas.add(lblGastos);
        panelEstadisticas.add(lblBalance);

        graficoPresupuesto = new GraficoPresupuesto();
        JButton btnActualizar = new JButton("Actualizar Resumen");
        btnActualizar.addActionListener(e -> actualizarResumen());
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnActualizar);

        panel.add(panelEstadisticas, BorderLayout.NORTH);
        panel.add(graficoPresupuesto, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);
        LOGGER.fine("Panel de Resumen creado.");
        return panel;
    }

    private void configurarVentana() {
        LOGGER.fine("Configurando ventana principal...");
        setTitle("Sistema Financiero Personal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        LOGGER.info("Actualizando tablas y resumen iniciales...");
        actualizarTablas(); // Esto ya llama a actualizarComboBoxMetas()
        actualizarResumen();
        // No es necesario llamar a actualizarComboBoxMetas() directamente aquí si actualizarTablas() ya lo hace.
        // Pero si queremos una carga explícita e independiente al inicio:
        // actualizarComboBoxMetas();
        LOGGER.info("Ventana principal configurada.");
    }

    private void agregarTransaccion() {
        LOGGER.info("Evento: agregarTransaccion iniciado.");
        try {
            String descripcion = txtDescripcion.getText().trim();
            String montoStr = txtMonto.getText().trim();
            String categoria = txtCategoria.getText().trim();
            String tipo = (String) cmbTipoTransaccion.getSelectedItem();
            LOGGER.fine("Datos leídos: Desc=''" + descripcion + "'', Monto=''" + montoStr + "'', Cat=''" + categoria + "'', Tipo=''" + tipo + "''");

            if (descripcion.isEmpty() || categoria.isEmpty() || montoStr.isEmpty()) {
                LOGGER.warning("Campos incompletos para agregar transacción.");
                JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double monto = Double.parseDouble(montoStr);
            LOGGER.fine("Monto parseado: " + monto);

            LOGGER.info("Llamando a fachada para registrar " + tipo + "...");
            boolean exitoOperacion;
            if (tipo.equals("INGRESO")) {
                exitoOperacion = sistema.registrarIngreso(descripcion, monto, categoria);
            } else {
                exitoOperacion = sistema.registrarGasto(descripcion, monto, categoria);
            }
            LOGGER.info("Resultado de fachada para registrar " + tipo + ": " + exitoOperacion);

            if (exitoOperacion) {
                txtDescripcion.setText("");
                txtMonto.setText("");
                txtCategoria.setText("");
                actualizarTablaTransacciones();
                actualizarResumen();
                JOptionPane.showMessageDialog(this, "Transacción registrada exitosamente.");
                LOGGER.info("Transacción registrada y UI actualizada.");
            } else {
                 JOptionPane.showMessageDialog(this, "Error al registrar la transacción. Verifique los datos o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                 LOGGER.warning("Registro de transacción fallido según la fachada.");
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error de formato de número al agregar transacción.", e);
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al agregar transacción.", e);
            JOptionPane.showMessageDialog(this, "Error inesperado al registrar transacción: " + e.getMessage(), "Error Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarComboBoxMetas() {
        LOGGER.fine("Actualizando ComboBox de metas para contribución...");
        if (cmbMetasContribucion == null) {
            LOGGER.warning("cmbMetasContribucion es null, no se puede actualizar.");
            return;
        }
        try {
            cmbMetasContribucion.removeAllItems(); // Limpiar ítems existentes
            LOGGER.finer("Llamando a fachada: obtenerMetasEnProgreso");
            List<MetaFinanciera> metasEnProgreso = sistema.obtenerMetasEnProgreso();

            if (metasEnProgreso != null && !metasEnProgreso.isEmpty()) {
                LOGGER.finer("Fachada devolvió " + metasEnProgreso.size() + " metas en progreso.");
                for (MetaFinanciera meta : metasEnProgreso) {
                    cmbMetasContribucion.addItem(meta.getNombre());
                }
                cmbMetasContribucion.setEnabled(true);
                // Habilitar también el campo de monto y el botón de contribuir si estaban deshabilitados
                if (txtMontoContribucion != null) txtMontoContribucion.setEnabled(true);
                // Suponiendo que btnContribuirAMeta es accesible o se maneja su estado donde se crea
            } else {
                LOGGER.info("No hay metas en progreso para mostrar en el ComboBox de contribución.");
                cmbMetasContribucion.addItem("No hay metas para contribuir");
                cmbMetasContribucion.setEnabled(false);
                if (txtMontoContribucion != null) txtMontoContribucion.setEnabled(false);
            }
            LOGGER.fine("ComboBox de metas para contribución actualizado.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar ComboBox de metas para contribución.", e);
            JOptionPane.showMessageDialog(this, "Error al cargar lista de metas para contribución: " + e.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
            if (cmbMetasContribucion != null) {
                cmbMetasContribucion.removeAllItems();
                cmbMetasContribucion.addItem("Error al cargar metas");
                cmbMetasContribucion.setEnabled(false);
            }
            if (txtMontoContribucion != null) txtMontoContribucion.setEnabled(false);
        }
    }

    private void contribuirAMetaSeleccionada() {
        LOGGER.info("Evento: contribuirAMetaSeleccionada iniciado.");
        try {
            Object itemSeleccionado = cmbMetasContribucion.getSelectedItem();
            if (itemSeleccionado == null || itemSeleccionado.toString().equals("No hay metas para contribuir") || itemSeleccionado.toString().isEmpty()) {
                LOGGER.warning("No se seleccionó una meta válida para contribuir.");
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una meta válida de la lista.", "Meta no Seleccionada", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String nombreMeta = itemSeleccionado.toString();

            String montoStr = txtMontoContribucion.getText().trim();
            LOGGER.fine("Datos leídos: Meta=''" + nombreMeta + "'', Monto Contribución=''" + montoStr + "''");

            if (montoStr.isEmpty()) {
                LOGGER.warning("Monto de contribución vacío.");
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un monto a contribuir.", "Monto Vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double montoContribucion = Double.parseDouble(montoStr);
            if (montoContribucion <= 0) {
                LOGGER.warning("Monto de contribución no positivo: " + montoContribucion);
                JOptionPane.showMessageDialog(this, "El monto a contribuir debe ser positivo.", "Monto Inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LOGGER.fine("Monto de contribución parseado: " + montoContribucion);

            LOGGER.info("Llamando a fachada para contribuir a meta...");
            boolean exito = sistema.contribuirAMeta(nombreMeta, montoContribucion);
            LOGGER.info("Resultado de fachada para contribuir a meta: " + exito);

            if (exito) {
                txtMontoContribucion.setText("");
                actualizarTablaMetas();
                // actualizarComboBoxMetas(); // Se actualiza para reflejar posible cambio de estado de la meta (ej. completada)
                JOptionPane.showMessageDialog(this, "Contribución de " + String.format("$%.2f", montoContribucion) + " a la meta '" + nombreMeta + "' realizada exitosamente.");
                LOGGER.info("Contribución realizada y UI actualizada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al realizar la contribución a la meta '" + nombreMeta + "'. Verifique los datos o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Contribución a meta fallida según la fachada.");
            }

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error de formato de número al contribuir a meta.", e);
            JOptionPane.showMessageDialog(this, "El monto a contribuir debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al contribuir a meta.", e);
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + e.getMessage(), "Error Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearMeta() {
        LOGGER.info("Evento: crearMeta iniciado.");
        try {
            String nombre = txtNombreMeta.getText().trim();
            String montoStr = txtMontoMeta.getText().trim();
            String descripcion = txtDescripcionMeta.getText().trim();
            java.util.Date fechaUtil = (java.util.Date) spnFechaMeta.getValue();
            LOGGER.fine("Datos leídos: Nombre=''" + nombre + "'', Monto=''" + montoStr + "'', Desc=''" + descripcion + "'', FechaUtil=" + fechaUtil);

            if (nombre.isEmpty() || descripcion.isEmpty() || montoStr.isEmpty()) {
                LOGGER.warning("Campos incompletos para crear meta.");
                JOptionPane.showMessageDialog(this, "Por favor complete nombre, monto y descripción de la meta.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double montoObjetivo = Double.parseDouble(montoStr);
            LocalDate fecha = fechaUtil.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LOGGER.fine("Meta parseada: MontoObj=" + montoObjetivo + ", Fecha=" + fecha);

            LOGGER.info("Llamando a fachada para crear meta...");
            boolean exitoOperacion = sistema.crearMeta(nombre, montoObjetivo, fecha, descripcion);
            LOGGER.info("Resultado de fachada para crear meta: " + exitoOperacion);

            if(exitoOperacion) {
                txtNombreMeta.setText("");
                txtMontoMeta.setText("");
                txtDescripcionMeta.setText("");
                actualizarTablaMetas(); // Refresca la tabla de metas
                actualizarComboBoxMetas(); // Refresca el ComboBox con la nueva meta
                JOptionPane.showMessageDialog(this, "Meta creada exitosamente.");
                LOGGER.info("Meta creada y UI actualizada, ComboBox de metas actualizado.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear la meta. Verifique los datos o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Creación de meta fallida según la fachada.");
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error de formato de número al crear meta.", e);
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al crear meta.", e);
            JOptionPane.showMessageDialog(this, "Error inesperado al crear meta: " + e.getMessage(), "Error Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearPresupuesto() {
        LOGGER.info("Evento: crearPresupuesto iniciado.");
        try {
            String nombre = txtNombrePresupuesto.getText().trim();
            int mes = (Integer) spnMesPresupuesto.getValue();
            int año = (Integer) spnAñoPresupuesto.getValue();
            LOGGER.fine("Datos leídos: Nombre=''" + nombre + "'', Mes=" + mes + ", Año=" + año);

            if (nombre.isEmpty()) {
                LOGGER.warning("Nombre de presupuesto vacío.");
                JOptionPane.showMessageDialog(this, "Por favor ingrese un nombre para el presupuesto.", "Campo Incompleto", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LOGGER.info("Llamando a fachada para crear presupuesto...");
            boolean exito = sistema.crearPresupuesto(nombre, mes, año);
            LOGGER.info("Resultado de fachada para crear presupuesto: " + exito);

            if(exito) {
                actualizarTablaPresupuestos();
                JOptionPane.showMessageDialog(this, "Presupuesto '" + nombre + "' creado exitosamente para " + mes + "/" + año + ".");
                LOGGER.info("Presupuesto creado y UI actualizada.");
            } else {
                 JOptionPane.showMessageDialog(this, "Error al crear el presupuesto. Verifique los datos o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                 LOGGER.warning("Creación de presupuesto fallida según la fachada.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al crear presupuesto.", e);
            JOptionPane.showMessageDialog(this, "Ocurrió un error al crear el presupuesto: " + e.getMessage(), "Error Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarCategoriaPresupuesto() { //El nombre del método se mantiene, pero su botón asociado cambió
        LOGGER.info("Evento: agregarCategoriaPresupuesto (Actualizar Presupuesto) iniciado.");
        try {
            String nombrePresupuesto = txtNombrePresupuesto.getText().trim();
            String especificacion = txtCategoriaPresupuesto.getText().trim(); // Ahora es especificación
            String valorStr = txtLimitePresupuesto.getText().trim(); // Ahora es valor
            int mes = (Integer) spnMesPresupuesto.getValue();
            int año = (Integer) spnAñoPresupuesto.getValue();
            LOGGER.fine("Datos leídos: Presupuesto=''" + nombrePresupuesto + "'' ("+mes+"/"+año+"), Especificación=''" + especificacion + "'', Valor=''" + valorStr + "''");

            if (nombrePresupuesto.isEmpty() || especificacion.isEmpty() || valorStr.isEmpty()) {
                LOGGER.warning("Campos incompletos para actualizar presupuesto (agregar especificación).");
                JOptionPane.showMessageDialog(this, "Por favor ingrese Nombre Presupuesto, Mes, Año, Especificación y Valor.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double valor = Double.parseDouble(valorStr);
            LOGGER.fine("Valor parseado: " + valor);

            LOGGER.info("Llamando a fachada para agregar/actualizar especificación en presupuesto ("+nombrePresupuesto+", "+mes+", "+año+")...");
            boolean exito = sistema.agregarCategoriaAPresupuesto(nombrePresupuesto, mes, año, especificacion, valor);
            LOGGER.info("Resultado de fachada para agregar/actualizar especificación: " + exito);

            if (exito) {
                txtCategoriaPresupuesto.setText(""); // Limpiar campo de especificación
                txtLimitePresupuesto.setText("");    // Limpiar campo de valor
                actualizarTablaPresupuestos();
                JOptionPane.showMessageDialog(this, "Especificación '" + especificacion + "' actualizada/agregada al presupuesto '" + nombrePresupuesto + "' ("+mes+"/"+año+") exitosamente.");
                LOGGER.info("Especificación agregada/actualizada y UI actualizada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar/agregar la especificación al presupuesto. Verifique los datos o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Actualización de presupuesto (agregar especificación) fallida según la fachada.");
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error de formato de número al agregar especificación a presupuesto.", e);
            JOptionPane.showMessageDialog(this, "El Valor debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al agregar especificación a presupuesto.", e);
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + e.getMessage(), "Error Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarTablas() {
        LOGGER.info("Actualizando todas las tablas...");
        actualizarTablaTransacciones();
        actualizarTablaMetas();
        actualizarTablaPresupuestos();
        // Es buena idea actualizar el ComboBox de metas aquí también,
        // por si una meta se completa y debe desaparecer de la lista de contribución.
        actualizarComboBoxMetas();
        LOGGER.info("Todas las tablas actualizadas (o intento realizado).");
    }

    private void actualizarTablaTransacciones() {
        LOGGER.fine("Actualizando tabla de transacciones...");
        try {
            modeloTablaTransacciones.setRowCount(0);
            LOGGER.finer("Llamando a fachada: obtenerTransacciones");
            List<Transaccion> transacciones = sistema.obtenerTransacciones();
            if (transacciones != null) {
                LOGGER.finer("Fachada devolvió " + transacciones.size() + " transacciones.");
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
            } else {
                 LOGGER.warning("sistema.obtenerTransacciones() devolvió null.");
            }
            LOGGER.fine("Tabla de transacciones actualizada.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar la tabla de transacciones.", e);
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla de transacciones: " + e.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarTablaMetas() {
        LOGGER.fine("Actualizando tabla de metas...");
        try {
            modeloTablaMetas.setRowCount(0);
            LOGGER.finer("Llamando a fachada: obtenerMetas");
            List<MetaFinanciera> metas = sistema.obtenerMetas();
             if (metas != null) {
                LOGGER.finer("Fachada devolvió " + metas.size() + " metas.");
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
            } else {
                LOGGER.warning("sistema.obtenerMetas() devolvió null.");
            }
            LOGGER.fine("Tabla de metas actualizada.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar la tabla de metas.", e);
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla de metas: " + e.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarTablaPresupuestos() {
        LOGGER.fine("Actualizando tabla de presupuestos...");
        try {
            modeloTablaPresupuestos.setRowCount(0); // Limpiar tabla existente
            LOGGER.finer("Llamando a fachada: obtenerPresupuestos");
            List<Presupuesto> presupuestos = sistema.obtenerPresupuestos();

            if (presupuestos != null) {
                LOGGER.finer("Fachada devolvió " + presupuestos.size() + " presupuestos generales.");
                for (Presupuesto p : presupuestos) {
                    LOGGER.finer("Procesando presupuesto: " + p.getNombre() + " (" + p.getMes() + "/" + p.getAño() + ")");
                    if (p.getLimitesPorCategoria().isEmpty()) {
                        // Si un presupuesto no tiene especificaciones, podríamos mostrar una fila indicándolo
                        // o simplemente no mostrar nada para ese presupuesto en esta vista detallada.
                        // Por ahora, no se muestra nada si no hay especificaciones.
                        LOGGER.finer("Presupuesto '" + p.getNombre() + "' no tiene especificaciones.");
                    } else {
                        for (java.util.Map.Entry<String, Double> entry : p.getLimitesPorCategoria().entrySet()) {
                            String especificacionNombre = entry.getKey();
                            Double valorAsignado = entry.getValue();
                            Double gastoRealizado = p.getGastosPorCategoria().getOrDefault(especificacionNombre, 0.0);

                            Object[] fila = {
                                p.getNombre(),
                                p.getMes() + "/" + p.getAño(),
                                especificacionNombre,
                                String.format("$%.2f", valorAsignado),
                                String.format("$%.2f", gastoRealizado)
                            };
                            modeloTablaPresupuestos.addRow(fila);
                            LOGGER.finest("Fila agregada a tabla presupuestos: " + java.util.Arrays.toString(fila));
                        }
                    }
                }
            } else {
                 LOGGER.warning("sistema.obtenerPresupuestos() devolvió null.");
            }
            LOGGER.fine("Tabla de presupuestos actualizada con detalle por especificación.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar la tabla de presupuestos.", e);
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla de presupuestos: " + e.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarResumen() {
        LOGGER.fine("Actualizando panel de resumen...");
        try {
            LOGGER.finer("Llamando a fachada para datos de resumen...");
            double ingresos = sistema.obtenerTotalIngresos();
            double gastos = sistema.obtenerTotalGastos();
            double balance = sistema.obtenerBalance();
            LOGGER.finer("Datos de resumen obtenidos: Ing=" + ingresos + ", Gas=" + gastos + ", Bal=" + balance);

            lblIngresos.setText(String.format("Ingresos: $%.2f", ingresos));
            lblGastos.setText(String.format("Gastos: $%.2f", gastos));
            lblBalance.setText(String.format("Balance: $%.2f", balance));

            if (balance >= 0) {
                lblBalance.setBackground(new Color(70, 130, 180));
            } else {
                lblBalance.setBackground(new Color(255, 165, 0));
            }

            if (graficoPresupuesto != null && sistema !=null) {
                 LOGGER.finer("Actualizando datos del gráfico de presupuesto...");
                 graficoPresupuesto.actualizarDatos(sistema.obtenerGastosPorCategoria());
                 graficoPresupuesto.repaint();
                 LOGGER.finer("Gráfico de presupuesto actualizado.");
            }
            LOGGER.fine("Panel de resumen actualizado.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el resumen financiero.", e);
            JOptionPane.showMessageDialog(this, "Error al actualizar el resumen financiero: " + e.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
            lblIngresos.setText("Ingresos: Error");
            lblGastos.setText("Gastos: Error");
            lblBalance.setText("Balance: Error");
        }
    }
}
