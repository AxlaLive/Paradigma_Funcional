import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import datechooser.beans.DateChooserCombo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import servicio.ServicioTemperatura; 

public class FrmCambioTemperatura extends JFrame { 
    private JComboBox<String> cmbCiudad; 
    private DateChooserCombo dccDesde, dccHasta; 
    private DateChooserCombo dccExtremos; 
    private JTabbedPane tpDatos;
    private JPanel pnlGrafica; 
    private JPanel pnlEstadisticas; 
    private final String[] CIUDADES = {"Bogota", "Medellin", "Cali", "Todas"};

    public FrmCambioTemperatura() { 
        setTitle("Análisis de Temperaturas por Ciudad");
        setSize(850, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JPanel pnlDatosProceso = crearPanelControl();
        pnlGrafica = new JPanel(new BorderLayout());
        pnlGrafica.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel(new BorderLayout()); 
        pnlEstadisticas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        JScrollPane spEstadisticas = new JScrollPane(pnlEstadisticas);

        tpDatos = new JTabbedPane();
        tpDatos.addTab("Gráfica de Promedios", spGrafica);
        tpDatos.addTab("Análisis de Extremos", spEstadisticas);
        
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlDatosProceso, BorderLayout.NORTH);
        pnlCenter.add(tpDatos, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH); 
        getContentPane().add(pnlCenter, BorderLayout.CENTER);
        setLocationRelativeTo(null); 
    }

    private JPanel crearPanelControl() {
        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5)); 
        pnlDatosProceso.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        pnlDatosProceso.add(new JLabel("Ciudad: "));
        cmbCiudad = new JComboBox<>(CIUDADES);
        cmbCiudad.setSelectedItem("Todas");
        cmbCiudad.setPreferredSize(new Dimension(100, 25));
        pnlDatosProceso.add(cmbCiudad);
        
        pnlDatosProceso.add(new JLabel("Desde "));
        dccDesde = new DateChooserCombo();
        dccDesde.setPreferredSize(new Dimension(100, 25));
        pnlDatosProceso.add(dccDesde);
        
        pnlDatosProceso.add(new JLabel(" Hasta "));
        dccHasta = new DateChooserCombo();
        dccHasta.setPreferredSize(new Dimension(100, 25));
        pnlDatosProceso.add(dccHasta);
        
        return pnlDatosProceso;
    }

    private void btnGraficarClick() {
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Error de Rango", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Double> promedios = ServicioTemperatura.calcularPromedioPorCiudadEnRango(desde, hasta);

        if (promedios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron datos en el rango seleccionado.", "Sin Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : promedios.entrySet()) {
            dataset.addValue(entry.getValue(), entry.getKey(), "Promedio (°C)"); 
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Promedio de Temperatura por Ciudad (" + desde + " a " + hasta + ")", 
            "Ciudad", 
            "Temperatura Promedio (°C)", 
            dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        pnlGrafica.removeAll();
        pnlGrafica.add(chartPanel, BorderLayout.CENTER);
        pnlGrafica.revalidate();
        pnlGrafica.repaint();
        
        tpDatos.setSelectedIndex(0); 
    }

    private void btnAnalizarExtremosClick() {
        LocalDate fecha = dccExtremos.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String resultado = ServicioTemperatura.analizarExtremosPorFecha(fecha);
        JTextArea txtResultado = new JTextArea(resultado);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtResultado.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        pnlEstadisticas.removeAll();
        pnlEstadisticas.add(new JScrollPane(txtResultado), BorderLayout.CENTER);
        pnlEstadisticas.revalidate();
        pnlEstadisticas.repaint();

        tpDatos.setSelectedIndex(1); 
    }
}
