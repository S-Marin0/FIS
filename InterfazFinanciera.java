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
    private JComboBox<String> cmbPresupuestosExistentes; // Nuevo ComboBox para seleccionar presupuestos

    // Componentes existentes del resumen
    private JLabel lblBalance, lblIngresos, lblGastos; // Totales generales
    private GraficoPresupuesto graficoPresupuesto;

    // Nuevos componentes para el resumen mejorado
    private JLabel lblIngresosMesActual, lblGastosMesActual;
    private JPanel panelProgresoPresupuesto; // Para mostrar porcentajes de categorías del presupuesto actual
    private JTextArea areaAlertas; // Para mostrar metas próximas y alertas de presupuesto

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

        // Columnas para el modelo de tabla, incluyendo ID para la lógica de eliminación
        String[] columnasModelo = {"ID", "Fecha", "Tipo", "Descripción", "Monto", "Categoría"};
        // Columnas que se mostrarán en la JTable (sin ID)
        String[] columnasVista = {"Fecha", "Tipo", "Descripción", "Monto", "Categoría"};

        modeloTablaTransacciones = new DefaultTableModel(columnasModelo, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que ninguna celda sea editable
            }
        };
        tablaTransacciones = new JTable(modeloTablaTransacciones);

        // Ocultar la columna ID de la vista de la tabla
        tablaTransacciones.removeColumn(tablaTransacciones.getColumnModel().getColumn(0));


        JScrollPane scrollTransacciones = new JScrollPane(tablaTransacciones);
        scrollTransacciones.setBorder(BorderFactory.createTitledBorder("Historial de Transacciones"));

        JButton btnEliminarTransaccion = new JButton("Eliminar Transacción Seleccionada");
        btnEliminarTransaccion.addActionListener(e -> eliminarTransaccionSeleccionada());

        JPanel panelBotonesTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonesTabla.add(btnEliminarTransaccion);

        panel.add(panelEntrada, BorderLayout.NORTH);
        panel.add(scrollTransacciones, BorderLayout.CENTER);
        panel.add(panelBotonesTabla, BorderLayout.SOUTH);
        LOGGER.fine("Panel de Transacciones creado con botón de eliminar.");
        return panel;
    }

    private void eliminarTransaccionSeleccionada() {
        LOGGER.info("Evento: eliminarTransaccionSeleccionada iniciado.");
        int filaSeleccionadaVista = tablaTransacciones.getSelectedRow();
        if (filaSeleccionadaVista == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una transacción de la tabla para eliminar.", "Ninguna Transacción Seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convertir el índice de la fila de la vista al índice del modelo (si la tabla está ordenada o filtrada)
        int filaSeleccionadaModelo = tablaTransacciones.convertRowIndexToModel(filaSeleccionadaVista);

        // El ID está en la columna 0 del modelo, que está oculta en la vista
        int idTransaccion = (Integer) modeloTablaTransacciones.getValueAt(filaSeleccionadaModelo, 0);
        String descTransaccion = (String) modeloTablaTransacciones.getValueAt(filaSeleccionadaModelo, 3); // Columna Descripción en el modelo

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar la transacción:\n'" + descTransaccion + "' (ID: " + idTransaccion + ")?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            LOGGER.fine("Usuario confirmó la eliminación de la transacción ID: " + idTransaccion);
            boolean exito = sistema.eliminarTransaccion(idTransaccion);
            if (exito) {
                actualizarTablaTransacciones();
                actualizarResumen(); // El resumen general y del mes actual pueden cambiar
                JOptionPane.showMessageDialog(this, "Transacción eliminada exitosamente.", "Eliminación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                LOGGER.info("Transacción ID " + idTransaccion + " eliminada y UI actualizada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar la transacción. Verifique la consola.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Eliminación de transacción ID " + idTransaccion + " fallida según la fachada.");
            }
        } else {
            LOGGER.fine("Usuario canceló la eliminación de la transacción ID: " + idTransaccion);
        }
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
        JPanel panelSuperiorMetas = new JPanel(new BorderLayout());
        panelSuperiorMetas.add(panelEntrada, BorderLayout.NORTH);
        panelSuperiorMetas.add(panelContribucion, BorderLayout.CENTER); // Contribución debajo de creación

        String[] columnasMetas = {"Nombre", "Objetivo", "Actual", "Progreso", "Fecha Límite"};
        modeloTablaMetas = new DefaultTableModel(columnasMetas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable
            }
        };
        tablaMetas = new JTable(modeloTablaMetas);
        JScrollPane scrollMetas = new JScrollPane(tablaMetas);
        scrollMetas.setBorder(BorderFactory.createTitledBorder("Metas Financieras"));

        JButton btnEliminarMeta = new JButton("Eliminar Meta Seleccionada");
        btnEliminarMeta.addActionListener(e -> eliminarMetaSeleccionada());
        JPanel panelBotonEliminarMeta = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonEliminarMeta.add(btnEliminarMeta);

        JPanel panelTablaYBotones = new JPanel(new BorderLayout());
        panelTablaYBotones.add(scrollMetas, BorderLayout.CENTER);
        panelTablaYBotones.add(panelBotonEliminarMeta, BorderLayout.SOUTH);

        panel.add(panelSuperiorMetas, BorderLayout.NORTH);
        panel.add(panelTablaYBotones, BorderLayout.CENTER);
        LOGGER.fine("Panel de Metas creado con sección de contribución y botón de eliminar.");
        return panel;
    }

    private void eliminarMetaSeleccionada() {
        LOGGER.info("Evento: eliminarMetaSeleccionada iniciado.");
        int filaSeleccionada = tablaMetas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una meta de la tabla para eliminar.", "Ninguna Meta Seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // El nombre de la meta está en la primera columna (índice 0)
        String nombreMeta = (String) tablaMetas.getValueAt(filaSeleccionada, 0);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar la meta: '" + nombreMeta + "'?",
                "Confirmar Eliminación de Meta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            LOGGER.fine("Usuario confirmó la eliminación de la meta: " + nombreMeta);
            boolean exito = sistema.eliminarMeta(nombreMeta);
            if (exito) {
                actualizarTablaMetas();
                actualizarComboBoxMetas(); // Para que desaparezca del combo de contribuciones
                // También podría ser necesario actualizar el resumen si las metas afectan alguna alerta
                actualizarResumen();
                JOptionPane.showMessageDialog(this, "Meta eliminada exitosamente.", "Eliminación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                LOGGER.info("Meta " + nombreMeta + " eliminada y UI actualizada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar la meta. Verifique la consola.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Eliminación de meta " + nombreMeta + " fallida según la fachada.");
            }
        } else {
            LOGGER.fine("Usuario canceló la eliminación de la meta: " + nombreMeta);
        }
    }

    private void eliminarPresupuestoSeleccionado() {
        LOGGER.info("Evento: eliminarPresupuestoSeleccionado iniciado.");
        Object itemSeleccionado = cmbPresupuestosExistentes.getSelectedItem();
        if (itemSeleccionado == null || itemSeleccionado.toString().equals("No hay presupuestos") || itemSeleccionado.toString().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un presupuesto de la lista para eliminar.", "Ningún Presupuesto Seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String presupuestoSeleccionadoStr = itemSeleccionado.toString();
        // Formato: "Nombre (Mes/Año)"
        String nombrePresupuesto = presupuestoSeleccionadoStr.substring(0, presupuestoSeleccionadoStr.lastIndexOf(" (")).trim();
        String mesAñoStr = presupuestoSeleccionadoStr.substring(presupuestoSeleccionadoStr.lastIndexOf(" (") + 2, presupuestoSeleccionadoStr.length() - 1);
        String[] partesMesAño = mesAñoStr.split("/");
        int mes = Integer.parseInt(partesMesAño[0]);
        int año = Integer.parseInt(partesMesAño[1]);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el presupuesto completo:\n'" + nombrePresupuesto + "' para " + mes + "/" + año + "?\nEsto eliminará también todas sus especificaciones asociadas.",
                "Confirmar Eliminación de Presupuesto", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            LOGGER.fine("Usuario confirmó la eliminación del presupuesto: " + nombrePresupuesto + " (" + mes + "/" + año + ")");
            boolean exito = sistema.eliminarPresupuesto(nombrePresupuesto, mes, año);
            if (exito) {
                actualizarComboBoxPresupuestos(); // Quitarlo del ComboBox
                actualizarTablaPresupuestos();  // Limpiar/actualizar la tabla de detalles
                actualizarResumen(); // El resumen podría cambiar (alertas de presupuesto)
                JOptionPane.showMessageDialog(this, "Presupuesto eliminado exitosamente.", "Eliminación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                LOGGER.info("Presupuesto " + nombrePresupuesto + " (" + mes + "/" + año + ") eliminado y UI actualizada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el presupuesto. Verifique la consola.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Eliminación de presupuesto " + nombrePresupuesto + " (" + mes + "/" + año + ") fallida según la fachada.");
            }
        } else {
            LOGGER.fine("Usuario canceló la eliminación del presupuesto: " + nombrePresupuesto + " (" + mes + "/" + año + ")");
        }
    }

    private JPanel crearPanelPresupuestos() {
        LOGGER.fine("Creando panel de Presupuestos...");
        JPanel panel = new JPanel(new BorderLayout(0, 10)); // Añadido espacio vertical entre componentes

        // Panel para crear un nuevo presupuesto
        JPanel panelCreacion = new JPanel(new GridBagLayout());
        panelCreacion.setBorder(BorderFactory.createTitledBorder("Crear Nuevo Presupuesto"));
        GridBagConstraints gbcCreacion = new GridBagConstraints();
        gbcCreacion.insets = new Insets(5, 5, 5, 5);
        gbcCreacion.anchor = GridBagConstraints.WEST;

        gbcCreacion.gridx = 0; gbcCreacion.gridy = 0; panelCreacion.add(new JLabel("Nombre Presupuesto:"), gbcCreacion);
        txtNombrePresupuesto = new JTextField(15);
        gbcCreacion.gridx = 1; panelCreacion.add(txtNombrePresupuesto, gbcCreacion);

        gbcCreacion.gridx = 0; gbcCreacion.gridy = 1; panelCreacion.add(new JLabel("Mes:"), gbcCreacion);
        spnMesPresupuesto = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1));
        gbcCreacion.gridx = 1; panelCreacion.add(spnMesPresupuesto, gbcCreacion);

        gbcCreacion.gridx = 0; gbcCreacion.gridy = 2; panelCreacion.add(new JLabel("Año:"), gbcCreacion);
        spnAñoPresupuesto = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2030, 1));
        gbcCreacion.gridx = 1; panelCreacion.add(spnAñoPresupuesto, gbcCreacion);

        JButton btnCrearPresupuesto = new JButton("Crear Presupuesto");
        btnCrearPresupuesto.addActionListener(e -> crearPresupuesto());
        gbcCreacion.gridx = 1; gbcCreacion.gridy = 3; gbcCreacion.anchor = GridBagConstraints.CENTER;
        panelCreacion.add(btnCrearPresupuesto, gbcCreacion);

        // Panel para agregar especificaciones a un presupuesto existente
        JPanel panelEspecificaciones = new JPanel(new GridBagLayout());
        panelEspecificaciones.setBorder(BorderFactory.createTitledBorder("Agregar Especificación a Presupuesto"));
        GridBagConstraints gbcEspecificaciones = new GridBagConstraints();
        gbcEspecificaciones.insets = new Insets(5, 5, 5, 5);
        gbcEspecificaciones.anchor = GridBagConstraints.WEST;

        gbcEspecificaciones.gridx = 0; gbcEspecificaciones.gridy = 0; panelEspecificaciones.add(new JLabel("Seleccionar Presupuesto:"), gbcEspecificaciones);
        cmbPresupuestosExistentes = new JComboBox<>();
        // cmbPresupuestosExistentes será poblado por actualizarComboBoxPresupuestos()
        gbcEspecificaciones.gridx = 1; gbcEspecificaciones.gridy = 0; gbcEspecificaciones.fill = GridBagConstraints.HORIZONTAL;
        panelEspecificaciones.add(cmbPresupuestosExistentes, gbcEspecificaciones);

        gbcEspecificaciones.gridx = 0; gbcEspecificaciones.gridy = 1; panelEspecificaciones.add(new JLabel("Especificación:"), gbcEspecificaciones);
        txtCategoriaPresupuesto = new JTextField(15); // Usamos el mismo nombre de variable pero representa la especificación
        gbcEspecificaciones.gridx = 1; gbcEspecificaciones.fill = GridBagConstraints.HORIZONTAL;
        panelEspecificaciones.add(txtCategoriaPresupuesto, gbcEspecificaciones);

        gbcEspecificaciones.gridx = 0; gbcEspecificaciones.gridy = 2; panelEspecificaciones.add(new JLabel("Valor:"), gbcEspecificaciones);
        txtLimitePresupuesto = new JTextField(15); // Usamos el mismo nombre de variable pero representa el valor
        gbcEspecificaciones.gridx = 1; gbcEspecificaciones.fill = GridBagConstraints.HORIZONTAL;
        panelEspecificaciones.add(txtLimitePresupuesto, gbcEspecificaciones);

        JButton btnAgregarEspecificacion = new JButton("Agregar Especificación");
        btnAgregarEspecificacion.addActionListener(e -> agregarEspecificacionAPresupuestoSeleccionado());
        gbcEspecificaciones.gridx = 1; gbcEspecificaciones.gridy = 3; gbcEspecificaciones.anchor = GridBagConstraints.CENTER;
        panelEspecificaciones.add(btnAgregarEspecificacion, gbcEspecificaciones);

        // Panel superior que contiene creación y especificaciones
        JPanel panelIntermedio = new JPanel(new BorderLayout(10,0));
        panelIntermedio.add(panelEspecificaciones, BorderLayout.CENTER);

        JButton btnEliminarPresupuesto = new JButton("Eliminar Presupuesto Seleccionado (del ComboBox)");
        btnEliminarPresupuesto.addActionListener(e -> eliminarPresupuestoSeleccionado());
        JPanel panelBotonEliminarPresupuesto = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotonEliminarPresupuesto.add(btnEliminarPresupuesto);
        panelIntermedio.add(panelBotonEliminarPresupuesto, BorderLayout.SOUTH);


        JPanel panelSuperior = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 fila, 2 columnas, con espacio horizontal
        panelSuperior.add(panelCreacion);
        // panelSuperior.add(panelEspecificaciones);
        panelSuperior.add(panelIntermedio);


        // Tabla de presupuestos (detalle de especificaciones)
        String[] columnasPresupuestos = {"Presupuesto", "Mes/Año", "Especificación", "Valor Asignado", "Gasto Realizado"};
        modeloTablaPresupuestos = new DefaultTableModel(columnasPresupuestos, 0);
        tablaPresupuestos = new JTable(modeloTablaPresupuestos);
        JScrollPane scrollPresupuestos = new JScrollPane(tablaPresupuestos);
        scrollPresupuestos.setBorder(BorderFactory.createTitledBorder("Detalle de Presupuestos por Especificación"));

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollPresupuestos, BorderLayout.CENTER);
        LOGGER.fine("Panel de Presupuestos reestructurado.");
        return panel;
    }

    private JPanel crearPanelResumen() {
        LOGGER.fine("Creando panel de Resumen...");
        JPanel panelPrincipalResumen = new JPanel(new BorderLayout(10, 10));

        // Panel para estadísticas generales y del mes actual
        JPanel panelEstadisticasSuperiores = new JPanel(new GridLayout(2, 1, 5, 5)); // 2 filas, 1 columna

        // Subpanel para Balance General (Ingresos, Gastos, Balance Totales)
        JPanel panelBalanceGeneral = new JPanel(new GridLayout(1, 3, 10, 10));
        panelBalanceGeneral.setBorder(BorderFactory.createTitledBorder("Balance General Acumulado"));
        lblIngresos = new JLabel("Ingresos Totales: $0.00", SwingConstants.CENTER);
        // ... (configuración de lblIngresos como antes)
        lblGastos = new JLabel("Gastos Totales: $0.00", SwingConstants.CENTER);
        // ... (configuración de lblGastos como antes)
        lblBalance = new JLabel("Balance Total: $0.00", SwingConstants.CENTER);
        // ... (configuración de lblBalance como antes)
        panelBalanceGeneral.add(lblIngresos);
        panelBalanceGeneral.add(lblGastos);
        panelBalanceGeneral.add(lblBalance);
        panelEstadisticasSuperiores.add(panelBalanceGeneral);

        // Subpanel para Ingresos vs Gastos del Mes Actual
        JPanel panelMesActual = new JPanel(new GridLayout(1, 2, 10, 10));
        panelMesActual.setBorder(BorderFactory.createTitledBorder("Finanzas del Mes Actual"));
        lblIngresosMesActual = new JLabel("Ingresos Mes: $0.00", SwingConstants.CENTER);
        lblGastosMesActual = new JLabel("Gastos Mes: $0.00", SwingConstants.CENTER);
        panelMesActual.add(lblIngresosMesActual);
        panelMesActual.add(lblGastosMesActual);
        panelEstadisticasSuperiores.add(panelMesActual);

        panelPrincipalResumen.add(panelEstadisticasSuperiores, BorderLayout.NORTH);

        // Panel central con Gráfico y Progreso de Presupuesto
        JPanel panelCentral = new JPanel(new BorderLayout(10,10));

        graficoPresupuesto = new GraficoPresupuesto(); // Desglose por categoría (mes actual)
        graficoPresupuesto.setBorder(BorderFactory.createTitledBorder("Desglose de Gastos por Categoría (Mes Actual)"));
        panelCentral.add(graficoPresupuesto, BorderLayout.CENTER);

        panelProgresoPresupuesto = new JPanel(); // Se poblará en actualizarResumen
        panelProgresoPresupuesto.setLayout(new BoxLayout(panelProgresoPresupuesto, BoxLayout.Y_AXIS));
        JScrollPane scrollProgreso = new JScrollPane(panelProgresoPresupuesto);
        scrollProgreso.setBorder(BorderFactory.createTitledBorder("Progreso del Presupuesto Actual (%)"));
        scrollProgreso.setPreferredSize(new Dimension(300, 150)); // Ajustar tamaño según necesidad
        panelCentral.add(scrollProgreso, BorderLayout.EAST);

        panelPrincipalResumen.add(panelCentral, BorderLayout.CENTER);

        // Panel inferior para Alertas y Botón de Actualizar
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        areaAlertas = new JTextArea(5, 30); // Próximos hitos o alertas
        areaAlertas.setEditable(false);
        areaAlertas.setLineWrap(true);
        areaAlertas.setWrapStyleWord(true);
        JScrollPane scrollAlertas = new JScrollPane(areaAlertas);
        scrollAlertas.setBorder(BorderFactory.createTitledBorder("Alertas y Próximos Hitos"));
        panelInferior.add(scrollAlertas, BorderLayout.CENTER);

        JButton btnActualizar = new JButton("Actualizar Resumen");
        btnActualizar.addActionListener(e -> actualizarResumen());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.add(btnActualizar);
        panelInferior.add(panelBoton, BorderLayout.SOUTH);

        panelPrincipalResumen.add(panelInferior, BorderLayout.SOUTH);

        // Aplicar estilos a los JLabels nuevos si es necesario (similar a los existentes)
        Font fontBold16 = new Font("SansSerif", Font.BOLD, 16); // Reusar o definir una fuente estándar
        Color colorIngresos = new Color(34, 139, 34);
        Color colorGastos = new Color(220, 20, 60);
        Color colorBalance = new Color(70, 130, 180);

        lblIngresos.setOpaque(true); lblIngresos.setBackground(colorIngresos); lblIngresos.setForeground(Color.WHITE); lblIngresos.setFont(fontBold16);
        lblGastos.setOpaque(true); lblGastos.setBackground(colorGastos); lblGastos.setForeground(Color.WHITE); lblGastos.setFont(fontBold16);
        lblBalance.setOpaque(true); lblBalance.setBackground(colorBalance); lblBalance.setForeground(Color.WHITE); lblBalance.setFont(fontBold16);

        lblIngresosMesActual.setOpaque(true); lblIngresosMesActual.setBackground(colorIngresos); lblIngresosMesActual.setForeground(Color.WHITE); lblIngresosMesActual.setFont(fontBold16);
        lblGastosMesActual.setOpaque(true); lblGastosMesActual.setBackground(colorGastos); lblGastosMesActual.setForeground(Color.WHITE); lblGastosMesActual.setFont(fontBold16);


        LOGGER.fine("Panel de Resumen mejorado creado.");
        return panelPrincipalResumen;
    }

    private void configurarVentana() {
        LOGGER.fine("Configurando ventana principal...");
        setTitle("Sistema Financiero Personal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        LOGGER.info("Actualizando tablas y resumen iniciales...");
        actualizarTablas(); // Esto llama a actualizarComboBoxMetas() y ahora también a actualizarComboBoxPresupuestos()
        actualizarResumen();
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
                actualizarComboBoxMetas(); // Actualizar ComboBox ya que la meta pudo haberse completado o su estado cambiado
                JOptionPane.showMessageDialog(this, "Contribución de " + String.format("$%.2f", montoContribucion) + " a la meta '" + nombreMeta + "' realizada exitosamente.");
                LOGGER.info("Contribución realizada y UI actualizada, ComboBox de metas actualizado.");
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
                txtNombrePresupuesto.setText(""); // Limpiar campo después de la creación
                // spnMesPresupuesto y spnAñoPresupuesto pueden mantenerse o resetearse según preferencia
                actualizarComboBoxPresupuestos(); // Actualizar el ComboBox con el nuevo presupuesto
                actualizarTablaPresupuestos(); // Actualizar la tabla (aunque inicialmente no tendrá especificaciones)
                JOptionPane.showMessageDialog(this, "Presupuesto '" + nombre + "' creado exitosamente para " + mes + "/" + año + ".");
                LOGGER.info("Presupuesto creado y UI actualizada, ComboBox de presupuestos actualizado.");
            } else {
                 JOptionPane.showMessageDialog(this, "Error al crear el presupuesto. Verifique si ya existe uno con el mismo nombre para ese mes/año o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                 LOGGER.warning("Creación de presupuesto fallida según la fachada.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al crear presupuesto.", e);
            JOptionPane.showMessageDialog(this, "Ocurrió un error al crear el presupuesto: " + e.getMessage(), "Error Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarEspecificacionAPresupuestoSeleccionado() {
        LOGGER.info("Evento: agregarEspecificacionAPresupuestoSeleccionado iniciado.");
        try {
            Object itemSeleccionado = cmbPresupuestosExistentes.getSelectedItem();
            if (itemSeleccionado == null || itemSeleccionado.toString().equals("No hay presupuestos") || itemSeleccionado.toString().isEmpty()) {
                LOGGER.warning("No se seleccionó un presupuesto válido.");
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un presupuesto de la lista.", "Presupuesto no Seleccionado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // El formato en el ComboBox es "Nombre (Mes/Año)"
            String presupuestoSeleccionadoStr = itemSeleccionado.toString();
            String nombrePresupuesto = presupuestoSeleccionadoStr.substring(0, presupuestoSeleccionadoStr.lastIndexOf(" (")).trim();
            String mesAñoStr = presupuestoSeleccionadoStr.substring(presupuestoSeleccionadoStr.lastIndexOf(" (") + 2, presupuestoSeleccionadoStr.length() - 1);
            String[] partesMesAño = mesAñoStr.split("/");
            int mes = Integer.parseInt(partesMesAño[0]);
            int año = Integer.parseInt(partesMesAño[1]);

            String especificacion = txtCategoriaPresupuesto.getText().trim();
            String valorStr = txtLimitePresupuesto.getText().trim();
            LOGGER.fine("Datos leídos: Presupuesto=''" + nombrePresupuesto + "'' ("+mes+"/"+año+"), Especificación=''" + especificacion + "'', Valor=''" + valorStr + "''");

            if (especificacion.isEmpty() || valorStr.isEmpty()) {
                LOGGER.warning("Campos incompletos para agregar especificación.");
                JOptionPane.showMessageDialog(this, "Por favor ingrese Especificación y Valor.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double valor = Double.parseDouble(valorStr);
            if (valor <= 0) {
                LOGGER.warning("Valor de especificación no positivo: " + valor);
                JOptionPane.showMessageDialog(this, "El Valor debe ser positivo.", "Valor Inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LOGGER.fine("Valor parseado: " + valor);

            LOGGER.info("Llamando a fachada para agregar especificación en presupuesto ("+nombrePresupuesto+", "+mes+", "+año+")...");
            boolean exito = sistema.agregarCategoriaAPresupuesto(nombrePresupuesto, mes, año, especificacion, valor);
            LOGGER.info("Resultado de fachada para agregar especificación: " + exito);

            if (exito) {
                txtCategoriaPresupuesto.setText("");
                txtLimitePresupuesto.setText("");
                actualizarTablaPresupuestos(); // Actualizar la tabla para mostrar la nueva especificación
                JOptionPane.showMessageDialog(this, "Especificación '" + especificacion + "' agregada al presupuesto '" + nombrePresupuesto + "' ("+mes+"/"+año+") exitosamente.");
                LOGGER.info("Especificación agregada y UI actualizada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar la especificación al presupuesto. Verifique los datos o consulte la consola.", "Error de Operación", JOptionPane.ERROR_MESSAGE);
                LOGGER.warning("Agregar especificación fallida según la fachada.");
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
        actualizarComboBoxMetas();
        actualizarComboBoxPresupuestos(); // Asegurarse de que el ComboBox de presupuestos también se actualiza
        LOGGER.info("Todas las tablas y ComboBoxes relevantes actualizados (o intento realizado).");
    }

    private void actualizarComboBoxPresupuestos() {
        LOGGER.fine("Actualizando ComboBox de presupuestos existentes...");
        if (cmbPresupuestosExistentes == null) {
            LOGGER.warning("cmbPresupuestosExistentes es null, no se puede actualizar.");
            return;
        }
        try {
            Object selectedItem = cmbPresupuestosExistentes.getSelectedItem(); // Guardar selección actual
            cmbPresupuestosExistentes.removeAllItems();
            LOGGER.finer("Llamando a fachada: obtenerPresupuestos");
            List<Presupuesto> presupuestos = sistema.obtenerPresupuestos(); // Asume que esto devuelve todos los presupuestos base

            if (presupuestos != null && !presupuestos.isEmpty()) {
                LOGGER.finer("Fachada devolvió " + presupuestos.size() + " presupuestos.");
                for (Presupuesto p : presupuestos) {
                    // Crear un String único para cada presupuesto, por ejemplo, "Nombre (Mes/Año)"
                    String itemPresupuesto = String.format("%s (%d/%d)", p.getNombre(), p.getMes(), p.getAño());
                    cmbPresupuestosExistentes.addItem(itemPresupuesto);
                }
                cmbPresupuestosExistentes.setEnabled(true);
                if (selectedItem != null && cmbPresupuestosExistentes.getModel().getSize() > 0) {
                    // Intentar restaurar la selección si todavía existe
                    for (int i = 0; i < cmbPresupuestosExistentes.getItemCount(); i++) {
                        if (selectedItem.equals(cmbPresupuestosExistentes.getItemAt(i))) {
                            cmbPresupuestosExistentes.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } else {
                LOGGER.info("No hay presupuestos existentes para mostrar en el ComboBox.");
                cmbPresupuestosExistentes.addItem("No hay presupuestos");
                cmbPresupuestosExistentes.setEnabled(false);
            }
            LOGGER.fine("ComboBox de presupuestos existentes actualizado.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar ComboBox de presupuestos existentes.", e);
            JOptionPane.showMessageDialog(this, "Error al cargar lista de presupuestos: " + e.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
            if (cmbPresupuestosExistentes != null) {
                cmbPresupuestosExistentes.removeAllItems();
                cmbPresupuestosExistentes.addItem("Error al cargar presupuestos");
                cmbPresupuestosExistentes.setEnabled(false);
            }
        }
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
                        t.getId(), // Columna 0 para el ID (oculta en la vista)
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
            // 1. Saldo Total Actual (ya existente, solo ajustamos etiqueta)
            LOGGER.finer("Llamando a fachada para datos de balance general...");
            double ingresosTotales = sistema.obtenerTotalIngresos();
            double gastosTotales = sistema.obtenerTotalGastos();
            double balanceTotal = sistema.obtenerBalance(); // Esto es ingresosTotales - gastosTotales
            LOGGER.finer("Datos de balance general: Ing=" + ingresosTotales + ", Gas=" + gastosTotales + ", Bal=" + balanceTotal);

            lblIngresos.setText(String.format("Ingresos Totales: $%.2f", ingresosTotales));
            lblGastos.setText(String.format("Gastos Totales: $%.2f", gastosTotales));
            lblBalance.setText(String.format("Balance Total: $%.2f", balanceTotal));
            lblBalance.setBackground(balanceTotal >= 0 ? new Color(70, 130, 180) : new Color(255, 165, 0));

            // 2. Ingresos vs. Gastos (mes actual)
            LOGGER.finer("Llamando a fachada para datos del mes actual...");
            double ingresosMes = sistema.obtenerTotalIngresosMesActual();
            double gastosMes = sistema.obtenerTotalGastosMesActual();
            LOGGER.finer("Datos del mes actual: IngMes=" + ingresosMes + ", GasMes=" + gastosMes);
            lblIngresosMesActual.setText(String.format("Ingresos Mes: $%.2f", ingresosMes));
            lblGastosMesActual.setText(String.format("Gastos Mes: $%.2f", gastosMes));

            // 3. Porcentaje de presupuesto utilizado (por categoría)
            LOGGER.finer("Actualizando progreso del presupuesto actual...");
            panelProgresoPresupuesto.removeAll(); // Limpiar antes de repoblar
            Presupuesto presupuestoActual = sistema.obtenerPresupuestoActual();
            if (presupuestoActual != null) {
                if (presupuestoActual.getLimitesPorCategoria().isEmpty()) {
                    panelProgresoPresupuesto.add(new JLabel("  Presupuesto '" + presupuestoActual.getNombre() + "' no tiene especificaciones."));
                } else {
                    panelProgresoPresupuesto.add(new JLabel("  Presupuesto: " + presupuestoActual.getNombre() + " (" + presupuestoActual.getMes() + "/" + presupuestoActual.getAño() + ")"));
                    for (Map.Entry<String, Double> entry : presupuestoActual.getLimitesPorCategoria().entrySet()) {
                        String categoria = entry.getKey();
                        double limite = entry.getValue();
                        double gastado = presupuestoActual.getGastosPorCategoria().getOrDefault(categoria, 0.0);
                        double porcentajeUsado = (limite > 0) ? (gastado / limite) * 100 : 0;

                        JLabel lblCategoriaProgreso = new JLabel(String.format("  - %s: $%.2f de $%.2f (%.1f%%)", categoria, gastado, limite, porcentajeUsado));
                        panelProgresoPresupuesto.add(lblCategoriaProgreso);
                        if (porcentajeUsado > 90) {
                           lblCategoriaProgreso.setForeground(Color.RED);
                        } else if (porcentajeUsado > 75) {
                           lblCategoriaProgreso.setForeground(Color.ORANGE);
                        }
                    }
                }
            } else {
                panelProgresoPresupuesto.add(new JLabel("  No hay presupuesto definido para el mes actual."));
            }
            panelProgresoPresupuesto.revalidate();
            panelProgresoPresupuesto.repaint();

            // 4. Desglose por categoría (gráfico del mes actual)
            if (graficoPresupuesto != null && sistema != null) {
                LOGGER.finer("Actualizando datos del gráfico de presupuesto (mes actual)...");
                Map<String, Double> gastosCategoriaMes = sistema.obtenerGastosPorCategoriaMesActual();
                graficoPresupuesto.actualizarDatos(gastosCategoriaMes);
                graficoPresupuesto.repaint();
                LOGGER.finer("Gráfico de presupuesto (mes actual) actualizado.");
            }

            // 5. Próximos hitos o alertas
            LOGGER.finer("Actualizando alertas y próximos hitos...");
            StringBuilder alertasTexto = new StringBuilder();
            final int DIAS_PROXIMIDAD_METAS = 7; // Metas para la próxima semana
            List<MetaFinanciera> metasProximas = sistema.obtenerAlertasMetasProximas(DIAS_PROXIMIDAD_METAS);
            if (!metasProximas.isEmpty()) {
                alertasTexto.append("Metas Próximas (en ").append(DIAS_PROXIMIDAD_METAS).append(" días):\n");
                for (MetaFinanciera meta : metasProximas) {
                    alertasTexto.append(String.format("  - %s (Vence: %s, Progreso: %.1f%%)\n", meta.getNombre(), meta.getFechaLimite().toString(), meta.getPorcentajeCompletado()));
                }
            } else {
                alertasTexto.append("No hay metas próximas en los siguientes ").append(DIAS_PROXIMIDAD_METAS).append(" días.\n");
            }
            alertasTexto.append("\nAlertas de Presupuesto (Mes Actual):\n");
            boolean alertasPresupuestoEncontradas = false;
            if (presupuestoActual != null && !presupuestoActual.getLimitesPorCategoria().isEmpty()) {
                for (Map.Entry<String, Double> entry : presupuestoActual.getLimitesPorCategoria().entrySet()) {
                    String categoria = entry.getKey();
                    double limite = entry.getValue();
                    double gastado = presupuestoActual.getGastosPorCategoria().getOrDefault(categoria, 0.0);
                    double porcentajeUsado = (limite > 0) ? (gastado / limite) * 100 : 0;
                    if (porcentajeUsado >= 80) { // Umbral de alerta
                        alertasTexto.append(String.format("  - ¡Cuidado! Categoría '%s' al %.1f%% de su límite.\n", categoria, porcentajeUsado));
                        alertasPresupuestoEncontradas = true;
                    }
                }
            }
            if (!alertasPresupuestoEncontradas) {
                 if (presupuestoActual == null) {
                    alertasTexto.append("  No hay presupuesto definido para el mes actual para verificar alertas.\n");
                 } else if (presupuestoActual.getLimitesPorCategoria().isEmpty()) {
                    alertasTexto.append(String.format("  Presupuesto '%s' no tiene especificaciones para alertas.\n", presupuestoActual.getNombre()));
                 } else {
                    alertasTexto.append("  Ninguna categoría del presupuesto actual supera el umbral de alerta (80%).\n");
                 }
            }
            areaAlertas.setText(alertasTexto.toString());
            areaAlertas.setCaretPosition(0); // Mostrar desde el inicio

            LOGGER.fine("Panel de resumen actualizado completamente.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el resumen financiero.", e);
            JOptionPane.showMessageDialog(this, "Error al actualizar el resumen financiero: " + e.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
            // Resetear etiquetas a estado de error
            lblIngresos.setText("Ingresos Totales: Error");
            lblGastos.setText("Gastos Totales: Error");
            lblBalance.setText("Balance Total: Error");
            lblIngresosMesActual.setText("Ingresos Mes: Error");
            lblGastosMesActual.setText("Gastos Mes: Error");
            panelProgresoPresupuesto.removeAll();
            panelProgresoPresupuesto.add(new JLabel("Error al cargar datos del presupuesto."));
            panelProgresoPresupuesto.revalidate();
            panelProgresoPresupuesto.repaint();
            areaAlertas.setText("Error al cargar alertas.");
            if (graficoPresupuesto != null) graficoPresupuesto.actualizarDatos(Collections.emptyMap());
        }
    }
}
